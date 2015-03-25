package sbes.scenario;

import java.util.ArrayList;
import java.util.List;

public class TestScenarioRepository {

	private static TestScenarioRepository instance;

	private List<TestScenario> scenarios;
	private List<TestScenario> counterexamples;

	private TestScenarioRepository() {
		scenarios = new ArrayList<TestScenario>();
		counterexamples = new ArrayList<TestScenario>();
	}

	public static TestScenarioRepository I() {
		if (instance == null) {
			instance = new TestScenarioRepository();
		}
		return instance;
	}
	
	public static void reset() {
		instance = null;
	}
	
	public void resetCounterexamples() {
		counterexamples.clear();
	}
	
	public List<TestScenario> getScenarios() {
		ArrayList<TestScenario> all = new ArrayList<>();
		all.addAll(scenarios);
		all.addAll(counterexamples);
		return all;
	}

	public void addScenario(final TestScenario testScenario) {
		scenarios.add(testScenario);
	}
	
	public void addScenarios(final List<TestScenario> initialScenarios) {
		scenarios.addAll(initialScenarios);
	}
	
	public void addCounterexample(final TestScenario counterexampleScenario) {
		counterexamples.add(counterexampleScenario);
	}
	
}
