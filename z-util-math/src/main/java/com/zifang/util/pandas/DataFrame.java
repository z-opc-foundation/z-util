package com.zifang.util.pandas;

import com.zifang.util.pandas.num.Index;

import java.util.*;
import java.util.function.Function;

/**
 * DataFrame 类 - 对标 pandas DataFrame
 * 提供二维表格数据结构和数据分析功能
 */
public class DataFrame {

    private Map<String, Series> columns;
    private Index index;
    private List<String> columnNames;

    // ==================== 构造函数 ====================

    /**
     * DataFrame方法。
     */
    public DataFrame() {
        this.columns = new LinkedHashMap<>();
        this.index = Index.range(0);
        this.columnNames = new ArrayList<>();
    }

    /**
     * DataFrame方法。
     * * @param data MapString,类型参数
     */
    public DataFrame(Map<String, double[]> data) {
        this(data, null);
    }

    /**
     * DataFrame方法。
     * * @param data MapString,类型参数
     *
     * @param index String[]类型参数
     */
    public DataFrame(Map<String, double[]> data, String[] index) {
        this.columns = new LinkedHashMap<>();
        this.columnNames = new ArrayList<>(data.keySet());

        int maxLength = 0;
        for (double[] arr : data.values()) {
            maxLength = Math.max(maxLength, arr.length);
        }

        if (index == null) {
            this.index = Index.range(maxLength);
        } else {
            this.index = Index.of(index);
        }

        for (Map.Entry<String, double[]> entry : data.entrySet()) {
            Series series = new Series(entry.getValue(), this.index, entry.getKey(), null);
            this.columns.put(entry.getKey(), series);
        }
    }

    /**
     * DataFrame方法。
     * * @param records ListMapString,类型参数
     */
    public DataFrame(List<Map<String, Object>> records) {
        this.columns = new LinkedHashMap<>();

        if (records.isEmpty()) {
            this.index = Index.range(0);
            this.columnNames = new ArrayList<>();
            return;
        }

        // 收集所有列名
        Set<String> allColumns = new LinkedHashSet<>();
        for (Map<String, Object> record : records) {
            allColumns.addAll(record.keySet());
        }
        this.columnNames = new ArrayList<>(allColumns);

        // 创建索引
        this.index = Index.range(records.size());

        // 为每列创建 Series
        for (String colName : this.columnNames) {
            double[] values = new double[records.size()];
            for (int i = 0; i < records.size(); i++) {
                Object val = records.get(i).get(colName);
                values[i] = val instanceof Number ? ((Number) val).doubleValue() : Double.NaN;
            }
            Series series = new Series(values, this.index, colName, null);
            this.columns.put(colName, series);
        }
    }

    // ==================== 静态工厂方法 ====================

    /**
     * fromMap方法。
     * * @param data MapString,类型参数
     *
     * @return static DataFrame类型返回值
     */
    public static DataFrame fromMap(Map<String, double[]> data) {
        return new DataFrame(data);
    }

    /**
     * fromRecords方法。
     * * @param records ListMapString,类型参数
     *
     * @return static DataFrame类型返回值
     */
    public static DataFrame fromRecords(List<Map<String, Object>> records) {
        return new DataFrame(records);
    }

    /**
     * fromCSV方法。
     * * @param csvContent String类型参数
     *
     * @return static DataFrame类型返回值
     */
    public static DataFrame fromCSV(String csvContent) {
        // 简化实现
        String[] lines = csvContent.split("\n");
        if (lines.length < 2) return new DataFrame();

        String[] headers = lines[0].split(",");
        List<Map<String, Object>> records = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            Map<String, Object> record = new HashMap<>();
            for (int j = 0; j < headers.length && j < values.length; j++) {
                try {
                    record.put(headers[j], Double.parseDouble(values[j]));
                } catch (NumberFormatException e) {
                    record.put(headers[j], values[j]);
                }
            }
            records.add(record);
        }

        return new DataFrame(records);
    }

    // ==================== 列操作 ====================

    /**
     * merge方法。
     * * @param left DataFrame类型参数
     *
     * @param right   DataFrame类型参数
     * @param leftOn  String类型参数
     * @param rightOn String类型参数
     * @param how     String类型参数
     * @return static DataFrame类型返回值
     */
    public static DataFrame merge(DataFrame left, DataFrame right, String leftOn, String rightOn, String how) {
        // 简化实现：inner join
        Series leftSeries = left.get(leftOn);
        Series rightSeries = right.get(rightOn);

        Map<Double, List<Integer>> leftMap = new HashMap<>();
        double[] leftValues = leftSeries.toArray();
        for (int i = 0; i < leftValues.length; i++) {
            leftMap.computeIfAbsent(leftValues[i], k -> new ArrayList<>()).add(i);
        }

        List<String> newIndex = new ArrayList<>();
        Map<String, List<Double>> resultData = new LinkedHashMap<>();

        // 初始化列
        for (String col : left.columnNames) {
            resultData.put(col, new ArrayList<>());
        }
        for (String col : right.columnNames) {
            if (!resultData.containsKey(col)) {
                resultData.put(col, new ArrayList<>());
            }
        }

        double[] rightValues = rightSeries.toArray();
        for (int i = 0; i < rightValues.length; i++) {
            List<Integer> matches = leftMap.get(rightValues[i]);
            if (matches != null) {
                for (int leftIdx : matches) {
                    newIndex.add(left.index.get(leftIdx) + "_" + right.index.get(i));

                    for (String col : left.columnNames) {
                        resultData.get(col).add(left.get(col).toArray()[leftIdx]);
                    }
                    for (String col : right.columnNames) {
                        double val = right.get(col).toArray()[i];
                        if (resultData.containsKey(col) && resultData.get(col).size() < resultData.get(left.columnNames.get(0)).size()) {
                            resultData.get(col).add(val);
                        } else if (!resultData.containsKey(col)) {
                            resultData.get(col).add(val);
                        }
                    }
                }
            }
        }

        Map<String, double[]> finalData = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : resultData.entrySet()) {
            double[] arr = new double[entry.getValue().size()];
            for (int i = 0; i < entry.getValue().size(); i++) {
                arr[i] = entry.getValue().get(i);
            }
            finalData.put(entry.getKey(), arr);
        }

        return new DataFrame(finalData, newIndex.toArray(new String[0]));
    }

    /**
     * get方法。
     * * @param column String类型参数
     *
     * @return Series类型返回值
     */
    public Series get(String column) {
        return columns.get(column);
    }

    /**
     * get方法。
     * * @param columns String...类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame get(String... columns) {
        Map<String, double[]> newData = new LinkedHashMap<>();
        for (String col : columns) {
            Series series = this.columns.get(col);
            if (series != null) {
                newData.put(col, series.toArray());
            }
        }
        return new DataFrame(newData, this.index.toArray());
    }

    /**
     * addColumn方法。
     * * @param name String类型参数
     *
     * @param data double[]类型参数
     * @return DataFrame类型返回值
     */
    public DataFrame addColumn(String name, double[] data) {
        Series series = new Series(data, this.index, name, null);
        this.columns.put(name, series);
        this.columnNames.add(name);
        return this;
    }

    /**
     * addColumn方法。
     * * @param name String类型参数
     *
     * @param series Series类型参数
     * @return DataFrame类型返回值
     */
    public DataFrame addColumn(String name, Series series) {
        this.columns.put(name, series);
        if (!this.columnNames.contains(name)) {
            this.columnNames.add(name);
        }
        return this;
    }

    /**
     * dropColumn方法。
     * * @param name String类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame dropColumn(String name) {
        this.columns.remove(name);
        this.columnNames.remove(name);
        return this;
    }

    // ==================== 行操作 ====================

    /**
     * rename方法。
     * * @param mapping MapString,类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame rename(Map<String, String> mapping) {
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String oldName = entry.getKey();
            String newName = entry.getValue();
            Series series = this.columns.get(oldName);
            if (series != null) {
                series.setName(newName);
                this.columns.remove(oldName);
                this.columns.put(newName, series);
                int idx = this.columnNames.indexOf(oldName);
                if (idx >= 0) {
                    this.columnNames.set(idx, newName);
                }
            }
        }
        return this;
    }

    /**
     * nRows方法。
     *
     * @return int类型返回值
     */
    public int nRows() {
        return this.index.size();
    }

    /**
     * nCols方法。
     *
     * @return int类型返回值
     */
    public int nCols() {
        return this.columnNames.size();
    }

    /**
     * head方法。
     * * @param n int类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame head(int n) {
        int end = Math.min(n, this.nRows());
        return this.slice(0, end);
    }

    /**
     * head方法。
     *
     * @return DataFrame类型返回值
     */
    public DataFrame head() {
        return head(5);
    }

    /**
     * tail方法。
     * * @param n int类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame tail(int n) {
        int start = Math.max(0, this.nRows() - n);
        return this.slice(start, this.nRows());
    }

    /**
     * tail方法。
     *
     * @return DataFrame类型返回值
     */
    public DataFrame tail() {
        return tail(5);
    }

    // ==================== 索引操作 ====================

    /**
     * slice方法。
     * * @param start int类型参数
     *
     * @param end int类型参数
     * @return DataFrame类型返回值
     */
    public DataFrame slice(int start, int end) {
        Index newIndex = this.index.slice(start, end);
        Map<String, double[]> newData = new LinkedHashMap<>();

        for (String col : this.columnNames) {
            Series series = this.columns.get(col);
            double[] values = series.toArray();
            double[] sliced = java.util.Arrays.copyOfRange(values, start, end);
            newData.put(col, sliced);
        }

        return new DataFrame(newData, newIndex.toArray());
    }

    /**
     * index方法。
     *
     * @return Index类型返回值
     */
    public Index index() {
        return this.index;
    }

    /**
     * setIndex方法。
     * * @param newIndex Index类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame setIndex(Index newIndex) {
        if (newIndex.size() != this.nRows()) {
            throw new IllegalArgumentException("New index size must match number of rows");
        }
        this.index = newIndex;
        for (Series series : this.columns.values()) {
            series.setIndex(newIndex);
        }
        return this;
    }

    // ==================== 过滤和选择 ====================

    /**
     * columns方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> columns() {
        return new ArrayList<>(this.columnNames);
    }

    /**
     * filter方法。
     * * @param condition FunctionSeries,类型参数
     *
     * @param column String类型参数
     * @return DataFrame类型返回值
     */
    public DataFrame filter(Function<Series, Boolean> condition, String column) {
        Series target = this.columns.get(column);
        if (target == null) {
            throw new IllegalArgumentException("Column not found: " + column);
        }

        List<Integer> matchingRows = new ArrayList<>();
        double[] values = target.toArray();
        for (int i = 0; i < values.length; i++) {
            Series rowSeries = new Series(new double[]{values[i]}, this.index, column, null);
            if (condition.apply(rowSeries)) {
                matchingRows.add(i);
            }
        }

        int start = matchingRows.isEmpty() ? 0 : matchingRows.get(0);
        int end = matchingRows.isEmpty() ? 0 : matchingRows.get(matchingRows.size() - 1) + 1;
        return this.slice(start, end);
    }

    // ==================== 排序 ====================

    /**
     * query方法。
     * * @param expression String类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame query(String expression) {
        // 简化实现 - 实际应该解析表达式
        return this;
    }

    /**
     * sort_values方法。
     * * @param column String类型参数
     *
     * @param ascending boolean类型参数
     * @return DataFrame类型返回值
     */
    public DataFrame sort_values(String column, boolean ascending) {
        Series target = this.columns.get(column);
        if (target == null) {
            throw new IllegalArgumentException("Column not found: " + column);
        }

        double[] values = target.toArray();
        Integer[] indices = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, (i1, i2) -> {
            int cmp = Double.compare(values[i1], values[i2]);
            return ascending ? cmp : -cmp;
        });

        String[] newIndex = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            newIndex[i] = this.index.get(indices[i]);
        }

        Map<String, double[]> newData = new LinkedHashMap<>();
        for (String col : this.columnNames) {
            Series series = this.columns.get(col);
            double[] colValues = series.toArray();
            double[] sorted = new double[colValues.length];
            for (int i = 0; i < indices.length; i++) {
                sorted[i] = colValues[indices[i]];
            }
            newData.put(col, sorted);
        }

        return new DataFrame(newData, newIndex);
    }

    /**
     * sort_values方法。
     * * @param column String类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame sort_values(String column) {
        return sort_values(column, true);
    }

    /**
     * sort_index方法。
     * * @param ascending boolean类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame sort_index(boolean ascending) {
        String[] labels = this.index.toArray();
        Integer[] indices = new Integer[labels.length];
        for (int i = 0; i < labels.length; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, (i1, i2) -> {
            int cmp = labels[i1].compareTo(labels[i2]);
            return ascending ? cmp : -cmp;
        });

        String[] newIndex = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            newIndex[i] = labels[indices[i]];
        }

        Map<String, double[]> newData = new LinkedHashMap<>();
        for (String col : this.columnNames) {
            Series series = this.columns.get(col);
            double[] values = series.toArray();
            double[] sorted = new double[values.length];
            for (int i = 0; i < indices.length; i++) {
                sorted[i] = values[indices[i]];
            }
            newData.put(col, sorted);
        }

        return new DataFrame(newData, newIndex);
    }

    // ==================== 统计方法 ====================

    /**
     * sort_index方法。
     *
     * @return DataFrame类型返回值
     */
    public DataFrame sort_index() {
        return sort_index(true);
    }

    /**
     * sum方法。
     *
     * @return Series类型返回值
     */
    public Series sum() {
        double[] sums = new double[this.columnNames.size()];
        for (int i = 0; i < this.columnNames.size(); i++) {
            String col = this.columnNames.get(i);
            sums[i] = this.columns.get(col).sum();
        }
        return new Series(sums, Index.of(this.columnNames.toArray(new String[0])), "sum", null);
    }

    /**
     * mean方法。
     *
     * @return Series类型返回值
     */
    public Series mean() {
        double[] means = new double[this.columnNames.size()];
        for (int i = 0; i < this.columnNames.size(); i++) {
            String col = this.columnNames.get(i);
            means[i] = this.columns.get(col).mean();
        }
        return new Series(means, Index.of(this.columnNames.toArray(new String[0])), "mean", null);
    }

    /**
     * std方法。
     *
     * @return Series类型返回值
     */
    public Series std() {
        double[] stds = new double[this.columnNames.size()];
        for (int i = 0; i < this.columnNames.size(); i++) {
            String col = this.columnNames.get(i);
            stds[i] = this.columns.get(col).std();
        }
        return new Series(stds, Index.of(this.columnNames.toArray(new String[0])), "std", null);
    }

    /**
     * var方法。
     *
     * @return Series类型返回值
     */
    public Series var() {
        double[] vars = new double[this.columnNames.size()];
        for (int i = 0; i < this.columnNames.size(); i++) {
            String col = this.columnNames.get(i);
            vars[i] = this.columns.get(col).var();
        }
        return new Series(vars, Index.of(this.columnNames.toArray(new String[0])), "var", null);
    }

    /**
     * min方法。
     *
     * @return Series类型返回值
     */
    public Series min() {
        double[] mins = new double[this.columnNames.size()];
        for (int i = 0; i < this.columnNames.size(); i++) {
            String col = this.columnNames.get(i);
            mins[i] = this.columns.get(col).min();
        }
        return new Series(mins, Index.of(this.columnNames.toArray(new String[0])), "min", null);
    }

    /**
     * max方法。
     *
     * @return Series类型返回值
     */
    public Series max() {
        double[] maxs = new double[this.columnNames.size()];
        for (int i = 0; i < this.columnNames.size(); i++) {
            String col = this.columnNames.get(i);
            maxs[i] = this.columns.get(col).max();
        }
        return new Series(maxs, Index.of(this.columnNames.toArray(new String[0])), "max", null);
    }

    // ==================== 数据清洗 ====================

    /**
     * describe方法。
     *
     * @return DataFrame类型返回值
     */
    public DataFrame describe() {
        String[] stats = {"count", "mean", "std", "min", "25%", "50%", "75%", "max"};
        Map<String, double[]> descData = new LinkedHashMap<>();

        for (String col : this.columnNames) {
            Series series = this.columns.get(col);
            double[] values = new double[8];
            values[0] = series.count();
            values[1] = series.mean();
            values[2] = series.std();
            values[3] = series.min();
            values[4] = series.percentile(25);
            values[5] = series.percentile(50);
            values[6] = series.percentile(75);
            values[7] = series.max();
            descData.put(col, values);
        }

        return new DataFrame(descData, stats);
    }

    /**
     * fillna方法。
     * * @param value double类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame fillna(double value) {
        DataFrame result = new DataFrame();
        result.index = this.index;
        result.columnNames = new ArrayList<>(this.columnNames);

        for (String col : this.columnNames) {
            Series filled = this.columns.get(col).fillna(value);
            result.columns.put(col, filled);
        }

        return result;
    }

    /**
     * dropna方法。
     *
     * @return DataFrame类型返回值
     */
    public DataFrame dropna() {
        // 找出包含 NaN 的行
        boolean[] hasNaN = new boolean[this.nRows()];
        for (String col : this.columnNames) {
            Series series = this.columns.get(col);
            double[] values = series.toArray();
            for (int i = 0; i < values.length; i++) {
                if (Double.isNaN(values[i])) {
                    hasNaN[i] = true;
                }
            }
        }

        // 保留不包含 NaN 的行
        List<Integer> keepRows = new ArrayList<>();
        for (int i = 0; i < hasNaN.length; i++) {
            if (!hasNaN[i]) {
                keepRows.add(i);
            }
        }

        if (keepRows.isEmpty()) {
            return new DataFrame();
        }

        int start = keepRows.get(0);
        int end = keepRows.get(keepRows.size() - 1) + 1;
        return this.slice(start, end);
    }

    /**
     * 插值填充缺失值
     * 使用线性插值方法填充 NaN 值
     *
     * @return 插值后的 DataFrame
     */
    public DataFrame interpolate() {
        return interpolate("linear");
    }

    /**
     * 使用指定方法进行插值
     *
     * @param method 插值方法："linear", "forward", "backward"
     * @return 插值后的 DataFrame
     */
    public DataFrame interpolate(String method) {
        DataFrame result = new DataFrame();
        result.index = this.index;
        result.columnNames = new ArrayList<>(this.columnNames);

        for (String col : this.columnNames) {
            Series series = this.columns.get(col);
            Series interpolated;

            switch (method.toLowerCase()) {
                case "forward":
                case "ffill":
                    interpolated = com.zifang.util.pandas.interpolate.Interpolator.forward(series);
                    break;
                case "backward":
                case "bfill":
                    interpolated = com.zifang.util.pandas.interpolate.Interpolator.backward(series);
                    break;
                case "linear":
                default:
                    interpolated = com.zifang.util.pandas.interpolate.Interpolator.linear(series);
                    break;
            }

            result.columns.put(col, interpolated);
        }

        return result;
    }

    /**
     * dropDuplicates方法。
     *
     * @return DataFrame类型返回值
     */
    public DataFrame dropDuplicates() {
        // 简化实现：基于所有列去重
        Set<List<Double>> seen = new LinkedHashSet<>();
        List<Integer> keepRows = new ArrayList<>();

        for (int i = 0; i < this.nRows(); i++) {
            List<Double> row = new ArrayList<>();
            for (String col : this.columnNames) {
                row.add(this.columns.get(col).toArray()[i]);
            }
            if (!seen.contains(row)) {
                seen.add(row);
                keepRows.add(i);
            }
        }

        if (keepRows.isEmpty()) {
            return new DataFrame();
        }

        int start = keepRows.get(0);
        int end = keepRows.get(keepRows.size() - 1) + 1;
        return this.slice(start, end);
    }

    // ==================== 分组聚合 ====================

    /**
     * dropDuplicates方法。
     * * @param columns String...类型参数
     *
     * @return DataFrame类型返回值
     */
    public DataFrame dropDuplicates(String... columns) {
        // 基于指定列去重
        Set<List<Double>> seen = new LinkedHashSet<>();
        List<Integer> keepRows = new ArrayList<>();

        for (int i = 0; i < this.nRows(); i++) {
            List<Double> row = new ArrayList<>();
            for (String col : columns) {
                row.add(this.columns.get(col).toArray()[i]);
            }
            if (!seen.contains(row)) {
                seen.add(row);
                keepRows.add(i);
            }
        }

        if (keepRows.isEmpty()) {
            return new DataFrame();
        }

        int start = keepRows.get(0);
        int end = keepRows.get(keepRows.size() - 1) + 1;
        return this.slice(start, end);
    }

    /**
     * groupby方法。
     * * @param column String类型参数
     *
     * @return GroupBy类型返回值
     */
    public GroupBy groupby(String column) {
        return new GroupBy(this, column);
    }

    // ==================== 连接和合并 ====================

    /**
     * join方法。
     * * @param other DataFrame类型参数
     *
     * @param how String类型参数
     * @return DataFrame类型返回值
     */
    public DataFrame join(DataFrame other, String how) {
        // 简化实现：基于索引连接
        return merge(this, other, this.index.toArray()[0], other.index.toArray()[0], how);
    }

    /**
     * toArray方法。
     *
     * @return double[][]类型返回值
     */
    public double[][] toArray() {
        double[][] result = new double[this.nRows()][this.nCols()];
        for (int i = 0; i < this.nRows(); i++) {
            for (int j = 0; j < this.columnNames.size(); j++) {
                String col = this.columnNames.get(j);
                result[i][j] = this.columns.get(col).toArray()[i];
            }
        }
        return result;
    }

    // ==================== 数据导出 ====================

    /**
     * toMap方法。
     *
     * @return Map<String, double[]>类型返回值
     */
    public Map<String, double[]> toMap() {
        Map<String, double[]> result = new LinkedHashMap<>();
        for (String col : this.columnNames) {
            result.put(col, this.columns.get(col).toArray());
        }
        return result;
    }

    /**
     * toCSV方法。
     *
     * @return String类型返回值
     */
    public String toCSV() {
        StringBuilder sb = new StringBuilder();

        // 表头
        sb.append(String.join(",", this.columnNames)).append("\n");

        // 数据行
        for (int i = 0; i < this.nRows(); i++) {
            List<String> row = new ArrayList<>();
            for (String col : this.columnNames) {
                row.add(String.valueOf(this.columns.get(col).toArray()[i]));
            }
            sb.append(String.join(",", row)).append("\n");
        }

        return sb.toString();
    }

    /**
     * toJSON方法。
     *
     * @return String类型返回值
     */
    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < this.nRows(); i++) {
            if (i > 0) sb.append(",");
            sb.append("{");

            List<String> fields = new ArrayList<>();
            for (String col : this.columnNames) {
                double val = this.columns.get(col).toArray()[i];
                fields.add("\"" + col + "\":" + val);
            }
            sb.append(String.join(",", fields));
            sb.append("}");
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 基本信息
        sb.append("DataFrame(").append(this.nRows()).append(" rows x ").append(this.nCols()).append(" columns)\n");

        // 列名
        sb.append("Columns: ").append(String.join(", ", this.columnNames)).append("\n");

        // 打印前几行
        int displayRows = Math.min(this.nRows(), 10);
        for (int i = 0; i < displayRows; i++) {
            sb.append(this.index.get(i)).append(" ");
            for (String col : this.columnNames) {
                sb.append(this.columns.get(col).toArray()[i]).append(" ");
            }
            sb.append("\n");
        }

        if (this.nRows() > 10) {
            sb.append("... (").append(this.nRows() - 10).append(" more rows)\n");
        }

        return sb.toString();
    }

    // ==================== 描述和打印 ====================

    /**
     * print方法。
     */
    public void print() {
        System.out.println(this.toString());
    }

    /**
     * info方法。
     */
    public void info() {
        System.out.println("<class 'util-math.DataFrame'>");
        System.out.println("RangeIndex: " + this.nRows() + " entries, 0 to " + (this.nRows() - 1));
        System.out.println("Data columns (total " + this.nCols() + " columns):");
        System.out.println(" #   Column  Non-Null Count  Dtype");
        System.out.println("---  ------  --------------  -----");
        for (int i = 0; i < this.columnNames.size(); i++) {
            String col = this.columnNames.get(i);
            System.out.printf(" %d   %s  %d non-null     float64%n", i, col, this.nRows());
        }
    }

    /**
     * GroupBy类。
     */
    public class GroupBy {
        private DataFrame df;
        private String column;
        private Map<Double, List<Integer>> groups;

        /**
         * GroupBy方法。
         * * @param df DataFrame类型参数
         *
         * @param column String类型参数
         */
        public GroupBy(DataFrame df, String column) {
            this.df = df;
            this.column = column;
            this.groups = new HashMap<>();

            Series series = df.get(column);
            double[] values = series.toArray();
            for (int i = 0; i < values.length; i++) {
                groups.computeIfAbsent(values[i], k -> new ArrayList<>()).add(i);
            }
        }

        /**
         * sum方法。
         *
         * @return DataFrame类型返回值
         */
        public DataFrame sum() {
            return aggregate(Series::sum);
        }

        /**
         * mean方法。
         *
         * @return DataFrame类型返回值
         */
        public DataFrame mean() {
            return aggregate(Series::mean);
        }

        /**
         * count方法。
         *
         * @return DataFrame类型返回值
         */
        public DataFrame count() {
            return aggregate(series -> Double.valueOf(series.count()));
        }

        /**
         * min方法。
         *
         * @return DataFrame类型返回值
         */
        public DataFrame min() {
            return aggregate(Series::min);
        }

        /**
         * max方法。
         *
         * @return DataFrame类型返回值
         */
        public DataFrame max() {
            return aggregate(Series::max);
        }

        /**
         * std方法。
         *
         * @return DataFrame类型返回值
         */
        public DataFrame std() {
            return aggregate(Series::std);
        }

        /**
         * var方法。
         *
         * @return DataFrame类型返回值
         */
        public DataFrame var() {
            return aggregate(Series::var);
        }

        private DataFrame aggregate(Function<Series, Double> aggFunc) {
            List<String> groupKeys = new ArrayList<>();
            List<Double> groupValues = new ArrayList<>();

            for (Map.Entry<Double, List<Integer>> entry : groups.entrySet()) {
                groupKeys.add(String.valueOf(entry.getKey()));
                groupValues.add(entry.getKey());
            }

            Map<String, double[]> resultData = new LinkedHashMap<>();
            resultData.put(column, groupValues.stream().mapToDouble(Double::doubleValue).toArray());

            for (String col : df.columnNames) {
                if (col.equals(column)) continue;

                double[] aggValues = new double[groups.size()];
                int i = 0;
                for (List<Integer> indices : groups.values()) {
                    double[] groupData = new double[indices.size()];
                    Series series = df.get(col);
                    for (int j = 0; j < indices.size(); j++) {
                        groupData[j] = series.toArray()[indices.get(j)];
                    }
                    Series groupSeries = new Series(groupData);
                    aggValues[i++] = aggFunc.apply(groupSeries);
                }
                resultData.put(col, aggValues);
            }

            return new DataFrame(resultData, groupKeys.toArray(new String[0]));
        }
    }
}
