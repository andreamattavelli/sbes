package sbes.scenario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sbes.exceptions.SBESException;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.scenario.generalizer.TestScenarioGeneralizer;
import sbes.testcase.Carver;
import sbes.testcase.CarvingContext;

public class TestScenarioLoader {

	private static final Logger logger = new Logger(TestScenarioLoader.class);
	
	public static List<TestScenario> loadTestScenarios() {
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
		else if (carvedTests.size() > 1) {
			logger.warn("Found more than one test scenario! Heuristically pruning to one, for faster convergence");
			List<CarvingResult> retain = carvedTests.subList(0, 1);
			carvedTests.retainAll(retain);
		}
		
		List<TestScenario> scenarios = testToArrayScenario(carvedTests);

		logger.info("Generated " + scenarios.size() + " initial test scenarios - Done");
		
		return scenarios;
	}

	private static List<TestScenario> testToArrayScenario(List<CarvingResult> carvedTests) {
		List<TestScenario> scenarios = new ArrayList<TestScenario>();
		logger.debug("Generalizing carved bodies to array-based test scenarios");
		TestScenarioGeneralizer tsg = new TestScenarioGeneralizer();
		for (CarvingResult carvedTest : carvedTests) {
			TestScenario ts = tsg.testToTestScenario(carvedTest);
			if (ts != null) {
				scenarios.add(ts);
				TestScenarioRepository.I().addScenario(ts);
			}
		}
		logger.debug("Generalization - done");
		return scenarios;
	}

}
