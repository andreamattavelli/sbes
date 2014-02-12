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
		ClasspathHandler.checkClasspath();
		
		DirectoryUtils directory = DirectoryUtils.getInstance();
		directory.createExperimentDir();

		TestScenarioGenerator scenarioGenerator = TestScenarioGenerator.getInstance();
		scenarioGenerator.generateTestScenarios();

		List<TestScenario> initialScenarios = scenarioGenerator.getScenarios();

		StubGenerator firstPhaseGenerator = new FirstPhaseStubStrategy(initialScenarios);
		Stub firstPhaseStub = firstPhaseGenerator.generateStub();

		firstPhaseStub.dumpStub(DirectoryUtils.getInstance().getExperimentDir() + File.separator +
								"evosuite-test/org/graphstream/graph/implementations"); //FIXME

		CompilationContext cc = new CompilationContext(DirectoryUtils.getInstance().getExperimentDir() + File.separator +
				"evosuite-test/org/graphstream/graph/implementations", firstPhaseStub.getStubName() + ".java",
				Options.I().getClassesPath() + File.pathSeparatorChar + 
				SBES.class.getProtectionDomain().getCodeSource().getLocation().getPath()); //FIXME

		if (!Compilation.compile(cc)) {
			throw new SBESException("");
		}

		ExecutionManager manager = new ExecutionManager();
		Evosuite evosuiteCommand = new EvosuiteFirstStageStrategy(firstPhaseStub.getStubName(), 
				ClassUtils.getMethodname(Options.I().getMethodSignature()));
		ExecutionResult result = manager.execute(evosuiteCommand);

		System.out.println(result.getStderr());
		System.out.println(result.getStdout());
	}

}
