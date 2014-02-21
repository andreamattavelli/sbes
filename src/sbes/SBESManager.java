package sbes;

import java.io.File;
import java.util.List;

import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteFirstStage;
import sbes.evosuite.EvosuiteSecondStage;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.scenario.GenericTestScenario;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioGenerator;
import sbes.statistics.Statistics;
import sbes.stub.Stub;
import sbes.stub.generator.FirstStageGenericStubGenerator;
import sbes.stub.generator.FirstStageStubGenerator;
import sbes.stub.generator.SecondStageStubGenerator;
import sbes.stub.generator.StubGenerator;
import sbes.testcase.Carver;
import sbes.testcase.CarvingContext;
import sbes.testcase.CarvingResult;
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
		statistics.synthesisStarted();
		
		// =================================== INIT =================================== 
		DirectoryUtils directory = DirectoryUtils.I();
		ClasspathUtils.checkClasspath();

		// ===================== INITIAL TEST SCENARIO GENERATION =====================
		TestScenarioGenerator scenarioGenerator = TestScenarioGenerator.getInstance();
		scenarioGenerator.generateTestScenarios();
		List<TestScenario> initialScenarios = scenarioGenerator.getScenarios();

		// ======================= FIRST PHASE STUB GENERATION ========================
		boolean generics = false;
		for (TestScenario testScenario : initialScenarios) {
			if (testScenario instanceof GenericTestScenario) {
				generics = true;
				break;
			}
		}
		StubGenerator firstPhaseGenerator;
		if (generics) {
			firstPhaseGenerator = new FirstStageGenericStubGenerator(initialScenarios);
		}
		else {
			firstPhaseGenerator = new FirstStageStubGenerator(initialScenarios);
		}
		Stub initialStub = firstPhaseGenerator.generateStub();
		directory.createFirstStubDir();
		initialStub.dumpStub(directory.getFirstStubDir());

		Stub stub = initialStub;
		ExecutionManager manager = new ExecutionManager();

		boolean terminated = false;
		while (!terminated) {
			statistics.iterationStarted();
			// ======================== FIRST PHASE SYNTHESIS =========================
			CarvingResult candidateES = synthesizeEquivalentSequence(stub, manager, directory);

			// ===================== SECOND PHASE STUB GENERATION =====================
			// generate second stub from carved test case
			StubGenerator secondPhaseGenerator = new SecondStageStubGenerator(stub, candidateES);
			Stub secondStub = secondPhaseGenerator.generateStub();
			directory.createSecondStubDir();
			secondStub.dumpStub(directory.getSecondStubDir());

			// ================== SECOND PHASE COUNTEREXAMPLE SEARCH ==================
			// compile second stub
			CarvingResult counterexample = generateCounterexample(secondStub, manager, directory);

			// determine exit condition: solutionFound || time expired
			if (counterexample == null) {
				// if solution is found: found equivalent sequence, terminate!
				terminated = true;
			}
			else {
				// if solution is found: add test scenario to stub
				TestScenario ts = TestScenarioGenerator.getInstance().carvedTestToScenario(counterexample);
				initialScenarios.add(ts);
				StubGenerator counterexampleGenerator = new FirstStageStubGenerator(initialScenarios);
				stub = counterexampleGenerator.generateStub();
				directory.createFirstStubDir();
				stub.dumpStub(directory.getFirstStubDir());
			}
			statistics.iterationFinished();
		}
		
		statistics.synthesisFinished();
		
		statistics.print();
	}
	
	private CarvingResult synthesizeEquivalentSequence(Stub stub, ExecutionManager manager, DirectoryUtils directory) {
		logger.info("Synthesizing equivalent sequence candidate");
		String signature = Options.I().getMethodSignature();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory = IOUtils.concatPath(directory.getFirstStubDir(), packagename);
		
		String classPath =	Options.I().getClassesPath() + File.pathSeparatorChar + 
							Options.I().getJunitPath() + File.pathSeparatorChar +
							Options.I().getEvosuitePath() + File.pathSeparatorChar +
							directory.getFirstStubDir() + File.pathSeparatorChar +
							this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		
		// compile stub
		CompilationContext compilationContext = new CompilationContext(	testDirectory, 
																		stub.getStubName() + ".java", 
																		directory.getFirstStubDir(), 
																		classPath);
		
		boolean compilationSucceeded = Compilation.compile(compilationContext);
		if (!compilationSucceeded) {
			throw new SBESException("Unable to compile first-stage stub " + stub.getStubName());
		}
		
		// run evosuite
		String stubSignature = ClassUtils.getPackage(Options.I().getMethodSignature()) + '.' + stub.getStubName();
		Evosuite evosuite = new EvosuiteFirstStage(stubSignature, 
															ClassUtils.getMethodname(Options.I().getMethodSignature()), 
															classPath);
		ExecutionResult result = manager.execute(evosuite);
		
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
		
		if (candidates.isEmpty()) {
			throw new SBESException("Unable to carve any candidate");
		}
		else if (candidates.size() > 1) {
			logger.warn("More than one candidate! Pruning to first one");
		}
		
		logger.info("Synthesizing equivalent sequence candidate - done");
		return candidates.get(0);
	}

	private CarvingResult generateCounterexample(Stub secondStub, ExecutionManager manager, DirectoryUtils directory) {
		logger.info("Generating counterexample");
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
			throw new SBESException("Unable to compile second-stage stub " + secondStub.getStubName());
		}
		
		// run evosuite
		String stubSignature = ClassUtils.getPackage(Options.I().getMethodSignature()) + '.' + secondStub.getStubName();
		Evosuite evosuite = new EvosuiteSecondStage(stubSignature, 
															ClassUtils.getMethodname(Options.I().getMethodSignature()), 
															classPath);
		ExecutionResult result = manager.execute(evosuite);
		
		logger.debug(result.getStdout());
		logger.debug(result.getStderr());
		
		// carve result
		CarvingContext carvingContext = new CarvingContext(IOUtils.concatPath(result.getOutputDir(), packagename), result.getFilename());
		Carver carver = new Carver(carvingContext, false);
		List<CarvingResult> candidates = carver.carveBodyFromTests();

		CarvingResult toReturn = null;
		if (candidates.isEmpty()) {
			logger.info("No counterexample found!");
		}
		else {
			logger.info("Counterexample found");
			if (candidates.size() > 1) {
				logger.warn("More than one counterexample! Pruning to first one");
			}
			toReturn = candidates.get(0);
		}
		
		logger.info("Generating counterexample - done");
		return toReturn;
	}
	
}
