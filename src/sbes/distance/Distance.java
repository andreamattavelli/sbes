package sbes.distance;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sbes.logging.Logger;
import sbes.util.ReflectionUtils;

public class Distance {

	private static final Logger logger = new Logger(Distance.class);

	public static final double DIFFERENT_CLASSES_WEIGHT = 20.0d;
	public static final double ARRAY_CELL_FACTOR = 5.0d;
	public static final double NULL_WEIGHT = 10.0d;
	
	private static final List<DistancePair> worklist = new LinkedList<DistancePair>();
	private static final Map<Object, Integer> visited = new IdentityHashMap<Object, Integer>();
	
	private Distance() {}
	
	public static double distance(Object o1, Object o2) {
		logger.debug("distance between: " + o1 + " and " + o2);
		if (o1 == null && o2 == null) {
			logger.debug("both null");
			return 0.0d;
		}
		else if (o1 == null ^ o2 == null) {
			logger.debug("one of the two is null");
			return ObjectDistance.getNullDistance(o1, o2);
		}
		
		Class<?> c1 = o1.getClass();
		Class<?> c2 = o2.getClass();

		if (!c1.getClass().equals(c2.getClass())) {
			// Do we want to penalize it? A penalty could affect the ability of
			// the technique to synthesize equivalent sequences..or not?
			;
		}
		
		worklist.clear();
		visited.clear();
		
		return calculate(o1, o2);
	}
	
	private static double calculate(Object o1, Object o2) {
		double distance = 0.0d;
		int fieldCount = 0;
		
		worklist.add(new DistancePair(o1, o2));

		while (!worklist.isEmpty()) {
			DistancePair pair = worklist.remove(0);
			Object obj1 = pair.o1;
			Object obj2 = pair.o2;
			
			//========================================CORNER CASES========================================
			//------------------NULL-------------------
			if (obj1 == null && obj2 == null) {
				continue;
			}
			else if (obj1 == null ^ obj2 == null) {
				distance += ObjectDistance.getNullDistance(obj1, obj2);
				continue;
			}
			
			//------------DIFFERENT CLASSES------------
			else if (!obj1.getClass().equals(obj2.getClass())) {
				logger.debug("different classes: " + obj1.getClass() + " vs " + obj2.getClass());
				distance += DIFFERENT_CLASSES_WEIGHT;
				continue;
			}
			
			//----------------PRIMITIVE----------------
			// this definition of primitive contains also
			// primitive classes (e.g. Integer)
			else if (ReflectionUtils.isPrimitive(obj1)) {
				distance += PrimitiveDistance.distance(obj1, obj2);
				continue;
			}
			
			//------------------STRING-----------------
			else if (ReflectionUtils.isString(obj1)) {
				distance += LevenshteinDistance.calculateDistance((String) obj1, (String) obj2);
				continue;
			}
			
			//-----------------ARRAYS------------------
			else if (ReflectionUtils.isArray(obj1)) {
				distance += handleArray(obj1, obj2);
				continue;
			}
			
			//----------CIRCULAR DEPENDENCIES----------
			else if (visited.put(obj1, 1) != null && visited.put(obj2, 2) != null) {
				continue;
			}
			
			//------------------OBJECT-----------------
			List<Field> fs1 = ReflectionUtils.getInheritedPrivateFields(obj1.getClass());
			List<Field> fs2 = ReflectionUtils.getInheritedPrivateFields(obj2.getClass());
			for (int i = 0; i < fs1.size(); i++) {
				try {
					Field f1 = fs1.get(i);
					Field f2 = fs2.get(i);
					
					f1.setAccessible(true);
					f2.setAccessible(true);
					
					// skip comparison of constants
					if (ReflectionUtils.isConstant(f1) && ReflectionUtils.isConstant(f2)) {
						logger.debug("Skip: " + Modifier.toString(f1.getModifiers()) + " " + f1.getType() + " " + f1.getName());
						continue;
					}
					else if (FieldFilter.exclude(f1) || FieldFilter.exclude(f2)) {
						logger.debug("Exclude: " + Modifier.toString(f1.getModifiers()) + " " + f1.getType() + " " + f1.getName());
						continue;
					}
					
					ComparisonType type = getComparisonType(f1.getType(), f2.getType());
					switch (type) {
					case PRIMITIVE:
						// this definition of primitives contains only real 
						// primitive values (e.g int, char, ..) primitive 
						// classes (e.g. Integer) are treated as object and 
						// handled in the subsequent iteration as corner case
						distance += PrimitiveDistance.distance(f1, obj1, f2, obj2);
						break;
					case STRING:
						distance += LevenshteinDistance.calculateDistance((String) f1.get(obj1), (String) f2.get(obj2));
						break;
					case ARRAY:
						distance += handleArray(f1, obj1, f2, obj2);
						break;
					case OBJECT:
						// null values and corner cases are managed at the 
						// beginning of the iteration
						Object obj1value = f1.get(obj1);
						Object obj2value = f2.get(obj2);
						worklist.add(new DistancePair(obj1value, obj2value));
						break;
					default:
						logger.error("Unknown comparison type: " + type);
						break;
					}
				} catch (Exception e) {
					logger.error("Error during distance calculation", e);
				}
			}
		}
		
		if (fieldCount > 0) {
			return distance / (double) fieldCount; 
		}
		else {
			return distance;
		}
	}
	
	private static double handleArray(Object obj1, Object obj2) {
		double distance = 0.0d;
		
		ComparisonType arrayType = getComparisonType(obj1.getClass().getComponentType(), obj1.getClass().getComponentType());
		switch (arrayType) {
		case OBJECT:
			try {
				Object[] castedF1 = Object[].class.cast(obj1);
				Object[] castedF2 = Object[].class.cast(obj2);
				int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
				for (int i = 0; i < length; i++) {
					worklist.add(new DistancePair(castedF1[i], castedF2[i]));
				}
				distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
			}
			catch (IllegalArgumentException e) {
				logger.error("Error during cast", e);
			}
			break;
		case PRIMITIVE:
			distance += PrimitiveDistance.distance(obj1, obj2);
			break;
		case STRING:
			try {
				String[] castedF1 = String[].class.cast(obj1);
				String[] castedF2 = String[].class.cast(obj2);
				int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
				for (int i = 0; i < length; i++) {
					distance += LevenshteinDistance.calculateDistance(castedF1[i], castedF2[i]);
				}
				distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
			}
			catch (IllegalArgumentException e) {
				logger.error("Error during cast", e);
			}
			break;
		case ARRAY:
			int length = Math.min(Array.getLength(obj1), Array.getLength(obj2));
			for (int i = 0; i < length; i++) {
				distance += handleArray(Array.get(obj1, i), Array.get(obj2, i));
			}
			distance += (Math.max(Array.getLength(obj1), Array.getLength(obj2)) - length) * Distance.ARRAY_CELL_FACTOR;
			break;
		default:
			logger.error("Unknown comparison type: " + arrayType);
			break;
		}
		
		return distance;
	}

	private static double handleArray(Field f1, Object obj1, Field f2, Object obj2) {
		double distance = 0.0d;
		
		ComparisonType arrayType = getComparisonType(f1.getType().getComponentType(), f2.getType().getComponentType());
		switch (arrayType) {
		case OBJECT:
			try {
				Object[] castedF1 = Object[].class.cast(f1.get(obj1));
				Object[] castedF2 = Object[].class.cast(f2.get(obj2));
				int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
				for (int i = 0; i < length; i++) {
					worklist.add(new DistancePair(castedF1[i], castedF2[i]));
				}
				distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("Error during cast", e);
			}
			break;
		case PRIMITIVE:
			try {
				Class<?> f1Type = f1.getType().getComponentType() == null ? f1.getType() : f1.getType().getComponentType();
				if (f1Type.equals(int.class)) {
					int[] castedF1 = int[].class.cast(f1.get(obj1));
					int[] castedF2 = int[].class.cast(f2.get(obj2));
					
					int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
					for (int i = 0; i < length; i++) {
						worklist.add(new DistancePair(castedF1[i], castedF2[i]));
					}
					distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
				}
				else if (f1Type.equals(char.class)) {
					char[] castedF1 = char[].class.cast(f1.get(obj1));
					char[] castedF2 = char[].class.cast(f2.get(obj2));
					
					int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
					for (int i = 0; i < length; i++) {
						worklist.add(new DistancePair(castedF1[i], castedF2[i]));
					}
					distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
				}
				else if (f1Type.equals(short.class)) {
					short[] castedF1 = short[].class.cast(f1.get(obj1));
					short[] castedF2 = short[].class.cast(f2.get(obj2));
					
					int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
					for (int i = 0; i < length; i++) {
						worklist.add(new DistancePair(castedF1[i], castedF2[i]));
					}
					distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
				}
				else if (f1Type.equals(long.class)) {
					long[] castedF1 = long[].class.cast(f1.get(obj1));
					long[] castedF2 = long[].class.cast(f2.get(obj2));
					
					int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
					for (int i = 0; i < length; i++) {
						worklist.add(new DistancePair(castedF1[i], castedF2[i]));
					}
					distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
				}
				else if (f1Type.equals(float.class)) {
					float[] castedF1 = float[].class.cast(f1.get(obj1));
					float[] castedF2 = float[].class.cast(f2.get(obj2));
					
					int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
					for (int i = 0; i < length; i++) {
						worklist.add(new DistancePair(castedF1[i], castedF2[i]));
					}
					distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
				}
				else if (f1Type.equals(double.class)) {
					double[] castedF1 = double[].class.cast(f1.get(obj1));
					double[] castedF2 = double[].class.cast(f2.get(obj2));
					
					int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
					for (int i = 0; i < length; i++) {
						worklist.add(new DistancePair(castedF1[i], castedF2[i]));
					}
					distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
				}
				else if (f1Type.equals(boolean.class)) {
					boolean[] castedF1 = boolean[].class.cast(f1.get(obj1));
					boolean[] castedF2 = boolean[].class.cast(f2.get(obj2));
					
					int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
					for (int i = 0; i < length; i++) {
						worklist.add(new DistancePair(castedF1[i], castedF2[i]));
					}
					distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
				}
				else if (f1Type.equals(byte.class)) {
					byte[] castedF1 = byte[].class.cast(f1.get(obj1));
					byte[] castedF2 = byte[].class.cast(f2.get(obj2));
					
					int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
					for (int i = 0; i < length; i++) {
						worklist.add(new DistancePair(castedF1[i], castedF2[i]));
					}
					distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
				}		
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("Error during cast", e);
			}
			//distance += PrimitiveDistance.distance(f1, obj1, f2, obj2);
			break;
		case STRING:
			try {
				String[] castedF1 = String[].class.cast(f1.get(obj1));
				String[] castedF2 = String[].class.cast(f2.get(obj2));
				int length = Math.min(Array.getLength(castedF1), Array.getLength(castedF2));
				for (int i = 0; i < length; i++) {
					distance += LevenshteinDistance.calculateDistance(castedF1[i], castedF2[i]);
				}
				distance += (Math.max(Array.getLength(castedF1), Array.getLength(castedF2)) - length) * Distance.ARRAY_CELL_FACTOR;
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("Error during cast", e);
			}
			break;
		case ARRAY:
			
			break;
		default:
			logger.error("Unknown comparison type: " + arrayType);
			break;
		}
		
		return distance;
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