package sbes.util;

import java.lang.reflect.TypeVariable;
import java.util.Map;

public class GenericsUtils {

	public static String toGenericsString(Map<TypeVariable<?>, String> genericToConcreteClasses) {
		return genericToConcreteClasses.values().toString().replace("[", "").replace("]", "");
	}
	
}
