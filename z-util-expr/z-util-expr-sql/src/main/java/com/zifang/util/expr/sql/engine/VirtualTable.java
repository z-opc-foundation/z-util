package com.zifang.util.expr.sql.engine;

import java.util.*;

/**
 * 虚拟表，封装 List&lt;Map&lt;String, Object&gt;&gt; 数据。
 * <p>
 * 每行是一个 Map（列名 → 值），支持按列名取值、获取所有列名、行迭代等操作。
 * 支持按列创建索引，加速等值查询和 JOIN 操作。
 */
public class VirtualTable {

    private final List<Map<String, Object>> rows;
    private final Set<String> columns;
    /** 索引：列名 → (值 → 行索引列表) */
    private final Map<String, Map<Object, List<Integer>>> indexes = new HashMap<>();

    public VirtualTable(List<Map<String, Object>> rows) {
        this.rows = rows != null ? rows : Collections.emptyList();
        this.columns = inferColumns(this.rows);
    }

    private static Set<String> inferColumns(List<Map<String, Object>> rows) {
        Set<String> cols = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            cols.addAll(row.keySet());
        }
        return cols;
    }

    // ==================== 索引操作 ====================

    /**
     * 为指定列创建哈希索引。
     */
    public void createIndex(String columnName) {
        if (columnName == null) return;
        String lowerCol = columnName.toLowerCase();
        if (indexes.containsKey(lowerCol)) return;

        Map<Object, List<Integer>> index = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            Object val = getCellValue(rows.get(i), columnName);
            if (val == null) continue;
            index.computeIfAbsent(val, k -> new ArrayList<>()).add(i);
        }
        indexes.put(lowerCol, index);
    }

    /**
     * 检查指定列是否已有索引。
     */
    public boolean hasIndex(String columnName) {
        return columnName != null && indexes.containsKey(columnName.toLowerCase());
    }

    /**
     * 通过索引查找指定列等于给定值的所有行。
     */
    public List<Map<String, Object>> lookupByIndex(String columnName, Object value) {
        if (columnName == null || value == null) return null;
        Map<Object, List<Integer>> index = indexes.get(columnName.toLowerCase());
        if (index == null) return null;

        List<Integer> rowIndices = lookupIndexInternal(index, value);
        if (rowIndices == null) return null;

        List<Map<String, Object>> result = new ArrayList<>(rowIndices.size());
        for (int idx : rowIndices) {
            result.add(rows.get(idx));
        }
        return result;
    }

    /**
     * 通过索引查找，返回行索引列表。
     */
    public List<Integer> lookupIndicesByIndex(String columnName, Object value) {
        if (columnName == null || value == null) return null;
        Map<Object, List<Integer>> index = indexes.get(columnName.toLowerCase());
        if (index == null) return null;

        return lookupIndexInternal(index, value);
    }

    /**
     * 索引内部查找，处理数值类型不匹配（Integer vs Long）。
     * HashMap.get() 使用 equals()，Integer.equals(Long) 返回 false，
     * 所以需要特殊处理数值类型的跨类型查找。
     */
    private List<Integer> lookupIndexInternal(Map<Object, List<Integer>> index, Object value) {
        // 直接查找
        List<Integer> result = index.get(value);
        if (result != null) return result;

        // 数值类型回退：HashMap.get() 使用 equals()，Integer.equals(Long) 返回 false，
        // 所以当 value 是 Number 时，用 double 值进行跨类型数值匹配
        if (value instanceof Number) {
            double targetDouble = ((Number) value).doubleValue();
            for (Map.Entry<Object, List<Integer>> entry : index.entrySet()) {
                if (entry.getKey() instanceof Number) {
                    double keyDouble = ((Number) entry.getKey()).doubleValue();
                    if (Double.compare(targetDouble, keyDouble) == 0) {
                        return entry.getValue();
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * 获取指定列的所有不重复索引值。
     */
    public Set<Object> getIndexedValues(String columnName) {
        if (columnName == null) return null;
        Map<Object, List<Integer>> index = indexes.get(columnName.toLowerCase());
        return index != null ? index.keySet() : null;
    }

    // ==================== 数据访问 ====================

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public Map<String, Object> getRow(int index) {
        return rows.get(index);
    }

    public Set<String> getColumns() {
        return columns;
    }

    public List<String> getColumnNames() {
        return new ArrayList<>(columns);
    }

    public static Object getCellValue(Map<String, Object> row, String columnName) {
        if (row == null || columnName == null) return null;
        Object val = row.get(columnName);
        if (val != null || row.containsKey(columnName)) return val;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (columnName.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Object getValue(int rowIndex, String columnName) {
        return getCellValue(rows.get(rowIndex), columnName);
    }

    /**
     * 转为 Table 包装对象。
     */
    public Table toTable(String name) {
        return Table.fromVirtualTable(this, name);
    }
}
