package sbes;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sbes.distance.AllDistanceTests;
import sbes.scenario.AllScenarioTests;
import sbes.stub.generator.AllGeneratorTests;
import sbes.util.AllUtilsTests;

@RunWith(Suite.class)
@SuiteClasses({ 
	AllDistanceTests.class,
	AllScenarioTests.class,
	AllGeneratorTests.class,
	AllUtilsTests.class
})
public class AllTests {

}
