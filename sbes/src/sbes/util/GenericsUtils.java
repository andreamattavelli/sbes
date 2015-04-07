package sbes.util;

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
	
	public static String replaceGenericWithConcreteType(String generic, Map<TypeVariable<?>, String> genericToConcreteClasses) {
		String toReturn = generic;
		Set<TypeVariable<?>> types = genericToConcreteClasses.keySet();
		for (TypeVariable<?> typeVariable : types) {
			if (toReturn.contains('<' + typeVariable.toString() + '>')) {
				toReturn = toReturn.replaceAll('<' + typeVariable.toString() + '>', '<' + genericToConcreteClasses.get(typeVariable) + '>');
			}
			else if (toReturn.contains('<' + typeVariable.toString())) {
				toReturn = toReturn.replaceAll('<' + typeVariable.toString(), '<' + genericToConcreteClasses.get(typeVariable));
			}
			else if (toReturn.contains(typeVariable.toString() + '>')) {
				toReturn = toReturn.replaceAll(typeVariable.toString() + '>', genericToConcreteClasses.get(typeVariable) + '>');
			}
			else if (toReturn.contains(", " + typeVariable.toString())) {
				toReturn = toReturn.replaceAll(", " + typeVariable.toString(), ", " + genericToConcreteClasses.get(typeVariable));
			}
		}
		return toReturn;
	}

	public static String resolveGenericType(Type t) {
		if (t instanceof ParameterizedType) {
			return resolveParameterizedType((ParameterizedType) t);
		}
		else if (t instanceof TypeVariable<?>) {
			return resolveTypeVariable((TypeVariable<?>) t);
		}
		else if (t instanceof WildcardType) {
			return resolveWildcardType((WildcardType) t);
		}
		else if (t instanceof Class<?>) {
			return resolveClass((Class<?>) t);
		}
		// GenericArrayType?
		return "";
	}
	
	private static String resolveClass(Class<?> t) {
		if (t.getCanonicalName() != null) {
			return t.getCanonicalName();
		}
		else { 
			return t.getName();
		}
	}

	private static String resolveWildcardType(WildcardType t) {
		return t.toString();
	}

	private static String resolveTypeVariable(TypeVariable<?> t) {
		return t.getName();
	}

	private static String resolveParameterizedType(ParameterizedType pt) {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(resolveGenericType(pt.getRawType()));
		toReturn.append("<");
		for (int i = 0; i < pt.getActualTypeArguments().length; i++) {
			toReturn.append(resolveGenericType(pt.getActualTypeArguments()[i]));
			if ((i+1) < pt.getActualTypeArguments().length) {
				toReturn.append(", ");
			}
		}
		toReturn.append(">");
		return toReturn.toString();
	}
	
}
