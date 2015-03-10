package sbes.scenario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sbes.exceptions.SBESException;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.result.TestScenario;
import sbes.testcase.Carver;
import sbes.testcase.CarvingContext;

public class TestScenarioGenerator {

	private static final Logger logger = new Logger(TestScenarioGenerator.class);

	private static TestScenarioGenerator instance;

	private List<TestScenario> scenarios;
	private List<TestScenario> counterexamples;

	private TestScenarioGenerator() {
		scenarios = new ArrayList<TestScenario>();
		counterexamples = new ArrayList<TestScenario>();
	}

	public static TestScenarioGenerator getInstance() {
		if (instance == null) {
			instance = new TestScenarioGenerator();
		}
		return instance;
	}
	
	public void reset() {
		counterexamples.clear();
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
	
	public void carvedCounterexampleToScenario(CarvingResult carvedCounterexample) {
		CounterexampleGeneralizer generalizer = new CounterexampleGeneralizer(scenarios.size());
		TestScenario scenario = generalizer.generalizeCounterexampleToScenario(carvedCounterexample);
		counterexamples.add(scenario);
	}

	public List<TestScenario> getScenarios() {
		ArrayList<TestScenario> all = new ArrayList<>();
		all.addAll(scenarios);
		all.addAll(counterexamples);
		return all;
	}

}
