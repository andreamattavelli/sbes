package sbes;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sbes.ast.inliner.AllInlinerTests;
import sbes.ast.renamer.AllAstRenamerTests;
import sbes.distance.AllDistanceTests;
import sbes.result.AllResultTests;
import sbes.scenario.AllScenarioTests;
import sbes.stub.generator.AllGeneratorTests;
import sbes.symbolic.AllSymbolicTests;
import sbes.testcase.AllTestCaseTests;
import sbes.util.AllUtilsTests;

@RunWith(Suite.class)
@SuiteClasses({
	AllInlinerTests.class,
	AllAstRenamerTests.class,
	AllDistanceTests.class,
	AllResultTests.class,
	AllScenarioTests.class,
	AllGeneratorTests.class,
	AllSymbolicTests.class,
	AllTestCaseTests.class,
	AllUtilsTests.class
	
})
public class AllTests {

}
