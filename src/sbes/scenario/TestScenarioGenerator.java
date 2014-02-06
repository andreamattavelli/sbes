package sbes.scenario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sbes.Options;
import sbes.SBESException;
import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteTestScenarioStrategy;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.execution.WorkerException;
import sbes.logging.Logger;
import sbes.testcase.Carver;
import sbes.testcase.CarvingContext;
import sbes.testcase.CarvingResult;
import sbes.testcase.Compilation;
import sbes.testcase.CompilationContext;
import sbes.util.ClassUtils;
import sbes.util.DirectoryUtils;

public class TestScenarioGenerator {
	
	private static final Logger logger = new Logger(TestScenarioGenerator.class);

	public List<TestScenario> generateTestScenarios() throws SBESException {
		logger.info("Generating initial test scenarios");
		try {
			ExecutionResult result = generate();
			
			logger.debug("Check whether the generated test cases compile");
			if (!isCompilable(result)) {
				throw new SBESException("Unable to generate compilable test scenarios, give up!");
			}
			
			List<CarvingResult> carvedTests = carveTestScenarios(result);
			
			if (carvedTests.isEmpty()) {
				throw new SBESException("Unable to generate any test scenarios, give up!");
			}
			
			List<TestScenario> scenarios = testToArrayScenario(carvedTests);
			
			logger.info("Generated " + scenarios.size() + " initial test scenarios - Done");
			return scenarios;
		} catch (WorkerException e) {
			logger.fatal("Stopping test scenario generation: " + e.getMessage());
			throw new SBESException("Unable to generate initial test scenarios");
		}
	}

	private boolean isCompilable(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = ClassUtils.getPackage(signature);
		String testDirectory = DirectoryUtils.toPath(result.getOutputDir(),
													 packagename.replaceAll("\\.", File.separator));
		
		String classPath =	Options.I().getClassesPath() + File.pathSeparatorChar + 
							Options.I().getJunitPath() + File.pathSeparatorChar +
							Options.I().getEvosuitePath();
		
		CompilationContext context = new CompilationContext(testDirectory, result.getFilename(), classPath);
		
		return Compilation.compile(context);
	}

	private ExecutionResult generate() {
		ExecutionManager manager = new ExecutionManager();
		Evosuite evosuiteCommand = new EvosuiteTestScenarioStrategy(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()), 
																	ClassUtils.getMethodname(Options.I().getMethodSignature()));
		return manager.execute(evosuiteCommand);
	}
	
	private List<CarvingResult> carveTestScenarios(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = ClassUtils.getPackage(signature);
		String testDirectory = DirectoryUtils.toPath(result.getOutputDir(),
													 packagename.replaceAll("\\.", File.separator));
		
		CarvingContext context = new CarvingContext(testDirectory, result.getFilename());
		
		Carver carver = new Carver(context);
		return carver.carveBodyFromTests();
	}
	
	private List<TestScenario> testToArrayScenario(List<CarvingResult> carvedTests) {
		logger.debug("Generalizing carved bodies to array-based test scenarios");
		List<TestScenario> scenarios = new ArrayList<TestScenario>();
		
		for (CarvingResult carvedTest : carvedTests) {
			scenarios.add(generalizeTestToScenario(carvedTest));
		}
		
		logger.debug("Generalization - done");
		return scenarios;
	}
	
	private TestScenario generalizeTestToScenario(CarvingResult carvedTest) {
		
		return null;
	}
	
}
