package com.zifang.util.pandas.num;

import java.util.Arrays;
import java.util.List;

/**
 * Nums 类 - numpy 函数库的静态方法封装
 * 提供数组创建和操作的静态方法，类似于 Python numpy 库
 */
public class Nums {

    /**
     * 随机数生成器实例
     */
    public static final NumRandom random = new NumRandom();

    /**
     * 从数组对象创建 Num 实例
     *
     * @param array 数组对象，支持 double[]、int[] 等
     * @return Num 实例
     */
    public static Num array(Object array) {
        return new Num(array);
    }

    /**
     * 从 List 创建 Num 实例
     *
     * @param list 列表对象
     * @return Num 实例
     */
    public static Num array(List<?> list) {
        return new Num(list.toArray());
    }

    /**
     * 按形状创建未初始化的全零数组，类似于 numpy.empty()/numpy.zeros(shape, dtype)
     *
     * @param shape 数组形状
     * @param dType 数据类型（仅影响 dtype 标注）
     * @return 全零 Num 实例
     */
    public static Num array(int[] shape, DType dType) {
        if (dType == null) {
            return Num.zeros(shape);
        }
        return Num.zeros(shape, dType);
    }

    /**
     * 按形状创建数组并用 {@code objs} 的值填充
     *
     * @param shape 数组形状
     * @param objs  值来源，长度必须等于 shape 的元素总数
     * @param dType 数据类型（为 null 时按 FLOAT64 处理）
     * @return 填充后的 Num 实例
     * @throws IllegalArgumentException 元素个数与形状对不上
     */
    public static Num array(int[] shape, Object[] objs, DType dType) {
        if (shape == null || shape.length == 0) {
            throw new IllegalArgumentException("shape 不能为空");
        }
        int expected = 1;
        for (int dim : shape) {
            expected *= dim;
        }
        if (objs == null || objs.length != expected) {
            throw new IllegalArgumentException("元素个数与形状不匹配：形状 " + Arrays.toString(shape)
                    + " 需要 " + expected + " 个，实际给了 "
                    + (objs == null ? 0 : objs.length) + " 个");
        }
        Object array = toArray(shape, objs);
        return new Num(array);
    }

    /**
     * 用 {@code objs} 就地覆盖 {@code num} 的数据
     *
     * @param num  目标 Num 实例
     * @param objs 新的值，长度必须与 num 的元素总数一致
     * @return 同一个 {@code num} 实例（便于链式调用）
     * @throws IllegalArgumentException num 为 null 或元素个数不匹配
     */
    public static Num fill(Num num, Object[] objs) {
        if (num == null) {
            throw new IllegalArgumentException("num 不能为 null");
        }
        int[] shape = num.shape();
        if (objs == null || objs.length != num.size()) {
            throw new IllegalArgumentException("元素个数不匹配：需要 " + num.size()
                    + " 个，实际给了 " + (objs == null ? 0 : objs.length) + " 个");
        }
        // Num 的底层数组是直接持有的（data() 不复制），所以把值写回原数组
        // 才算「就地覆盖」——只 new 一个新的 Num 再返回，调用方的 num 根本没变。
        copyInto(toArray(shape, objs), num.data());
        return num;
    }

    /**
     * 在给定间隔内返回均匀间隔的数值
     * <p>
     * 使用方式：
     * <ul>
     *   <li>aRange(10) 返回 0~9</li>
     *   <li>aRange(10.0) 返回 0.0 ~ 9.0</li>
     *   <li>aRange(5, 15) 返回 5 ~ 14</li>
     *   <li>aRange(5.0, 12.0, 2) 返回 5.0~12.0，步长为2</li>
     * </ul>
     *
     * @param i 可以是单个数字(结束值)或多个参数(开始, 结束, 步长)
     * @return 均匀间隔的数值数组
     * @throws IllegalArgumentException 参数个数不在 1~3 之间，或步长为 0
     */
    public static Num aRange(Object... i) {
        if (i == null || i.length < 1 || i.length > 3) {
            throw new IllegalArgumentException("aRange 需要 1~3 个参数，实际 " + (i == null ? 0 : i.length) + " 个");
        }
        double start;
        double stop;
        double step;
        if (i.length == 1) {
            start = 0;
            stop = toDouble(i[0], "结束值");
            step = 1;
        } else if (i.length == 2) {
            start = toDouble(i[0], "开始值");
            stop = toDouble(i[1], "结束值");
            step = 1;
        } else {
            start = toDouble(i[0], "开始值");
            stop = toDouble(i[1], "结束值");
            step = toDouble(i[2], "步长");
        }
        if (step == 0) {
            throw new IllegalArgumentException("步长不能为 0");
        }
        return Num.arange(start, stop, step);
    }

    // inspace()可以用来返回在间隔[开始，停止]上计算的num个均匀间隔的样本：
    // print(np.linspace(10,15,num=20))

    /**
     * 选定区域，返回均匀间隔的样本数组
     * <p>
     * 类似于 numpy.linspace()
     *
     * @param i        开始数值
     * @param j        停止数值
     * @param num      均匀间隔的样本数量
     * @param endPoint 是否包含最后的值
     * @return 均匀间隔的样本数组
     */
    public static Num linSpace(Number i, Number j, Integer num, Boolean endPoint) {
        if (i == null || j == null || num == null) {
            throw new IllegalArgumentException("开始值、停止值与样本数量都不能为 null");
        }
        return Num.linspace(i.doubleValue(), j.doubleValue(), num, endPoint == null || endPoint);
    }

    //
    // print(np.zeros((3,5),dtype=np.int)) # dtype可以将元素变成整数

    /**
     * 创建数组且用 0 填充
     * <p>
     * 类似于 numpy.zeros()
     *
     * @param shapes 数组形状
     * @param dType  数据类型（可为 null）
     * @return 全零 Num 实例
     */
    public static Num zeros(Integer[] shapes, DType dType) {
        if (shapes == null || shapes.length == 0) {
            throw new IllegalArgumentException("形状不能为空");
        }
        int[] shape = new int[shapes.length];
        for (int i = 0; i < shapes.length; i++) {
            if (shapes[i] == null) {
                throw new IllegalArgumentException("形状第 " + i + " 维不能为 null");
            }
            shape[i] = shapes[i];
        }
        return array(shape, dType);
    }

    // ar2=np.ones(9) # 用1填充

    /**
     * 创建用 1 填充的数组
     * <p>
     * 类似于 numpy.ones()
     * <p>
     * ⚠️ 签名从「无参」改成「接收形状」。原无参版本根本无法实现 ——
     * 没有形状就构造不出数组，只能永远抛异常。
     *
     * @param shape 数组形状，如 {@code ones(3, 4)} 得到 3×4 的全一矩阵
     * @return 全一 Num 实例
     */
    public static Num ones(int... shape) {
        return Num.ones(shape);
    }

    // print(np.eye(5)) # 中间数是1，其他都是0

    /**
     * 创建单位矩阵
     * <p>
     * 类似于 numpy.eye()
     * <p>
     * ⚠️ 签名从「无参」改成「接收边长」，理由同 {@link #ones(int...)}。
     *
     * @param n 单位矩阵边长
     * @return 单位矩阵 Num 实例
     */
    public static Num eye(int n) {
        return Num.eye(n);
    }

    /**
     * 横向连接多个数组，类似于 numpy.hstack()
     * <p>
     * 全部是一维数组时，拼成一个更长的一维数组（与 numpy 一致）；
     * 全部是二维数组时要求行数相同，按列拼接。
     * <p>
     * ⚠️ 签名从「无参实例方法」改成「接收数组的静态方法」：
     * 无参版本没有数据可拼，原本无法实现。
     *
     * @param arrays 待连接的数组
     * @return 连接结果
     * @throws IllegalArgumentException 没有传数组、维度不一致或行数不一致
     */
    public static Num hStack(Num... arrays) {
        Num[] checked = requireSameRank(arrays, "hStack");
        if (checked[0].nDim() == 1) {
            int total = 0;
            for (Num a : checked) {
                total += a.size();
            }
            double[] flat = new double[total];
            int k = 0;
            for (Num a : checked) {
                Object data = a.data();
                for (int i = 0, n = java.lang.reflect.Array.getLength(data); i < n; i++) {
                    flat[k++] = ((Number) java.lang.reflect.Array.get(data, i)).doubleValue();
                }
            }
            return new Num(flat);
        }

        double[][] first = asRows(checked[0]);
        int rowCount = first.length;
        int totalCols = 0;
        for (Num a : checked) {
            double[][] rows = asRows(a);
            if (rows.length != rowCount) {
                throw new IllegalArgumentException("hStack 要求所有数组行数相同，第一个是 " + rowCount
                        + " 行，遇到 " + rows.length + " 行");
            }
            totalCols += rows[0].length;
        }
        double[][] out = new double[rowCount][totalCols];
        for (int r = 0; r < rowCount; r++) {
            int c = 0;
            for (Num a : checked) {
                double[] row = asRows(a)[r];
                System.arraycopy(row, 0, out[r], c, row.length);
                c += row.length;
            }
        }
        return new Num(out);
    }

    /**
     * 纵向连接多个数组，类似于 numpy.vstack()
     * <p>
     * 一维数组会被当作「一行」处理，与 numpy 一致。
     *
     * @param arrays 待连接的数组
     * @return 连接结果
     * @throws IllegalArgumentException 没有传数组、维度不一致或列数不一致
     */
    public static Num vStack(Num... arrays) {
        Num[] checked = requireSameRank(arrays, "vStack");
        int cols = checked[0].nDim() == 1 ? checked[0].size() : asRows(checked[0])[0].length;
        int totalRows = 0;
        for (Num a : checked) {
            int c = a.nDim() == 1 ? a.size() : asRows(a)[0].length;
            if (c != cols) {
                throw new IllegalArgumentException("vStack 要求所有数组列数相同，第一个是 " + cols
                        + " 列，遇到 " + c + " 列");
            }
            totalRows += a.nDim() == 1 ? 1 : a.shape()[0];
        }
        double[][] out = new double[totalRows][];
        int r = 0;
        for (Num a : checked) {
            double[][] rows = asRows(a);
            for (double[] row : rows) {
                out[r++] = row;
            }
        }
        return new Num(out);
    }

    // print(np.hsplit(ar,2)[0])
    // print(np.vsplit(ar,4))

    /**
     * 沿列方向把数组等分成 {@code sections} 份，类似于 numpy.hsplit()
     *
     * @param array    待分割的数组（一维时视为只有一行）
     * @param sections 份数，必须为正且能整除列数
     * @return 分割结果，顺序与原数组一致
     * @throws IllegalArgumentException 份数非法或不能整除
     */
    public static Num[] hsplit(Num array, int sections) {
        double[][] rows = asRows(requireArray(array, "hsplit"));
        int cols = rows[0].length;
        int width = requireDivisible(cols, sections, "hsplit", "列数");
        Num[] parts = new Num[sections];
        for (int s = 0; s < sections; s++) {
            double[][] chunk = new double[rows.length][];
            for (int r = 0; r < rows.length; r++) {
                chunk[r] = new double[width];
                System.arraycopy(rows[r], s * width, chunk[r], 0, width);
            }
            parts[s] = new Num(chunk);
        }
        return parts;
    }

    /**
     * 沿行方向把数组等分成 {@code sections} 份，类似于 numpy.vsplit()
     *
     * @param array    待分割的数组（一维时按元素个数切分，每份仍是等长的一维数组）
     * @param sections 份数，必须为正且能整除元素个数
     * @return 分割结果，顺序与原数组一致
     * @throws IllegalArgumentException 份数非法或不能整除
     */
    public static Num[] vsplit(Num array, int sections) {
        Num target = requireArray(array, "vsplit");
        if (target.nDim() == 1) {
            double[][] rows = asRows(target);
            int width = requireDivisible(rows[0].length, sections, "vsplit", "元素个数");
            Num[] parts = new Num[sections];
            for (int s = 0; s < sections; s++) {
                double[] chunk = new double[width];
                System.arraycopy(rows[0], s * width, chunk, 0, width);
                parts[s] = new Num(chunk);
            }
            return parts;
        }
        double[][] rows = asRows(target);
        int height = requireDivisible(rows.length, sections, "vsplit", "行数");
        Num[] parts = new Num[sections];
        for (int s = 0; s < sections; s++) {
            double[][] chunk = new double[height][];
            for (int r = 0; r < height; r++) {
                chunk[r] = rows[s * height + r].clone();
            }
            parts[s] = new Num(chunk);
        }
        return parts;
    }

    // ==================== 私有辅助方法 ====================

    private static double toDouble(Object o, String what) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        throw new IllegalArgumentException(what + "必须是数字，实际为: " + o);
    }

    /**
     * 按形状把 {@code objs} 摊进一维或二维数组
     */
    private static Object toArray(int[] shape, Object[] objs) {
        if (shape.length == 1) {
            double[] arr = new double[shape[0]];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = requireNumber(objs[i]);
            }
            return arr;
        }
        if (shape.length == 2) {
            double[][] arr = new double[shape[0]][shape[1]];
            int k = 0;
            for (int r = 0; r < shape[0]; r++) {
                for (int c = 0; c < shape[1]; c++) {
                    arr[r][c] = requireNumber(objs[k++]);
                }
            }
            return arr;
        }
        throw new UnsupportedOperationException("只支持一维与二维，收到 " + shape.length + " 维");
    }

    private static double requireNumber(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        throw new IllegalArgumentException("元素必须是数字，实际为: " + o);
    }

    /**
     * 把 {@code source} 的值逐个写回 {@code target}（两者结构必须一致）
     * <p>
     * 用反射按「实际类型」读，而不是强转 {@code double[]} ——
     * {@link Num} 也可能是 {@code int[]} 之类建的，写回时得按它原来的类型存回去。
     */
    private static void copyInto(Object source, Object target) {
        int len = java.lang.reflect.Array.getLength(source);
        for (int i = 0; i < len; i++) {
            Object v = java.lang.reflect.Array.get(source, i);
            Object cell = java.lang.reflect.Array.get(target, i);
            if (cell != null && cell.getClass().isArray()) {
                copyInto(v, cell);
            } else {
                double d = ((Number) v).doubleValue();
                java.lang.reflect.Array.set(target, i, narrow(d, cell == null ? Double.class : cell.getClass()));
            }
        }
    }

    /**
     * 把 double 值收窄回目标数组的分量类型；目标不是数字类型时原样放回
     */
    private static Object narrow(double value, Class<?> componentType) {
        if (componentType == double.class || componentType == Double.class) {
            return value;
        }
        if (componentType == int.class || componentType == Integer.class) {
            return (int) value;
        }
        if (componentType == long.class || componentType == Long.class) {
            return (long) value;
        }
        if (componentType == float.class || componentType == Float.class) {
            return (float) value;
        }
        if (componentType == short.class || componentType == Short.class) {
            return (short) value;
        }
        if (componentType == byte.class || componentType == Byte.class) {
            return (byte) value;
        }
        return value;
    }

    /**
     * 把任意 Num 摊成二维行数组（一维数组视为只有一行），用于栈/分割操作
     */
    private static double[][] asRows(Num num) {
        Object data = num.data();
        int dim = num.nDim();
        if (dim == 1) {
            int n = java.lang.reflect.Array.getLength(data);
            double[] row = new double[n];
            for (int i = 0; i < n; i++) {
                row[i] = ((Number) java.lang.reflect.Array.get(data, i)).doubleValue();
            }
            return new double[][]{row};
        }
        if (dim == 2) {
            int rows = java.lang.reflect.Array.getLength(data);
            double[][] out = new double[rows][];
            for (int r = 0; r < rows; r++) {
                Object rowObj = java.lang.reflect.Array.get(data, r);
                int cols = java.lang.reflect.Array.getLength(rowObj);
                double[] row = new double[cols];
                for (int c = 0; c < cols; c++) {
                    row[c] = ((Number) java.lang.reflect.Array.get(rowObj, c)).doubleValue();
                }
                out[r] = row;
            }
            return out;
        }
        throw new IllegalArgumentException("栈与分割操作只支持一维与二维数组，实际 " + dim + " 维");
    }

    /**
     * 校验：至少一个数组、维度一致、元素非 null
     */
    private static Num[] requireSameRank(Num[] arrays, String op) {
        if (arrays == null || arrays.length == 0) {
            throw new IllegalArgumentException(op + " 至少需要一个数组");
        }
        for (Num a : arrays) {
            if (a == null) {
                throw new IllegalArgumentException(op + " 的数组不能为 null");
            }
        }
        int rank = arrays[0].nDim();
        for (Num a : arrays) {
            if (a.nDim() != rank) {
                throw new IllegalArgumentException(op + " 要求所有数组维度一致，遇到 " + rank + " 维与 "
                        + a.nDim() + " 维混用");
            }
        }
        return arrays;
    }

    private static Num requireArray(Num array, String op) {
        if (array == null) {
            throw new IllegalArgumentException(op + " 的数组不能为 null");
        }
        return array;
    }

    /**
     * 校验份数为正且能整除 total，返回每份的宽度
     */
    private static int requireDivisible(int total, int sections, String op, String what) {
        if (sections <= 0) {
            throw new IllegalArgumentException(op + " 的份数必须为正数，实际 " + sections);
        }
        if (total % sections != 0) {
            throw new IllegalArgumentException(op + " 要求" + what + " " + total + " 能被份数 "
                    + sections + " 整除");
        }
        return total / sections;
    }
}
