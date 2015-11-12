package sbes.distance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

public class PrimitiveDistanceTest {
	
	@BeforeClass
	public static void setUp() throws Exception {
//		Options.I().setLogLevel(Level.FATAL);
	}
	
	@Test
	public void test00()  throws Throwable  {
		boolean[] booleanArray0 = new boolean[7];
		double double0 = PrimitiveDistance.distance((Object) booleanArray0, (Object) booleanArray0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test01()  throws Throwable  {
		double double0 = PrimitiveDistance.booleanDistance(false, true);
		assertEquals(1.0, double0, 0.01D);
	}

	@Test
	public void test02()  throws Throwable  {
		short[] shortArray0 = new short[3];
		double double0 = PrimitiveDistance.shortDistance(shortArray0, shortArray0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test03()  throws Throwable  {
		Object object0 = new Object();
		Character character0 = new Character('k');
		double double0 = PrimitiveDistance.distance(object0, (Object) character0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test04()  throws Throwable  {
		double double0 = PrimitiveDistance.distance((Object) (byte)0, (Object) (byte)0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test05()  throws Throwable  {
		Boolean boolean0 = Boolean.valueOf(true);
		double double0 = PrimitiveDistance.distance((Object) boolean0, (Object) boolean0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test06()  throws Throwable  {
		Object object0 = new Object();
		// Undeclared exception!
		try {
			PrimitiveDistance.distance((Object) 0.0, object0);
			fail("Expecting exception: ClassCastException");

		} catch(ClassCastException e) {
			//
			// java.lang.Object cannot be cast to java.lang.Double
			//
		}
	}

	@Test
	public void test07()  throws Throwable  {
		double[] doubleArray0 = new double[4];
		double double0 = PrimitiveDistance.distance((Object) doubleArray0, (Object) doubleArray0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test08()  throws Throwable  {
		Short short0 = new Short((short) (-2718));
		Float float0 = new Float((double) (short) (-2718));
		// Undeclared exception!
		try {
			PrimitiveDistance.distance((Object) float0, (Object) short0);
			fail("Expecting exception: ClassCastException");

		} catch(ClassCastException e) {
			//
			// java.lang.Short cannot be cast to java.lang.Float
			//
		}
	}

	@Test
	public void test09()  throws Throwable  {
		float[] floatArray0 = new float[5];
		double double0 = PrimitiveDistance.distance((Object) floatArray0, (Object) floatArray0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test10()  throws Throwable  {
		Long long0 = new Long((long) (short)448);
		double double0 = PrimitiveDistance.distance((Object) long0, (Object) long0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test11()  throws Throwable  {
		long[] longArray0 = new long[16];
		double double0 = PrimitiveDistance.distance((Object) longArray0, (Object) longArray0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test12()  throws Throwable  {
		Boolean boolean0 = Boolean.valueOf(true);
		short[] shortArray0 = new short[19];
		// Undeclared exception!
		try {
			PrimitiveDistance.distance((Object) shortArray0, (Object) boolean0);
			fail("Expecting exception: ClassCastException");

		} catch(ClassCastException e) {
			//
			// java.lang.Boolean cannot be cast to [S
			//
		}
	}

	@Test
	public void test13()  throws Throwable  {
		Character character0 = new Character('Z');
		Object object0 = new Object();
		// Undeclared exception!
		try {
			PrimitiveDistance.distance((Object) character0, object0);
			fail("Expecting exception: ClassCastException");

		} catch(ClassCastException e) {
			//
			// java.lang.Object cannot be cast to java.lang.Character
			//
		}
	}

	@Test
	public void test14()  throws Throwable  {
		double[] doubleArray0 = new double[4];
		Integer integer0 = new Integer(0);
		// Undeclared exception!
		try {
			PrimitiveDistance.distance((Object) integer0, (Object) doubleArray0);
			fail("Expecting exception: ClassCastException");

		} catch(ClassCastException e) {
			//
			// [D cannot be cast to java.lang.Integer
			//
		}
	}

	@Test
	public void test16()  throws Throwable  {
		int[] intArray0 = new int[1];
		double double0 = PrimitiveDistance.distance((Object) intArray0, (Object) intArray0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test17()  throws Throwable  {
		byte[] byteArray0 = new byte[6];
		double double0 = PrimitiveDistance.distance((Object) byteArray0, (Object) byteArray0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test18()  throws Throwable  {
		Short short0 = new Short((short)1262);
		double double0 = PrimitiveDistance.distance((Object) short0, (Object) short0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test19()  throws Throwable  {
		char[] charArray0 = new char[5];
		double double0 = PrimitiveDistance.distance((Object) charArray0, (Object) charArray0);
		assertEquals(0.0, double0, 0.01D);
	}

	@Test
	public void test20()  throws Throwable  {
		double double0 = PrimitiveDistance.charDistance('k', 'k');
		assertEquals(0.0, double0, 0.01D);
	}
	
}
