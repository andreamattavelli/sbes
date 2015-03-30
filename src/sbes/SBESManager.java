package sbes;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sbes.ast.CloneMethodCallsVisitor;
import sbes.exceptions.CompilationException;
import sbes.exceptions.GenerationException;
import sbes.exceptions.SBESException;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.execution.evosuite.Evosuite;
import sbes.execution.evosuite.EvosuiteFirstStage;
import sbes.execution.evosuite.EvosuiteSecondStage;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.result.EquivalenceRepository;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioLoader;
import sbes.scenario.TestScenarioRepository;
import sbes.scenario.generalizer.CounterexampleGeneralizer;
import sbes.statistics.Statistics;
import sbes.stoppingcondition.StoppingCondition;
import sbes.stub.CounterexampleStub;
import sbes.stub.Stub;
import sbes.stub.generator.AbstractStubGenerator;
import sbes.stub.generator.first.FirstStageGeneratorFactory;
import sbes.stub.generator.first.FirstStageGeneratorStub;
import sbes.stub.generator.second.SecondStageGeneratorFactory;
import sbes.testcase.Carver;
import sbes.testcase.CarvingContext;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;
import sbes.util.ClassUtils;
import sbes.util.ClasspathUtils;
import sbes.util.DirectoryUtils;
import sbes.util.EvosuiteUtils;
import sbes.util.IOUtils;

public class SBESManager {

	private static final Logger logger = new Logger(SBESManager.class);
	
	private Statistics statistics;
	
	public SBESManager() {
		this.statistics = new Statistics();
	}
	
	public void generate() throws SBESException {
		ClasspathUtils.checkClasspath();
		
		List<String> targetMethods = computeTargets();
		while (targetMethods.size() > 0 && !SBESShutdownInterceptor.isInterrupted()) {
			String method = targetMethods.remove(0);
			setup(method);
			try {
				// generation
				generateEquivalencesForMethod();
			} catch (Throwable t) {
				logger.fatal(t.getMessage());
				t.printStackTrace();
			} finally {
				// cleanup
				cleanup(method);
			}
		}
		
		EquivalenceRepository.getInstance().printEquivalences();
	}

	private void generateEquivalencesForMethod() throws SBESException {
		statistics.processStarted();
		
		// INIT 
		DirectoryUtils directory = DirectoryUtils.I();

		// TEST SCENARIO LOADING
		statistics.scenarioStarted();
		
		List<TestScenario> initialScenarios = TestScenarioLoader.loadTestScenarios();
		// load test scenarios from path
		if (initialScenarios.isEmpty()) {
			throw new SBESException("Unable to load any initial test scenarios");
		}
		
		statistics.scenarioFinished();
		
		StoppingCondition stoppingCondition = new StoppingCondition();
		stoppingCondition.init();
		try {
			/*
			 * -- main loop --
			 * we loop until we reach a desired stopping condition:
			 *   - time
			 *   - iterations
			 *   - unable to synthesize a candidate 
			 */
			while (!stoppingCondition.isReached() && !SBESShutdownInterceptor.isInterrupted()) {
				TestScenarioRepository.I().resetCounterexamples();
				directory.createEquivalenceDirs();
				directory.createFirstStubDir();
				
				IOUtils.formatIterationStartMessage(logger, directory);
				
				// FIRST PHASE STUB GENERATION
				FirstStageGeneratorStub firstPhaseGenerator = FirstStageGeneratorFactory.createGenerator(initialScenarios);
				Stub initialStub = firstPhaseGenerator.generateStub();
				initialStub.dumpStub(directory.getFirstStubDir());

				// independent variable to be updated with the latest stub to be used
				Stub stub = initialStub;

				/*
				 * -- iteration loop --
				 * we loop until we either find an equivalence sequence (no counterexamples),
				 *   we are able to synthesize a valid candidate, or we reach a time/iteration stopping condition
				 */
				try {
					boolean terminateIterations = false;
					while (!terminateIterations && !stoppingCondition.isInternallyReached() && !SBESShutdownInterceptor.isInterrupted()) {
						statistics.iterationStarted();

						// FIRST PHASE: SYNTHESIS OF CANDIDATE
						CarvingResult candidateES = synthesizeCandidateEquivalence(stub);
						stoppingCondition.update(candidateES);

						if (candidateES == null) {
							// not able to carve any candidate, stop iteration
							terminateIterations = true;
						}
						else {
							// SECOND PHASE: VALIDATION OF CANDIDATE (search for a counterexample)
							// search for a counterexample
							CarvingResult counterexample = generateCounterexample(firstPhaseGenerator, stub, candidateES);

							// determine exit condition: counterexample found || timeout
							if (counterexample == null) {
								// timeout: found equivalent sequence, stop iteration
								terminateIterations = true;
							}
							else {
								// counterexample found: generate the corresponding test scenario and add it to the stub
								stub = generateFirstStageStubWithCounterexample(counterexample);
							}
						}

						statistics.iterationFinished();
					} // end iteration
				} catch (CompilationException | GenerationException e) {
					logger.fatal("Iteration aborted due to: " + e.getMessage());
					statistics.iterationFinished();
				}
				
				IOUtils.formatIterationEndMessage(logger, directory);
			} // end search
		} catch (SBESException e) {
			logger.fatal("Execution aborted due to: " + e.getMessage());
		}
		
		logger.info("Stopping equivalent sequence generation");
		
		statistics.processFinished();
		statistics.writeCSV();
	}

	private CarvingResult synthesizeCandidateEquivalence(final Stub stub) {
		logger.info("Synthesizing equivalent sequence candidate");
		statistics.synthesisStarted();
		
		final DirectoryUtils directory = DirectoryUtils.I();
		
		String signature 	= Options.I().getTargetMethod();
		String packagename 	= IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory= IOUtils.concatFilePath(directory.getFirstStubDir(), packagename);
		
		String classPath 	= IOUtils.concatClassPath(	Options.I().getClassesPath(), 
														Options.I().getJunitPath(), 
														Options.I().getEvosuitePath(), 
														directory.getFirstStubDir(),
														this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		
		// compile stub
		CompilationContext compilationContext = new CompilationContext(	testDirectory, 
																		stub.getStubName() + ".java", 
																		directory.getFirstStubDir(), 
																		classPath);
		
		boolean compilationSucceeded = Compilation.compile(compilationContext);
		if (!compilationSucceeded) {
			logger.fatal("Unable to compile first-stage stub " + stub.getStubName());
			throw new CompilationException("Unable to compile first-stage stub " + stub.getStubName());
		}
		
		// run evosuite
		String stubSignature = ClassUtils.getPackage(Options.I().getTargetMethod()) + '.' + stub.getStubName();
		Evosuite evosuite = new EvosuiteFirstStage(	stubSignature, 
													ClassUtils.getMethodname(Options.I().getTargetMethod()), 
													classPath);
		ExecutionResult result = ExecutionManager.execute(evosuite);
		
		if (SBESShutdownInterceptor.isInterrupted()) {
			return null;
		}
		
		dumpEvosuiteLog(result, directory.getFirstStubEvosuiteDir());
		
		if (Options.I().isVerbose()) {
			logger.info(result.getStdout());
			logger.info(result.getStderr());
		}
		
		// analyze synthesis process
		if (!EvosuiteUtils.generatedCandidate(result.getStdout())) {
			if (!EvosuiteUtils.succeeded(result) && result.getStderr().length() > 0) {
				logger.error(result.getStdout());
				logger.error(result.getStderr());
			}
			logger.warn("Unable to synthesize a valid candidate");
			return null;
		}
		
		// carve result
		CarvingContext carvingContext = new CarvingContext(IOUtils.concatFilePath(result.getOutputDir(), packagename), result.getFilename());
		Carver carver = new Carver(carvingContext, false);
		List<CarvingResult> candidates = carver.carveBodyFromTests();
		
		if (candidates.size() > 1) {
			logger.warn("More than one candidate! Pruning to first one");
		}
		
		statistics.synthesisFinished();
		logger.info("Synthesizing equivalent sequence candidate - done");
		
		if (candidates.isEmpty()) {
			logger.warn("Unable to carve any candidate");
			return null;
		}
		else {
			logger.info("Successfully synthesized a valid candidate");
		}
		return candidates.get(0);
	}

	private CarvingResult generateCounterexample(final FirstStageGeneratorStub firstPhaseGenerator, final Stub stub,
												 final CarvingResult candidateES) {
		final DirectoryUtils directory = DirectoryUtils.I();
		
		statistics.counterexampleStarted();
		
		AbstractStubGenerator secondPhaseGenerator = SecondStageGeneratorFactory.createGenerator(firstPhaseGenerator, stub, candidateES);
		Stub secondStub = secondPhaseGenerator.generateStub();
		directory.createSecondStubDir();
		secondStub.dumpStub(directory.getSecondStubDir());
		
		String signature =		Options.I().getTargetMethod();
		String packagename =	IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory =	IOUtils.concatFilePath(directory.getSecondStubDir(), packagename);
		
		String classPath = IOUtils.concatClassPath(	Options.I().getClassesPath(), 
													Options.I().getJunitPath(),	
													Options.I().getEvosuitePath(), 
													directory.getSecondStubDir(),
													this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		
		// compile stub
		CompilationContext compilationContext = new CompilationContext(	testDirectory, 
																		secondStub.getStubName() + ".java", 
																		directory.getSecondStubDir(), 
																		classPath);
		
		boolean compilationSucceeded = Compilation.compile(compilationContext);
		if (!compilationSucceeded) {
			logger.fatal("Unable to compile second-stage stub " + secondStub.getStubName());
			throw new CompilationException("Unable to compile second-stage stub " + secondStub.getStubName());
		}
		
		// run evosuite
		String stubSignature = ClassUtils.getPackage(Options.I().getTargetMethod()) + '.' + secondStub.getStubName();
		Evosuite evosuite = new EvosuiteSecondStage(stubSignature, 
													ClassUtils.getMethodname(Options.I().getTargetMethod()), 
													classPath);
		
		logger.info("Generating counterexample");
		
		ExecutionResult result = ExecutionManager.execute(evosuite);
		
		if (SBESShutdownInterceptor.isInterrupted()) {
			return null;
		}
		
		dumpEvosuiteLog(result, directory.getSecondStubEvosuiteDir());
		
		if (Options.I().isVerbose()) {
			logger.info("EvoSuite Stdout:" + '\n' + result.getStdout());
			logger.info("EvoSuite Stderr:" + '\n' + result.getStderr());
		}
		
		// carve result
		CarvingContext carvingContext = new CarvingContext(IOUtils.concatFilePath(result.getOutputDir(), packagename), result.getFilename());
		Carver carver = new Carver(carvingContext, false);
		List<CarvingResult> candidates = carver.carveBodyFromTests();

		CarvingResult toReturn = null;
		if (candidates.isEmpty()) {
			CounterexampleStub cStub = (CounterexampleStub) secondStub;
			logger.info("No counterexample found!");
			CloneMethodCallsVisitor cov = new CloneMethodCallsVisitor();
			cov.visit(cStub.getEquivalence().getBody(), null);
			if (cov.getMethods().isEmpty()) {
				logger.debug("Spurious result, iterating");
			}
			else {
				logger.info("Equivalence synthesized: " + System.lineSeparator() + cStub.getEquivalence().toString());
				EquivalenceRepository.getInstance().addEquivalence(cStub.getEquivalence());
			}
		}
		else {
			logger.info("Counterexample found, refining search space!");
			if (candidates.size() > 1) {
				logger.warn("More than one counterexample synthesized");
			}
			toReturn = candidates.get(0);
		}
		
		statistics.counterexampleFinished();
		logger.info("Generating counterexample - done");
		return toReturn;
	}

	private Stub generateFirstStageStubWithCounterexample(final CarvingResult counterexample) {
		DirectoryUtils directory = DirectoryUtils.I();
		CounterexampleGeneralizer cg = new CounterexampleGeneralizer();
		TestScenario counterexampleScenario = cg.counterexampleToTestScenario(counterexample);
		TestScenarioRepository.I().addCounterexample(counterexampleScenario);
		
		AbstractStubGenerator counterexampleGenerator = FirstStageGeneratorFactory.createGenerator(TestScenarioRepository.I().getScenarios());
		Stub stub = counterexampleGenerator.generateStub();
		directory.createFirstStubDir();
		stub.dumpStub(directory.getFirstStubDir());
		
		return stub;
	}
	
	private void setup(String method) {
		IOUtils.formatInitMessage(logger, method);
		Options.I().setTargetMethod(method);
	}
	
	private void cleanup(String method) {
		DirectoryUtils.reset();
		TestScenarioRepository.reset();
		
		logger.info("Finished generation of equivalences");
		IOUtils.formatEndMessage(logger, method);
		System.out.println("");
		System.out.println("");
	}

	private List<String> computeTargets() {
		List<String> targetMethods = new ArrayList<>();
		if (Options.I().getTargetMethod() != null) {
			logger.info("Target: single method");
			logger.info("Target methods:");
			logger.info("  " + Options.I().getTargetMethod());
			targetMethods.add(Options.I().getTargetMethod());
		}
		else {
			logger.info("Target: class " + Options.I().getTargetClass());
			logger.info("Target methods:");
			Class<?> clazz = ClassUtils.getClass(Options.I().getTargetClass());
			Method[] methods = ClassUtils.getClassMethods(clazz);
			for (Method method : methods) {
				if (method.getName().equals("toString") 	|| 
						method.getName().equals("hashCode") ||
						method.getName().equals("equals") 	||
						method.getName().equals("toArray")) {
					continue;
				}
				String signature = ClassUtils.getMethodSignature(clazz, method);
				logger.info("  " + signature);
				targetMethods.add(signature);
			}
		}
		System.out.println();
		return targetMethods;
	}
	
	private void dumpEvosuiteLog(final ExecutionResult result, final String directory) {
		try {
			Files.write(Paths.get(directory + "/evosuite-out.txt"), Arrays.toString(result.getCommand()).getBytes());
			Files.write(Paths.get(directory + "/evosuite-out.txt"), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
			Files.write(Paths.get(directory + "/evosuite-out.txt"), result.getStdout().getBytes(), StandardOpenOption.APPEND);
			if (result.getStderr().length() > 0) {
				Files.write(Paths.get(directory + "/evosuite-err.txt"), result.getStderr().getBytes());
			}
		} catch (IOException e) {
			logger.error("Unable to dump EvoSuite log files", e);
		}
	}
	
}
