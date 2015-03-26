package sbes.util;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

public class ClassUtilsTest {

	@Test
	public void test0()  throws Throwable  {
		Set<Class<?>> interfaces = ClassUtils.getInterfaces(java.util.HashMap.class);
		assertEquals(3, interfaces.size());
		assertTrue(interfaces.contains(java.util.Map.class));
	}
	
	@Test
	public void test1()  throws Throwable  {
		Set<Class<?>> interfaces = ClassUtils.getInterfaces(java.util.Vector.class);
		assertEquals(5, interfaces.size());
		assertTrue(interfaces.contains(java.util.Collection.class));
		assertTrue(interfaces.contains(java.util.RandomAccess.class));
		assertTrue(interfaces.contains(java.util.List.class));
	}
	
}
