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
	
}
