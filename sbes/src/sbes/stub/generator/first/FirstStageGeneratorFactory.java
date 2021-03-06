package sbes.stub.generator.first;

import java.util.List;

import sbes.scenario.TestScenario;
import sbes.scenario.TestScenarioWithGenerics;

public class FirstStageGeneratorFactory {

	private FirstStageGeneratorFactory() {}
	
	public static FirstStageGeneratorStub createGenerator(List<TestScenario> scenarios) {
		boolean generics = false;
		for (TestScenario testScenario : scenarios) {
			if (testScenario instanceof TestScenarioWithGenerics) {
				generics = true;
				break;
			}
		}
		if (generics) {
			return new FirstStageGeneratorStubWithGenerics(scenarios);
		} else {
			return new FirstStageGeneratorStub(scenarios);
		}
	}
	
}
