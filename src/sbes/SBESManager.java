package sbes;

import japa.parser.ast.ImportDeclaration;

import java.util.List;

import sbes.ast.CloneObjVisitor;
import sbes.ast.CounterexampleVisitor;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.execution.evosuite.Evosuite;
import sbes.execution.evosuite.EvosuiteFirstStage;
import sbes.execution.evosuite.EvosuiteSecondStage;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.result.EquivalenceRepository;
import sbes.result.TestScenario;
import sbes.scenario.TestScenarioGenerator;
import sbes.statistics.Statistics;
import sbes.stoppingcondition.StoppingCondition;
import sbes.stub.CounterexampleStub;
import sbes.stub.Stub;
import sbes.stub.generator.StubGenerator;
import sbes.stub.generator.first.FirstStageGeneratorFactory;
import sbes.stub.generator.first.FirstStageStubGenerator;
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
	
	public void generateEquivalences() throws SBESException {
		statistics.processStarted();
		
		// INIT 
		DirectoryUtils directory = DirectoryUtils.I();
		ClasspathUtils.checkClasspath();

		// TEST SCENARIO LOADING
		statistics.scenarioStarted();
		
		TestScenarioGenerator scenarioGenerator = TestScenarioGenerator.getInstance();
		// load test scenarios from path
		scenarioGenerator.loadTestScenarios();
		List<TestScenario> initialScenarios = scenarioGenerator.getScenarios();
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
				directory.createEquivalenceDirs();
				directory.createFirstStubDir();
				
				logger.info("=========================================================================== " + 
							"Starting synthesis attempt #" + directory.getEquivalences());
				
				// FIRST PHASE STUB GENERATION
				FirstStageStubGenerator firstPhaseGenerator = FirstStageGeneratorFactory.createGenerator(initialScenarios);
				Stub initialStub = firstPhaseGenerator.generateStub();
				initialStub.dumpStub(directory.getFirstStubDir());

				// independent variable to be updated with the latest stub to be used
				Stub stub = initialStub;

				/*
				 * -- iteration loop --
				 * we loop until we either find an equivalence sequence (no counterexamples),
				 *   we are able to synthesize a valid candidate, or we reach a time/iteration stopping condition
				 */
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
						// found a candidate, validate
						// SECOND PHASE: COUNTEREXAMPLE SEARCH
						
						// generate second stub from carved test case
						StubGenerator secondPhaseGenerator = SecondStageGeneratorFactory.createGenerator(firstPhaseGenerator, stub, candidateES);
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
				
				logger.info("=========================================================================== " + 
							"Finished synthesis attempt #" + directory.getEquivalences());
			} // end search
		} catch (SBESException e) {
			logger.error(e.getMessage());
		}
		
		logger.info("Stopping equivalent sequence generation");
		EquivalenceRepository.getInstance().printEquivalences();
		
		statistics.processFinished();
		statistics.writeCSV();
	}

	private CarvingResult synthesizeCandidateEquivalence(Stub stub, DirectoryUtils directory) {
		logger.info("Synthesizing equivalent sequence candidate");
		statistics.synthesisStarted();
		
		String signature 	= Options.I().getMethodSignature();
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
			throw new SBESException("Unable to compile first-stage stub " + stub.getStubName());
		}
		
		// run evosuite
		String stubSignature = ClassUtils.getPackage(Options.I().getMethodSignature()) + '.' + stub.getStubName();
		Evosuite evosuite = new EvosuiteFirstStage(	stubSignature, 
													ClassUtils.getMethodname(Options.I().getMethodSignature()), 
													classPath);
		ExecutionResult result = ExecutionManager.execute(evosuite);
		
		if (SBESShutdownInterceptor.isInterrupted()) {
			return null;
		}
		
//		logger.debug(result.getStdout());
//		logger.debug(result.getStderr());
		
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
		return candidates.get(0);
	}

	private CarvingResult generateCounterexample(Stub secondStub, DirectoryUtils directory) {
		logger.info("Generating counterexample");
		statistics.counterexampleStarted();
		
		String signature =		Options.I().getMethodSignature();
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
			throw new SBESException("Unable to compile second-stage stub " + secondStub.getStubName());
		}
		
		// run evosuite
		String stubSignature = ClassUtils.getPackage(Options.I().getMethodSignature()) + '.' + secondStub.getStubName();
		Evosuite evosuite = new EvosuiteSecondStage(stubSignature, 
													ClassUtils.getMethodname(Options.I().getMethodSignature()), 
													classPath);
		ExecutionResult result = ExecutionManager.execute(evosuite);
		
		if (SBESShutdownInterceptor.isInterrupted()) {
			return null;
		}
		
//		logger.debug(result.getStdout());
//		logger.debug(result.getStderr());
		
		// carve result
		CarvingContext carvingContext = new CarvingContext(IOUtils.concatFilePath(result.getOutputDir(), packagename), result.getFilename());
		Carver carver = new Carver(carvingContext, false);
		List<CarvingResult> candidates = carver.carveBodyFromTests();

		CarvingResult toReturn = null;
		if (candidates.isEmpty()) {
			CounterexampleStub cStub = (CounterexampleStub) secondStub;
			logger.info("No counterexample found!");
			CloneObjVisitor cov = new CloneObjVisitor();
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
		cleanCounterexample(counterexample);
		TestScenarioGenerator.getInstance().carvedCounterexampleToScenario(counterexample);
		StubGenerator counterexampleGenerator = FirstStageGeneratorFactory.createGenerator(TestScenarioGenerator.getInstance().getScenarios());
		Stub stub = counterexampleGenerator.generateStub();
		directory.createFirstStubDir();
		stub.dumpStub(directory.getFirstStubDir());
		return stub;
	}
	
	private void cleanCounterexample(CarvingResult counterexample) {
		String classname = ClassUtils.getSimpleClassname(Options.I().getMethodSignature());
		CounterexampleVisitor cv = new CounterexampleVisitor();
		cv.visit(counterexample.getBody(), classname);
		
		for (int i = 0; i < counterexample.getImports().size(); i++) {
			ImportDeclaration importDecl = counterexample.getImports().get(i);
			if (importDecl.getName().getName().endsWith(classname + "_Stub_2")) {
				counterexample.getImports().remove(importDecl);
				i--;
			}
		}
	}
	
}
