package sbes;

import java.io.File;
import java.util.List;

import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteFirstStageStrategy;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioGenerator;
import sbes.stub.Stub;
import sbes.stub.generator.FirstPhaseStubStrategy;
import sbes.stub.generator.StubGenerator;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;
import sbes.util.ClassUtils;
import sbes.util.ClasspathHandler;
import sbes.util.DirectoryUtils;

public class SBESGenerator {

	public void generateES() throws SBESException {
		// =================================== INIT =================================== 
		DirectoryUtils directory = DirectoryUtils.I();
		ClasspathHandler.checkClasspath();

		// ===================== INITIAL TEST SCENARIO GENERATION =====================
		TestScenarioGenerator scenarioGenerator = TestScenarioGenerator.getInstance();
		scenarioGenerator.generateTestScenarios();
		List<TestScenario> initialScenarios = scenarioGenerator.getScenarios();

		// ======================= FIRST PHASE STUB GENERATION ========================
		StubGenerator firstPhaseGenerator = new FirstPhaseStubStrategy(initialScenarios);
		Stub firstPhaseStub = firstPhaseGenerator.generateStub();
		directory.createFirstStubDir();
		firstPhaseStub.dumpStub(directory.getFirstStubDir());

//		CompilationContext cc = new CompilationContext(directory.getExperimentDir(), firstPhaseStub.getStubName() + ".java",
//				Options.I().getClassesPath() + File.pathSeparatorChar + 
//				SBES.class.getProtectionDomain().getCodeSource().getLocation().getPath()); //FIXME
//
//		if (!Compilation.compile(cc)) {
//			throw new SBESException("Unable to generate compilable stub, give up!");
//		}
		
//		while (foundSolution || iterations > maxIterations)
		// ========================== FIRST PHASE SYNTHESIS ===========================
//		ExecutionManager manager = new ExecutionManager();
//		Evosuite evosuiteCommand = new EvosuiteFirstStageStrategy(firstPhaseStub.getStubName(), 
//				ClassUtils.getMethodname(Options.I().getMethodSignature()));
//		ExecutionResult result = manager.execute(evosuiteCommand);
		
		// ======================= SECOND PHASE STUB GENERATION =======================
		// ========================== SECOND PHASE SYNTHESIS ==========================
	}

}
