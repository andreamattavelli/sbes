package sbes.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
			assertTrue(ReflectionUtils.canUse(constructors[0]));
			assertFalse(ReflectionUtils.isDefault(constructors[1].getModifiers()));
			
		} catch (ClassNotFoundException e) {
			fail();
		}
	}
	
	@Test
	public void test2() {
		InternalClassloader ic = new InternalClassloader("./test/resources/guava-12.0.1.jar");
		try {
			Class<?> c = Class.forName("com.google.common.collect.TreeMultiset", false, ic.getClassLoader());
			Constructor<?> constructors[] = c.getDeclaredConstructors();
			
			assertEquals(2, constructors.length);
			
			assertFalse(ReflectionUtils.isDefault(constructors[0].getModifiers()));
			assertTrue(ReflectionUtils.canUse(constructors[0]));
			assertFalse(ReflectionUtils.isDefault(constructors[1].getModifiers()));
			
		} catch (ClassNotFoundException e) {
			fail();
		}
	}
	
	@Test
	public void test3()  throws Throwable  {
		Set<Class<?>> interfaces = ReflectionUtils.getInterfaces(java.util.HashMap.class);
		assertEquals(3, interfaces.size());
		assertTrue(interfaces.contains(java.util.Map.class));
	}
	
	@Test
	public void test4()  throws Throwable  {
		Set<Class<?>> interfaces = ReflectionUtils.getInterfaces(java.util.Vector.class);
		assertEquals(5, interfaces.size());
		assertTrue(interfaces.contains(java.util.Collection.class));
		assertTrue(interfaces.contains(java.util.RandomAccess.class));
		assertTrue(interfaces.contains(java.util.List.class));
	}
	
	@Test
	public void test5()  throws Throwable  {
		assertTrue(ReflectionUtils.isPrimitive(Integer.class));
		assertTrue(ReflectionUtils.isPrimitive(Short.class));
		assertTrue(ReflectionUtils.isPrimitive(Long.class));
		assertTrue(ReflectionUtils.isPrimitive(Boolean.class));
		assertTrue(ReflectionUtils.isPrimitive(Character.class));
		assertTrue(ReflectionUtils.isPrimitive(Double.class));
		assertTrue(ReflectionUtils.isPrimitive(Float.class));
		assertTrue(ReflectionUtils.isPrimitive(Byte.class));
	}
	
	@Test
	public void test6()  throws Throwable  {
		assertTrue(ReflectionUtils.primitivesStringRepresentation.contains("Integer"));
		assertTrue(ReflectionUtils.primitivesStringRepresentation.contains("Short"));
		assertTrue(ReflectionUtils.primitivesStringRepresentation.contains("Long"));
		assertTrue(ReflectionUtils.primitivesStringRepresentation.contains("Boolean"));
		assertTrue(ReflectionUtils.primitivesStringRepresentation.contains("Character"));
		assertTrue(ReflectionUtils.primitivesStringRepresentation.contains("Double"));
		assertTrue(ReflectionUtils.primitivesStringRepresentation.contains("Float"));
		assertTrue(ReflectionUtils.primitivesStringRepresentation.contains("Byte"));
	}
	
	@Test
	public void test7()  throws Throwable  {
		InternalClassloader ic = new InternalClassloader("./test/resources/guava-12.0.1.jar");
		try {
			Class<?> c = Class.forName("com.google.common.collect.ConcurrentHashMultiset", false, ic.getClassLoader());
			Method method = c.getMethod("add", Object.class, int.class);
			
			assertEquals(int.class, method.getReturnType());
			assertTrue(ReflectionUtils.isPrimitive(method.getReturnType()));
			
		} catch (ClassNotFoundException e) {
			fail();
		}
	}
	
	@Test
	public void test8()  throws Throwable  {
		Class<?> clazz = Object.class;
		assertEquals(0, ReflectionUtils.arrayCardinality(clazz));
		Class<?> clazz1 = Object[].class;
		assertEquals(1, ReflectionUtils.arrayCardinality(clazz1));
		Class<?> clazz2 = Object[][].class;
		assertEquals(2, ReflectionUtils.arrayCardinality(clazz2));
		Class<?> clazz3 = Object[][][].class;
		assertEquals(3, ReflectionUtils.arrayCardinality(clazz3));
	}

}
