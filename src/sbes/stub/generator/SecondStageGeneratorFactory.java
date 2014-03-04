package sbes.stub.generator;

import sbes.result.CarvingResult;
import sbes.stub.Stub;

public class SecondStageGeneratorFactory {

	private SecondStageGeneratorFactory() {}
	
	public static SecondStageStubGenerator createGenerator(FirstStageStubGenerator firstGenerator, Stub stub, CarvingResult candidateES) {
		if (firstGenerator instanceof FirstStageGenericStubGenerator) {
			FirstStageGenericStubGenerator fs = (FirstStageGenericStubGenerator) firstGenerator;
			return new SecondStageGenericStubGenerator(stub, candidateES, fs.getConcreteClass());
		}
		else {
			return new SecondStageStubGenerator(stub, candidateES);
		}
	}
	
}
