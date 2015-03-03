package sbes.stub.generator.second;

import japa.parser.ast.body.FieldDeclaration;

import java.util.ArrayList;
import java.util.List;

import sbes.result.CarvingResult;
import sbes.result.TestScenario;
import sbes.stub.Stub;
import sbes.stub.generator.first.FirstStageGeneratorStubWithGenerics;
import sbes.stub.generator.first.FirstStageGeneratorStub;

public class SecondStageGeneratorFactory {

	private SecondStageGeneratorFactory() {}
	
	public static SecondStageGeneratorStub createGenerator(FirstStageGeneratorStub firstGenerator, Stub stub, CarvingResult candidateES) {
		List<FieldDeclaration> fields = new ArrayList<>();
		for (TestScenario scenario : firstGenerator.getScenarios()) {
			fields.addAll(scenario.getInputs());
		}
		if (firstGenerator instanceof FirstStageGeneratorStubWithGenerics) {
			FirstStageGeneratorStubWithGenerics fs = (FirstStageGeneratorStubWithGenerics) firstGenerator;
			return new SecondStageGeneratorGenericStub(firstGenerator.getScenarios(), stub, candidateES, fields, fs.getConcreteClass());
		}
		else {
			return new SecondStageGeneratorStub(firstGenerator.getScenarios(), stub, candidateES, fields);
		}
	}
	
}
