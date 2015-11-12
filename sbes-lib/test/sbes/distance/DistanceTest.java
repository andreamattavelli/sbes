package sbes.distance;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class DistanceTest {

	@BeforeClass
	public static void setUp() throws Exception {
//		Options.I().setLogLevel(Level.FATAL);
	}
	
	@Test
	public void test0()  throws Throwable  {
		Object object0 = new Object();
		DistancePair distancePair0 = new DistancePair(object0, object0);
		Object object1 = new Object();
		DistancePair distancePair1 = new DistancePair(object1, object1);
		double double0 = Distance.distance((Object) distancePair1, (Object) distancePair0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test1()  throws Throwable  {
		DistancePair distancePair0 = new DistancePair((Object) "#i:c<!{*He3YI", (Object) "#i:c<!{*He3YI");
		distancePair0.o2 = (Object) distancePair0;
		double double0 = Distance.distance(distancePair0.o2, (Object) distancePair0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test2()  throws Throwable  {
		double double0 = Distance.distance((Object) 0.0, (Object) 0.0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test3()  throws Throwable  {
		Integer integer0 = new Integer(1);
		DistancePair distancePair0 = new DistancePair((Object) null, (Object) integer0);
		DistancePair distancePair1 = new DistancePair((Object) null, (Object) null);
		double double0 = Distance.distance((Object) distancePair0, (Object) distancePair1);
		assertEquals(1000000.0, double0, 0.01D);
	}

	@Test
	public void test4()  throws Throwable  {
		DistancePair distancePair0 = new DistancePair((Object) null, (Object) null);
		DistancePair distancePair1 = new DistancePair((Object) ";qPDZInrSn", (Object) ";qPDZInrSn");
		double double0 = Distance.distance((Object) distancePair0, (Object) distancePair1);
		assertEquals(2000000.0, double0, 0.01D);
	}

	@Test
	public void test5()  throws Throwable  {
		Object object0 = new Object();
		double double0 = Distance.distance(object0, (Object) null);
		assertEquals(1000000.0, double0, 0.01D);
	}

	@Test
	public void test6()  throws Throwable  {
		Integer integer0 = new Integer(1);
		double double0 = Distance.distance((Object) null, (Object) integer0);
		assertEquals(1000000.0, double0, 0.01D);
	}

	@Test
	public void test7()  throws Throwable  {
		double double0 = Distance.distance((Object) null, (Object) null);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test8()  throws Throwable  {
		DistancePair distancePair0 = new DistancePair((Object) "#i:c<!{*He3YI", (Object) "#i:c<!{*He3YI");
		double double0 = Distance.distance(distancePair0.o2, (Object) distancePair0);
		assertEquals(1000.0, double0, 0.01D);
	}
	
}
