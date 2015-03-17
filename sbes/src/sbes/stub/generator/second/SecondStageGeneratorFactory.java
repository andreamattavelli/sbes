package sbes.stub.generator.second;

import japa.parser.ast.body.FieldDeclaration;

import java.util.ArrayList;
import java.util.List;

import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.stub.generator.first.FirstStageGeneratorStub;
import sbes.stub.generator.first.FirstStageGeneratorStubWithGenerics;

public class SecondStageGeneratorFactory {

	private SecondStageGeneratorFactory() {}
	
	public static SecondStageGeneratorStub createGenerator(FirstStageGeneratorStub firstGenerator, Stub stub, CarvingResult candidateES) {
		List<FieldDeclaration> fields = new ArrayList<>();
		for (TestScenario scenario : firstGenerator.getScenarios()) {
			fields.addAll(scenario.getInputAsFields());
		}
		if (firstGenerator instanceof FirstStageGeneratorStubWithGenerics) {
			FirstStageGeneratorStubWithGenerics fs = (FirstStageGeneratorStubWithGenerics) firstGenerator;
			return new SecondStageGeneratorStubWithGenerics(firstGenerator.getScenarios(), stub, candidateES, fields, fs.getGenericToConcreteClasses());
		}
		else {
			return new SecondStageGeneratorStub(firstGenerator.getScenarios(), stub, candidateES, fields);
		}
	}
	
}
