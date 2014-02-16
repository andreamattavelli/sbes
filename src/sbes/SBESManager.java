package sbes;

import java.io.File;
import java.util.List;

import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteFirstStageStrategy;
import sbes.evosuite.EvosuiteSecondStageStrategy;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.logging.Logger;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioGenerator;
import sbes.statistics.Statistics;
import sbes.stub.Stub;
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
		StubGenerator firstPhaseGenerator = new FirstStageStubGenerator(initialScenarios);
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
			
			// if solution is not found: add test scenario to stub
			
			// determine exit condition: solutionFound || time expired
			if (counterexample == null) {
				terminated = true;
			}
			statistics.iterationFinished();
		}
		
		statistics.synthesisFinished();
		
		statistics.print();
	}
	
	private CarvingResult synthesizeEquivalentSequence(Stub stub, ExecutionManager manager, DirectoryUtils directory) {
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
		Evosuite evosuite = new EvosuiteFirstStageStrategy(stubSignature, 
															ClassUtils.getMethodname(Options.I().getMethodSignature()), 
															classPath);
		ExecutionResult result = manager.execute(evosuite);
		
		// analyze synthesis process
		if (!EvosuiteUtils.succeeded(result.getStdout(), result.getStderr())) {
			throw new SBESException("Unable to synthesize a valid candidate");
		}
		if (!EvosuiteUtils.generatedCandidate(result.getStdout())) {
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
		
		return candidates.get(0);
	}

	private CarvingResult generateCounterexample(Stub secondStub, ExecutionManager manager, DirectoryUtils directory) {
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
		Evosuite evosuite = new EvosuiteSecondStageStrategy(stubSignature, 
															ClassUtils.getMethodname(Options.I().getMethodSignature()), 
															classPath);
		ExecutionResult result = manager.execute(evosuite);
		
		// analyze test case
		// carve result
		
		return null;
	}
	
}
