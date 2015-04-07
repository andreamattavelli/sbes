package sbes.util;

import java.lang.reflect.Method;

public class ClassUtils {

	public static String getCanonicalClassname(final String signature) {
		return signature.substring(0, signature.lastIndexOf('.'));
	}
	
	public static String getSimpleClassname(final String signature) {
		String canonical = getCanonicalClassname(signature); 
		return canonical.substring(canonical.lastIndexOf('.') + 1);
	}
	
	public static String getSimpleClassnameFromCanonical(final String canonical) {
		String toReturn = canonical.substring(canonical.lastIndexOf('.') + 1); 
		if (toReturn.contains("<")) {
			toReturn = toReturn.substring(0, toReturn.indexOf('<'));
		}
		return toReturn;
	}
	
	public static String getPackage(final String signature) {
		String canonical = getCanonicalClassname(signature); 
		return canonical.substring(0, canonical.lastIndexOf('.'));
	}
	
	public static String getMethodSignature(final Class<?> clazz, final Method method) {
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

	public static String getBytecodeRepresentation(final Class<?> c) {
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
