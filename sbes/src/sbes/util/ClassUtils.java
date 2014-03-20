package sbes.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sbes.stub.GenerationException;

public class ClassUtils {

	public static Method[] getClassMethods(Class<?> c) {
		List<Method> toReturn =  new ArrayList<Method>();
		
		for (Method method : c.getMethods()) {
			if (!method.getDeclaringClass().equals(Class.class) &&
				!method.getDeclaringClass().equals(Object.class) &&
				!method.isBridge() &&
				!method.isSynthetic()) {
				toReturn.add(method);
			}
		}
		
		return toReturn.toArray(new Method[0]);
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
