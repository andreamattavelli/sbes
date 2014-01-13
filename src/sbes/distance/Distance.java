package sbes.distance;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sbes.util.ReflectionUtils;

public class Distance {

	public static double calculateDistance(Object o1, Object o2) {
		System.out.println("CALCULATE");
		if (o1 == null && o2 == null) {
			return 0;
		}
		else if (o1 == null ^ o2 == null) {
			if (o1 == null) {
				return getInheritedPrivateFields(o2.getClass()).size();
			}
			else {
				if (ReflectionUtils.isPrimitive(o1)) {
					return PrimitiveDistance.basicDistance(o1);
				}
				return getInheritedPrivateFields(o1.getClass()).size();
			}
		}

		double distance = 0.0;
		int counterFields = 0;
		Class<?> c1 = o1.getClass();
		Class<?> c2 = o2.getClass();

		if (!c1.getClass().equals(c2.getClass())) {
			/* Do we want to penalize it?
			 * A penalty could affect the ability of the technique to synthesize
			 *  equivalent sequences..or not?
			 */
			;
		}

		if (ReflectionUtils.isPrimitive(o1)) {
			return PrimitiveDistance.distance(o1, o2);
		}

		Map<Object, Integer> visited = new IdentityHashMap<Object, Integer>();
		List<DistancePair> worklist = new LinkedList<DistancePair>();
		worklist.add(new DistancePair(o1, o2));

		while (!worklist.isEmpty()) {
			DistancePair pair = worklist.remove(0);
			Object obj1 = pair.o1;
			Object obj2 = pair.o2;

			List<Field> fs1 = getInheritedPrivateFields(obj1.getClass());
			List<Field> fs2 = getInheritedPrivateFields(obj2.getClass());

			if (fs1.size() != fs2.size()) {
				// Not of the same type?
				System.err.println("Odd..");
			}

			for (int i = 0; i < fs1.size(); i++) {
				try {
					Field f1 = fs1.get(i);
					Field f2 = fs2.get(i);

					f1.setAccessible(true);
					f2.setAccessible(true);

					if (Modifier.isFinal(f1.getModifiers()) && Modifier.isStatic(f1.getModifiers()) &&
							Modifier.isFinal(f2.getModifiers()) && Modifier.isStatic(f2.getModifiers())) {
						// it's a constant, skip it
						System.out.println("SKIP: " + Modifier.toString(f1.getModifiers()) + " " + f1.getType() + " " + f1.getName());
						continue;
					}
					else if (Modifier.isTransient(f1.getModifiers()) && Modifier.isTransient(f2.getModifiers())) {
						// do we want to skip it?
					}

					counterFields++;

					System.out.println(Modifier.toString(f1.getModifiers()) + " " + f1.getType() + " " + f1.getName());

					ComparisonType comparison = getComparisonType(f1.getType(), f2.getType());
					if (comparison == ComparisonType.PRIMITIVE) {
						distance += PrimitiveDistance.distance(f1, obj1, f2, obj2);
					}
					else if (comparison == ComparisonType.STRING) {
						distance += stringComparison(f1.get(obj1), f2.get(obj2));
					}
					else if (comparison == ComparisonType.ARRAY) {
						//get array type
						ComparisonType arrayType = getComparisonType(f1.getType().getComponentType(), f2.getType().getComponentType());

						if (arrayType == ComparisonType.OBJECT) {
							// object
							Object[] castedF1 = Object[].class.cast(f1.get(obj1));
							Object[] castedF2 = Object[].class.cast(f2.get(obj2));
							for (int j = 0; j < castedF1.length; j++) {
								if (visited.put(castedF1[j], 1) == null &&
										visited.put(castedF2[j], 1) == null) { // we skip circular references
									worklist.add(new DistancePair(castedF1[j], castedF2[j]));
								}
							}
						}
						else if (arrayType == ComparisonType.STRING) {
							String[] castedF1 = String[].class.cast(f1.get(obj1));
							String[] castedF2 = String[].class.cast(f2.get(obj2));
							for (int j = 0; j < castedF1.length; j++) {
								distance += LevenshteinDistance.calculateDistance(castedF1[j], castedF2[j]);
							} 
						}
						else if (arrayType == ComparisonType.PRIMITIVE) {
							distance += PrimitiveDistance.distance(f1, obj1, f2, obj2);
						}
						else {
							//TODO: array of array, recursion?
						}
					}
					else {
						Object obj1value = f1.get(obj1);
						Object obj2value = f2.get(obj2);

						if (visited.put(obj1value, 1) == null &&
								visited.put(obj2value, 1) == null) { // we skip circular references
							if (obj1 == null && obj2 == null) {
								//distance: 0
								continue;
							}
							else if (obj1 == null ^ obj2 == null) {
								int fields = 0;
								if (obj1 == null) {
									fields = getInheritedPrivateFields(obj2.getClass()).size();
								}
								else {
									fields = getInheritedPrivateFields(obj1.getClass()).size();
								}
								distance += fields;
							}
							else {
								worklist.add(new DistancePair(f1.get(obj1), f2.get(obj2)));
							}
						}
					}
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}

		if (counterFields == 0) {
			return 0;
		}
		else {
			return distance/counterFields;
		}
	}

	private static List<Field> getInheritedPrivateFields(Class<?> type) {
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

	private static ComparisonType getComparisonType(Class<?> f1, Class<?> f2) {
		if (f1.isPrimitive()) {
			return ComparisonType.PRIMITIVE;
		}
		else if (f1.isArray()) {
			return ComparisonType.ARRAY;
		}
		else if (f1.equals(String.class)) {
			return ComparisonType.STRING;
		}
		return ComparisonType.OBJECT;
	}

	private static int stringComparison(Object obj, Object obj2) {
		return LevenshteinDistance.calculateDistance((String) obj, (String) obj2);
	}

}

enum ComparisonType {
	ARRAY, PRIMITIVE, STRING, OBJECT
}

class DistancePair {
	Object o1;
	Object o2;

	public DistancePair(Object o1, Object o2) {
		this.o1 = o1;
		this.o2 = o2;
	}
}