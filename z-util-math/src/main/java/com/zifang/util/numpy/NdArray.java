package com.zifang.util.numpy;

import java.util.Arrays;

/**
 * N-dimensional array implementation
 */
public class NdArray {
    private final DType dtype;
    private final Shape shape;
    private Object data;

    private NdArray(Object data, DType dtype, Shape shape) {
        this.data = data;
        this.dtype = dtype;
        this.shape = shape;
    }

    /**
     * create方法。
     * * @param data Object类型参数
     *
     * @param dtype DType类型参数
     * @param shape Shape类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray create(Object data, DType dtype, Shape shape) {
        return new NdArray(data, dtype, shape);
    }

    /**
     * zeros方法。
     * * @param shape Shape类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray zeros(Shape shape, DType dtype) {
        int size = shape.size();
        Object data = Array.createZeroArray(dtype, size);
        return new NdArray(data, dtype, shape);
    }

    /**
     * ones方法。
     * * @param shape Shape类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray ones(Shape shape, DType dtype) {
        int size = shape.size();
        Object data = Array.createOneArray(dtype, size);
        return new NdArray(data, dtype, shape);
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
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be zero");
        }
        int size = (int) Math.ceil((double) (stop - start) / step);
        Object data = Array.arange(dtype, start, stop, step, size);
        return new NdArray(data, dtype, new Shape(size));
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
        return arange(start, stop, 1, dtype);
    }

    /**
     * arange方法。
     * * @param stop int类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray arange(int stop, DType dtype) {
        return arange(0, stop, 1, dtype);
    }

    /**
     * array方法。
     * * @param data Object类型参数
     *
     * @param dtype DType类型参数
     * @return static NdArray类型返回值
     */
    public static NdArray array(Object data, DType dtype) {
        if (data instanceof Object[]) {
            Object[] arr = (Object[]) data;
            return new NdArray(arr, dtype, new Shape(arr.length));
        }
        if (data instanceof byte[]) {
            byte[] arr = (byte[]) data;
            return new NdArray(arr, dtype, new Shape(arr.length));
        }
        if (data instanceof short[]) {
            short[] arr = (short[]) data;
            return new NdArray(arr, dtype, new Shape(arr.length));
        }
        if (data instanceof int[]) {
            int[] arr = (int[]) data;
            return new NdArray(arr, dtype, new Shape(arr.length));
        }
        if (data instanceof long[]) {
            long[] arr = (long[]) data;
            return new NdArray(arr, dtype, new Shape(arr.length));
        }
        if (data instanceof float[]) {
            float[] arr = (float[]) data;
            return new NdArray(arr, dtype, new Shape(arr.length));
        }
        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            return new NdArray(arr, dtype, new Shape(arr.length));
        }
        if (data instanceof boolean[]) {
            boolean[] arr = (boolean[]) data;
            return new NdArray(arr, dtype, new Shape(arr.length));
        }
        throw new IllegalArgumentException("Unsupported array type: " + data.getClass());
    }

    /**
     * getData方法。
     *
     * @return Object类型返回值
     */
    public Object getData() {
        return data;
    }

    /**
     * getDtype方法。
     *
     * @return DType类型返回值
     */
    public DType getDtype() {
        return dtype;
    }

    /**
     * getShape方法。
     *
     * @return Shape类型返回值
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * size方法。
     *
     * @return int类型返回值
     */
    public int size() {
        return shape.size();
    }

    /**
     * ndim方法。
     *
     * @return int类型返回值
     */
    public int ndim() {
        return shape.ndim();
    }

    /**
     * get方法。
     * * @param indices int...类型参数
     *
     * @return Object类型返回值
     */
    public Object get(int... indices) {
        if (indices.length != shape.ndim()) {
            throw new IllegalArgumentException("Number of indices must match dimensions");
        }
        int flatIndex = calculateFlatIndex(indices);
        return Array.get(data, flatIndex);
    }

    /**
     * set方法。
     * * @param value Object类型参数
     *
     * @param indices int...类型参数
     */
    public void set(Object value, int... indices) {
        if (indices.length != shape.ndim()) {
            throw new IllegalArgumentException("Number of indices must match dimensions");
        }
        int flatIndex = calculateFlatIndex(indices);
        Array.set(data, flatIndex, value);
    }

    private int calculateFlatIndex(int... indices) {
        int flatIndex = 0;
        int multiplier = 1;
        for (int i = shape.ndim() - 1; i >= 0; i--) {
            flatIndex += indices[i] * multiplier;
            multiplier *= shape.get(i);
        }
        return flatIndex;
    }

    /**
     * reshape方法。
     * * @param newShape int...类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray reshape(int... newShape) {
        int size = shape.size();
        int expectedSize = 1;
        for (int dim : newShape) {
            expectedSize *= dim;
        }
        if (size != expectedSize) {
            throw new IllegalArgumentException("Total size must remain the same: " + size + " vs " + expectedSize);
        }
        return new NdArray(data, dtype, new Shape(newShape));
    }

    /**
     * transpose方法。
     *
     * @return NdArray类型返回值
     */
    public NdArray transpose() {
        if (shape.ndim() < 2) {
            return this;
        }
        int ndim = shape.ndim();
        int[] perm = new int[ndim];
        for (int i = 0; i < ndim; i++) {
            perm[i] = ndim - 1 - i;
        }
        return transpose(perm);
    }

    /**
     * transpose方法。
     * * @param axes int...类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray transpose(int... axes) {
        if (axes.length != shape.ndim()) {
            throw new IllegalArgumentException("Number of axes must match dimensions");
        }
        int[] newShape = new int[axes.length];
        for (int i = 0; i < axes.length; i++) {
            newShape[i] = shape.get(axes[i]);
        }
        Object transposedData = Array.transpose(data, shape.getDimensions(), axes);
        return new NdArray(transposedData, dtype, new Shape(newShape));
    }

    /**
     * slice方法。
     * * @param slices Slice...类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray slice(Slice... slices) {
        if (slices.length > shape.ndim()) {
            throw new IllegalArgumentException("Too many slices");
        }
        Slice[] fullSlices = new Slice[shape.ndim()];
        int sliceIdx = 0;
        for (int i = 0; i < shape.ndim(); i++) {
            if (sliceIdx < slices.length) {
                fullSlices[i] = slices[sliceIdx++];
            } else {
                fullSlices[i] = Slice.all();
            }
        }
        return sliceInternal(fullSlices);
    }

    private NdArray sliceInternal(Slice[] slices) {
        int[] newShape = new int[shape.ndim()];
        int[][] allIndices = new int[shape.ndim()][];
        int totalSize = 1;

        for (int i = 0; i < shape.ndim(); i++) {
            allIndices[i] = slices[i].resolve(shape.get(i));
            newShape[i] = allIndices[i].length;
            totalSize *= newShape[i];
        }

        Object newData = Array.createZeroArray(dtype, totalSize);
        int[] idx = new int[shape.ndim()];
        int[] dstIdx = new int[]{0};
        sliceRecursive(data, newData, allIndices, idx, 0, dstIdx);
        return new NdArray(newData, dtype, new Shape(newShape));
    }

    private void sliceRecursive(Object src, Object dst, int[][] indices, int[] idx, int dim, int[] dstIdx) {
        if (dim == indices.length) {
            int flatSrcIdx = calculateFlatIndex(idx);
            Array.copy(src, flatSrcIdx, dst, dstIdx[0]++);
            return;
        }
        for (int i = 0; i < indices[dim].length; i++) {
            idx[dim] = indices[dim][i];
            sliceRecursive(src, dst, indices, idx, dim + 1, dstIdx);
        }
    }

    /**
     * copy方法。
     *
     * @return NdArray类型返回值
     */
    public NdArray copy() {
        Object newData = Array.copy(data, shape.size(), dtype);
        return new NdArray(newData, dtype, shape);
    }

    /**
     * fill方法。
     * * @param value Object类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray fill(Object value) {
        Array.fill(data, value, shape.size());
        return this;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NdArray(shape=").append(shape);
        sb.append(", dtype=").append(dtype);
        sb.append(", data=");
        sb.append(Array.toString(data, shape.size()));
        sb.append(")");
        return sb.toString();
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NdArray ndArray = (NdArray) o;
        if (!shape.equals(ndArray.shape) || dtype != ndArray.dtype) return false;
        return Array.equals(data, ndArray.data, shape.size());
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        int result = Arrays.hashCode(shape.getDimensions());
        result = 31 * result + dtype.hashCode();
        result = 31 * result + Array.hashCode(data, shape.size());
        return result;
    }
}
