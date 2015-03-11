package sbes;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sbes.distance.DistanceTest;
import sbes.stub.FirstStageStubGeneratorTest;
import sbes.stub.SecondStageStubGeneratorTest;

@RunWith(Suite.class)
@SuiteClasses({DistanceTest.class, FirstStageStubGeneratorTest.class, SecondStageStubGeneratorTest.class})
public class AllTests {

}
