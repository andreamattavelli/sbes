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
	
}
