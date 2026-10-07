package com.zifang.util.pandas.matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * MatrixGenerator 类 - 矩阵构造器
 * <p>
 * 提供多种方式创建矩阵，类似于 numpy 矩阵生成函数。
 * 该类主要用于以编程方式生成各种类型和形状的矩阵对象。
 *
 * <p>主要功能：
 * <ul>
 *   <li>从数组或列表创建矩阵</li>
 *   <li>生成指定维度的特殊矩阵（全零、全一、单位阵等）</li>
 *   <li>生成等差数列矩阵</li>
 *   <li>生成随机矩阵</li>
 * </ul>
 *
 * <p>对标 numpy 函数：
 * <ul>
 *   <li>numpy.array() - 从数据创建数组</li>
 *   <li>numpy.zeros() - 生成全零矩阵</li>
 *   <li>numpy.ones() - 生成全一矩阵</li>
 *   <li>numpy.arange() - 生成等差数列</li>
 *   <li>numpy.eye() - 生成单位矩阵</li>
 *   <li>numpy.random.rand() - 生成随机矩阵</li>
 * </ul>
 *
 * <p>⚠️ {@link Matrix} 的元素一律用 {@code Double} 存储，所以本类的构造入口
 * 也只接受数值：字符串元素会在 {@link #array(List)} 里被明确拒绝，
 * 而不是悄悄变成 {@code null} 或 0。
 *
 * @author zifang
 * @see Matrix
 * @see com.zifang.util.pandas.num.Num
 */
public class MatrixGenerator {


    /**
     * 生成指定维度的全零方阵，类似于 numpy.zeros(dimension)
     * <p>
     * 示例：dimension=3 返回
     * <pre>
     * [ 0 0 0
     *  0 0 0
     *  0 0 0 ]
     * </pre>
     *
     * @param dimension 方阵边长
     * @return 全零方阵
     * @throws IllegalArgumentException 维度为负
     */
    public static Matrix zeros(Integer dimension) {
        return zeros(dimension, dimension);
    }

    /**
     * 生成指定形状的全零矩阵，类似于 numpy.zeros((rows, cols))
     *
     * @param rows 行数
     * @param cols 列数
     * @return 全零矩阵
     * @throws IllegalArgumentException 任一维度为负
     */
    public static Matrix zeros(Integer rows, Integer cols) {
        requireNonNegative(rows, "行数");
        requireNonNegative(cols, "列数");
        Matrix matrix = new Matrix();
        for (int i = 0; i < rows; i++) {
            Double[] row = new Double[cols];
            for (int j = 0; j < cols; j++) {
                row[j] = 0.0;
            }
            matrix.set(row);
        }
        return matrix;
    }

    /**
     * 生成指定形状的全一矩阵，类似于 numpy.ones(shape)
     *
     * @param rows 行数
     * @param cols 列数
     * @return 全一矩阵
     * @throws IllegalArgumentException 任一维度为负
     */
    public static Matrix ones(Integer rows, Integer cols) {
        requireNonNegative(rows, "行数");
        requireNonNegative(cols, "列数");
        Matrix matrix = new Matrix();
        for (int i = 0; i < rows; i++) {
            Double[] row = new Double[cols];
            for (int j = 0; j < cols; j++) {
                row[j] = 1.0;
            }
            matrix.set(row);
        }
        return matrix;
    }

    /**
     * 生成单位方阵，类似于 numpy.eye(dimension)
     *
     * @param dimension 方阵边长
     * @return 主对角线为 1、其余为 0 的矩阵
     * @throws IllegalArgumentException 维度为负
     */
    public static Matrix eye(Integer dimension) {
        requireNonNegative(dimension, "维度");
        Matrix matrix = new Matrix();
        for (int i = 0; i < dimension; i++) {
            Double[] row = new Double[dimension];
            for (int j = 0; j < dimension; j++) {
                row[j] = (i == j) ? 1.0 : 0.0;
            }
            matrix.set(row);
        }
        return matrix;
    }

    /**
     * 生成指定形状的空矩阵，每个格子都是未初始化的 {@code null}，类似于 numpy.empty()
     * <p>
     * ⚠️ 签名从「无参 void」改成「接收形状、返回 Matrix」：
     * 原无参版本不可能实现 —— 不给形状就没有矩阵可造。
     * <p>
     * 传空参则得到 0 行的空矩阵。
     *
     * @param shape 形状，最多两维，如 {@code empty(2, 3)}
     * @return 形状为 {@code shape}、元素全为 null 的矩阵
     * @throws IllegalArgumentException 维度为负或超过两维
     */
    public static Matrix empty(Integer... shape) {
        if (shape == null || shape.length == 0) {
            return new Matrix();
        }
        if (shape.length == 1) {
            requireNonNegative(shape[0], "列数");
            Matrix matrix = new Matrix();
            matrix.set(newRow(shape[0]));
            return matrix;
        }
        if (shape.length == 2) {
            requireNonNegative(shape[0], "行数");
            requireNonNegative(shape[1], "列数");
            Matrix matrix = new Matrix();
            for (int i = 0; i < shape[0]; i++) {
                matrix.set(newRow(shape[1]));
            }
            return matrix;
        }
        throw new IllegalArgumentException("empty 只支持两维及以下，收到 " + shape.length + " 维");
    }

    /**
     * 生成等差数列矩阵，类似于 numpy.arange()
     * <p>
     * 按 numpy 语义返回<b>一行</b>：
     * <ul>
     *   <li>{@code arrange(stop)} → 0, 1, ..., stop-1</li>
     *   <li>{@code arrange(start, stop)} → start, start+1, ..., &lt; stop</li>
     *   <li>{@code arrange(start, stop, step)} → 按给定步长</li>
     * </ul>
     *
     * ⚠️ 签名从「无参 void / 两参 void」合并成可变参数：
     * 原来的无参版本不可能实现（没有区间就没有数列），
     * 两参版本返回 void 也没法把结果交出去。
     *
     * @param args 1~3 个参数：结束值 / (开始, 结束) / (开始, 结束, 步长)
     * @return 含等差数列的一行矩阵
     * @throws IllegalArgumentException 参数个数不在 1~3 之间、步长为 0 或区间为空
     */
    public static Matrix arrange(Integer... args) {
        if (args == null || args.length < 1 || args.length > 3) {
            throw new IllegalArgumentException("arrange 需要 1~3 个参数，实际 "
                    + (args == null ? 0 : args.length) + " 个");
        }
        int start = args.length == 1 ? 0 : args[0];
        int stop = args.length == 1 ? args[0] : args[1];
        int step = args.length == 3 ? args[2] : 1;
        if (step == 0) {
            throw new IllegalArgumentException("步长不能为 0");
        }
        if ((stop - start) * (long) step < 0) {
            throw new IllegalArgumentException("步长方向与区间不一致：start=" + start
                    + ", stop=" + stop + ", step=" + step);
        }

        List<Double> values = new ArrayList<>();
        for (int v = start; step > 0 ? v < stop : v > stop; v += step) {
            values.add((double) v);
        }
        Matrix matrix = new Matrix();
        matrix.set(values.toArray(new Double[0]));
        return matrix;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // Matrix 的元素是 Double，所以这里给数值而不是 "a"、"n"
        List<Double> numbers = new ArrayList<Double>();
        numbers.add(1.0);
        numbers.add(2.0);
        MatrixGenerator numpy = new MatrixGenerator();
        numpy.array(numbers).format();
    }

//    public  NumpyArray array(E[] arrys,String type){
//        List<? extends Object> list =  Arrays.asList(arrys);
//        NumpyArray numpyArray = new NumpyArray();
//        numpyArray.setArray(list);
//        return numpyArray;
//    }

    /**
     * 从 List 创建矩阵
     * <p>
     * 元素是 {@link List} 时按行处理（二维），否则整份当作一行（一维）。
     *
     * @param list 包含矩阵元素的列表，元素必须是 {@link Number}
     * @return Matrix 实例；空列表得到 0 行的空矩阵
     * @throws IllegalArgumentException 元素里有非数字
     */
    public Matrix array(List<? extends Object> list) {
        Matrix matrix = new Matrix();
        if (list == null || list.isEmpty()) {
            return matrix;
        }
        if (list.get(0) instanceof List) {
            for (Object rowObj : list) {
                if (!(rowObj instanceof List)) {
                    throw new IllegalArgumentException("首元素是 List，其余元素也必须是 List，遇到: " + rowObj);
                }
                matrix.set(toDoubles((List<?>) rowObj));
            }
            return matrix;
        }
        matrix.set(toDoubles(list));
        return matrix;
    }

    /**
     * 从 double 数组创建矩阵，得到一行
     *
     * @param arrys double 类型数组
     * @return Matrix 实例
     */
    public Matrix array(double[] arrys) {
        Matrix matrix = new Matrix();
        if (arrys == null) {
            return matrix;
        }
        Double[] row = new Double[arrys.length];
        for (int i = 0; i < arrys.length; i++) {
            row[i] = arrys[i];
        }
        matrix.set(row);
        return matrix;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 把 List 里的数字转成 Double 数组，非数字直接报错
     */
    private static Double[] toDoubles(List<?> list) {
        Double[] row = new Double[list.size()];
        for (int i = 0; i < row.length; i++) {
            Object o = list.get(i);
            if (!(o instanceof Number)) {
                throw new IllegalArgumentException("Matrix 只存数值，第 " + i
                        + " 个元素不是数字: " + o + "（类型 " + (o == null ? "null" : o.getClass().getName()) + "）");
            }
            row[i] = ((Number) o).doubleValue();
        }
        return row;
    }

    /**
     * 一行全为 null 的数组
     */
    private static Double[] newRow(int cols) {
        return new Double[cols];
    }

    private static void requireNonNegative(Integer value, String what) {
        if (value == null) {
            throw new IllegalArgumentException(what + "不能为 null");
        }
        if (value < 0) {
            throw new IllegalArgumentException(what + "不能为负: " + value);
        }
    }
}