package sbes.distance;

import sbes.util.ReflectionUtils;

public class ObjectDistance {

	private static final double NULL_WEIGHT = 1.0d;
	
	public static double getNullDistance(Object o1, Object o2) {
		if (o1 == null) {
			if (ReflectionUtils.isPrimitive(o2)) {
				return 1.0 * NULL_WEIGHT;
			}
			else {
				int size = ReflectionUtils.getInheritedPrivateFields(o2.getClass()).size();
				return size > 0 ? size * NULL_WEIGHT : NULL_WEIGHT;
			}
		}
		else {
			if (ReflectionUtils.isPrimitive(o1)) {
				return 1.0 * NULL_WEIGHT;
			}
			else {
				int size = ReflectionUtils.getInheritedPrivateFields(o1.getClass()).size();
				return size > 0 ? size * NULL_WEIGHT : NULL_WEIGHT;
			}
		}
	}
	
}
