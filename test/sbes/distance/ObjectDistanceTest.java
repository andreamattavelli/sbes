package sbes.distance;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import sbes.logging.Level;
import sbes.option.Options;

public class ObjectDistanceTest {

	@BeforeClass
	public static void setUp() throws Exception {
		Options.I().setLogLevel(Level.FATAL);
	}
	
	@Test
	public void test0()  throws Throwable  {
		ObjectDistance objectDistance0 = new ObjectDistance();
		Integer integer0 = new Integer((-14));
		double double0 = ObjectDistance.getNullDistance((Object) integer0, (Object) objectDistance0);
		assertEquals(1000000.0, double0, 0.01D);
	}

	@Test
	public void test1()  throws Throwable  {
		Integer integer0 = new Integer((-14));
		double double0 = ObjectDistance.getNullDistance((Object) null, (Object) integer0);
		assertEquals(1000000.0, double0, 0.01D);
	}

	@Test
	public void test2()  throws Throwable  {
		double double0 = ObjectDistance.getNullDistance((Object) null, (Object) "sbes.utilXRefleti[nUtiP");
		assertEquals(1000000.0, double0, 0.01D);
	}

	@Test
	public void test3()  throws Throwable  {
		ObjectDistance objectDistance0 = new ObjectDistance();
		double double0 = ObjectDistance.getNullDistance((Object) objectDistance0, (Object) null);
		assertEquals(1000000.0, double0, 0.01D);
	}

}
