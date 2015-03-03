package sbes.stub.generator.first;

import java.util.List;

import sbes.result.TestScenario;
import sbes.scenario.GenericTestScenario;

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
