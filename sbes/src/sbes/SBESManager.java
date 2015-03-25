package sbes;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
		
		List<String> targetMethods = new ArrayList<>();
		
		if (Options.I().getTargetMethod() != null) {
			targetMethods.add(Options.I().getTargetMethod());
		}
		else {
			Class<?> clazz = ClassUtils.getClass(Options.I().getTargetClass());
			Method[] methods = ClassUtils.getClassMethods(clazz);
			for (Method method : methods) {
				targetMethods.add(ClassUtils.getMethodSignature(clazz, method));
			}
		}
		
		while (targetMethods.size() > 0 && !SBESShutdownInterceptor.isInterrupted()) {
			String method = targetMethods.remove(0);
			
			//setup
			IOUtils.formatInitMessage(logger, method);
			Options.I().setTargetMethod(method);
			
			try {
				// generation
				generateEquivalencesForMethod();
			} catch (Throwable t) {
				logger.error(t.getMessage());
			} finally {
				// cleanup
				logger.info("Finished generation of equivalences");
				System.out.println("");
				System.out.println("");
				cleanup();
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
						CarvingResult candidateES = synthesizeCandidateEquivalence(stub, directory);
						stoppingCondition.update(candidateES);

						if (candidateES == null) {
							// not able to carve any candidate, stop iteration
							terminateIterations = true;
						}
						else {
							// SECOND PHASE: VALIDATION OF CANDIDATE (search for a counterexample)
							// generate second stub from carved test case
							AbstractStubGenerator secondPhaseGenerator = SecondStageGeneratorFactory.createGenerator(firstPhaseGenerator, stub, candidateES);
							Stub secondStub = secondPhaseGenerator.generateStub();
							directory.createSecondStubDir();
							secondStub.dumpStub(directory.getSecondStubDir());

							// search for a counterexample
							CarvingResult counterexample = generateCounterexample(secondStub, directory);

							// determine exit condition: counterexample found || timeout
							if (counterexample == null) {
								// timeout: found equivalent sequence, stop iteration
								terminateIterations = true;
							}
							else {
								// counterexample found: generate the corresponding test scenario and add it to the stub
								stub = generateTestScenarioFromCounterexample(directory, counterexample);
							}
						}

						statistics.iterationFinished();
					} // end iteration
				} catch (CompilationException e) {
					logger.fatal("Iteration aborted due to: " + e.getMessage());
					statistics.iterationFinished();
				}
				
				IOUtils.formatIterationEndMessage(logger, directory);
			} // end search
		} catch (SBESException | GenerationException e) {
			logger.fatal("Execution aborted due to: " + e.getMessage());
		}
		
		logger.info("Stopping equivalent sequence generation");
		
		statistics.processFinished();
		statistics.writeCSV();
	}

	private CarvingResult synthesizeCandidateEquivalence(Stub stub, DirectoryUtils directory) {
		logger.info("Synthesizing equivalent sequence candidate");
		statistics.synthesisStarted();
		
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
		
		if (Options.I().isVerbose()) {
			logger.info(result.getStdout());
			logger.info(result.getStderr());
		}
		
		// analyze synthesis process
		if (!EvosuiteUtils.generatedCandidate(result.getStdout())) {
			if (!EvosuiteUtils.succeeded(result)) {
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

	private CarvingResult generateCounterexample(Stub secondStub, DirectoryUtils directory) {
		logger.info("Generating counterexample");
		statistics.counterexampleStarted();
		
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
		ExecutionResult result = ExecutionManager.execute(evosuite);
		
		if (SBESShutdownInterceptor.isInterrupted()) {
			return null;
		}
		
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
	
	private Stub generateTestScenarioFromCounterexample(DirectoryUtils directory, CarvingResult counterexample) {
		CounterexampleGeneralizer cg = new CounterexampleGeneralizer();
		TestScenario counterexampleScenario = cg.counterexampleToTestScenario(counterexample);
		TestScenarioRepository.I().addCounterexample(counterexampleScenario);
		
		AbstractStubGenerator counterexampleGenerator = FirstStageGeneratorFactory.createGenerator(TestScenarioRepository.I().getScenarios());
		Stub stub = counterexampleGenerator.generateStub();
		directory.createFirstStubDir();
		stub.dumpStub(directory.getFirstStubDir());
		
		return stub;
	}
	
	private void cleanup() {
		DirectoryUtils.reset();
		TestScenarioRepository.reset();
	}
	
}
