package sbes.scenario;

import java.io.File;
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
import sbes.util.ClassUtils;
import sbes.util.DirectoryUtils;

public class TestScenarioGenerator {
	
	private static final Logger logger = new Logger(TestScenarioGenerator.class);

	public void generateTestScenarios() throws SBESException {
		logger.info("Generating initial test scenarios");
		try {
			ExecutionResult result = generate();
			List<CarvingResult> scenarios = carveTestScenarios(result);
			
			if (scenarios.isEmpty()) {
				throw new SBESException("Unable to generate any test scenarios, give up!");
			}
			
		} catch (WorkerException e) {
			logger.fatal("Stopping test scenario generation: " + e.getMessage());
			throw new SBESException("Unable to generate initial test scenarios");
		}
		logger.info("Generating initial test scenarios - Done");
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
	
}
