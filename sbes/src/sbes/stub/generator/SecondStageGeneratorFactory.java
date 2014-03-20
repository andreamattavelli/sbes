package sbes.stub.generator;

import japa.parser.ast.body.FieldDeclaration;

import java.util.ArrayList;
import java.util.List;

import sbes.result.CarvingResult;
import sbes.result.TestScenario;
import sbes.stub.Stub;

public class SecondStageGeneratorFactory {

	private SecondStageGeneratorFactory() {}
	
	public static SecondStageStubGenerator createGenerator(FirstStageStubGenerator firstGenerator, Stub stub, CarvingResult candidateES) {
		List<FieldDeclaration> fields = new ArrayList<>();
		for (TestScenario scenario : firstGenerator.scenarios) {
			fields.addAll(scenario.getInputs());
		}
		if (firstGenerator instanceof FirstStageGenericStubGenerator) {
			FirstStageGenericStubGenerator fs = (FirstStageGenericStubGenerator) firstGenerator;
			return new SecondStageGenericStubGenerator(stub, candidateES, fields, fs.getConcreteClass());
		}
		else {
			return new SecondStageStubGenerator(stub, candidateES, fields);
		}
	}
	
}
