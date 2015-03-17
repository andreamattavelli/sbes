package sbes;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sbes.distance.AllDistanceTests;
import sbes.scenario.AllScenarioTests;
import sbes.stub.generator.AllGeneratorTests;

@RunWith(Suite.class)
@SuiteClasses({ 
	AllDistanceTests.class,
	AllScenarioTests.class,
	AllGeneratorTests.class
})
public class AllTests {

}
