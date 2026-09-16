package com.zifang.util.pandas;

import com.zifang.util.pandas.num.DType;
import com.zifang.util.pandas.num.Index;
import com.zifang.util.pandas.num.Num;
import com.zifang.util.pandas.num.Nums;
import com.zifang.util.pandas.window.EWM;

import java.util.*;
import java.util.function.Function;

/**
 * Series 类 - 对标 pandas Series
 * 带标签的一维数组
 */
public class Series {

    private Num data;
    private Index index;
    private String name;
    private DType dtype;

    // ==================== 构造函数 ====================

    /**
     * Series方法。
     * * @param data Object类型参数
     */
    public Series(Object data) {
        this(data, (Index) null, null, null);
    }

    /**
     * Series方法。
     * * @param data Object类型参数
     *
     * @param index String[]类型参数
     */
    public Series(Object data, String[] index) {
        this(data, Index.of(index), null, null);
    }

    /**
     * Series方法。
     * * @param data Object类型参数
     *
     * @param index Index类型参数
     */
    public Series(Object data, Index index) {
        this(data, index, null, null);
    }

    /**
     * Series方法。
     * * @param data Object类型参数
     *
     * @param name String类型参数
     */
    public Series(Object data, String name) {
        this(data, (Index) null, name, null);
    }

    /**
     * Series方法。
     * * @param data Object类型参数
     *
     * @param index Index类型参数
     * @param name  String类型参数
     * @param dtype DType类型参数
     */
    public Series(Object data, Index index, String name, DType dtype) {
        if (data instanceof Num) {
            this.data = (Num) data;
        } else if (data instanceof double[]) {
            this.data = new Num(data);
        } else if (data instanceof int[]) {
            this.data = Nums.array(data);
        } else if (data instanceof List) {
            this.data = Nums.array((List<?>) data);
        } else if (data instanceof String[]) {
            // 将字符串数组转换为数值索引
            String[] strArray = (String[]) data;
            double[] numericValues = new double[strArray.length];
            for (int i = 0; i < strArray.length; i++) {
                try {
                    numericValues[i] = Double.parseDouble(strArray[i]);
                } catch (NumberFormatException e) {
                    numericValues[i] = i; // 如果无法解析为数字，使用索引作为值
                }
            }
            this.data = new Num(numericValues);
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + data.getClass());
        }

        if (index == null) {
            this.index = Index.range(this.data.size());
        } else {
            if (index.size() != this.data.size()) {
                throw new IllegalArgumentException("Index length " + index.size() +
                        " does not match data length " + this.data.size());
            }
            this.index = index;
        }

        this.name = name;
        this.dtype = dtype != null ? dtype : this.data.dtype();
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建 Series 的便捷工厂方法（对标 pandas.Series）
     *
     * @param values 可变参数值列表
     * @return 新创建的 Series 对象
     */
    public static Series of(String[] values) {
        // 将字符串值转换为数值索引，同时保留字符串信息
        double[] numericValues = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            try {
                // 尝试将字符串解析为数字
                numericValues[i] = Double.parseDouble(values[i]);
            } catch (NumberFormatException e) {
                // 如果不是数字，使用索引作为值
                numericValues[i] = i;
            }
        }
        return new Series(numericValues, Index.of(values), "string_series", null);
    }


    /**
     * 整数类型的便捷工厂方法
     */
    public static Series of(int[] values) {
        return new Series(values);
    }


    /**
     * fromMap方法。
     * * @param map MapString,类型参数
     *
     * @return static Series类型返回值
     */
    public static Series fromMap(Map<String, ?> map) {
        String[] keys = map.keySet().toArray(new String[0]);
        double[] values = new double[map.size()];
        int i = 0;
        for (Object value : map.values()) {
            if (value instanceof Number) {
                values[i++] = ((Number) value).doubleValue();
            } else {
                throw new IllegalArgumentException("Map values must be numeric");
            }
        }
        return new Series(values, keys);
    }


    /**
     * 数值类型的便捷工厂方法
     *
     * @param values 可变参数数值列表
     * @return 新创建的 Series 对象
     */
    public static Series of(double[] values) {
        return new Series(values);
    }


    /**
     * 带索引的便捷工厂方法
     *
     * @param index  索引数组
     * @param values 数值数组
     * @return 新创建的 Series 对象
     */
    public static Series of(String[] index, double[] values) {
        return new Series(values, index);
    }

    // ==================== 数据访问 ====================

    /**
     * get方法。
     * * @param i int类型参数
     *
     * @return double类型返回值
     */
    public double get(int i) {
        return getDataAsDoubleArray()[i];
    }

    /**
     * get方法。
     * * @param label String类型参数
     *
     * @return double类型返回值
     */
    public double get(String label) {
        int i = index.getLocation(label);
        if (i < 0) {
            throw new NoSuchElementException("Label '" + label + "' not found in index");
        }
        return get(i);
    }

    /**
     * get方法。
     * * @param labels String[]类型参数
     *
     * @return Series类型返回值
     */
    public Series get(String[] labels) {
        double[] values = new double[labels.length];
        for (int i = 0; i < labels.length; i++) {
            values[i] = get(labels[i]);
        }
        return new Series(values, Index.of(labels), name, dtype);
    }

    /**
     * get方法。
     * * @param indices int[]类型参数
     *
     * @return Series类型返回值
     */
    public Series get(int[] indices) {
        double[] data = getDataAsDoubleArray();
        double[] values = new double[indices.length];
        String[] newIndex = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            values[i] = data[indices[i]];
            newIndex[i] = index.get(indices[i]);
        }
        return new Series(values, Index.of(newIndex), name, dtype);
    }

    /**
     * slice方法。
     * * @param start int类型参数
     *
     * @param stop int类型参数
     * @return Series类型返回值
     */
    public Series slice(int start, int stop) {
        return get(Arrays.copyOfRange(getIndexAsIntArray(), start, stop));
    }

    /**
     * loc方法。
     * * @param start String类型参数
     *
     * @param end String类型参数
     * @return Series类型返回值
     */
    public Series loc(String start, String end) {
        int startIdx = index.getLocation(start);
        int endIdx = index.getLocation(end);
        if (startIdx < 0 || endIdx < 0) {
            throw new NoSuchElementException("Start or end label not found");
        }
        return slice(Math.min(startIdx, endIdx), Math.max(startIdx, endIdx) + 1);
    }

    /**
     * iloc方法。
     * * @param start int类型参数
     *
     * @param end int类型参数
     * @return Series类型返回值
     */
    public Series iloc(int start, int end) {
        return slice(start, end);
    }

    // ==================== 布尔索引和过滤 ====================

    /**
     * filter方法。
     * * @param condition FunctionDouble,类型参数
     *
     * @return Series类型返回值
     */
    public Series filter(Function<Double, Boolean> condition) {
        double[] data = getDataAsDoubleArray();
        List<Double> values = new ArrayList<>();
        List<String> newIndex = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            if (condition.apply(data[i])) {
                values.add(data[i]);
                newIndex.add(index.get(i));
            }
        }
        return new Series(values.stream().mapToDouble(Double::doubleValue).toArray(),
                Index.of(newIndex.toArray(new String[0])), name, dtype);
    }

    /**
     * where方法。
     * * @param condition FunctionDouble,类型参数
     *
     * @param thenValue double类型参数
     * @param elseValue double类型参数
     * @return Series类型返回值
     */
    public Series where(Function<Double, Boolean> condition, double thenValue, double elseValue) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = condition.apply(data[i]) ? thenValue : elseValue;
        }
        return new Series(result, index, name, dtype);
    }

    // ==================== 算术运算 ====================

    /**
     * add方法。
     * * @param other Series类型参数
     *
     * @return Series类型返回值
     */
    public Series add(Series other) {
        checkAlignment(other);
        return new Series(data.add(other.data), index, name, dtype);
    }

    /**
     * add方法。
     * * @param scalar double类型参数
     *
     * @return Series类型返回值
     */
    public Series add(double scalar) {
        return new Series(data.add(scalar), index, name, dtype);
    }

    /**
     * subtract方法。
     * * @param other Series类型参数
     *
     * @return Series类型返回值
     */
    public Series subtract(Series other) {
        checkAlignment(other);
        return new Series(data.subtract(other.data), index, name, dtype);
    }

    /**
     * subtract方法。
     * * @param scalar double类型参数
     *
     * @return Series类型返回值
     */
    public Series subtract(double scalar) {
        return new Series(data.subtract(scalar), index, name, dtype);
    }

    /**
     * multiply方法。
     * * @param other Series类型参数
     *
     * @return Series类型返回值
     */
    public Series multiply(Series other) {
        checkAlignment(other);
        return new Series(data.multiply(other.data), index, name, dtype);
    }

    /**
     * multiply方法。
     * * @param scalar double类型参数
     *
     * @return Series类型返回值
     */
    public Series multiply(double scalar) {
        return new Series(data.multiply(scalar), index, name, dtype);
    }

    /**
     * divide方法。
     * * @param other Series类型参数
     *
     * @return Series类型返回值
     */
    public Series divide(Series other) {
        checkAlignment(other);
        return new Series(data.divide(other.data), index, name, dtype);
    }

    /**
     * divide方法。
     * * @param scalar double类型参数
     *
     * @return Series类型返回值
     */
    public Series divide(double scalar) {
        return new Series(data.divide(scalar), index, name, dtype);
    }

    /**
     * pow方法。
     * * @param exponent double类型参数
     *
     * @return Series类型返回值
     */
    public Series pow(double exponent) {
        return new Series(data.pow(exponent), index, name, dtype);
    }

    /**
     * abs方法。
     *
     * @return Series类型返回值
     */
    public Series abs() {
        return new Series(data.abs(), index, name, dtype);
    }

    /**
     * sqrt方法。
     *
     * @return Series类型返回值
     */
    public Series sqrt() {
        return new Series(data.sqrt(), index, name, dtype);
    }

    /**
     * log方法。
     *
     * @return Series类型返回值
     */
    public Series log() {
        return new Series(data.log(), index, name, dtype);
    }

    /**
     * exp方法。
     *
     * @return Series类型返回值
     */
    public Series exp() {
        return new Series(data.exp(), index, name, dtype);
    }

    // ==================== 统计方法 ====================

    /**
     * sum方法。
     *
     * @return double类型返回值
     */
    public double sum() {
        return data.sum();
    }

    /**
     * mean方法。
     *
     * @return double类型返回值
     */
    public double mean() {
        return data.mean();
    }

    /**
     * max方法。
     *
     * @return double类型返回值
     */
    public double max() {
        return data.max();
    }

    /**
     * min方法。
     *
     * @return double类型返回值
     */
    public double min() {
        return data.min();
    }

    /**
     * std方法。
     *
     * @return double类型返回值
     */
    public double std() {
        return data.std();
    }

    /**
     * var方法。
     *
     * @return double类型返回值
     */
    public double var() {
        return data.var();
    }

    /**
     * median方法。
     *
     * @return double类型返回值
     */
    public double median() {
        double[] sorted = getDataAsDoubleArray().clone();
        java.util.Arrays.sort(sorted);
        int n = sorted.length;
        if (n % 2 == 0) {
            return (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
        } else {
            return sorted[n / 2];
        }
    }

    /**
     * percentile方法。
     * * @param q double类型参数
     *
     * @return double类型返回值
     */
    public double percentile(double q) {
        if (q < 0 || q > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }
        double[] sorted = getDataAsDoubleArray().clone();
        java.util.Arrays.sort(sorted);
        double index = q / 100.0 * (sorted.length - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        double weight = index - lower;
        return sorted[lower] * (1 - weight) + sorted[upper] * weight;
    }

    /**
     * count方法。
     *
     * @return long类型返回值
     */
    public long count() {
        return getDataAsDoubleArray().length;
    }

    /**
     * sem方法。
     *
     * @return double类型返回值
     */
    public double sem() {
        return std() / Math.sqrt(count());
    }

    /**
     * cumsum方法。
     *
     * @return Series类型返回值
     */
    public Series cumsum() {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        double sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
            result[i] = sum;
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * cumprod方法。
     *
     * @return Series类型返回值
     */
    public Series cumprod() {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        double prod = 1;
        for (int i = 0; i < data.length; i++) {
            prod *= data[i];
            result[i] = prod;
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * diff方法。
     * * @param periods int类型参数
     *
     * @return Series类型返回值
     */
    public Series diff(int periods) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            if (i < periods) {
                result[i] = Double.NaN;
            } else {
                result[i] = data[i] - data[i - periods];
            }
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * diff方法。
     *
     * @return Series类型返回值
     */
    public Series diff() {
        return diff(1);
    }

    /**
     * pct_change方法。
     * * @param periods int类型参数
     *
     * @return Series类型返回值
     */
    public Series pct_change(int periods) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            if (i < periods || data[i - periods] == 0) {
                result[i] = Double.NaN;
            } else {
                result[i] = (data[i] - data[i - periods]) / data[i - periods];
            }
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * pct_change方法。
     *
     * @return Series类型返回值
     */
    public Series pct_change() {
        return pct_change(1);
    }

    // ==================== 比较运算 ====================

    /**
     * eq方法。
     * * @param value double类型参数
     *
     * @return Series类型返回值
     */
    public Series eq(double value) {
        return compare(v -> v == value);
    }

    /**
     * eq方法。
     * * @param other Series类型参数
     *
     * @return Series类型返回值
     */
    public Series eq(Series other) {
        checkAlignment(other);
        double[] a = getDataAsDoubleArray();
        double[] b = other.getDataAsDoubleArray();
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] == b[i] ? 1.0 : 0.0;
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * ne方法。
     * * @param value double类型参数
     *
     * @return Series类型返回值
     */
    public Series ne(double value) {
        return compare(v -> v != value);
    }

    /**
     * gt方法。
     * * @param value double类型参数
     *
     * @return Series类型返回值
     */
    public Series gt(double value) {
        return compare(v -> v > value);
    }

    /**
     * ge方法。
     * * @param value double类型参数
     *
     * @return Series类型返回值
     */
    public Series ge(double value) {
        return compare(v -> v >= value);
    }

    /**
     * lt方法。
     * * @param value double类型参数
     *
     * @return Series类型返回值
     */
    public Series lt(double value) {
        return compare(v -> v < value);
    }

    /**
     * le方法。
     * * @param value double类型参数
     *
     * @return Series类型返回值
     */
    public Series le(double value) {
        return compare(v -> v <= value);
    }

    private Series compare(java.util.function.DoublePredicate condition) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = condition.test(data[i]) ? 1.0 : 0.0;
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * all方法。
     *
     * @return boolean类型返回值
     */
    public boolean all() {
        double[] data = getDataAsDoubleArray();
        for (double v : data) {
            if (v == 0.0 || Double.isNaN(v)) {
                return false;
            }
        }
        return true;
    }

    /**
     * any方法。
     *
     * @return boolean类型返回值
     */
    public boolean any() {
        double[] data = getDataAsDoubleArray();
        for (double v : data) {
            if (v != 0.0 && !Double.isNaN(v)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 缺失值处理 ====================

    /**
     * isna方法。
     *
     * @return Series类型返回值
     */
    public Series isna() {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = Double.isNaN(data[i]) ? 1.0 : 0.0;
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * notna方法。
     *
     * @return Series类型返回值
     */
    public Series notna() {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = !Double.isNaN(data[i]) ? 1.0 : 0.0;
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * fillna方法。
     * * @param value double类型参数
     *
     * @return Series类型返回值
     */
    public Series fillna(double value) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = Double.isNaN(data[i]) ? value : data[i];
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * dropna方法。
     *
     * @return Series类型返回值
     */
    public Series dropna() {
        double[] data = getDataAsDoubleArray();
        List<Double> values = new ArrayList<>();
        List<String> newIndex = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            if (!Double.isNaN(data[i])) {
                values.add(data[i]);
                newIndex.add(index.get(i));
            }
        }
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return new Series(result, Index.of(newIndex.toArray(new String[0])), name, dtype);
    }

    // ==================== 其他方法 ====================

    /**
     * copy方法。
     *
     * @return Series类型返回值
     */
    public Series copy() {
        return new Series(data, index, name, dtype);
    }

    /**
     * head方法。
     * * @param n int类型参数
     *
     * @return Series类型返回值
     */
    public Series head(int n) {
        return slice(0, Math.min(n, (int) count()));
    }

    /**
     * head方法。
     *
     * @return Series类型返回值
     */
    public Series head() {
        return head(5);
    }

    /**
     * tail方法。
     * * @param n int类型参数
     *
     * @return Series类型返回值
     */
    public Series tail(int n) {
        int size = (int) count();
        return slice(Math.max(0, size - n), size);
    }

    /**
     * tail方法。
     *
     * @return Series类型返回值
     */
    public Series tail() {
        return tail(5);
    }

    /**
     * sort_values方法。
     *
     * @return Series类型返回值
     */
    public Series sort_values() {
        return sort_values(true);
    }

    /**
     * sort_values方法。
     * * @param ascending boolean类型参数
     *
     * @return Series类型返回值
     */
    public Series sort_values(boolean ascending) {
        double[] data = getDataAsDoubleArray();
        Integer[] indices = new Integer[data.length];
        for (int i = 0; i < data.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, (i1, i2) -> {
            int cmp = Double.compare(data[i1], data[i2]);
            return ascending ? cmp : -cmp;
        });
        double[] sortedData = new double[data.length];
        String[] sortedIndex = new String[data.length];
        for (int i = 0; i < indices.length; i++) {
            sortedData[i] = data[indices[i]];
            sortedIndex[i] = index.get(indices[i]);
        }
        return new Series(sortedData, Index.of(sortedIndex), name, dtype);
    }

    /**
     * sort_index方法。
     *
     * @return Series类型返回值
     */
    public Series sort_index() {
        return sort_index(true);
    }

    /**
     * sort_index方法。
     * * @param ascending boolean类型参数
     *
     * @return Series类型返回值
     */
    public Series sort_index(boolean ascending) {
        String[] labels = index.toArray();
        Integer[] indices = new Integer[labels.length];
        for (int i = 0; i < labels.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, (i1, i2) -> {
            int cmp = labels[i1].compareTo(labels[i2]);
            return ascending ? cmp : -cmp;
        });
        double[] data = getDataAsDoubleArray();
        double[] sortedData = new double[data.length];
        String[] sortedIndex = new String[data.length];
        for (int i = 0; i < indices.length; i++) {
            sortedData[i] = data[indices[i]];
            sortedIndex[i] = labels[indices[i]];
        }
        return new Series(sortedData, Index.of(sortedIndex), name, dtype);
    }

    /**
     * round方法。
     * * @param decimals int类型参数
     *
     * @return Series类型返回值
     */
    public Series round(int decimals) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        double factor = Math.pow(10, decimals);
        for (int i = 0; i < data.length; i++) {
            result[i] = Math.round(data[i] * factor) / factor;
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * round方法。
     *
     * @return Series类型返回值
     */
    public Series round() {
        return round(0);
    }

    /**
     * clip方法。
     * * @param lower double类型参数
     *
     * @param upper double类型参数
     * @return Series类型返回值
     */
    public Series clip(double lower, double upper) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = Math.max(lower, Math.min(data[i], upper));
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * between方法。
     * * @param left double类型参数
     *
     * @param right double类型参数
     * @return Series类型返回值
     */
    public Series between(double left, double right) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (data[i] >= left && data[i] <= right) ? 1.0 : 0.0;
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * unique方法。
     *
     * @return Series类型返回值
     */
    public Series unique() {
        double[] data = getDataAsDoubleArray();
        Set<Double> seen = new LinkedHashSet<>();
        for (double v : data) {
            seen.add(v);
        }
        double[] result = new double[seen.size()];
        int i = 0;
        for (Double v : seen) {
            result[i++] = v;
        }
        return new Series(result, name);
    }

    /**
     * nunique方法。
     *
     * @return int类型返回值
     */
    public int nunique() {
        return (int) unique().count();
    }

    /**
     * value_counts方法。
     *
     * @return Series类型返回值
     */
    public Series value_counts() {
        double[] data = getDataAsDoubleArray();
        Map<Double, Integer> counts = new HashMap<>();
        for (double v : data) {
            counts.put(v, counts.getOrDefault(v, 0) + 1);
        }
        double[] values = new double[counts.size()];
        String[] index = new String[counts.size()];
        int i = 0;
        for (Map.Entry<Double, Integer> entry : counts.entrySet()) {
            values[i] = entry.getValue();
            index[i] = String.valueOf(entry.getKey());
            i++;
        }
        return new Series(values, Index.of(index), "count", DType.INT64);
    }

    /**
     * is_unique方法。
     *
     * @return boolean类型返回值
     */
    public boolean is_unique() {
        return nunique() == count();
    }

    /**
     * is_monotonic_increasing方法。
     *
     * @return boolean类型返回值
     */
    public boolean is_monotonic_increasing() {
        double[] data = getDataAsDoubleArray();
        for (int i = 1; i < data.length; i++) {
            if (data[i] < data[i - 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * is_monotonic_decreasing方法。
     *
     * @return boolean类型返回值
     */
    public boolean is_monotonic_decreasing() {
        double[] data = getDataAsDoubleArray();
        for (int i = 1; i < data.length; i++) {
            if (data[i] > data[i - 1]) {
                return false;
            }
        }
        return true;
    }

    // ==================== 窗口操作 ====================

    /**
     * 创建滚动窗口对象
     *
     * @param window 窗口大小
     * @return Rolling 窗口对象
     */
    public com.zifang.util.pandas.window.Rolling rolling(int window) {
        return new com.zifang.util.pandas.window.Rolling(this, window);
    }

    /**
     * 创建扩展窗口对象
     *
     * @return Expanding 窗口对象
     */
    public com.zifang.util.pandas.window.Expanding expanding() {
        return new com.zifang.util.pandas.window.Expanding(this);
    }

    /**
     * 创建指数加权移动窗口对象
     *
     * @param alpha 衰减因子
     * @return EWM 窗口对象
     */
    public com.zifang.util.pandas.window.EWM ewm(double alpha) {
        return new com.zifang.util.pandas.window.EWM(this, alpha);
    }

    /**
     * 使用 COM 创建 EWM
     *
     * @param com 质心
     * @return EWM 窗口对象
     */
    public com.zifang.util.pandas.window.EWM ewmCom(double com) {
        return EWM.com(this, com);
    }

    /**
     * 使用跨度创建 EWM
     *
     * @param span 跨度
     * @return EWM 窗口对象
     */
    public com.zifang.util.pandas.window.EWM ewmSpan(double span) {
        return com.zifang.util.pandas.window.EWM.span(this, span);
    }

    // ==================== 字符串操作 ====================

    /**
     * 获取字符串访问器，用于执行字符串操作
     *
     * @return StringAccessor 字符串操作访问器
     */
    public com.zifang.util.pandas.str.StringAccessor str() {
        return new com.zifang.util.pandas.str.StringAccessor(this);
    }

    // ==================== apply 和 map ====================

    /**
     * apply方法。
     * * @param func FunctionDouble,类型参数
     *
     * @return Series类型返回值
     */
    public Series apply(Function<Double, Double> func) {
        double[] data = getDataAsDoubleArray();
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = func.apply(data[i]);
        }
        return new Series(result, index, name, dtype);
    }

    /**
     * map方法。
     * * @param mapping MapDouble,类型参数
     *
     * @return Series类型返回值
     */
    public Series map(Map<Double, Double> mapping) {
        return apply(v -> mapping.getOrDefault(v, v));
    }

    // ==================== 重命名和元数据 ====================

    /**
     * rename方法。
     * * @param newName String类型参数
     *
     * @return Series类型返回值
     */
    public Series rename(String newName) {
        return new Series(data, index, newName, dtype);
    }

    /**
     * name方法。
     *
     * @return String类型返回值
     */
    public String name() {
        return name;
    }

    /**
     * setName方法。
     * * @param name String类型参数
     *
     * @return Series类型返回值
     */
    public Series setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * index方法。
     *
     * @return Index类型返回值
     */
    public Index index() {
        return index;
    }

    /**
     * setIndex方法。
     * * @param newIndex Index类型参数
     *
     * @return Series类型返回值
     */
    public Series setIndex(Index newIndex) {
        if (newIndex.size() != this.index.size()) {
            throw new IllegalArgumentException("New index must have same size as old index");
        }
        return new Series(data, newIndex, name, dtype);
    }

    /**
     * setIndex方法。
     * * @param newIndex String[]类型参数
     *
     * @return Series类型返回值
     */
    public Series setIndex(String[] newIndex) {
        return setIndex(Index.of(newIndex));
    }

    /**
     * dtype方法。
     *
     * @return DType类型返回值
     */
    public DType dtype() {
        return dtype;
    }

    // ==================== 数据导出 ====================

    /**
     * toArray方法。
     *
     * @return double[]类型返回值
     */
    public double[] toArray() {
        return getDataAsDoubleArray().clone();
    }

    /**
     * toList方法。
     *
     * @return List<Double>类型返回值
     */
    public List<Double> toList() {
        double[] arr = getDataAsDoubleArray();
        List<Double> list = new ArrayList<>(arr.length);
        for (double v : arr) {
            list.add(v);
        }
        return list;
    }

    /**
     * toMap方法。
     *
     * @return Map<String, Double>类型返回值
     */
    public Map<String, Double> toMap() {
        Map<String, Double> map = new LinkedHashMap<>();
        double[] data = getDataAsDoubleArray();
        for (int i = 0; i < data.length; i++) {
            map.put(index.get(i), data[i]);
        }
        return map;
    }

    /**
     * toNum方法。
     *
     * @return Num类型返回值
     */
    public Num toNum() {
        return data;
    }

    // ==================== 描述和打印 ====================

    /**
     * describe方法。
     *
     * @return String类型返回值
     */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("count    ").append(String.format("%.0f", (double) count())).append("\n");
        sb.append("mean     ").append(String.format("%.6f", mean())).append("\n");
        sb.append("std      ").append(String.format("%.6f", std())).append("\n");
        sb.append("min      ").append(String.format("%.6f", min())).append("\n");
        sb.append("25%      ").append(String.format("%.6f", percentile(25))).append("\n");
        sb.append("50%      ").append(String.format("%.6f", median())).append("\n");
        sb.append("75%      ").append(String.format("%.6f", percentile(75))).append("\n");
        sb.append("max      ").append(String.format("%.6f", max())).append("\n");
        return sb.toString();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isEmpty()) {
            sb.append(name).append("\n");
        }
        double[] data = getDataAsDoubleArray();
        int displayCount = Math.min(data.length, 10);
        for (int i = 0; i < displayCount; i++) {
            sb.append(index.get(i)).append("    ").append(data[i]).append("\n");
        }
        if (data.length > 10) {
            sb.append("...\n");
            sb.append("Length: ").append(data.length).append(", dtype: ").append(dtype);
        }
        return sb.toString();
    }

    // ==================== 私有辅助方法 ====================

    private double[] getDataAsDoubleArray() {
        if (data.data() instanceof double[]) {
            return (double[]) data.data();
        }
        throw new UnsupportedOperationException("Cannot convert data to double array");
    }

    private int[] getIndexAsIntArray() {
        int[] result = new int[index.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    private void checkAlignment(Series other) {
        if (this.index.size() != other.index.size()) {
            throw new IllegalArgumentException("Series must have the same length for this operation");
        }
    }

    /**
     * length方法。
     *
     * @return int类型返回值
     */
    public int length() {
        return index.size();
    }
}
