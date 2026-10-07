package com.zifang.util.pandas.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 矩阵类
 * <p>
 * 以「行优先的二维表」为内部表示：{@code List<List<Double>>}，每个内层 List 是一行。
 * 对标 numpy 的 {@code ndarray}，但只支持二维及以下，这是本类的实现范围。
 * <p>
 * <b>元素一律用 {@link Double} 存储</b>，不使用基本类型数组 ——
 * 基本类型数组（{@code int[]} 等）在 Java 里<b>不能</b>向上转型成
 * {@code Object[]}，一旦按引用数组处理就会 ClassCastException。
 * 这也是 {@link #set(Double...)} 收 {@code Double} 而不是 {@code double} 的原因。
 */
public class Matrix {

    private List<List<Double>> data = new ArrayList<>();

    /**
     * Matrix方法。
     */
    public Matrix() {
    }

    /**
     * 用二维数组直接构造矩阵
     *
     * @param values 行优先的二维数组，外层长度是行数、内层长度是列数
     * @throws IllegalArgumentException 如果数组非矩形（各行长度不一致）
     */
    public Matrix(double[][] values) {
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }
        int cols = values.length > 0 ? values[0].length : 0;
        List<List<Double>> rows = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            if (values[i].length != cols) {
                throw new IllegalArgumentException(
                        "矩阵必须是矩形：第 " + i + " 行长度为 " + values[i].length + "，与首行的 " + cols + " 不一致");
            }
            List<Double> row = new ArrayList<>(cols);
            for (double v : values[i]) {
                row.add(v);
            }
            rows.add(row);
        }
        this.data = rows;
    }

    /**
     * 矩阵乘法，结果**原地**写回本矩阵
     * <p>
     * 即 {@code this = this × another}。两个矩阵必须同形（行数与列数都相等）。
     * <p>
     * 注：numpy 的 {@code matmul} 不要求同形（可做 (m,k)×(k,n)→(m,n)），
     * 但本实现取的是「同形矩阵相乘」这一种，因为矩阵要能被就地更新。
     *
     * @param another 右乘矩阵
     * @throws IllegalArgumentException 如果两矩阵不同形
     */
    public void multiply(Matrix another) {
        if (another == null) {
            throw new IllegalArgumentException("another must not be null");
        }
        int[] lhs = shapeOf();
        int[] rhs = another.shapeOf();
        if (lhs[0] != rhs[0] || lhs[1] != rhs[1]) {
            throw new IllegalArgumentException(
                    "矩阵乘法要求同形：本矩阵 " + lhs[0] + "x" + lhs[1]
                            + "，另一个 " + rhs[0] + "x" + rhs[1]);
        }
        int n = lhs[0];
        List<List<Double>> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<Double> row = new ArrayList<>(n);
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += data.get(i).get(k) * another.data.get(k).get(j);
                }
                row.add(sum);
            }
            result.add(row);
        }
        this.data = result;
    }

    /**
     * 矩阵的一行
     * <p>
     * 每调用一次追加一行。传 {@code set(1.0, 2.0, 3.0)} 等价于传
     * {@code Double[]{1.0, 2.0, 3.0}} —— 基本类型实参会先自动装箱。
     * <p>
     * ⚠️ 允许行长度不一致（此时 {@link #size()} 统计实际元素总数而非 rows×cols）。
     * 这与 numpy 会直接报错的行为不同，是为了让「先拼行、后定形」的场景可用。
     *
     * @param arrays 这一行的元素
     */
    public void set(Double... arrays) {
        data.add(Arrays.asList(arrays));
    }

    /**
     * 美化输出：按行对齐打印矩阵
     * <p>
     * 输出到标准输出，列宽按各列最长数字动态计算（含负号与小数点）。
     */
    public void format() {
        if (data.isEmpty()) {
            System.out.println("[]");
            return;
        }
        int cols = columnCount();
        int[] width = new int[cols];
        for (int j = 0; j < cols; j++) {
            for (List<Double> row : data) {
                String cell = formatCell(row, j);
                if (cell != null && cell.length() > width[j]) {
                    width[j] = cell.length();
                }
            }
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < data.size(); i++) {
            if (i > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append(" [");
            for (int j = 0; j < cols; j++) {
                if (j > 0) {
                    sb.append(", ");
                }
                String cell = formatCell(data.get(i), j);
                if (cell == null) {
                    sb.append(spaces(width[j]));
                } else {
                    sb.append(spaces(width[j] - cell.length())).append(cell);
                }
            }
            sb.append(']');
        }
        sb.append(']');
        System.out.println(sb);
    }

    /**
     * 打印形状，形如 {@code (3, 4)}；空矩阵打印 {@code (0,)}
     */
    public void shape() {
        int[] s = shapeOf();
        System.out.println("(" + s[0] + (s[1] == 0 ? "" : ", " + s[1]) + ")");
    }

    /**
     * 打印元素类型
     */
    public void dtype() {
        System.out.println("float64");
    }

    /**
     * 打印维度个数：二维矩阵是 2，空矩阵是 0
     */
    public void ndim() {
        System.out.println(data.isEmpty() ? 0 : 2);
    }

    /**
     * 切片方法
     *
     * @return 内部数据的视图（可修改，修改会直接影响本矩阵）
     */
    public List<List<Double>> slice() {
        return data;
    }

    /**
     * size方法。
     *
     * @return 矩阵中元素的总个数（所有行的长度之和）
     */
    public int size() {
        int total = 0;
        for (List<Double> row : data) {
            total += row.size();
        }
        return total;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 取形状：首元素为行数，次元素为最长行的列数
     */
    private int[] shapeOf() {
        int rows = data.size();
        int cols = 0;
        for (List<Double> row : data) {
            if (row.size() > cols) {
                cols = row.size();
            }
        }
        return new int[]{rows, cols};
    }

    /**
     * 取列数（最长行的长度）
     */
    private int columnCount() {
        int cols = 0;
        for (List<Double> row : data) {
            if (row.size() > cols) {
                cols = row.size();
            }
        }
        return cols;
    }

    /**
     * 取某行第 j 列的显示文本；该行较短时返回 null
     */
    private String formatCell(List<Double> row, int j) {
        if (j >= row.size()) {
            return null;
        }
        Double v = row.get(j);
        if (v == null) {
            return "null";
        }
        double d = v;
        if (d == Math.rint(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    private String spaces(int n) {
        if (n <= 0) {
            return "";
        }
        char[] buf = new char[n];
        Arrays.fill(buf, ' ');
        return new String(buf);
    }

    private Integer analysisPadding() {
        Integer max = 0;
        for (List<Double> row : data) {
            for (Double col : row) {
                Integer cu = String.valueOf(col).length();
                if (cu > max) {
                    max = cu;
                }
            }
        }
        return max;
    }
}