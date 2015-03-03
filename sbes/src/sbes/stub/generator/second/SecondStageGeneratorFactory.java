package sbes.stub.generator.second;

import japa.parser.ast.body.FieldDeclaration;

import java.util.ArrayList;
import java.util.List;

import sbes.result.CarvingResult;
import sbes.result.TestScenario;
import sbes.stub.Stub;
import sbes.stub.generator.first.FirstStageGenericStubGenerator;
import sbes.stub.generator.first.FirstStageStubGenerator;

public class SecondStageGeneratorFactory {

	private SecondStageGeneratorFactory() {}
	
	public static SecondStageStubGenerator createGenerator(FirstStageStubGenerator firstGenerator, Stub stub, CarvingResult candidateES) {
		List<FieldDeclaration> fields = new ArrayList<>();
		for (TestScenario scenario : firstGenerator.getScenarios()) {
			fields.addAll(scenario.getInputs());
		}
		if (firstGenerator instanceof FirstStageGenericStubGenerator) {
			FirstStageGenericStubGenerator fs = (FirstStageGenericStubGenerator) firstGenerator;
			return new SecondStageGenericStubGenerator(firstGenerator.getScenarios(), stub, candidateES, fields, fs.getConcreteClass());
		}
		else {
			return new SecondStageStubGenerator(firstGenerator.getScenarios(), stub, candidateES, fields);
		}
	}
	
}
