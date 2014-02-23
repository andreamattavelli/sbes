package sbes.stub.generator;

import java.util.List;

import sbes.scenario.GenericTestScenario;
import sbes.scenario.TestScenario;

public class FirstStageGeneratorFactory {

	private FirstStageGeneratorFactory() {}
	
	public static FirstStageStubGenerator createGenerator(List<TestScenario> scenarios) {
		boolean generics = false;
		for (TestScenario testScenario : scenarios) {
			if (testScenario instanceof GenericTestScenario) {
				generics = true;
				break;
			}
		}
		if (generics) {
			return new FirstStageGenericStubGenerator(scenarios);
		}
		else {
			return new FirstStageStubGenerator(scenarios);
		}
	}
	
}
