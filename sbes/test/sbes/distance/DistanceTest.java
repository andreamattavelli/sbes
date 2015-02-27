package sbes.distance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DistanceTest {

	@Test
	public void testDistance_1()
		throws Exception {
		Object o1 = null;
		Object o2 = null;

		double result = Distance.distance(o1, o2);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	@Test
	public void testDistance_2()
		throws Exception {
		Object o1 = null;
		Object o2 = new Object();

		double result = Distance.distance(o1, o2);

		// add additional test code here
		assertEquals(1000000.0d, result, 0.1);
	}

	@Test
	public void testDistance_3()
		throws Exception {
		Object o1 = new Object();
		Object o2 = new Object();

		double result = Distance.distance(o1, o2);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	@Test
	public void testDistance_4()
		throws Exception {
		String s1 = "pippo";
		String s2 = "pippo";

		double result = Distance.distance(s1, s2);

		// add additional test code here
		assertEquals(0.0, result, 0.1);
	}

	@Test
	public void testDistance_5()
		throws Exception {
		String s1 = "pippo";
		String s2 = "pluto";

		double result = Distance.distance(s1, s2);

		// add additional test code here
		assertEquals(3.0, result, 0.1);
	}
	
	@Test
	public void testDistance_6()
		throws Exception {
		A o1 = new A();
		B o2 = new B();

		double result = Distance.distance(o1, o2);

		// add additional test code here
		assertEquals(1000.0, result, 0.1);
	}
	
	@Test
	public void testDistance_7()
		throws Exception {
		A o1 = new A();
		B o2 = null;

		double result = Distance.distance(o1, o2);

		// add additional test code here
		assertEquals(1000000.0d, result, 0.1);
	}
	
	class A {
		int a1;
		float a2;
		double a3;
		Object a4;
	}

	class B {
		char[] b1;
	}
}