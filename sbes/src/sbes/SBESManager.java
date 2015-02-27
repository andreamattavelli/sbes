package sbes;

import japa.parser.ast.ImportDeclaration;

import java.io.File;
import java.util.List;

import sbes.ast.CloneObjVisitor;
import sbes.ast.CounterexampleVisitor;
import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteFirstStage;
import sbes.evosuite.EvosuiteSecondStage;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.result.EquivalenceRepository;
import sbes.result.StoppingCondition;
import sbes.result.TestScenario;
import sbes.scenario.TestScenarioGenerator;
import sbes.statistics.Statistics;
import sbes.stub.CounterexampleStub;
import sbes.stub.Stub;
import sbes.stub.generator.FirstStageGeneratorFactory;
import sbes.stub.generator.FirstStageStubGenerator;
import sbes.stub.generator.SecondStageGeneratorFactory;
import sbes.stub.generator.StubGenerator;
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
	
	public void generateES() throws SBESException {
		statistics.processStarted();
		
		// INIT 
		DirectoryUtils directory = DirectoryUtils.I();
		ClasspathUtils.checkClasspath();

		// INITIAL TEST SCENARIO LOADING
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
			while (!stoppingCondition.isReached()) {
				directory.createEquivalenceDirs();
				// FIRST PHASE STUB GENERATION
				FirstStageStubGenerator firstPhaseGenerator = FirstStageGeneratorFactory.createGenerator(initialScenarios);
				Stub initialStub = firstPhaseGenerator.generateStub();
				directory.createFirstStubDir();
				initialStub.dumpStub(directory.getFirstStubDir());

				Stub stub = initialStub;

				/*
				 * -- iteration loop --
				 * we loop until we either find an equivalence sequence (no counterexamples),
				 *   we are able to synthesize a valid candidate, or we reach a time/iteration stopping condition
				 */
				boolean terminated = false;
				while (!terminated || !stoppingCondition.isReached()) {
					statistics.iterationStarted();
					// FIRST PHASE: SYNTHESIS OF CANDIDATE
					CarvingResult candidateES = synthesizeCandidateEquivalence(stub, directory);
					stoppingCondition.update(candidateES);

					if (candidateES == null) {
						// not able to carve any candidate, stop iteration
						terminated = true;
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
						CarvingResult counterexample = generateCounterexample(secondStub,directory);

						// determine exit condition: counterexample found || timeout
						if (counterexample == null) {
							// timeout: found equivalent sequence, stop iteration
							terminated = true;
						}
						else {
							// counterexample found: generate the corresponding test scenario and add it to the stub
							stub = generateTestScenarioFromCounterexample(directory, counterexample);
						}
					}
					statistics.iterationFinished();
				} // end iteration
				
			} // end search
		} catch (SBESException e) {
			logger.error(e.getMessage());
			logger.info("Stopping equivalent sequence generation");
			EquivalenceRepository.getInstance().printEquivalences();
		}
		
		statistics.processFinished();
		statistics.writeCSV();
	}

	private CarvingResult synthesizeCandidateEquivalence(Stub stub, DirectoryUtils directory) {
		logger.info("Synthesizing equivalent sequence candidate");
		statistics.synthesisStarted();
		
		String signature 	= Options.I().getMethodSignature();
		String packagename 	= IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory= IOUtils.concatPath(directory.getFirstStubDir(), packagename);
		
		String classPath =	Options.I().getClassesPath() + File.pathSeparatorChar + 
							Options.I().getJunitPath() 	 + File.pathSeparatorChar +
							Options.I().getEvosuitePath()+ File.pathSeparatorChar +
							directory.getFirstStubDir()  + File.pathSeparatorChar +
							this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		
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
		
		logger.debug(result.getStdout());
		logger.debug(result.getStderr());
		
		// analyze synthesis process
		if (!EvosuiteUtils.generatedCandidate(result.getStdout())) {
			if (!EvosuiteUtils.succeeded(result)) {
				logger.error(result.getStdout());
				logger.error(result.getStderr());
			}
			throw new SBESException("Unable to synthesize a valid candidate");
		}
		
		// carve result
		CarvingContext carvingContext = new CarvingContext(IOUtils.concatPath(result.getOutputDir(), packagename), result.getFilename());
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
		
		String signature = Options.I().getMethodSignature();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory = IOUtils.concatPath(directory.getSecondStubDir(), packagename);
		
		String classPath =	Options.I().getClassesPath() + File.pathSeparatorChar + 
							Options.I().getJunitPath() + File.pathSeparatorChar +
							Options.I().getEvosuitePath() + File.pathSeparatorChar +
							directory.getSecondStubDir() + File.pathSeparatorChar +
							this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		
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
		
		logger.debug(result.getStdout());
		logger.debug(result.getStderr());
		
		// carve result
		CarvingContext carvingContext = new CarvingContext(IOUtils.concatPath(result.getOutputDir(), packagename), result.getFilename());
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
			logger.info("Counterexample found");
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
