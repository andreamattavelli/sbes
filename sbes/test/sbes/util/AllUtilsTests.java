package sbes.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ReflectionUtilsTest.class,
	GenericsUtilsTest.class,
	ASTUtilsTest.class
})
public class AllUtilsTests {

}
