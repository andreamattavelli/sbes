package sbes.distance;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	DistanceTest.class, 
	LevenshteinDistanceTest.class,
	ObjectDistanceTest.class, 
	PrimitiveDistanceTest.class
})
public class AllDistanceTests {

}
