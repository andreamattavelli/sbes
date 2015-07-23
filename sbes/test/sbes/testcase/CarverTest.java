package sbes.testcase;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import sbes.option.Options;
import sbes.result.CarvingResult;

public class CarverTest {

	@Test
	public void test() {
		Options.I().setTargetMethod("stack.util.Stack.push(Object)");
		CarvingContext cc = new CarvingContext("./test/resources", "TestSuite_method_under_test.java");
		Carver carver = new Carver(cc, false);
		List<CarvingResult> list = carver.carveBodyFromTests();
		Assert.assertEquals(3, list.size());
	}
	
}
