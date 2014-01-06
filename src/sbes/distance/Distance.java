package sbes.distance;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Distance {

	public static double calculateDistance(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		else if (o1 == null ^ o2 == null) {
			if (o1 == null) {
				return getInheritedPrivateFields(o2.getClass()).size();
			}
			else {
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
						distance += primitiveDistance(f1, obj1, f2, obj2);
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
								if (visited.put(castedF1[i], 1) == null &&
										visited.put(castedF2[i], 1) == null) { // we skip circular references
									worklist.add(new DistancePair(castedF1[i], castedF2[i]));
								}
							}
						}
						else if (arrayType == ComparisonType.STRING) {
							String[] castedF1 = String[].class.cast(f1.get(obj1));
							String[] castedF2 = String[].class.cast(f2.get(obj2));
							for (int j = 0; j < castedF1.length; j++) {
								distance += LevenshteinDistance.calculateDistance(castedF1[i], castedF2[i]);
							} 
						}
						else if (arrayType == ComparisonType.PRIMITIVE) {
							distance += primitiveArrayDistance(f1, obj1, f2, obj2);
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
		
		return distance/counterFields;
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
	
	private static double primitiveDistance(Field f1, Object obj1, Field f2, Object obj2) {
		double distance = 0;
		
		Class<?> f1Type = f1.getType();
		try {
			if (f1Type.equals(int.class)) {
				int f1Int = f1.getInt(obj1);
				int f2Int = f2.getInt(obj2);
				distance = Math.abs(f1Int - f2Int);
			}
			else if (f1Type.equals(char.class)) {
				char f1Char = f1.getChar(obj1);
				char f2Char = f2.getChar(obj2);
				String stringF1 = new String(new char[] {f1Char});
				String stringF2 = new String(new char[] {f2Char});
				distance = LevenshteinDistance.calculateDistance(stringF1, stringF2);
			}
			else if (f1Type.equals(short.class)) {
				short f1Short = f1.getShort(obj1);
				short f2Short = f2.getShort(obj2);
				distance = Math.abs(f1Short - f2Short);
			}
			else if (f1Type.equals(long.class)) {
				long f1Long = f1.getLong(obj1);
				long f2Long = f2.getLong(obj2);
				distance = Math.abs(f1Long - f2Long);
			}
			else if (f1Type.equals(float.class)) {
				float f1Float = f1.getFloat(obj1);
				float f2Float = f2.getFloat(obj2);
				distance = Math.abs(f1Float - f2Float);
			}
			else if (f1Type.equals(double.class)) {
				double f1Double = f1.getDouble(obj1);
				double f2Double = f2.getDouble(obj2);
				distance = Math.abs(f1Double - f2Double);
			}
			else if (f1Type.equals(boolean.class)) {
				boolean f1Boolean = f1.getBoolean(obj1);
				boolean f2Boolean = f2.getBoolean(obj2);
				distance = f1Boolean ^ f2Boolean ? 1 : 0;
			}
			else if (f1Type.equals(byte.class)) {
				byte f1Byte = f1.getByte(obj1);
				byte f2Byte = f2.getByte(obj2);
				distance = Math.abs(f1Byte - f2Byte);
			}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return distance;
	}
	
	private static double primitiveArrayDistance(Field f1, Object obj1, Field f2, Object obj2) {
		double distance = 0;
		
		Class<?> f1Type = f1.getType().getComponentType();
		try {
			if (f1Type.equals(int.class)) {
				int[] f1Int = int[].class.cast(f1.getInt(obj1));
				int[] f2Int = int[].class.cast(f2.getInt(obj2));
				for (int i = 0; i < f1Int.length; i++) {
					distance += Math.abs(f1Int[i] - f2Int[i]);
				}
			}
			else if (f1Type.equals(char.class)) {
				char[] castedF1 = char[].class.cast(f1.get(obj1));
				char[] castedF2 = char[].class.cast(f2.get(obj2));
				String stringF1 = new String(castedF1);
				String stringF2 = new String(castedF2);
				distance += LevenshteinDistance.calculateDistance(stringF1, stringF2);
			}
			else if (f1Type.equals(short.class)) {
				short[] f1Short = short[].class.cast(f1.getShort(obj1));
				short[] f2Short = short[].class.cast(f2.getShort(obj2));
				for (int i = 0; i < f1Short.length; i++) {
					distance += Math.abs(f1Short[i] - f2Short[i]);
				}
			}
			else if (f1Type.equals(long.class)) {
				long[] f1Long = long[].class.cast(f1.getLong(obj1));
				long[] f2Long = long[].class.cast(f2.getLong(obj2));
				for (int i = 0; i < f1Long.length; i++) {
					distance += Math.abs(f1Long[i] - f2Long[i]);
				}
			}
			else if (f1Type.equals(float.class)) {
				float[] f1Float = float[].class.cast(f1.getFloat(obj1));
				float[] f2Float = float[].class.cast(f2.getFloat(obj2));
				for (int i = 0; i < f1Float.length; i++) {
					distance += Math.abs(f1Float[i] - f2Float[i]);
				}
			}
			else if (f1Type.equals(double.class)) {
				double[] f1Double = double[].class.cast(f1.getDouble(obj1));
				double[] f2Double = double[].class.cast(f2.getDouble(obj2));
				for (int i = 0; i < f1Double.length; i++) {
					distance += Math.abs(f1Double[i] - f2Double[i]);
				}
			}
			else if (f1Type.equals(boolean.class)) {
				boolean[] f1Boolean = boolean[].class.cast(f1.getBoolean(obj1));
				boolean[] f2Boolean = boolean[].class.cast(f2.getBoolean(obj2));
				for (int i = 0; i < f1Boolean.length; i++) {
					distance += f1Boolean[i] ^ f2Boolean[i] ? 1 : 0;
				}
			}
			else if (f1Type.equals(byte.class)) {
				byte[] f1Byte = byte[].class.cast(f1.getByte(obj1));
				byte[] f2Byte = byte[].class.cast(f2.getByte(obj2));
				for (int i = 0; i < f1Byte.length; i++) {
					distance += Math.abs(f1Byte[i] - f2Byte[i]);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return distance;
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