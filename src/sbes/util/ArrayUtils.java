package sbes.util;

import java.lang.reflect.Array;


public class ArrayUtils {

    /**
     * An empty immutable {@code long} array.
     */
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    /**
     * An empty immutable {@code int} array.
     */
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    /**
     * An empty immutable {@code short} array.
     */
    private static final short[] EMPTY_SHORT_ARRAY = new short[0];
    /**
     * An empty immutable {@code byte} array.
     */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    /**
     * An empty immutable {@code double} array.
     */
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    /**
     * An empty immutable {@code float} array.
     */
    private static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    /**
     * An empty immutable {@code boolean} array.
     */
    private static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    /**
     * An empty immutable {@code char} array.
     */
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * The index value when an element is not found in a list or array: {@code -1}.
     * This value is returned by methods in this class and can also be used in comparisons with values returned by
     * various method from {@link java.util.List}.
     */
    public static final int INDEX_NOT_FOUND = -1;

	public ArrayUtils() {
		super();
	}

	public static char[] toPrimitive(final Character[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_CHAR_ARRAY;
		}
		final char[] result = new char[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].charValue();
		}
		return result;
	}

	public static long[] toPrimitive(final Long[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_LONG_ARRAY;
		}
		final long[] result = new long[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].longValue();
		}
		return result;
	}

	public static int[] toPrimitive(final Integer[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_INT_ARRAY;
		}
		final int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].intValue();
		}
		return result;
	}


	public static short[] toPrimitive(final Short[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_SHORT_ARRAY;
		}
		final short[] result = new short[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].shortValue();
		}
		return result;
	}


	public static byte[] toPrimitive(final Byte[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_BYTE_ARRAY;
		}
		final byte[] result = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].byteValue();
		}
		return result;
	}


	public static double[] toPrimitive(final Double[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_DOUBLE_ARRAY;
		}
		final double[] result = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].doubleValue();
		}
		return result;
	}


	public static float[] toPrimitive(final Float[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_FLOAT_ARRAY;
		}
		final float[] result = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].floatValue();
		}
		return result;
	}


	public static boolean[] toPrimitive(final Boolean[] array) {
		if (array == null) {
			return null;
		} else if (array.length == 0) {
			return EMPTY_BOOLEAN_ARRAY;
		}
		final boolean[] result = new boolean[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].booleanValue();
		}
		return result;
	}

	/**
     * <p>Copies the given array and adds the given element at the end of the new array.</p>
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.</p>
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     *  whose component type is the same as the element, unless the element itself is null,
     *  in which case the return type is Object[]</p>
     *
     * <pre>
     * ArrayUtils.add(null, null)      = [null]
     * ArrayUtils.add(null, "a")       = ["a"]
     * ArrayUtils.add(["a"], null)     = ["a", null]
     * ArrayUtils.add(["a"], "b")      = ["a", "b"]
     * ArrayUtils.add(["a", "b"], "c") = ["a", "b", "c"]
     * </pre>
     *
     * @param <T> the component type of the array
     * @param array  the array to "add" the element to, may be {@code null}
     * @param element  the object to add, may be {@code null}
     * @return A new array containing the existing elements plus the new element
     * The returned array type will be that of the input array (unless null),
     * in which case it will have the same type as the element.
     * If both are null, an IllegalArgumentException is thrown
     * @throws IllegalArgumentException if both arguments are null
     */
	public static <T> T[] add(final T[] array, final T element) {
		Class<?> type;
		if (array != null) {
			type = array.getClass();
		} else if (element != null) {
			type = element.getClass();
		} else {
			throw new IllegalArgumentException("Arguments cannot both be null");
		}

		@SuppressWarnings("unchecked")
		// type must be T
		final T[] newArray = (T[]) copyArrayGrow1(array, type);
		newArray[newArray.length - 1] = element;
		return newArray;
	}

	/**
     * Returns a copy of the given array of size 1 greater than the argument.
     * The last value of the array is left to the default value.
     *
     * @param array The array to copy, must not be {@code null}.
     * @param newArrayComponentType If {@code array} is {@code null}, create a
     * size 1 array of this type.
     * @return A new copy of the array of size 1 greater than the input.
     */
	private static Object copyArrayGrow1(final Object array, final Class<?> newArrayComponentType) {
		if (array != null) {
			final int arrayLength = Array.getLength(array);
			final Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
			System.arraycopy(array, 0, newArray, 0, arrayLength);
			return newArray;
		}
		return Array.newInstance(newArrayComponentType, 1);
	}
	
	/**
     * <p>Checks if the object is in the given array.</p>
     *
     * <p>The method returns {@code false} if a {@code null} array is passed in.</p>
     *
     * @param array  the array to search through
     * @param objectToFind  the object to find
     * @return {@code true} if the array contains the object
     */
    public static boolean contains(final Object[] array, final Object objectToFind) {
        return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
    }
    
    /**
     * <p>Finds the index of the given object in the array.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.</p>
     *
     * @param array  the array to search through for the object, may be {@code null}
     * @param objectToFind  the object to find, may be {@code null}
     * @return the index of the object within the array,
     *  {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
     */
    public static int indexOf(final Object[] array, final Object objectToFind) {
        return indexOf(array, objectToFind, 0);
    }

    /**
     * <p>Finds the index of the given object in the array starting at the given index.</p>
     *
     * <p>This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null} input array.</p>
     *
     * <p>A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).</p>
     *
     * @param array  the array to search through for the object, may be {@code null}
     * @param objectToFind  the object to find, may be {@code null}
     * @param startIndex  the index to start searching at
     * @return the index of the object within the array starting at the index,
     *  {@link #INDEX_NOT_FOUND} ({@code -1}) if not found or {@code null} array input
     */
    public static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else if (array.getClass().getComponentType().isInstance(objectToFind)) {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

}
