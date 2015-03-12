/*
 * This file was automatically generated by EvoSuite
 * Thu Mar 12 07:57:13 CET 2015
 */

package sbes.distance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class LevenshteinDistanceTest {

	@Test
	public void test0()  throws Throwable  {
		int int0 = LevenshteinDistance.calculateDistance("Jd!:=-l0m", "Jd!:=-l0m");
		assertEquals(0, int0);
	}

	@Test
	public void test1()  throws Throwable  {
		int int0 = LevenshteinDistance.calculateDistance("", "*A.d'hZ)A2");
		assertEquals(10, int0);
	}

	@Test
	public void test2()  throws Throwable  {
		// Undeclared exception!
		try {
			LevenshteinDistance.calculateDistance("Strings must not be null", (String) null);
			fail("Expecting exception: IllegalArgumentException");

		} catch(IllegalArgumentException e) {
			//
			// Strings must not be null
			//
		}
	}

	@Test
	public void test3()  throws Throwable  {
		int int0 = LevenshteinDistance.calculateDistance("=^INqI", "");
		assertEquals(6, int0);
	}

	@Test
	public void test4()  throws Throwable  {
		// Undeclared exception!
		try {
			LevenshteinDistance.calculateDistance((String) null, (String) null);
			fail("Expecting exception: MockIllegalArgumentException");

		} catch(IllegalArgumentException e) {
			//
			// Strings must not be null
			//
		}
	}

}
