package sbes.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClassUtils {

	public static Method[] getClassMethods(Class<?> c) {
		List<Method> toReturn =  new ArrayList<Method>();
		
		for (Method method : c.getMethods()) {
			if (!method.getDeclaringClass().equals(Class.class) &&
				!method.getDeclaringClass().equals(Object.class)) {
				toReturn.add(method);
			}
		}
		
		return toReturn.toArray(new Method[0]);
	}
	
	public static String getClassname(final String signature) {
		return signature.substring(0, signature.lastIndexOf('.'));
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
