package com.zifang.util.pandas.transform;

import com.zifang.util.pandas.DataFrame;
import com.zifang.util.pandas.Series;
import com.zifang.util.pandas.num.Index;

import java.util.*;

/**
 * Reshaper 类 - 数据重塑和转换操作
 * 对标 pandas 的 pivot, melt, stack, unstack 等功能
 * 提供数据透视、长宽转换、堆叠展开等数据转换能力
 */
public class Reshaper {

    /**
     * 数据透视 - 类似于 Excel 的数据透视表
     * 将长格式数据转换为宽格式
     *
     * @param df      输入数据框
     * @param index   作为行索引的列名
     * @param columns 作为列名的列名
     * @param values  作为值的列名
     * @return 透视后的数据框
     */
    public static DataFrame pivot(DataFrame df, String index, String columns, String values) {
        // 获取唯一索引值和列值
        Series indexSeries = df.get(index);
        Series columnSeries = df.get(columns);
        Series valuesSeries = df.get(values);

        // 收集唯一的索引和列值
        Set<String> uniqueIndex = new LinkedHashSet<>();
        Set<String> uniqueColumns = new LinkedHashSet<>();

        for (int i = 0; i < indexSeries.length(); i++) {
            uniqueIndex.add(String.valueOf(indexSeries.toArray()[i]));
        }

        for (int i = 0; i < columnSeries.length(); i++) {
            uniqueColumns.add(String.valueOf(columnSeries.toArray()[i]));
        }

        // 创建透视表数据结构
        List<String> indexList = new ArrayList<>(uniqueIndex);
        List<String> columnList = new ArrayList<>(uniqueColumns);

        // 使用 Map 存储值
        Map<String, Map<String, Double>> pivotData = new HashMap<>();
        for (String idx : indexList) {
            pivotData.put(idx, new HashMap<>());
        }

        // 填充数据
        for (int i = 0; i < indexSeries.length(); i++) {
            String idx = String.valueOf(indexSeries.toArray()[i]);
            String col = String.valueOf(columnSeries.toArray()[i]);
            double val = valuesSeries.toArray()[i];
            pivotData.get(idx).put(col, val);
        }

        // 创建结果 DataFrame
        Map<String, double[]> resultData = new LinkedHashMap<>();

        // 第一列是索引
        double[] indexValues = new double[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            try {
                indexValues[i] = Double.parseDouble(indexList.get(i));
            } catch (NumberFormatException e) {
                indexValues[i] = i;
            }
        }
        resultData.put(index, indexValues);

        // 其他列是透视的列
        for (String col : columnList) {
            double[] colValues = new double[indexList.size()];
            for (int i = 0; i < indexList.size(); i++) {
                String idx = indexList.get(i);
                Double val = pivotData.get(idx).get(col);
                colValues[i] = val != null ? val : Double.NaN;
            }
            resultData.put(col, colValues);
        }

        return new DataFrame(resultData, indexList.toArray(new String[0]));
    }

    /**
     * 数据融合 - 将宽格式数据转换为长格式
     * 是 pivot 的逆操作
     *
     * @param df        输入数据框
     * @param idVars    作为标识变量的列名列表
     * @param valueVars 需要融合的列名列表
     * @param varName   变量列的新名称
     * @param valueName 值列的新名称
     * @return 融合后的数据框
     */
    public static DataFrame melt(DataFrame df, List<String> idVars, List<String> valueVars,
                                 String varName, String valueName) {
        List<Map<String, Object>> records = new ArrayList<>();

        for (int i = 0; i < df.nRows(); i++) {
            for (String valueVar : valueVars) {
                Map<String, Object> record = new LinkedHashMap<>();

                // 添加标识变量
                for (String idVar : idVars) {
                    record.put(idVar, df.get(idVar).toArray()[i]);
                }

                // 添加变量名和值
                record.put(varName, valueVar);
                record.put(valueName, df.get(valueVar).toArray()[i]);

                records.add(record);
            }
        }

        return new DataFrame(records);
    }

    /**
     * 简化版 melt - 使用默认列名
     */
    public static DataFrame melt(DataFrame df, List<String> idVars, List<String> valueVars) {
        return melt(df, idVars, valueVars, "variable", "value");
    }

    /**
     * 堆叠操作 - 将列索引转换为行索引
     * 将宽格式转换为长格式
     *
     * @param df 输入数据框
     * @return 堆叠后的 Series 或 DataFrame
     */
    public static Series stack(DataFrame df) {
        List<Double> values = new ArrayList<>();
        List<String> newIndex = new ArrayList<>();

        String[] indexLabels = df.index().toArray();
        List<String> columns = df.columns();

        for (int i = 0; i < df.nRows(); i++) {
            for (int j = 0; j < columns.size(); j++) {
                String combinedIndex = indexLabels[i] + "_" + columns.get(j);
                newIndex.add(combinedIndex);
                values.add(df.get(columns.get(j)).toArray()[i]);
            }
        }

        double[] valueArray = values.stream().mapToDouble(Double::doubleValue).toArray();
        return new Series(valueArray, Index.of(newIndex.toArray(new String[0])), "stacked", null);
    }

    /**
     * 取消堆叠操作 - 将行索引转换为列索引
     * 将长格式转换为宽格式
     *
     * @param series 输入 Series（多级索引）
     * @return 取消堆叠后的 DataFrame
     */
    public static DataFrame unstack(Series series) {
        // 解析多级索引
        String[] indexParts = series.index().toArray();

        // 收集唯一的级别值
        Set<String> level0Set = new LinkedHashSet<>();
        Set<String> level1Set = new LinkedHashSet<>();

        Map<String, String> indexMap = new HashMap<>();
        for (String part : indexParts) {
            String[] parts = part.split("_", 2);
            if (parts.length == 2) {
                level0Set.add(parts[0]);
                level1Set.add(parts[1]);
                indexMap.put(part, parts[0] + "_" + parts[1]);
            }
        }

        List<String> level0List = new ArrayList<>(level0Set);
        List<String> level1List = new ArrayList<>(level1Set);

        // 创建 DataFrame
        Map<String, double[]> data = new LinkedHashMap<>();

        // 添加行索引列
        double[] rowIndexValues = new double[level0List.size()];
        for (int i = 0; i < level0List.size(); i++) {
            try {
                rowIndexValues[i] = Double.parseDouble(level0List.get(i));
            } catch (NumberFormatException e) {
                rowIndexValues[i] = i;
            }
        }

        // 为每个 level1 创建一列
        for (String level1 : level1List) {
            double[] colValues = new double[level0List.size()];
            Arrays.fill(colValues, Double.NaN);

            for (int i = 0; i < level0List.size(); i++) {
                String key = level0List.get(i) + "_" + level1;
                for (int j = 0; j < indexParts.length; j++) {
                    if (indexParts[j].equals(key)) {
                        colValues[i] = series.toArray()[j];
                        break;
                    }
                }
            }
            data.put(level1, colValues);
        }

        return new DataFrame(data, level0List.toArray(new String[0]));
    }

    /**
     * 交叉表 - 创建列联表
     *
     * @param df      输入数据框
     * @param rowVar  行变量列名
     * @param colVar  列变量列名
     * @param values  值列名（可选，如果不提供则计数）
     * @param aggFunc 聚合函数（可选，如果 values 提供则需要）
     * @return 交叉表 DataFrame
     */
    public static DataFrame crosstab(DataFrame df, String rowVar, String colVar) {
        Series rowSeries = df.get(rowVar);
        Series colSeries = df.get(colVar);

        // 收集唯一的行和列值
        Set<String> uniqueRows = new LinkedHashSet<>();
        Set<String> uniqueCols = new LinkedHashSet<>();

        for (int i = 0; i < rowSeries.length(); i++) {
            uniqueRows.add(String.valueOf(rowSeries.toArray()[i]));
        }

        for (int i = 0; i < colSeries.length(); i++) {
            uniqueCols.add(String.valueOf(colSeries.toArray()[i]));
        }

        List<String> rowList = new ArrayList<>(uniqueRows);
        List<String> colList = new ArrayList<>(uniqueCols);

        // 创建计数矩阵
        Map<String, Map<String, Integer>> counts = new HashMap<>();
        for (String row : rowList) {
            counts.put(row, new HashMap<>());
            for (String col : colList) {
                counts.get(row).put(col, 0);
            }
        }

        // 计数
        for (int i = 0; i < rowSeries.length(); i++) {
            String row = String.valueOf(rowSeries.toArray()[i]);
            String col = String.valueOf(colSeries.toArray()[i]);
            counts.get(row).put(col, counts.get(row).get(col) + 1);
        }

        // 创建 DataFrame
        Map<String, double[]> resultData = new LinkedHashMap<>();

        // 行索引列
        double[] rowIndexValues = new double[rowList.size()];
        for (int i = 0; i < rowList.size(); i++) {
            try {
                rowIndexValues[i] = Double.parseDouble(rowList.get(i));
            } catch (NumberFormatException e) {
                rowIndexValues[i] = i;
            }
        }
        resultData.put(rowVar, rowIndexValues);

        // 交叉表数据列
        for (String col : colList) {
            double[] colValues = new double[rowList.size()];
            for (int i = 0; i < rowList.size(); i++) {
                colValues[i] = counts.get(rowList.get(i)).get(col);
            }
            resultData.put(col, colValues);
        }

        return new DataFrame(resultData, rowList.toArray(new String[0]));
    }

    /**
     * 转置 DataFrame
     *
     * @param df 输入数据框
     * @return 转置后的数据框
     */
    public static DataFrame transpose(DataFrame df) {
        // 转置后：原列名变为行索引，原行索引变为列名
        List<String> newColumnNames = new ArrayList<>();
        newColumnNames.add("index");

        String[] originalIndex = df.index().toArray();
        for (String idx : originalIndex) {
            newColumnNames.add(idx);
        }

        List<String> originalColumns = df.columns();
        Map<String, double[]> resultData = new LinkedHashMap<>();

        // 第一列是原列名
        double[] firstCol = new double[originalColumns.size()];
        for (int i = 0; i < originalColumns.size(); i++) {
            try {
                firstCol[i] = Double.parseDouble(originalColumns.get(i));
            } catch (NumberFormatException e) {
                firstCol[i] = i;
            }
        }
        resultData.put("index", firstCol);

        // 其他列是原行数据
        for (int rowIdx = 0; rowIdx < originalIndex.length; rowIdx++) {
            double[] colData = new double[originalColumns.size()];
            for (int colIdx = 0; colIdx < originalColumns.size(); colIdx++) {
                colData[colIdx] = df.get(originalColumns.get(colIdx)).toArray()[rowIdx];
            }
            resultData.put(originalIndex[rowIdx], colData);
        }

        return new DataFrame(resultData, originalColumns.toArray(new String[0]));
    }

    /**
     * 简化版 pivot 表 - 直接指定数据
     */
    public static DataFrame pivotTable(DataFrame df, List<String> index, List<String> columns,
                                       String values, String aggFunc) {
        // 这是一个简化实现，实际应该根据 aggFunc 进行聚合
        // 这里假设 index 和 columns 各只有一列
        if (index.size() == 1 && columns.size() == 1) {
            return pivot(df, index.get(0), columns.get(0), values, aggFunc);
        }

        // 对于更复杂的情况，返回原数据框
        return df;
    }

    /**
     * 带聚合函数的 pivot
     */
    public static DataFrame pivot(DataFrame df, String index, String columns, String values, String aggFunc) {
        // 按索引和列分组并聚合
        Series indexSeries = df.get(index);
        Series columnSeries = df.get(columns);
        Series valuesSeries = df.get(values);

        // 收集唯一的行和列索引
        Set<String> uniqueRows = new LinkedHashSet<>();
        Set<String> uniqueCols = new LinkedHashSet<>();
        Map<String, Map<String, List<Double>>> groupData = new HashMap<>();

        for (int i = 0; i < indexSeries.length(); i++) {
            String rowKey = String.valueOf(indexSeries.toArray()[i]);
            String colKey = String.valueOf(columnSeries.toArray()[i]);
            double val = valuesSeries.toArray()[i];

            uniqueRows.add(rowKey);
            uniqueCols.add(colKey);

            groupData.computeIfAbsent(rowKey, k -> new HashMap<>())
                    .computeIfAbsent(colKey, k -> new ArrayList<>())
                    .add(val);
        }

        List<String> rowList = new ArrayList<>(uniqueRows);
        List<String> colList = new ArrayList<>(uniqueCols);

        // 聚合数据
        Map<String, double[]> resultData = new LinkedHashMap<>();

        // 行索引列
        double[] rowIndexValues = new double[rowList.size()];
        for (int i = 0; i < rowList.size(); i++) {
            try {
                rowIndexValues[i] = Double.parseDouble(rowList.get(i));
            } catch (NumberFormatException e) {
                rowIndexValues[i] = i;
            }
        }
        resultData.put(index, rowIndexValues);

        // 聚合函数处理
        for (String col : colList) {
            double[] colValues = new double[rowList.size()];
            for (int i = 0; i < rowList.size(); i++) {
                String row = rowList.get(i);
                List<Double> vals = groupData.getOrDefault(row, new HashMap<>()).getOrDefault(col, new ArrayList<>());

                if (vals.isEmpty()) {
                    colValues[i] = Double.NaN;
                } else {
                    colValues[i] = aggregateValues(vals, aggFunc);
                }
            }
            resultData.put(col, colValues);
        }

        return new DataFrame(resultData, rowList.toArray(new String[0]));
    }

    /**
     * 聚合函数实现
     */
    private static double aggregateValues(List<Double> values, String aggFunc) {
        switch (aggFunc.toLowerCase()) {
            case "mean":
                return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            case "sum":
                return values.stream().mapToDouble(Double::doubleValue).sum();
            case "min":
                return values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            case "max":
                return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            case "count":
                return values.size();
            case "std":
                double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
                return Math.sqrt(variance);
            default:
                return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }
    }
}
