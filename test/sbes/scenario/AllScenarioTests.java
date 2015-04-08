package sbes.scenario;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	CounterexampleGeneralizerTest.class, 
	TestScenarioRepositoryTest.class,
	TestScenarioGeneralizerTest.class, 
	TestScenarioLoaderTest.class
})
public class AllScenarioTests {

}
