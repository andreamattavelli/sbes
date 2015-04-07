package sbes.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.junit.Test;

import sbes.execution.InternalClassloader;

public class ReflectionUtilsTest {

	@Test
	public void test1() {
		InternalClassloader ic = new InternalClassloader("./test/resources/guava-12.0.1.jar");
		try {
			Class<?> c = Class.forName("com.google.common.collect.TreeMultimap", false, ic.getClassLoader());
			Constructor<?> constructors[] = c.getDeclaredConstructors();
			
			assertEquals(2, constructors.length);
			assertTrue(ReflectionUtils.isDefault(constructors[0].getModifiers()));
			assertFalse(ReflectionUtils.isDefault(constructors[1].getModifiers()));
			
		} catch (ClassNotFoundException e) {
			fail();
		}
	}
	
	@Test
	public void test2()  throws Throwable  {
		Set<Class<?>> interfaces = ReflectionUtils.getInterfaces(java.util.HashMap.class);
		assertEquals(3, interfaces.size());
		assertTrue(interfaces.contains(java.util.Map.class));
	}
	
	@Test
	public void test3()  throws Throwable  {
		Set<Class<?>> interfaces = ReflectionUtils.getInterfaces(java.util.Vector.class);
		assertEquals(5, interfaces.size());
		assertTrue(interfaces.contains(java.util.Collection.class));
		assertTrue(interfaces.contains(java.util.RandomAccess.class));
		assertTrue(interfaces.contains(java.util.List.class));
	}

}
