package sbes.stub.generator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sbes.stub.generator.first.FirstStageGeneratorFactoryTest;
import sbes.stub.generator.first.FirstStageStubGeneratorTest;
import sbes.stub.generator.first.FirstStageStubGeneratorTestWithoutGenerics;
import sbes.stub.generator.second.SecondStageGeneratorFactoryTest;
import sbes.stub.generator.second.SecondStageStubGeneratorTest;
import sbes.stub.generator.second.SecondStageStubGeneratorTestWithoutGenerics;
import sbes.stub.generator.second.symbolic.SecondStageStubGeneratorSETest;

@RunWith(Suite.class)
@SuiteClasses({
	FirstStageGeneratorFactoryTest.class,
	FirstStageStubGeneratorTest.class,
	FirstStageStubGeneratorTestWithoutGenerics.class,
	SecondStageGeneratorFactoryTest.class,
	SecondStageStubGeneratorTest.class,
	SecondStageStubGeneratorTestWithoutGenerics.class,
	SecondStageStubGeneratorSETest.class
})
public class AllGeneratorTests {

}
