package sbes.util;

import java.lang.reflect.Method;

public class MethodUtils {

	public static boolean methodFilter(Method method) {
		if (method.getName().equals("equals") ||
			method.getName().equals("hashCode") ||
			method.getName().equals("toString")) {
			return true;
		}
		return false;
	}
	
}
