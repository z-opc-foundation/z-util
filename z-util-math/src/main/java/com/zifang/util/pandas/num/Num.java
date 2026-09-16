package com.zifang.util.pandas.num;

import java.util.Arrays;

/**
 * Num 类 - Java 版本的 numpy ndarray
 * 提供多维数组的创建、操作和计算功能
 */
public class Num {

    private Object array;
    private DType dType;
    private int[] shape;
    private int size;

    /**
     * 构造方法，从数组对象创建 Num 实例
     *
     * @param array 数组对象，支持 double[] 或 double[][]
     * @throws RuntimeException 如果传入参数不是数组类型
     */
    public Num(Object array) {
        if (!array.getClass().isArray()) {
            throw new RuntimeException("param is not an array");
        }
        this.array = array;
        this.dType = inferDType(array);
        this.shape = computeShape();
        this.size = computeSize();
    }

    /**
     * 创建指定形状的全零数组，默认数据类型为 FLOAT64
     *
     * @param shape 数组形状，如 new int[]{3, 4} 表示 3x4 矩阵
     * @return 全零 Num 实例
     */
    public static Num zeros(int... shape) {
        return zeros(shape, DType.FLOAT64);
    }

    /**
     * 创建指定形状和数据类型的全零数组
     *
     * @param shape 数组形状
     * @param dtype 数据类型
     * @return 全零 Num 实例
     */
    public static Num zeros(int[] shape, DType dtype) {
        Object array = createArray(shape, dtype);
        return new Num(array);
    }

    /**
     * 创建指定形状的全一数组
     *
     * @param shape 数组形状
     * @return 全一 Num 实例
     */
    public static Num ones(int... shape) {
        return fill(shape, 1.0);
    }

    /**
     * 创建指定形状并用给定值填充的数组
     *
     * @param shape 数组形状
     * @param value 填充值
     * @return 填充后的 Num 实例
     */
    public static Num fill(int[] shape, double value) {
        Object array = createFilledArray(shape, value);
        return new Num(array);
    }

    /**
     * 生成指定范围的数组，类似于 numpy.arange()
     *
     * @param start 起始值（包含）
     * @param stop  结束值（不包含）
     * @param step  步长
     * @return 生成的 Num 实例
     */
    public static Num arange(double start, double stop, double step) {
        int size = (int) Math.ceil((stop - start) / step);
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = start + i * step;
        }
        return new Num(array);
    }

    /**
     * 生成从 0 到 stop 的数组，类似于 numpy.arange(stop)
     *
     * @param stop 结束值（不包含）
     * @return 生成的 Num 实例
     */
    public static Num arange(int stop) {
        return arange(0, stop, 1);
    }

    /**
     * 生成指定数量的等间隔数组，类似于 numpy.linspace()
     *
     * @param start 起始值
     * @param stop  结束值
     * @param num   样本数量
     * @return 生成的 Num 实例
     */
    public static Num linspace(double start, double stop, int num) {
        return linspace(start, stop, num, true);
    }

    /**
     * 生成指定数量的等间隔数组
     *
     * @param start    起始值
     * @param stop     结束值
     * @param num      样本数量
     * @param endpoint 是否包含结束值
     * @return 生成的 Num 实例
     */
    public static Num linspace(double start, double stop, int num, boolean endpoint) {
        double step = endpoint ? (stop - start) / (num - 1) : (stop - start) / num;
        double[] array = new double[num];
        for (int i = 0; i < num; i++) {
            array[i] = start + i * step;
        }
        return new Num(array);
    }

    /**
     * 创建单位矩阵，类似于 numpy.eye()
     *
     * @param i 矩阵维度
     * @return 单位矩阵
     * @throws RuntimeException 暂未实现
     */
    public static Num eye(int i) {
        throw new RuntimeException();
    }

    // ==================== 算术运算 ====================

    private static Object createArray(int[] shape, DType dtype) {
        if (shape.length == 1) {
            return new double[shape[0]];
        } else if (shape.length == 2) {
            return new double[shape[0]][shape[1]];
        }
        throw new UnsupportedOperationException("Arrays with dimension > 2 not yet fully supported");
    }

    private static Object createFilledArray(int[] shape, double value) {
        Object array = createArray(shape, DType.FLOAT64);
        fillArray(array, value);
        return array;
    }

    private static void fillArray(Object array, double value) {
        if (array instanceof double[]) {
            double[] arr = (double[]) array;
            java.util.Arrays.fill(arr, value);
        } else if (array instanceof double[][]) {
            double[][] arr = (double[][]) array;
            for (double[] row : arr) {
                java.util.Arrays.fill(row, value);
            }
        }
    }

    /**
     * 元素级加法，类似于 numpy.add()
     *
     * @param other 另一个 Num 实例
     * @return 相加结果
     */
    public Num add(Num other) {
        return elementWiseOp(other, (a, b) -> a + b);
    }

    /**
     * 标量加法，将数组每个元素加上标量值
     *
     * @param scalar 标量值
     * @return 相加结果
     */
    public Num add(double scalar) {
        return elementWiseOp(scalar, (a, b) -> a + b);
    }

    /**
     * 元素级减法，类似于 numpy.subtract()
     *
     * @param other 另一个 Num 实例
     * @return 相减结果
     */
    public Num subtract(Num other) {
        return elementWiseOp(other, (a, b) -> a - b);
    }

    /**
     * 标量减法，将数组每个元素减去标量值
     *
     * @param scalar 标量值
     * @return 相减结果
     */
    public Num subtract(double scalar) {
        return elementWiseOp(scalar, (a, b) -> a - b);
    }

    /**
     * 元素级乘法，类似于 numpy.multiply()
     *
     * @param other 另一个 Num 实例
     * @return 相乘结果
     */
    public Num multiply(Num other) {
        return elementWiseOp(other, (a, b) -> a * b);
    }

    /**
     * 标量乘法，将数组每个元素乘以标量值
     *
     * @param scalar 标量值
     * @return 相乘结果
     */
    public Num multiply(double scalar) {
        return elementWiseOp(scalar, (a, b) -> a * b);
    }

    // ==================== 统计方法 ====================

    /**
     * 元素级除法，类似于 numpy.divide()
     *
     * @param other 另一个 Num 实例
     * @return 相除结果
     */
    public Num divide(Num other) {
        return elementWiseOp(other, (a, b) -> a / b);
    }

    /**
     * 标量除法，将数组每个元素除以标量值
     *
     * @param scalar 标量值
     * @return 相除结果
     */
    public Num divide(double scalar) {
        return elementWiseOp(scalar, (a, b) -> a / b);
    }

    /**
     * 幂运算，类似于 numpy.pow()
     *
     * @param exponent 指数
     * @return 幂运算结果
     */
    public Num pow(double exponent) {
        return elementWiseOp(exponent, Math::pow);
    }

    /**
     * 计算所有元素的总和，类似于 numpy.sum()
     *
     * @return 所有元素的总和
     */
    public double sum() {
        return reduce((a, b) -> a + b, 0.0);
    }

    /**
     * 计算所有元素的平均值，类似于 numpy.mean()
     *
     * @return 平均值
     */
    public double mean() {
        return sum() / size;
    }

    /**
     * 计算所有元素的最大值，类似于 numpy.max()
     *
     * @return 最大值
     */
    public double max() {
        return reduce((a, b) -> Math.max(a, b), Double.NEGATIVE_INFINITY);
    }

    // ==================== 数组操作 ====================

    /**
     * 计算所有元素的最小值，类似于 numpy.min()
     *
     * @return 最小值
     */
    public double min() {
        return reduce((a, b) -> Math.min(a, b), Double.POSITIVE_INFINITY);
    }

    /**
     * 计算所有元素的标准差，类似于 numpy.std()
     *
     * @return 标准差
     */
    public double std() {
        return Math.sqrt(var());
    }

    /**
     * 计算所有元素的方差，类似于 numpy.var()
     *
     * @return 方差
     */
    public double var() {
        double mean = mean();
        return reduce((acc, val) -> acc + Math.pow(val - mean, 2), 0.0) / size;
    }

    // ==================== 数学函数 ====================

    /**
     * 重新调整数组形状，类似于 numpy.reshape()
     *
     * @param newShape 新的形状
     * @return 形状改变后的 Num 实例
     * @throws IllegalArgumentException 如果新形状的元素总数与原数组不匹配
     */
    public Num reshape(int... newShape) {
        int newSize = 1;
        for (int dim : newShape) {
            newSize *= dim;
        }
        if (newSize != size) {
            throw new IllegalArgumentException("Cannot reshape array of size " + size + " into shape " + Arrays.toString(newShape));
        }
        Num result = new Num(copyArray());
        result.shape = newShape.clone();
        return result;
    }

    /**
     * 计算矩阵转置，类似于 numpy.transpose()
     *
     * @return 转置后的 Num 实例（仅支持 2D 数组）
     * @throws UnsupportedOperationException 维度大于 2 时抛出
     */
    public Num transpose() {
        if (shape.length < 2) {
            return this;
        }
        // 简化的转置实现（仅支持2D）
        if (shape.length == 2) {
            int rows = shape[0];
            int cols = shape[1];
            double[][] transposed = new double[cols][rows];
            double[][] original = (double[][]) array;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    transposed[j][i] = original[i][j];
                }
            }
            Num result = new Num(transposed);
            result.shape = new int[]{cols, rows};
            return result;
        }
        throw new UnsupportedOperationException("Transpose for dimensions > 2 not yet implemented");
    }

    /**
     * 将多维数组展平为一维数组，类似于 numpy.flatten()
     *
     * @return 展平后的 Num 实例
     */
    public Num flatten() {
        return reshape(size);
    }

    /**
     * 计算正弦值，类似于 numpy.sin()
     *
     * @return 正弦值数组
     */
    public Num sin() {
        return apply(Math::sin);
    }

    /**
     * 计算余弦值，类似于 numpy.cos()
     *
     * @return 余弦值数组
     */
    public Num cos() {
        return apply(Math::cos);
    }

    /**
     * 计算正切值，类似于 numpy.tan()
     *
     * @return 正切值数组
     */
    public Num tan() {
        return apply(Math::tan);
    }

    /**
     * 计算指数函数值，类似于 numpy.exp()
     *
     * @return e^x 值数组
     */
    public Num exp() {
        return apply(Math::exp);
    }

    /**
     * 计算自然对数，类似于 numpy.log()
     *
     * @return ln(x) 值数组
     */
    public Num log() {
        return apply(Math::log);
    }

    // ==================== 工具方法 ====================

    /**
     * 计算以 10 为底的对数，类似于 numpy.log10()
     *
     * @return log10(x) 值数组
     */
    public Num log10() {
        return apply(Math::log10);
    }

    /**
     * 计算平方根，类似于 numpy.sqrt()
     *
     * @return sqrt(x) 值数组
     */
    public Num sqrt() {
        return apply(Math::sqrt);
    }

    /**
     * 计算绝对值，类似于 numpy.abs()
     *
     * @return 绝对值数组
     */
    public Num abs() {
        return apply(Math::abs);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Num(shape=" + Arrays.toString(shape) + ", dtype=" + dType + ", data=" + Arrays.deepToString((Object[]) array) + ")";
    }

    /**
     * 获取数组形状
     *
     * @return 形状数组的副本
     */
    public int[] shape() {
        return shape.clone();
    }

    /**
     * 获取数组维度数量
     *
     * @return 维度数量
     */
    public int nDim() {
        return shape.length;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取数组元素总数
     *
     * @return 元素总数
     */
    public int size() {
        return size;
    }

    /**
     * 获取数组数据类型
     *
     * @return 数据类型
     */
    public DType dtype() {
        return dType;
    }

    /**
     * 获取底层数组数据
     *
     * @return 数组数据对象
     */
    public Object data() {
        return array;
    }

    private DType inferDType(Object array) {
        String name = array.getClass().getName();
        if (name.contains("D") || name.contains("F")) {
            return DType.FLOAT64;
        }
        return DType.FLOAT64; // 默认使用 float64
    }

    private int[] computeShape() {
        java.util.List<Integer> dims = new java.util.ArrayList<>();
        Object current = array;
        while (current.getClass().isArray()) {
            dims.add(java.lang.reflect.Array.getLength(current));
            if (java.lang.reflect.Array.getLength(current) > 0) {
                current = java.lang.reflect.Array.get(current, 0);
            } else {
                break;
            }
        }
        int[] result = new int[dims.size()];
        for (int i = 0; i < dims.size(); i++) {
            result[i] = dims.get(i);
        }
        return result;
    }

    private int computeSize() {
        if (shape.length == 0) return 0;
        int total = 1;
        for (int dim : shape) {
            total *= dim;
        }
        return total;
    }

    private Object copyArray() {
        if (array instanceof double[]) {
            return ((double[]) array).clone();
        } else if (array instanceof double[][]) {
            double[][] original = (double[][]) array;
            double[][] copy = new double[original.length][];
            for (int i = 0; i < original.length; i++) {
                copy[i] = original[i].clone();
            }
            return copy;
        }
        return array;
    }

    private Num elementWiseOp(Num other, java.util.function.DoubleBinaryOperator op) {
        // 简化的实现，假设两个数组形状相同
        if (array instanceof double[] && other.array instanceof double[]) {
            double[] a = (double[]) array;
            double[] b = (double[]) other.array;
            double[] result = new double[a.length];
            for (int i = 0; i < a.length; i++) {
                result[i] = op.applyAsDouble(a[i], b[i]);
            }
            return new Num(result);
        }
        throw new UnsupportedOperationException("Element-wise operations for this shape not yet implemented");
    }

    private Num elementWiseOp(double scalar, java.util.function.DoubleBinaryOperator op) {
        if (array instanceof double[]) {
            double[] a = (double[]) array;
            double[] result = new double[a.length];
            for (int i = 0; i < a.length; i++) {
                result[i] = op.applyAsDouble(a[i], scalar);
            }
            return new Num(result);
        } else if (array instanceof double[][]) {
            double[][] a = (double[][]) array;
            double[][] result = new double[a.length][];
            for (int i = 0; i < a.length; i++) {
                result[i] = new double[a[i].length];
                for (int j = 0; j < a[i].length; j++) {
                    result[i][j] = op.applyAsDouble(a[i][j], scalar);
                }
            }
            return new Num(result);
        }
        throw new UnsupportedOperationException("Element-wise scalar operations for this shape not yet implemented");
    }

    private double reduce(java.util.function.DoubleBinaryOperator op, double identity) {
        if (array instanceof double[]) {
            double[] a = (double[]) array;
            double result = identity;
            for (double v : a) {
                result = op.applyAsDouble(result, v);
            }
            return result;
        } else if (array instanceof double[][]) {
            double[][] a = (double[][]) array;
            double result = identity;
            for (double[] row : a) {
                for (double v : row) {
                    result = op.applyAsDouble(result, v);
                }
            }
            return result;
        }
        throw new UnsupportedOperationException("Reduction operations for this shape not yet implemented");
    }

    /**
     * apply方法。
     * * @param op java.util.function.DoubleUnaryOperator类型参数
     *
     * @return Num类型返回值
     */
    public Num apply(java.util.function.DoubleUnaryOperator op) {
        if (array instanceof double[]) {
            double[] a = (double[]) array;
            double[] result = new double[a.length];
            for (int i = 0; i < a.length; i++) {
                result[i] = op.applyAsDouble(a[i]);
            }
            return new Num(result);
        } else if (array instanceof double[][]) {
            double[][] a = (double[][]) array;
            double[][] result = new double[a.length][];
            for (int i = 0; i < a.length; i++) {
                result[i] = new double[a[i].length];
                for (int j = 0; j < a[i].length; j++) {
                    result[i][j] = op.applyAsDouble(a[i][j]);
                }
            }
            return new Num(result);
        }
        throw new UnsupportedOperationException("Apply operations for this shape not yet implemented");
    }
}
