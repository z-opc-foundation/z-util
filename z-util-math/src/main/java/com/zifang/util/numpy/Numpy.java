package com.zifang.util.numpy;

/**
 * NumPy-like API for Java
 */
public final class Numpy {

    private Numpy() {
        // Utility class
    }

    /**
     * array方法。
     * * @param data Object类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray array(Object data, DType dtype) {
        return NdArray.array(data, dtype);
    }

    /**
     * array方法。
     * * @param data Object类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray array(Object data) {
        DType dtype = inferDType(data);
        return NdArray.array(data, dtype);
    }

    /**
     * zeros方法。
     * * @param shape Shape类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray zeros(Shape shape, DType dtype) {
        return NdArray.zeros(shape, dtype);
    }

    /**
     * zeros方法。
     * * @param shape int...类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray zeros(int... shape) {
        return NdArray.zeros(new Shape(shape), DType.FLOAT64);
    }

    /**
     * zeros方法。
     * * @param dtype DType类型参数
     *
     * @param shape int...类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray zeros(DType dtype, int... shape) {
        return NdArray.zeros(new Shape(shape), dtype);
    }

    /**
     * ones方法。
     * * @param shape Shape类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray ones(Shape shape, DType dtype) {
        return NdArray.ones(shape, dtype);
    }

    /**
     * ones方法。
     * * @param shape int...类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray ones(int... shape) {
        return NdArray.ones(new Shape(shape), DType.FLOAT64);
    }

    /**
     * ones方法。
     * * @param dtype DType类型参数
     *
     * @param shape int...类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray ones(DType dtype, int... shape) {
        return NdArray.ones(new Shape(shape), dtype);
    }

    /**
     * arange方法。
     * * @param start int类型参数
     *
     * @param stop  int类型参数
     * @param step  int类型参数
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray arange(int start, int stop, int step, DType dtype) {
        return NdArray.arange(start, stop, step, dtype);
    }

    /**
     * arange方法。
     * * @param start int类型参数
     *
     * @param stop  int类型参数
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray arange(int start, int stop, DType dtype) {
        return NdArray.arange(start, stop, 1, dtype);
    }

    /**
     * arange方法。
     * * @param stop int类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray arange(int stop, DType dtype) {
        return NdArray.arange(0, stop, 1, dtype);
    }

    /**
     * arange方法。
     * * @param start int类型参数
     *
     * @param stop int类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray arange(int start, int stop) {
        return NdArray.arange(start, stop, 1, DType.INT32);
    }

    /**
     * arange方法。
     * * @param stop int类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray arange(int stop) {
        return NdArray.arange(0, stop, 1, DType.INT32);
    }

    /**
     * empty方法。
     * * @param shape Shape类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray empty(Shape shape, DType dtype) {
        return NdArray.zeros(shape, dtype);
    }

    /**
     * empty方法。
     * * @param shape int...类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray empty(int... shape) {
        return NdArray.zeros(new Shape(shape), DType.FLOAT64);
    }

    /**
     * empty方法。
     * * @param dtype DType类型参数
     *
     * @param shape int...类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray empty(DType dtype, int... shape) {
        return NdArray.zeros(new Shape(shape), dtype);
    }

    /**
     * reshape方法。
     * * @param arr NdArray类型参数
     *
     * @param newShape int...类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray reshape(NdArray arr, int... newShape) {
        return arr.reshape(newShape);
    }

    /**
     * transpose方法。
     * * @param arr NdArray类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray transpose(NdArray arr) {
        return arr.transpose();
    }

    /**
     * transpose方法。
     * * @param arr NdArray类型参数
     *
     * @param axes int...类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray transpose(NdArray arr, int... axes) {
        return arr.transpose(axes);
    }

    /**
     * copy方法。
     * * @param arr NdArray类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray copy(NdArray arr) {
        return arr.copy();
    }

    /**
     * fill方法。
     * * @param arr NdArray类型参数
     *
     * @param value Object类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray fill(NdArray arr, Object value) {
        return arr.fill(value);
    }

    /**
     * shape方法。
     * * @param arr NdArray类型参数
     *
     * @return static Shape类型返回值
     */
    public static Shape shape(NdArray arr) {
        return arr.getShape();
    }

    /**
     * dtype方法。
     * * @param arr NdArray类型参数
     *
     * @return static DType类型返回值
     */
    public static DType dtype(NdArray arr) {
        return arr.getDtype();
    }

    /**
     * size方法。
     * * @param arr NdArray类型参数
     *
     * @return static int类型返回值
     */
    public static int size(NdArray arr) {
        return arr.size();
    }

    /**
     * ndim方法。
     * * @param arr NdArray类型参数
     *
     * @return static int类型返回值
     */
    public static int ndim(NdArray arr) {
        return arr.ndim();
    }

    /**
     * sum方法。
     * * @param a NdArray类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray sum(NdArray a) {
        if (a.size() == 0) {
            return a.copy();
        }
        Object result = Array.get(a.getData(), 0);
        for (int i = 1; i < a.size(); i++) {
            result = add(result, Array.get(a.getData(), i));
        }
        Object data = Array.createZeroArray(a.getDtype(), 1);
        Array.set(data, 0, result);
        return NdArray.create(data, a.getDtype(), new Shape(1));
    }

    /**
     * mean方法。
     * * @param a NdArray类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray mean(NdArray a) {
        if (a.size() == 0) {
            return a.copy();
        }
        NdArray s = sum(a);
        Object result = divide(Array.get(s.getData(), 0), a.size());
        Object data = Array.createZeroArray(a.getDtype(), 1);
        Array.set(data, 0, result);
        return NdArray.create(data, a.getDtype(), new Shape(1));
    }

    /**
     * max方法。
     * * @param a NdArray类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray max(NdArray a) {
        if (a.size() == 0) {
            return a.copy();
        }
        Comparable result = (Comparable) Array.get(a.getData(), 0);
        for (int i = 1; i < a.size(); i++) {
            Comparable val = (Comparable) Array.get(a.getData(), i);
            if (val.compareTo(result) > 0) {
                result = val;
            }
        }
        Object data = Array.createZeroArray(a.getDtype(), 1);
        Array.set(data, 0, result);
        return NdArray.create(data, a.getDtype(), new Shape(1));
    }

    /**
     * min方法。
     * * @param a NdArray类型参数
     *
     * @return static NdArray类型返回值
     */
    public static NdArray min(NdArray a) {
        if (a.size() == 0) {
            return a.copy();
        }
        Comparable result = (Comparable) Array.get(a.getData(), 0);
        for (int i = 1; i < a.size(); i++) {
            Comparable val = (Comparable) Array.get(a.getData(), i);
            if (val.compareTo(result) < 0) {
                result = val;
            }
        }
        Object data = Array.createZeroArray(a.getDtype(), 1);
        Array.set(data, 0, result);
        return NdArray.create(data, a.getDtype(), new Shape(1));
    }

    private static Object add(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            double da = ((Number) a).doubleValue();
            double db = ((Number) b).doubleValue();
            if (a instanceof Double || b instanceof Double) return da + db;
            if (a instanceof Float || b instanceof Float) return da + db;
            if (a instanceof Long || b instanceof Long) return (long) da + (long) db;
            return (int) da + (int) db;
        }
        throw new IllegalArgumentException("Unsupported operand types");
    }

    private static Object divide(Object a, int divisor) {
        if (a instanceof Number) {
            double da = ((Number) a).doubleValue();
            return da / divisor;
        }
        throw new IllegalArgumentException("Unsupported operand type");
    }

    private static DType inferDType(Object data) {
        if (data instanceof byte[]) return DType.INT8;
        if (data instanceof short[]) return DType.INT16;
        if (data instanceof int[]) return DType.INT32;
        if (data instanceof long[]) return DType.INT64;
        if (data instanceof float[]) return DType.FLOAT32;
        if (data instanceof double[]) return DType.FLOAT64;
        if (data instanceof boolean[]) return DType.BOOL;
        if (data instanceof String[]) return DType.STRING;
        if (data instanceof Object[]) return DType.OBJECT;
        return DType.OBJECT;
    }
}
