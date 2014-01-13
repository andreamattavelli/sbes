package sbes.util;


public class ArrayUtils {

    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

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

}
