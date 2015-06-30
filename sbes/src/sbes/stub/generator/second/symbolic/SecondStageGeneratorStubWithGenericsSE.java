package sbes.stub.generator.second.symbolic;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import sbes.logging.Logger;
import sbes.result.CarvingResult;
import sbes.scenario.TestScenario;
import sbes.stub.Stub;
import sbes.stub.generator.second.SecondStageGeneratorStubWithGenerics;

public class SecondStageGeneratorStubWithGenericsSE extends SecondStageGeneratorStubSE {

	private static final Logger logger = new Logger(SecondStageGeneratorStubWithGenerics.class);

	private Map<TypeVariable<?>, String> genericToConcreteClasses;

	public SecondStageGeneratorStubWithGenericsSE(
			final List<TestScenario> scenarios, 
			final Stub stub,
			final CarvingResult candidateES,
			final Map<TypeVariable<?>, String> genericToConcreteClasses) {
		super(scenarios, stub, candidateES);
		this.genericToConcreteClasses = genericToConcreteClasses;
	}
	
}
