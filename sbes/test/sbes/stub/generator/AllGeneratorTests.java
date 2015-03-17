package sbes.stub.generator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sbes.stub.generator.first.FirstStageGeneratorFactoryTest;
import sbes.stub.generator.first.FirstStageStubGeneratorTest;
import sbes.stub.generator.second.SecondStageGeneratorFactoryTest;
import sbes.stub.generator.second.SecondStageStubGeneratorTest;

@RunWith(Suite.class)
@SuiteClasses({
	FirstStageGeneratorFactoryTest.class,
	FirstStageStubGeneratorTest.class,
	SecondStageGeneratorFactoryTest.class,
	SecondStageStubGeneratorTest.class
})
public class AllGeneratorTests {

}
