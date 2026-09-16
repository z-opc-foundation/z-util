package com.zifang.util.core.lang;


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 数组相关的工具类
 */
public class ArraysUtil {

    /**
     * An empty immutable {@code Object} array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    /**
     * An empty immutable {@code Class} array.
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    /**
     * An empty immutable {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    // ---------------------------------------------------------------- resize
    /**
     * An empty immutable {@code long} array.
     */
    public static final long[] EMPTY_LONG_ARRAY = new long[0];

    // ---------------------------------------------------------------- append
    /**
     * An empty immutable {@code Long} array.
     */
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
    /**
     * An empty immutable {@code int} array.
     */
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    /**
     * An empty immutable {@code Integer} array.
     */
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
    /**
     * An empty immutable {@code short} array.
     */
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    /**
     * An empty immutable {@code Short} array.
     */
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
    /**
     * An empty immutable {@code byte} array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    // ---------------------------------------------------------------- insert
    /**
     * An empty immutable {@code Byte} array.
     */
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
    /**
     * An empty immutable {@code double} array.
     */
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    /**
     * An empty immutable {@code Double} array.
     */
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
    /**
     * An empty immutable {@code float} array.
     */
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    /**
     * An empty immutable {@code Float} array.
     */
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
    /**
     * An empty immutable {@code boolean} array.
     */
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    /**
     * An empty immutable {@code Boolean} array.
     */
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
    /**
     * An empty immutable {@code char} array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    /**
     * An empty immutable {@code Character} array.
     */
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];
    /**
     * The index value when an element is not found in a list or array: {@code -1}. This value is
     * returned by methods in this class and can also be used in comparisons with values returned by
     * various method from {@link java.util.List}.
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * 转换多个元素为整个数组
     */
    public static <T> T[] array(T... elements) {
        return elements;
    }

    /**
     * 合并多个数组变为一个数组
     */
    @SuppressWarnings({"unchecked"})
    /**
     * join方法。
     *      * @param arrays T[]...类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] join(T[]... arrays) {
        Class<T> componentType = (Class<T>) arrays.getClass().getComponentType().getComponentType();
        return join(componentType, arrays);
    }

    /**
     * Joins arrays using provided component type.
     */
    @SuppressWarnings({"unchecked"})
    /**
     * join方法。
     *      * @param componentType ClassT类型参数
     * @param arrays T[][]类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] join(Class<T> componentType, T[][] arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }
        int length = 0;
        for (T[] array : arrays) {
            length += array.length;
        }
        T[] result = (T[]) Array.newInstance(componentType, length);

        length = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, result, length, array.length);
            length += array.length;
        }
        return result;
    }

    /**
     * Resizes an array.
     */
    public static <T> T[] resize(T[] buffer, int newSize) {
        Class<T> componentType = (Class<T>) buffer.getClass().getComponentType();
        T[] temp = (T[]) Array.newInstance(componentType, newSize);
        System.arraycopy(buffer, 0, temp, 0, buffer.length >= newSize ? newSize : buffer.length);
        return temp;
    }

    /**
     * Appends an element to array.
     */
    public static <T> T[] append(T[] buffer, T newElement) {
        T[] t = resize(buffer, buffer.length + 1);
        t[buffer.length] = newElement;
        return t;
    }

    /**
     * append方法。
     * * @param buffer T[]类型参数
     *
     * @param newElement T[]类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] append(T[] buffer, T[] newElement) {
        List<T> list = java.util.Arrays.asList(buffer);
        list.addAll(java.util.Arrays.asList(newElement));
        return (T[]) list.toArray();
    }

    /**
     * Removes sub-array.
     */
    public static <T> T[] remove(T[] buffer, int offset, int length) {
        Class<T> componentType = (Class<T>) buffer.getClass().getComponentType();
        return remove(buffer, offset, length, componentType);
    }

    /**
     * Removes sub-array.
     */
    @SuppressWarnings({"unchecked"})
    /**
     * remove方法。
     *      * @param buffer T[]类型参数
     * @param offset int类型参数
     * @param length int类型参数
     * @param componentType ClassT类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] remove(T[] buffer, int offset, int length, Class<T> componentType) {
        int len2 = buffer.length - length;
        T[] temp = (T[]) Array.newInstance(componentType, len2);
        System.arraycopy(buffer, 0, temp, 0, offset);
        System.arraycopy(buffer, offset + length, temp, offset, len2 - offset);
        return temp;
    }

    /**
     * Returns subarray.
     */
    public static <T> T[] subarray(T[] buffer, int offset, int length) {
        Class<T> componentType = (Class<T>) buffer.getClass().getComponentType();
        return subarray(buffer, offset, length, componentType);
    }

    /**
     * Returns subarray.
     */
    @SuppressWarnings({"unchecked"})
    /**
     * subarray方法。
     *      * @param buffer T[]类型参数
     * @param offset int类型参数
     * @param length int类型参数
     * @param componentType ClassT类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] subarray(T[] buffer, int offset, int length, Class<T> componentType) {
        T[] temp = (T[]) Array.newInstance(componentType, length);
        System.arraycopy(buffer, offset, temp, 0, length);
        return temp;
    }

    /**
     * Inserts one array into another array.
     */
    public static <T> T[] insert(T[] dest, T[] src, int offset) {
        Class<T> componentType = (Class<T>) dest.getClass().getComponentType();
        return insert(dest, src, offset, componentType);
    }

    /**
     * Inserts one element into an array.
     */
    public static <T> T[] insert(T[] dest, T src, int offset) {
        Class<T> componentType = (Class<T>) dest.getClass().getComponentType();
        return insert(dest, src, offset, componentType);
    }


    // ---------------------------------------------------------------- indexof

    /**
     * Inserts one array into another array.
     */
    @SuppressWarnings({"unchecked"})
    /**
     * insert方法。
     *      * @param dest T[]类型参数
     * @param src T[]类型参数
     * @param offset int类型参数
     * @param componentType Class类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] insert(T[] dest, T[] src, int offset, Class componentType) {
        T[] temp = (T[]) Array.newInstance(componentType, dest.length + src.length);
        System.arraycopy(dest, 0, temp, 0, offset);
        System.arraycopy(src, 0, temp, offset, src.length);
        System.arraycopy(dest, offset, temp, src.length + offset, dest.length - offset);
        return temp;
    }

    /**
     * Inserts one element into another array.
     */
    @SuppressWarnings({"unchecked"})
    /**
     * insert方法。
     *      * @param dest T[]类型参数
     * @param src T类型参数
     * @param offset int类型参数
     * @param componentType Class类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] insert(T[] dest, T src, int offset, Class componentType) {
        T[] temp = (T[]) Array.newInstance(componentType, dest.length + 1);
        System.arraycopy(dest, 0, temp, 0, offset);
        temp[offset] = src;
        System.arraycopy(dest, offset, temp, offset + 1, dest.length - offset);
        return temp;
    }

    /**
     * Inserts one array into another at given offset.
     */
    public static <T> T[] insertAt(T[] dest, T[] src, int offset) {
        Class<T> componentType = (Class<T>) dest.getClass().getComponentType();
        return insertAt(dest, src, offset, componentType);
    }

    /**
     * Inserts one array into another at given offset.
     */
    @SuppressWarnings({"unchecked"})
    /**
     * insertAt方法。
     *      * @param dest T[]类型参数
     * @param src T[]类型参数
     * @param offset int类型参数
     * @param componentType Class类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] insertAt(T[] dest, T[] src, int offset, Class componentType) {
        T[] temp = (T[]) Array.newInstance(componentType, dest.length + src.length - 1);
        System.arraycopy(dest, 0, temp, 0, offset);
        System.arraycopy(src, 0, temp, offset, src.length);
        System.arraycopy(dest, offset + 1, temp, src.length + offset, dest.length - offset - 1);
        return temp;
    }

    /**
     * Converts to primitive array.
     */
    public static byte[] values(Byte[] array) {
        byte[] dest = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            Byte v = array[i];
            if (v != null) {
                dest[i] = v.byteValue();
            }
        }
        return dest;
    }

    /**
     * Converts to object array.
     */
    public static Byte[] valuesOf(byte[] array) {
        Byte[] dest = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            dest[i] = Byte.valueOf(array[i]);
        }
        return dest;
    }

    /**
     * Converts to primitive array.
     */
    public static char[] values(Character[] array) {
        char[] dest = new char[array.length];
        for (int i = 0; i < array.length; i++) {
            Character v = array[i];
            if (v != null) {
                dest[i] = v.charValue();
            }
        }
        return dest;
    }

    /**
     * Converts to object array.
     */
    public static Character[] valuesOf(char[] array) {
        Character[] dest = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            dest[i] = Character.valueOf(array[i]);
        }
        return dest;
    }

    /**
     * Converts to primitive array.
     */
    public static short[] values(Short[] array) {
        short[] dest = new short[array.length];
        for (int i = 0; i < array.length; i++) {
            Short v = array[i];
            if (v != null) {
                dest[i] = v.shortValue();
            }
        }
        return dest;
    }

    /**
     * Converts to object array.
     */
    public static Short[] valuesOf(short[] array) {
        Short[] dest = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            dest[i] = Short.valueOf(array[i]);
        }
        return dest;
    }

    /**
     * Converts to primitive array.
     */
    public static int[] values(Integer[] array) {
        int[] dest = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            Integer v = array[i];
            if (v != null) {
                dest[i] = v.intValue();
            }
        }
        return dest;
    }

    /**
     * Converts to object array.
     */
    public static Integer[] valuesOf(int[] array) {
        Integer[] dest = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            dest[i] = Integer.valueOf(array[i]);
        }
        return dest;
    }

    /**
     * Converts to primitive array.
     */
    public static long[] values(Long[] array) {
        long[] dest = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            Long v = array[i];
            if (v != null) {
                dest[i] = v.longValue();
            }
        }
        return dest;
    }

    /**
     * Converts to object array.
     */
    public static Long[] valuesOf(long[] array) {
        Long[] dest = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            dest[i] = Long.valueOf(array[i]);
        }
        return dest;
    }

    /**
     * Converts to primitive array.
     */
    public static float[] values(Float[] array) {
        float[] dest = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            Float v = array[i];
            if (v != null) {
                dest[i] = v.floatValue();
            }
        }
        return dest;
    }

    /**
     * Converts to object array.
     */
    public static Float[] valuesOf(float[] array) {
        Float[] dest = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            dest[i] = Float.valueOf(array[i]);
        }
        return dest;
    }

    /**
     * Converts to primitive array.
     */
    public static double[] values(Double[] array) {
        double[] dest = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            Double v = array[i];
            if (v != null) {
                dest[i] = v.doubleValue();
            }
        }
        return dest;
    }

    /**
     * Converts to object array.
     */
    public static Double[] valuesOf(double[] array) {
        Double[] dest = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            dest[i] = Double.valueOf(array[i]);
        }
        return dest;
    }

    /**
     * Converts to primitive array.
     */
    public static boolean[] values(Boolean[] array) {
        boolean[] dest = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            Boolean v = array[i];
            if (v != null) {
                dest[i] = v.booleanValue();
            }
        }
        return dest;
    }

    /**
     * Converts to object array.
     */
    public static Boolean[] valuesOf(boolean[] array) {
        Boolean[] dest = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            dest[i] = Boolean.valueOf(array[i]);
        }
        return dest;
    }

    /**
     * Finds the first occurrence of an element in an array.
     */
    public static int indexOf(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if an array contains given value.
     */
    public static boolean contains(byte[] array, byte value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence of given value in an array from specified given position.
     */
    public static int indexOf(byte[] array, byte value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(byte[] array, byte value, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence of an element in an array.
     */
    public static int indexOf(char[] array, char value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if an array contains given value.
     */
    public static boolean contains(char[] array, char value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence of given value in an array from specified given position.
     */
    public static int indexOf(char[] array, char value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(char[] array, char value, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence of an element in an array.
     */
    public static int indexOf(short[] array, short value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if an array contains given value.
     */
    public static boolean contains(short[] array, short value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence of given value in an array from specified given position.
     */
    public static int indexOf(short[] array, short value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(short[] array, short value, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence of an element in an array.
     */
    public static int indexOf(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if an array contains given value.
     */
    public static boolean contains(int[] array, int value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence of given value in an array from specified given position.
     */
    public static int indexOf(int[] array, int value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(int[] array, int value, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }


    // ---------------------------------------------------------------- indexof 2

    /**
     * Finds the first occurrence of an element in an array.
     */
    public static int indexOf(long[] array, long value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if an array contains given value.
     */
    public static boolean contains(long[] array, long value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence of given value in an array from specified given position.
     */
    public static int indexOf(long[] array, long value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(long[] array, long value, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence of an element in an array.
     */
    public static int indexOf(boolean[] array, boolean value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if an array contains given value.
     */
    public static boolean contains(boolean[] array, boolean value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence of given value in an array from specified given position.
     */
    public static int indexOf(boolean[] array, boolean value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(boolean[] array, boolean value, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence of value in <code>float</code> array.
     */
    public static int indexOf(float[] array, float value) {
        for (int i = 0; i < array.length; i++) {
            if (Float.compare(array[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if <code>float</code> array contains given value.
     */
    public static boolean contains(float[] array, float value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence of given value in <code>float</code>
     * array from specified given position.
     */
    public static int indexOf(float[] array, float value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (Float.compare(array[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in <code>float</code> array from specified given position and upto given length.
     */
    public static int indexOf(float[] array, float value, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (Float.compare(array[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence of value in <code>double</code> array.
     */
    public static int indexOf(double[] array, double value) {
        for (int i = 0; i < array.length; i++) {
            if (Double.compare(array[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns <code>true</code> if <code>double</code> array contains given value.
     */
    public static boolean contains(double[] array, double value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence of given value in <code>double</code>
     * array from specified given position.
     */
    public static int indexOf(double[] array, double value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (Double.compare(array[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in <code>double</code> array from specified given position and upto given length.
     */
    public static int indexOf(double[] array, double value, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            if (Double.compare(array[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(Object[] array, Object value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * contains方法。
     * * @param array Object[]类型参数
     *
     * @param value Object类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(Object[] array, Object value) {
        return indexOf(array, value) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(Object[] array, Object value, int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * contains方法。
     * * @param array Object[]类型参数
     *
     * @param value      Object类型参数
     * @param startIndex int类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(Object[] array, Object value, int startIndex) {
        return indexOf(array, value, startIndex) != -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(byte[] array, byte[] sub) {
        return indexOf(array, sub, 0, array.length);
    }

    /**
     * contains方法。
     * * @param array byte[]类型参数
     *
     * @param sub byte[]类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(byte[] array, byte[] sub) {
        return indexOf(array, sub) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(byte[] array, byte[] sub, int startIndex) {
        return indexOf(array, sub, startIndex, array.length);
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(byte[] array, byte[] sub, int startIndex, int endIndex) {
        int sublen = sub.length;
        if (sublen == 0) {
            return startIndex;
        }
        int total = endIndex - sublen + 1;
        byte c = sub[0];
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (array[i] != c) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (sub[j] != array[k]) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(char[] array, char[] sub) {
        return indexOf(array, sub, 0, array.length);
    }

    /**
     * contains方法。
     * * @param array char[]类型参数
     *
     * @param sub char[]类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(char[] array, char[] sub) {
        return indexOf(array, sub) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(char[] array, char[] sub, int startIndex) {
        return indexOf(array, sub, startIndex, array.length);
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(char[] array, char[] sub, int startIndex, int endIndex) {
        int sublen = sub.length;
        if (sublen == 0) {
            return startIndex;
        }
        int total = endIndex - sublen + 1;
        char c = sub[0];
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (array[i] != c) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (sub[j] != array[k]) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(short[] array, short[] sub) {
        return indexOf(array, sub, 0, array.length);
    }

    /**
     * contains方法。
     * * @param array short[]类型参数
     *
     * @param sub short[]类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(short[] array, short[] sub) {
        return indexOf(array, sub) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(short[] array, short[] sub, int startIndex) {
        return indexOf(array, sub, startIndex, array.length);
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(short[] array, short[] sub, int startIndex, int endIndex) {
        int sublen = sub.length;
        if (sublen == 0) {
            return startIndex;
        }
        int total = endIndex - sublen + 1;
        short c = sub[0];
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (array[i] != c) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (sub[j] != array[k]) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(int[] array, int[] sub) {
        return indexOf(array, sub, 0, array.length);
    }

    /**
     * contains方法。
     * * @param array int[]类型参数
     *
     * @param sub int[]类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(int[] array, int[] sub) {
        return indexOf(array, sub) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(int[] array, int[] sub, int startIndex) {
        return indexOf(array, sub, startIndex, array.length);
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(int[] array, int[] sub, int startIndex, int endIndex) {
        int sublen = sub.length;
        if (sublen == 0) {
            return startIndex;
        }
        int total = endIndex - sublen + 1;
        int c = sub[0];
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (array[i] != c) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (sub[j] != array[k]) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(long[] array, long[] sub) {
        return indexOf(array, sub, 0, array.length);
    }

    /**
     * contains方法。
     * * @param array long[]类型参数
     *
     * @param sub long[]类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(long[] array, long[] sub) {
        return indexOf(array, sub) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(long[] array, long[] sub, int startIndex) {
        return indexOf(array, sub, startIndex, array.length);
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(long[] array, long[] sub, int startIndex, int endIndex) {
        int sublen = sub.length;
        if (sublen == 0) {
            return startIndex;
        }
        int total = endIndex - sublen + 1;
        long c = sub[0];
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (array[i] != c) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (sub[j] != array[k]) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(boolean[] array, boolean[] sub) {
        return indexOf(array, sub, 0, array.length);
    }

    /**
     * contains方法。
     * * @param array boolean[]类型参数
     *
     * @param sub boolean[]类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(boolean[] array, boolean[] sub) {
        return indexOf(array, sub) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(boolean[] array, boolean[] sub, int startIndex) {
        return indexOf(array, sub, startIndex, array.length);
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(boolean[] array, boolean[] sub, int startIndex, int endIndex) {
        int sublen = sub.length;
        if (sublen == 0) {
            return startIndex;
        }
        int total = endIndex - sublen + 1;
        boolean c = sub[0];
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (array[i] != c) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (sub[j] != array[k]) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(float[] array, float[] sub) {
        return indexOf(array, sub, 0, array.length);
    }

    /**
     * contains方法。
     * * @param array float[]类型参数
     *
     * @param sub float[]类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(float[] array, float[] sub) {
        return indexOf(array, sub) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(float[] array, float[] sub, int startIndex) {
        return indexOf(array, sub, startIndex, array.length);
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(float[] array, float[] sub, int startIndex, int endIndex) {
        int sublen = sub.length;
        if (sublen == 0) {
            return startIndex;
        }
        int total = endIndex - sublen + 1;
        float c = sub[0];
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (Float.compare(array[i], c) != 0) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (Float.compare(sub[j], array[k]) != 0) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }

    /**
     * Finds the first occurrence in an array.
     */
    public static int indexOf(double[] array, double[] sub) {
        return indexOf(array, sub, 0, array.length);
    }

    /**
     * contains方法。
     * * @param array double[]类型参数
     *
     * @param sub double[]类型参数
     * @return static boolean类型返回值
     */
    public static boolean contains(double[] array, double[] sub) {
        return indexOf(array, sub) != -1;
    }

    /**
     * Finds the first occurrence in an array from specified given position.
     */
    public static int indexOf(double[] array, double[] sub, int startIndex) {
        return indexOf(array, sub, startIndex, array.length);
    }

    /**
     * Finds the first occurrence in an array from specified given position and upto given length.
     */
    public static int indexOf(double[] array, double[] sub, int startIndex, int endIndex) {
        int sublen = sub.length;
        if (sublen == 0) {
            return startIndex;
        }
        int total = endIndex - sublen + 1;
        double c = sub[0];
        mainloop:
        for (int i = startIndex; i < total; i++) {
            if (Double.compare(array[i], c) != 0) {
                continue;
            }
            int j = 1;
            int k = i + 1;
            while (j < sublen) {
                if (Double.compare(sub[j], array[k]) != 0) {
                    continue mainloop;
                }
                j++;
                k++;
            }
            return i;
        }
        return -1;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(String[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(byte[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(char[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(short[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(int[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(long[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(float[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(double[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Converts an array to string array.
     */
    public static String[] toStringArray(boolean[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Remove empty Strings from string array
     *
     * @param strings Array of String to be cleaned
     * @return Array of String without empty Strings
     */
    public static String[] removeEmptyStrings(String[] strings) {
        if (Objects.isNull(strings)) {
            throw new IllegalArgumentException("Input array should not be null");
        }
        return Arrays.stream(strings).filter(str -> str != null && !str.trim().isEmpty()).toArray(String[]::new);
    }

    /**
     * 判断数组是否 为空
     */
    private static <T> boolean isEmptyArray(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否 不为空
     */
    public static <T> boolean isNotEmptyArray(T[] array) {
        return array != null && array.length > 0;
    }

    /**
     * isDeeplyEqual方法。
     * * @param array1 T[]类型参数
     *
     * @param array2 T[]类型参数
     * @return static <T> boolean类型返回值
     */
    public static <T> boolean isDeeplyEqual(T[] array1, T[] array2) {
        if (array1 == null || array2 == null) { //清除参数是null的可能性
            return array1 == array2;
        } else if (array1.length != array2.length) { //排除length大小不同的问题
            return false;
        } else {
            for (int i = 0; i < array1.length; i++) {  //遍历判断是否完全一致
                if (!array1[i].equals(array2[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * isEmpty方法。
     * * @param array Object[]类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean isEmpty(Object[] array) {
        return null == array || array.length == 0;
    }

    /**
     * isNotEmpty方法。
     * * @param array Object[]类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    @SafeVarargs
    /**
     * asArray方法。
     *      * @param elements T...类型参数
     * @return static <T> T[]类型返回值
     */
    public static <T> T[] asArray(T... elements) {
        return elements;
    }

//
//    public static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
//        if (array == null) {
//            return INDEX_NOT_FOUND;
//        }
//        if (startIndex < 0) {
//            startIndex = 0;
//        }
//        if (objectToFind == null) {
//            for (int i = startIndex; i < array.length; i++) {
//                if (array[i] == null) {
//                    return i;
//                }
//            }
//        } else {
//            for (int i = startIndex; i < array.length; i++) {
//                if (objectToFind.equals(array[i])) {
//                    return i;
//                }
//            }
//        }
//        return INDEX_NOT_FOUND;
//    }

}