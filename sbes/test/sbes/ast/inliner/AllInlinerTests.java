package sbes.ast.inliner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ClassToMocksInlinerTest.class,
	InlinerTest.class
})
public class AllInlinerTests {

}
