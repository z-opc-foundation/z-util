package com.zifang.util.numpy;

import java.util.Arrays;

/**
 * Represents the shape of an NdArray
 */
public class Shape {
    private final int[] dimensions;

    /**
     * Shape方法。
     * * @param dimensions int...类型参数
     */
    public Shape(int... dimensions) {
        if (dimensions == null || dimensions.length == 0) {
            this.dimensions = new int[0];
        } else {
            this.dimensions = Arrays.copyOf(dimensions, dimensions.length);
        }
    }

    /**
     * Shape方法。
     * * @param dimensions long...类型参数
     */
    public Shape(long... dimensions) {
        if (dimensions == null || dimensions.length == 0) {
            this.dimensions = new int[0];
        } else {
            this.dimensions = new int[dimensions.length];
            for (int i = 0; i < dimensions.length; i++) {
                this.dimensions[i] = (int) dimensions[i];
            }
        }
    }

    /**
     * getDimensions方法。
     *
     * @return int[]类型返回值
     */
    public int[] getDimensions() {
        return Arrays.copyOf(dimensions, dimensions.length);
    }

    /**
     * ndim方法。
     *
     * @return int类型返回值
     */
    public int ndim() {
        return dimensions.length;
    }

    /**
     * size方法。
     *
     * @return int类型返回值
     */
    public int size() {
        int result = 1;
        for (int dim : dimensions) {
            result *= dim;
        }
        return result;
    }

    /**
     * get方法。
     * * @param index int类型参数
     *
     * @return int类型返回值
     */
    public int get(int index) {
        if (index < 0 || index >= dimensions.length) {
            throw new IndexOutOfBoundsException("Dimension index out of bounds: " + index);
        }
        return dimensions[index];
    }

    /**
     * reshape方法。
     * * @param newShape int...类型参数
     *
     * @return Shape类型返回值
     */
    public Shape reshape(int... newShape) {
        return new Shape(newShape);
    }

    /**
     * transpose方法。
     *
     * @return Shape类型返回值
     */
    public Shape transpose() {
        int[] transposed = new int[dimensions.length];
        for (int i = 0; i < dimensions.length; i++) {
            transposed[i] = dimensions[dimensions.length - 1 - i];
        }
        return new Shape(transposed);
    }

    /**
     * isScalar方法。
     *
     * @return boolean类型返回值
     */
    public boolean isScalar() {
        return dimensions.length == 0;
    }

    /**
     * isVector方法。
     *
     * @return boolean类型返回值
     */
    public boolean isVector() {
        return dimensions.length == 1;
    }

    /**
     * isMatrix方法。
     *
     * @return boolean类型返回值
     */
    public boolean isMatrix() {
        return dimensions.length == 2;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        if (dimensions.length == 0) {
            return "()";
        }
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < dimensions.length; i++) {
            sb.append(dimensions[i]);
            if (i < dimensions.length - 1) {
                sb.append(", ");
            }
        }
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
        Shape shape = (Shape) o;
        return Arrays.equals(dimensions, shape.dimensions);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Arrays.hashCode(dimensions);
    }
}
