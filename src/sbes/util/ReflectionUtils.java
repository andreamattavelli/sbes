package sbes.util;

import java.util.HashSet;
import java.util.Set;

public class ReflectionUtils {

	public final static Set<Class<?>> primitives;
	
	static {
		primitives = new HashSet<Class<?>>();
		primitives.add(Integer.class);
		primitives.add(Short.class);
		primitives.add(Long.class);
		primitives.add(Boolean.class);
		primitives.add(Character.class);
		primitives.add(Double.class);
		primitives.add(Float.class);
		primitives.add(Byte.class);
	}
	
	public static boolean isPrimitive(Object o) {
		if (o == null || o.getClass() == null) {
			return false;
		}
		
		Class<?> clazz = o.getClass().getComponentType() == null ? o.getClass() : o.getClass().getComponentType();
		if (clazz.isPrimitive() || primitives.contains(clazz)) {
			return true;
		}
		return false;
	}

}
