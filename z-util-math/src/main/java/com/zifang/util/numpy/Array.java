package com.zifang.util.numpy;

/**
 * Internal helper class for array operations
 */
public final class Array {

    /**
     * createZeroArray方法。
     * * @param dtype DType类型参数
     *
     * @param size int类型参数
     * @return static Object类型返回值
     */
    public static Object createZeroArray(DType dtype, int size) {
        switch (dtype) {
            case INT8:
                return new byte[size];
            case INT16:
                return new short[size];
            case INT32:
                return new int[size];
            case INT64:
                return new long[size];
            case FLOAT32:
                return new float[size];
            case FLOAT64:
                return new double[size];
            case BOOL:
                return new boolean[size];
            case STRING:
                return new String[size];
            default:
                return new Object[size];
        }
    }

    /**
     * createOneArray方法。
     * * @param dtype DType类型参数
     *
     * @param size int类型参数
     * @return static Object类型返回值
     */
    public static Object createOneArray(DType dtype, int size) {
        switch (dtype) {
            case INT8:
                byte[] byteOne = new byte[size];
                for (int i = 0; i < size; i++) byteOne[i] = 1;
                return byteOne;
            case INT16:
                short[] shortOne = new short[size];
                for (int i = 0; i < size; i++) shortOne[i] = 1;
                return shortOne;
            case INT32:
                int[] intOne = new int[size];
                for (int i = 0; i < size; i++) intOne[i] = 1;
                return intOne;
            case INT64:
                long[] longOne = new long[size];
                for (int i = 0; i < size; i++) longOne[i] = 1;
                return longOne;
            case FLOAT32:
                float[] floatOne = new float[size];
                for (int i = 0; i < size; i++) floatOne[i] = 1;
                return floatOne;
            case FLOAT64:
                double[] doubleOne = new double[size];
                for (int i = 0; i < size; i++) doubleOne[i] = 1;
                return doubleOne;
            case BOOL:
                boolean[] boolOne = new boolean[size];
                for (int i = 0; i < size; i++) boolOne[i] = true;
                return boolOne;
            case STRING:
                String[] strOne = new String[size];
                for (int i = 0; i < size; i++) strOne[i] = "1";
                return strOne;
            default:
                Object[] objOne = new Object[size];
                for (int i = 0; i < size; i++) objOne[i] = Integer.valueOf(1);
                return objOne;
        }
    }

    /**
     * arange方法。
     * * @param dtype DType类型参数
     *
     * @param start int类型参数
     * @param stop  int类型参数
     * @param step  int类型参数
     * @param size  int类型参数
     * @return static Object类型返回值
     */
    public static Object arange(DType dtype, int start, int stop, int step, int size) {
        switch (dtype) {
            case INT8:
                byte[] byteArr = new byte[size];
                for (int i = 0; i < size; i++) byteArr[i] = (byte) (start + i * step);
                return byteArr;
            case INT16:
                short[] shortArr = new short[size];
                for (int i = 0; i < size; i++) shortArr[i] = (short) (start + i * step);
                return shortArr;
            case INT32:
                int[] intArr = new int[size];
                for (int i = 0; i < size; i++) intArr[i] = start + i * step;
                return intArr;
            case INT64:
                long[] longArr = new long[size];
                for (int i = 0; i < size; i++) longArr[i] = start + (long) i * step;
                return longArr;
            case FLOAT32:
                float[] floatArr = new float[size];
                for (int i = 0; i < size; i++) floatArr[i] = start + (float) i * step;
                return floatArr;
            case FLOAT64:
                double[] doubleArr = new double[size];
                for (int i = 0; i < size; i++) doubleArr[i] = start + (double) i * step;
                return doubleArr;
            default:
                Object[] objArr = new Object[size];
                for (int i = 0; i < size; i++) objArr[i] = Integer.valueOf(start + i * step);
                return objArr;
        }
    }

    /**
     * get方法。
     * * @param array Object类型参数
     *
     * @param index int类型参数
     * @return static Object类型返回值
     */
    public static Object get(Object array, int index) {
        if (array instanceof byte[]) return ((byte[]) array)[index];
        if (array instanceof short[]) return ((short[]) array)[index];
        if (array instanceof int[]) return ((int[]) array)[index];
        if (array instanceof long[]) return ((long[]) array)[index];
        if (array instanceof float[]) return ((float[]) array)[index];
        if (array instanceof double[]) return ((double[]) array)[index];
        if (array instanceof boolean[]) return ((boolean[]) array)[index];
        if (array instanceof String[]) return ((String[]) array)[index];
        if (array instanceof Object[]) return ((Object[]) array)[index];
        throw new IllegalArgumentException("Unsupported array type");
    }

    /**
     * set方法。
     * * @param array Object类型参数
     *
     * @param index int类型参数
     * @param value Object类型参数
     * @return static void类型返回值
     */
    public static void set(Object array, int index, Object value) {
        if (array instanceof byte[] && value instanceof Number) {
            ((byte[]) array)[index] = ((Number) value).byteValue();
        } else if (array instanceof short[] && value instanceof Number) {
            ((short[]) array)[index] = ((Number) value).shortValue();
        } else if (array instanceof int[] && value instanceof Number) {
            ((int[]) array)[index] = ((Number) value).intValue();
        } else if (array instanceof long[] && value instanceof Number) {
            ((long[]) array)[index] = ((Number) value).longValue();
        } else if (array instanceof float[] && value instanceof Number) {
            ((float[]) array)[index] = ((Number) value).floatValue();
        } else if (array instanceof double[] && value instanceof Number) {
            ((double[]) array)[index] = ((Number) value).doubleValue();
        } else if (array instanceof boolean[] && value instanceof Boolean) {
            ((boolean[]) array)[index] = (Boolean) value;
        } else if (array instanceof String[] && value instanceof String) {
            ((String[]) array)[index] = (String) value;
        } else if (array instanceof Object[]) {
            ((Object[]) array)[index] = value;
        } else {
            throw new IllegalArgumentException("Unsupported array type or value");
        }
    }

    /**
     * copy方法。
     * * @param src Object类型参数
     *
     * @param size  int类型参数
     * @param dtype DType类型参数
     * @return static Object类型返回值
     */
    public static Object copy(Object src, int size, DType dtype) {
        if (src instanceof byte[]) {
            return ((byte[]) src).clone();
        }
        if (src instanceof short[]) {
            return ((short[]) src).clone();
        }
        if (src instanceof int[]) {
            return ((int[]) src).clone();
        }
        if (src instanceof long[]) {
            return ((long[]) src).clone();
        }
        if (src instanceof float[]) {
            return ((float[]) src).clone();
        }
        if (src instanceof double[]) {
            return ((double[]) src).clone();
        }
        if (src instanceof boolean[]) {
            return ((boolean[]) src).clone();
        }
        if (src instanceof String[]) {
            return ((String[]) src).clone();
        }
        if (src instanceof Object[]) {
            return ((Object[]) src).clone();
        }
        throw new IllegalArgumentException("Unsupported array type");
    }

    /**
     * copy方法。
     * * @param src Object类型参数
     *
     * @param srcIdx int类型参数
     * @param dst    Object类型参数
     * @param dstIdx int类型参数
     * @return static void类型返回值
     */
    public static void copy(Object src, int srcIdx, Object dst, int dstIdx) {
        Object value = get(src, srcIdx);
        set(dst, dstIdx, value);
    }

    /**
     * fill方法。
     * * @param array Object类型参数
     *
     * @param value Object类型参数
     * @param size  int类型参数
     * @return static void类型返回值
     */
    public static void fill(Object array, Object value, int size) {
        for (int i = 0; i < size; i++) {
            set(array, i, value);
        }
    }

    static String toString(Object array, int size) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(get(array, i));
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    static boolean equals(Object a, Object b, int size) {
        if (a instanceof byte[] && b instanceof byte[]) {
            return java.util.Arrays.equals((byte[]) a, (byte[]) b);
        }
        if (a instanceof short[] && b instanceof short[]) {
            return java.util.Arrays.equals((short[]) a, (short[]) b);
        }
        if (a instanceof int[] && b instanceof int[]) {
            return java.util.Arrays.equals((int[]) a, (int[]) b);
        }
        if (a instanceof long[] && b instanceof long[]) {
            return java.util.Arrays.equals((long[]) a, (long[]) b);
        }
        if (a instanceof float[] && b instanceof float[]) {
            return java.util.Arrays.equals((float[]) a, (float[]) b);
        }
        if (a instanceof double[] && b instanceof double[]) {
            return java.util.Arrays.equals((double[]) a, (double[]) b);
        }
        if (a instanceof boolean[] && b instanceof boolean[]) {
            return java.util.Arrays.equals((boolean[]) a, (boolean[]) b);
        }
        if (a instanceof String[] && b instanceof String[]) {
            return java.util.Arrays.equals((String[]) a, (String[]) b);
        }
        if (a instanceof Object[] && b instanceof Object[]) {
            return java.util.Arrays.equals((Object[]) a, (Object[]) b);
        }
        return false;
    }

    static int hashCode(Object array, int size) {
        if (array instanceof byte[]) return java.util.Arrays.hashCode((byte[]) array);
        if (array instanceof short[]) return java.util.Arrays.hashCode((short[]) array);
        if (array instanceof int[]) return java.util.Arrays.hashCode((int[]) array);
        if (array instanceof long[]) return java.util.Arrays.hashCode((long[]) array);
        if (array instanceof float[]) return java.util.Arrays.hashCode((float[]) array);
        if (array instanceof double[]) return java.util.Arrays.hashCode((double[]) array);
        if (array instanceof boolean[]) return java.util.Arrays.hashCode((boolean[]) array);
        if (array instanceof String[]) return java.util.Arrays.hashCode((String[]) array);
        if (array instanceof Object[]) return java.util.Arrays.hashCode((Object[]) array);
        return 0;
    }

    /**
     * transpose方法。
     * * @param data Object类型参数
     *
     * @param shape int[]类型参数
     * @param axes  int...类型参数
     * @return static Object类型返回值
     */
    public static Object transpose(Object data, int[] shape, int... axes) {
        int ndim = shape.length;
        int size = 1;
        for (int dim : shape) size *= dim;

        // Determine the new (permuted) shape: newShape[j] = shape[axes[j]]
        int[] newShape = new int[ndim];
        for (int j = 0; j < ndim; j++) {
            newShape[j] = shape[axes[j]];
        }

        // C-order strides for original shape
        int[] origStrides = new int[ndim];
        origStrides[ndim - 1] = 1;
        for (int i = ndim - 2; i >= 0; i--) {
            origStrides[i] = origStrides[i + 1] * shape[i + 1];
        }

        // C-order strides for the transposed (new) shape
        int[] newStrides = new int[ndim];
        newStrides[ndim - 1] = 1;
        for (int i = ndim - 2; i >= 0; i--) {
            newStrides[i] = newStrides[i + 1] * newShape[i + 1];
        }

        // Precompute the inverse axes: invAxes[old_axis] = position of that old axis in new array
        int[] invAxes = new int[ndim];
        for (int j = 0; j < ndim; j++) {
            invAxes[axes[j]] = j;
        }

        Object result = createZeroArray(DType.fromClass(data.getClass().getComponentType()), size);
        int[] coord = new int[ndim];

        for (int i = 0; i < size; i++) {
            // Decompose flat output index i into coordinates for the new shape
            int tmp = i;
            for (int j = ndim - 1; j >= 0; j--) {
                coord[j] = tmp % newShape[j];
                tmp = tmp / newShape[j];
            }

            // Map new coordinates to old coordinates using axes
            // axes[j] = old axis at new axis position j
            // So oldCoord[axes[j]] = newCoord[j]
            // oldIdx = Σ oldCoord[k] * origStrides[k]
            int oldIdx = 0;
            for (int k = 0; k < ndim; k++) {
                oldIdx += coord[axes[k]] * origStrides[k];
            }

            copy(data, oldIdx, result, i);
        }

        return result;
    }
}
