package sbes.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sbes.exceptions.GenerationException;
import sbes.exceptions.SBESException;
import sbes.execution.InternalClassloader;
import sbes.option.Options;

public class ClassUtils {

	private static final Map<Class<?>, Method[]> cache = new HashMap<Class<?>, Method[]>();
	
	public static Class<?> getClass(String className) {
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
		if (cache.containsKey(clazz)) {
			return cache.get(clazz);
		}
		
		List<Method> methods = new ArrayList<Method>();
		for (Class<?> c : getHierarchy(clazz)) {
			for (Method m : c.getDeclaredMethods()) {
				if (Modifier.isPublic(m.getModifiers())&& !m.isSynthetic() && !m.isBridge()) {
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
			public int compare(Method o1, Method o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		cache.put(clazz, methods.toArray(new Method[0]));
		
		return cache.get(clazz);
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
	
	public static String getCanonicalClassname(final String signature) {
		return signature.substring(0, signature.lastIndexOf('.'));
	}
	
	public static String getSimpleClassname(final String signature) {
		String canonical = getCanonicalClassname(signature); 
		return canonical.substring(canonical.lastIndexOf('.') + 1);
	}
	
	public static String getSimpleClassnameFromCanonical(final String canonical) {
		return canonical.substring(canonical.lastIndexOf('.') + 1);
	}
	
	public static String getPackage(final String signature) {
		String canonical = getCanonicalClassname(signature); 
		return canonical.substring(0, canonical.lastIndexOf('.'));
	}
	
	public static String getMethodSignature(Class<?> clazz, Method method) {
		StringBuilder methodSignature = new StringBuilder();
		methodSignature.append(clazz.getCanonicalName());
		methodSignature.append('.');
		methodSignature.append(method.getName());
		methodSignature.append('(');
		for (Class<?> c : method.getParameterTypes()) {
			methodSignature.append(c.getSimpleName());
			methodSignature.append(',');
		}
		methodSignature.append(')');
		return methodSignature.toString().replace(",)", ")");
	}
	
	public static String getMethodname(final String signature) {
		return signature.substring(signature.lastIndexOf('.') + 1);
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
			throw new GenerationException("Target method not found"); // failed to find method, give up
		}
		return targetMethod;
	}
	
	public static String getBytecodeSignature(final Method m) {
		StringBuilder builder = new StringBuilder();
		builder.append(m.getName());
		builder.append('(');
		for (int i = 0; i < m.getParameterTypes().length; i++) {
			builder.append(getBytecodeRepresentation(m.getParameterTypes()[i]));
		}
		builder.append(')');
		builder.append(getBytecodeRepresentation(m.getReturnType()));
		return builder.toString();
	}

	private static String getBytecodeRepresentation(final Class<?> c) {
		String className = c.getName();
		if (className.charAt(0) == '[') {
			return className.replaceAll("\\.", "/");
		}
		else {
			if (className.equals("byte")) {
				return "B";
			} else if (className.equals("char")) {
				return "C";
			} else if (className.equals("double")) {
				return "D";
			} else if (className.equals("float")) {
				return "F";
			} else if (className.equals("int")) {
				return "I";
			} else if (className.equals("long")) {
				return "J";
			} else if (className.equals("short")) {
				return "S";
			} else if (className.equals("void")) {
				return "V";
			} else if (className.equals("boolean")) {
				return "Z";
			}
			return "L" + className.replaceAll("\\.", "/") + ";";
		}
	}
	
}
