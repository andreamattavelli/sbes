package sbes.stub.generator.second;

import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.stub.Stub;
import sbes.stub.generator.first.FirstStageGeneratorStub;
import sbes.stub.generator.first.FirstStageGeneratorStubWithGenerics;
import sbes.stub.generator.second.symbolic.SecondStageGeneratorStubSE;
import sbes.stub.generator.second.symbolic.SecondStageGeneratorStubWithGenericsSE;

public class SecondStageGeneratorFactory {

	private SecondStageGeneratorFactory() {}
	
	public static SecondStageGeneratorStub createGenerator(FirstStageGeneratorStub firstGenerator, Stub stub, CarvingResult candidateES) {
		if (Options.I().isSymbolicExecutionCounterexample()) {
			if (firstGenerator instanceof FirstStageGeneratorStubWithGenerics) {
				FirstStageGeneratorStubWithGenerics fs = (FirstStageGeneratorStubWithGenerics) firstGenerator;
				return new SecondStageGeneratorStubWithGenericsSE(firstGenerator.getScenarios(), stub, candidateES, fs.getGenericToConcreteClasses());
			}
			else {
				return new SecondStageGeneratorStubSE(firstGenerator.getScenarios(), stub, candidateES);
			}
		} else {
			if (firstGenerator instanceof FirstStageGeneratorStubWithGenerics) {
				FirstStageGeneratorStubWithGenerics fs = (FirstStageGeneratorStubWithGenerics) firstGenerator;
				return new SecondStageGeneratorStubWithGenerics(firstGenerator.getScenarios(), stub, candidateES, fs.getGenericToConcreteClasses());
			}
			else {
				return new SecondStageGeneratorStub(firstGenerator.getScenarios(), stub, candidateES);
			}
		}
	}
	
}
