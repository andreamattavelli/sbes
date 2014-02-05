package sbes.scenario;

import sbes.Options;
import sbes.SBESException;
import sbes.evosuite.Evosuite;
import sbes.evosuite.EvosuiteTestScenarioStrategy;
import sbes.execution.ExecutionManager;
import sbes.execution.ExecutionResult;
import sbes.execution.WorkerException;
import sbes.logging.Logger;
import sbes.util.ClassUtils;

public class TestScenarioGenerator {
	
	private static final Logger logger = new Logger(TestScenarioGenerator.class);

	public void generateTestScenarios() throws SBESException {
		logger.info("Generating initial test scenarios");
		try {
			ExecutionResult result = generate();
//			carveTestCases(pair);			
		}
		catch (WorkerException e) {
			logger.fatal("Stopping test scenario generation: " + e.getMessage());
			throw new SBESException("Unable to generate initial test scenarios");
		}
		logger.info("Generating initial test scenarios - Done");
	}
	
	private ExecutionResult generate() {
		ExecutionManager manager = new ExecutionManager();
		Evosuite evosuiteCommand = new EvosuiteTestScenarioStrategy(ClassUtils.getClassname(Options.I().getMethodSignature()), 
																	ClassUtils.getMethodname(Options.I().getMethodSignature()));
		return manager.execute(evosuiteCommand);
	}
	
}
