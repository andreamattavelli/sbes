package sbes.scenario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sbes.SBESException;
import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteTestScenario;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.execution.WorkerException;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.result.TestScenario;
import sbes.testcase.Carver;
import sbes.testcase.CarvingContext;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;
import sbes.util.ClassUtils;
import sbes.util.EvosuiteUtils;
import sbes.util.IOUtils;

public class TestScenarioGenerator {

	private static final Logger logger = new Logger(TestScenarioGenerator.class);

	private static TestScenarioGenerator instance;

	private List<TestScenario> scenarios;

	private TestScenarioGenerator() {
		scenarios = new ArrayList<TestScenario>();
	}

	public static TestScenarioGenerator getInstance() {
		if (instance == null) {
			instance = new TestScenarioGenerator();
		}
		return instance;
	}

	public void generateTestScenarios() throws SBESException {
		logger.info("Generating initial test scenarios");
		try {
			ExecutionResult result = generate();

			logger.debug(result.getStdout());
			logger.debug(result.getStderr());
			
			logger.debug("Check whether the generation was successful");
			if (result.getExitStatus() != 0 || !EvosuiteUtils.succeeded(result)) {
				throw new SBESException("Generation failed due " + result.getStdout() + System.lineSeparator() + result.getStderr());
			}

			logger.debug("Check whether the generated test cases compile");
			if (!isCompilable(result)) {
				throw new SBESException("Unable to generate compilable test scenarios, give up!");
			}

			List<CarvingResult> carvedTests = carveTestScenarios(result);
			if (carvedTests.isEmpty()) {
				throw new SBESException("Unable to generate any test scenarios, give up!");
			}

			testToArrayScenario(carvedTests);

			logger.info("Generated " + scenarios.size() + " initial test scenarios - Done");
		} catch (WorkerException e) {
			logger.fatal("Stopping test scenario generation: " + e.getMessage());
			logger.error("Stack trace: ", e);
			throw new SBESException("Unable to generate initial test scenarios");
		}
	}
	
	public void loadTestScenarios() {
		logger.info("Loading initial test scenarios");
		File scenarioFile = Options.I().getTestScenarioPath();
		String scenarioPath = scenarioFile.getAbsolutePath();
		String scenarioDir = scenarioPath.substring(0, scenarioPath.lastIndexOf(File.separatorChar));
		String scenarioFilename = scenarioPath.substring(scenarioPath.lastIndexOf(File.separatorChar) + 1);
		
		CarvingContext context = new CarvingContext(scenarioDir, scenarioFilename);
		Carver carver = new Carver(context, true);
		List<CarvingResult> carvedTests = carver.carveBodyFromTests();
		if (carvedTests.isEmpty()) {
			throw new SBESException("Unable to generate any test scenarios, give up!");
		}
		
		testToArrayScenario(carvedTests);

		logger.info("Generated " + scenarios.size() + " initial test scenarios - Done");
	}
	
	private  ExecutionResult generate() {
		ExecutionManager manager = new ExecutionManager();
		Evosuite evosuiteCommand = new EvosuiteTestScenario(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()), 
				ClassUtils.getMethodname(Options.I().getMethodSignature()));
		return manager.execute(evosuiteCommand);
	}

	private boolean isCompilable(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory = IOUtils.concatPath(result.getOutputDir(), packagename);

		String classPath = Options.I().getClassesPath()
				+ File.pathSeparatorChar + Options.I().getJunitPath()
				+ File.pathSeparatorChar + Options.I().getEvosuitePath();

		return Compilation.compile(new CompilationContext(testDirectory, result.getFilename(), result.getOutputDir(), classPath));
	}

	private List<CarvingResult> carveTestScenarios(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory = IOUtils.concatPath(result.getOutputDir(), packagename);

		CarvingContext context = new CarvingContext(testDirectory, result.getFilename());

		Carver carver = new Carver(context, true);
		return carver.carveBodyFromTests();
	}

	private void testToArrayScenario(List<CarvingResult> carvedTests) {
		logger.debug("Generalizing carved bodies to array-based test scenarios");
		for (CarvingResult carvedTest : carvedTests) {
			TestScenarioGeneralizer generalizer = new TestScenarioGeneralizer(scenarios.size());
			TestScenario ts = generalizer.generalizeTestToScenario(carvedTest);
			if (ts != null) {
				scenarios.add(ts);
			}
		}
		logger.debug("Generalization - done");
	}

	public TestScenario carvedTestToScenario(CarvingResult carvedTest) {
		TestScenarioGeneralizer generalizer = new TestScenarioGeneralizer(scenarios.size());
		TestScenario scenario = generalizer.generalizeTestToScenario(carvedTest);
		scenarios.add(scenario);
		return scenario;
	}

	public List<TestScenario> getScenarios() {
		return scenarios;
	}

}
