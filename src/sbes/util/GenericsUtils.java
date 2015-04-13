package sbes.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Set;

public class GenericsUtils {

	public static String toGenericsString(Map<TypeVariable<?>, String> genericToConcreteClasses) {
		return genericToConcreteClasses.values().toString().replace("[", "").replace("]", "");
	}
	
	public static String resolveGenericType(Type t, Map<TypeVariable<?>, String> genericToConcreteClasses) {
		if (t instanceof ParameterizedType) {
			return resolveParameterizedType((ParameterizedType) t, genericToConcreteClasses);
		}
		else if (t instanceof TypeVariable<?>) {
			return resolveTypeVariable((TypeVariable<?>) t, genericToConcreteClasses);
		}
		else if (t instanceof WildcardType) {
			return resolveWildcardType((WildcardType) t, genericToConcreteClasses);
		}
		else if (t instanceof GenericArrayType) {
			return resolveGenericArrayType((GenericArrayType) t, genericToConcreteClasses);
		}
		return resolveClass((Class<?>) t);
	}

	protected static String resolveClass(Class<?> t) {
		if (t.getCanonicalName() != null) {
			return t.getCanonicalName();
		}
		else {
			return t.getName();
		}
	}

	protected static String resolveWildcardType(WildcardType t, Map<TypeVariable<?>, String> genericToConcreteClasses) {
		StringBuilder toReturn = new StringBuilder();
		if (t.getLowerBounds().length > 0) {
			toReturn.append("? super ");
			toReturn.append(resolveGenericType(t.getLowerBounds()[0], genericToConcreteClasses));
		}
		else if (t.getUpperBounds().length > 0) {
			toReturn.append("? extends ");
			toReturn.append(resolveGenericType(t.getUpperBounds()[0], genericToConcreteClasses));
		}
		else {
			toReturn.append("?");
		}
		return toReturn.toString();
	}

	protected static String resolveTypeVariable(TypeVariable<?> t, Map<TypeVariable<?>, String> genericToConcreteClasses) {
		String toReturn = t.getName();
		Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
		for (TypeVariable<?> typeVariable : types) {
			if (toReturn.contains(typeVariable.toString())) {
				toReturn = toReturn.replaceAll(typeVariable.toString(), genericToConcreteClasses.get(typeVariable));
			}
		}
		return toReturn;
	}

	protected static String resolveParameterizedType(ParameterizedType pt, Map<TypeVariable<?>, String> genericToConcreteClasses) {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(resolveGenericType(pt.getRawType(), genericToConcreteClasses));
		toReturn.append("<");
		for (int i = 0; i < pt.getActualTypeArguments().length; i++) {
			toReturn.append(resolveGenericType(pt.getActualTypeArguments()[i], genericToConcreteClasses));
			if ((i+1) < pt.getActualTypeArguments().length) {
				toReturn.append(", ");
			}
		}
		toReturn.append(">");
		return toReturn.toString();
	}
	
	protected static String resolveGenericArrayType(GenericArrayType t, Map<TypeVariable<?>, String> genericToConcreteClasses) {
		return resolveGenericType(t.getGenericComponentType(), genericToConcreteClasses) + "[]";
	}
	
}
