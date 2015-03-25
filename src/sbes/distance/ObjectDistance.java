package sbes.distance;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import sbes.util.ReflectionUtils;

public class ObjectDistance {

	public static double getNullDistance(final Object o1, final Object o2) {
		if (o1 == null) {
			if (ReflectionUtils.isPrimitive(o2)) {
				return 1.0 * Distance.NULL_WEIGHT;
			}
			else {
				int size = getObjectSize(o2);
				return size > 0 ? size * Distance.NULL_WEIGHT : Distance.NULL_WEIGHT;
			}
		}
		else {
			if (ReflectionUtils.isPrimitive(o1)) {
				return 1.0 * Distance.NULL_WEIGHT;
			}
			else {
				int size = getObjectSize(o1);
				return size > 0 ? size * Distance.NULL_WEIGHT : Distance.NULL_WEIGHT;
			}
		}
	}
	
	private static int getObjectSize(final Object o) {
		int size = 0;
		List<Class<?>> worklist = new LinkedList<Class<?>>();
		
		worklist.add(o.getClass());
		while (!worklist.isEmpty()) {
			Class<?> c = worklist.remove(0);
			List<Field> fields = ReflectionUtils.getInheritedPrivateFields(c);
			size = fields.size();
			for (Field field : fields) {
				Class<?> fieldClass = field.getType();
				size += ReflectionUtils.getInheritedPrivateFields(fieldClass).size();
				if (!fieldClass.equals(c) && !worklist.contains(fieldClass)) {
					worklist.add(field.getType());
				}
			}
		}
				
		return size;
	}
	
}
