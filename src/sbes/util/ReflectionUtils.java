package sbes.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sbes.exceptions.SBESException;
import sbes.execution.InternalClassloader;
import sbes.option.Options;

public class ReflectionUtils {

	private final static Map<Class<?>, Method[]> classToMethodsCache;
	public  final static Set<Class<?>> primitives;
	public  final static Set<String> primitivesStringRepresentation;
	
	static {
		classToMethodsCache = new HashMap<Class<?>, Method[]>();
		
		primitives = new HashSet<Class<?>>();
		primitives.add(Integer.class);
		primitives.add(Short.class);
		primitives.add(Long.class);
		primitives.add(Boolean.class);
		primitives.add(Character.class);
		primitives.add(Double.class);
		primitives.add(Float.class);
		primitives.add(Byte.class);
		
		primitivesStringRepresentation = new HashSet<String>();
		primitivesStringRepresentation.add("Integer");
		primitivesStringRepresentation.add("Short");
		primitivesStringRepresentation.add("Long");
		primitivesStringRepresentation.add("Boolean");
		primitivesStringRepresentation.add("Character");
		primitivesStringRepresentation.add("Double");
		primitivesStringRepresentation.add("Float");
		primitivesStringRepresentation.add("Byte");
	}
	
	public static boolean canUse(Constructor<?> constructor) {
		int mod = constructor.getModifiers();
		return !constructor.isSynthetic() && (Modifier.isPublic(mod) || Modifier.isProtected(mod) || isDefault(mod));
	}
	
	public static boolean canUse(Method method) {
		int mod = method.getModifiers();
		return Modifier.isPublic(mod) && !method.isSynthetic() && !method.isBridge();
	}
	
	/**
	 * Returns the number of inheritance hops between two classes.
	 * 
	 * @param child
	 *            the child class, may be null
	 * @param parent
	 *            the parent class, may be null
	 * @return the number of generations between the child and parent; 0 if the
	 *         same class; -1 if the classes are not related as child and parent
	 *         (includes where either class is null)
	 */
	public static int classDistance(final Class<?> child, final Class<?> parent) {
		if (child == null || parent == null) {
			return -1;
		}

		if (child.equals(parent)) {
			return 0;
		}

		final Class<?> cParent = child.getSuperclass();
		int d = parent.equals(cParent) ? 1 : 0;

		if (d == 1) {
			return d;
		}
		d += classDistance(cParent, parent);
		return d > 0 ? d + 1 : -1;
	}
	
	public static Method findTargetMethod(Method[] methods, String methodName) {
		Method targetMethod = null;
		String method = methodName.split("\\(")[0];
		String args[] = methodName.split("\\(")[1].replaceAll("\\)", "").split(",");
		if (args.length == 1) {
			args = args[0].equals("") ? new String[0] : args;
		}
		for (Method m : methods) {
			if (m.getName().equals(method) && m.getParameterTypes().length == args.length) {
				int i;
				for (i = 0; i < args.length; i++) {
					if (!m.getParameterTypes()[i].getCanonicalName().contains(args[i])) {
						break;
					}
				}
				if (i == args.length) {
					targetMethod = m;
					break;
				}
			}
		}
		if (targetMethod == null) {
			throw new SBESException("Target method not found"); // failed to find method, give up
		}
		return targetMethod;
	}
	
	public static int getArrayDimensionCount(final Class<?> array) {
		int count = 0;
		Class<?> arrayClass = array;
		while (arrayClass.isArray()) {
			count++;
			arrayClass = arrayClass.getComponentType();
		}
		return count;
	}
	
	public static Class<?> getClass(final String className) {
		Class<?> toReturn = null;
		try {
			InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
			toReturn = Class.forName(className, false, ic.getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new SBESException(e);
		}
		return toReturn;
	}
	
	public static Method[] getClassMethods(Class<?> clazz) {
		if (classToMethodsCache.containsKey(clazz)) {
			return classToMethodsCache.get(clazz);
		}
		
		List<Method> methods = new ArrayList<Method>();
		for (Class<?> c : ReflectionUtils.getHierarchy(clazz)) {
			for (Method m : c.getDeclaredMethods()) {
				if (canUse(m)) {
					methods.add(m);
				}
			}
		}
		
		// Remove duplicates in methods
		Method[] methodsArray = methods.toArray(new Method[0]);
		for (int i = 0; i < methodsArray.length - 1; i++) {
			for (int j = i + 1; j < methodsArray.length; j++) {
				Method m1 = methodsArray[i];
				Method m2 = methodsArray[j];
				if (m1.getName().equals(m2.getName())) {
					Class<?>[] p1 = m1.getParameterTypes();
					Class<?>[] p2 = m2.getParameterTypes();
					boolean override = true;
					if (p1.length == p2.length) {
						for (int k = 0; k < p1.length; k++) {
							if (!p1[k].equals(p2[k])) {
								override = false;
							}
						}
						if (override) {
							methodsArray[i] = null;
							break;
						}
					}
				}
			}
		}
		
		methods.clear();
		methods.addAll(Arrays.asList(methodsArray));
		for (int i = 0; i < methods.size(); i++) {
			if (methods.get(i) == null) {
				methods.remove(i);
				i--;
			}
		}
		
		Collections.sort(methods, new Comparator<Method>() {
			@Override
			public int compare(final Method o1, final Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		classToMethodsCache.put(clazz, methods.toArray(new Method[0]));
		
		return classToMethodsCache.get(clazz);
	}
	
	private static List<Class<?>> getHierarchy(Class<?> clazz) {
		List<Class<?>> hierarchy = new ArrayList<Class<?>>();
	
		// Build hierarchy
		hierarchy.add(clazz);
		Class<?> superclass = clazz.getSuperclass();
		while (superclass != null && !superclass.getCanonicalName().equals("java.lang.Object")) {
			hierarchy.add(superclass);	
			superclass = superclass.getSuperclass();
		}
		Collections.reverse(hierarchy);
		
		return hierarchy;
	}

	public static List<Field> getInheritedPrivateFields(final Class<?> type) {
		List<Field> result = new ArrayList<Field>();

		Class<?> i = type;
		while (i != null && i != Object.class) {
			for (Field field : i.getDeclaredFields()) {
				if (!field.isSynthetic()) {
					result.add(field);
				}
			}
			i = i.getSuperclass();
		}

		return result;
	}
	
	public static Set<Class<?>> getInterfaces(final Class<?> c) {
		Set<Class<?>> toReturn = new HashSet<Class<?>>();
		
		Class<?> current = c;
		while (!current.equals(Object.class)) {
			toReturn.addAll(Arrays.asList(current.getInterfaces()));
			current = current.getSuperclass();
		}
		
		return toReturn;
	}
	
	public static boolean isArray(final Object obj) {
		if (obj == null || obj.getClass() == null) {
			return false;
		}
		return obj.getClass().isArray();
	}

	public static boolean isConstant(final Field f) {
		if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
			return true;
		}
		return false;
	}

	public static boolean isDefault(final int mod) {
		return !Modifier.isPrivate(mod) && !Modifier.isProtected(mod) && !Modifier.isPublic(mod);
	}

	public static boolean isPrimitive(final Object o) {
		if (o == null || o.getClass() == null) {
			return false;
		}
		
		Class<?> clazz = o.getClass().getComponentType() == null ? o.getClass() : o.getClass().getComponentType();
		if (clazz.isPrimitive() || primitives.contains(clazz)) {
			return true;
		}
		return false;
	}

	public static boolean isString(final Object o) {
		if (o == null || o.getClass() == null) {
			return false;
		}
		else if (o.getClass().equals(String.class)) {
			return true;
		}
		return false;
	}

	public static boolean isTransient(final Field f) {
		if (Modifier.isTransient(f.getModifiers())) {
			return true;
		}
		return false;
	}

}
