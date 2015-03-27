package sbes.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class MethodUtils {

	private final static Set<String> excluded;
	
	static {
		excluded = new HashSet<String>();
		excluded.add("equals");
		excluded.add("hashCode");
		excluded.add("toString");
		excluded.add("clone");
	}
	
	public static boolean methodFilter(Method method) {
		if (excluded.contains(method.getName())) {
			return true;
		}
		return false;
	}
	
}
