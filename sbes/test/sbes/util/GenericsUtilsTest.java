package sbes.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

import org.junit.Test;

import sbes.execution.InternalClassloader;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;
import sun.reflect.generics.tree.FieldTypeSignature;

public class GenericsUtilsTest {

	@Test
	public void test()  throws Throwable  {
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		
		TypeVariable<?> e = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		assertEquals("Integer", GenericsUtils.resolveTypeVariable(e, genericToConcrete));
	}
	
	@Test
	public void test2()  throws Throwable  {
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		
		TypeVariable<?> e = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		assertEquals("K", GenericsUtils.resolveTypeVariable(e, genericToConcrete));
	}
	
	@Test
	public void test3()  throws Throwable  {
		assertEquals("java.lang.Object", GenericsUtils.resolveClass(Object.class));
		assertEquals("java.lang.String", GenericsUtils.resolveClass(String.class));
		assertEquals("java.lang.String[]", GenericsUtils.resolveClass(String[].class));
		assertEquals("java.lang.String[][]", GenericsUtils.resolveClass(String[][].class));
	}
	
	@Test
	public void test4()  throws Throwable  {
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		genericToConcrete.put(k, "Integer");
		
		TypeVariable<?> e = TypeVariableImpl.<GenericDeclaration>make(Object.class, "E", null, null);
		ParameterizedType pt = ParameterizedTypeImpl.make(Collection.class, new Type[]{e}, null);
		
		assertEquals("java.util.Collection<Integer>", GenericsUtils.resolveParameterizedType(pt, genericToConcrete));
	}
	
	@Test
	public void test5()  throws Throwable  {
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		
		TypeVariable<?> k2 = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v2 = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		ParameterizedType pt = ParameterizedTypeImpl.make(Map.class, new Type[]{k2,v2}, null);
		
		assertEquals("java.util.Map<Integer, String>", GenericsUtils.resolveParameterizedType(pt, genericToConcrete));
	}
	
	@Test
	public void test6()  throws Throwable  {
		Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
		TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
		genericToConcrete.put(k, "Integer");
		genericToConcrete.put(v, "String");
		
		TypeVariable<?> k2 = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
		WildcardType wt = WildcardTypeImpl.make(new FieldTypeSignature[] {}, new FieldTypeSignature[] {}, null);
		ParameterizedType pt = ParameterizedTypeImpl.make(Map.class, new Type[]{k2,wt}, null);
		
		assertEquals("java.util.Map<Integer, ?>", GenericsUtils.resolveParameterizedType(pt, genericToConcrete));
	}
	
	@Test
	public void test7()  throws Throwable  {
		InternalClassloader ic = new InternalClassloader("./test/resources/guava-12.0.1.jar");
		try {
			Map<TypeVariable<?>, String> genericToConcrete = new LinkedHashMap<>();
			TypeVariable<?> k = TypeVariableImpl.<GenericDeclaration>make(Object.class, "K", null, null);
			TypeVariable<?> v = TypeVariableImpl.<GenericDeclaration>make(Object.class, "V", null, null);
			genericToConcrete.put(k, "Integer");
			genericToConcrete.put(v, "String");
			
			Class<?> c = Class.forName("com.google.common.collect.Maps", false, ic.getClassLoader());
			Class<?> predicate = Class.forName("com.google.common.base.Predicate", false, ic.getClassLoader());
			Method method = c.getMethod("filterEntries", SortedMap.class, predicate);
			
			Type parameters[] = method.getGenericParameterTypes();
			assertEquals("java.util.SortedMap<Integer, String>", GenericsUtils.resolveGenericType(parameters[0], genericToConcrete));
			assertEquals("com.google.common.base.Predicate<? super java.util.Map.Entry<Integer, String>>", GenericsUtils.resolveGenericType(parameters[1], genericToConcrete));
			
		} catch (ClassNotFoundException e) {
			fail();
		}
	}
	
}
