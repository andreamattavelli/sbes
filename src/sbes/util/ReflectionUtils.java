package sbes.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	
	public static boolean isString(Object o) {
		if (o == null || o.getClass() == null) {
			return false;
		}
		else if (o.getClass().equals(String.class)) {
			return true;
		}
		return false;
	}
	
	public static boolean isConstant(Field f) {
		if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
			return true;
		}
		return false;
	}
	
	public static boolean isTransient(Field f) {
		if (Modifier.isTransient(f.getModifiers())) {
			return true;
		}
		return false;
	}
	
	public static List<Field> getInheritedPrivateFields(Class<?> type) {
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

	public static boolean isArray(Object obj) {
		if (obj == null || obj.getClass() == null) {
			return false;
		}
		return obj.getClass().isArray();
	}

}
