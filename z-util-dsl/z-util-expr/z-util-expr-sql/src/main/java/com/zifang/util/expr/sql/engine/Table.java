package com.zifang.util.expr.sql.engine;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 增强型虚拟表，支持链式数据操作、聚合、分组、索引和 SQL 查询。
 *
 * <pre>
 * Table users = Table.fromMaps("users", listOfMaps);
 * Table result = users
 *     .where("age", 18)                    // 等值过滤
 *     .where(row -> row.getInt("age") > 18) // 条件过滤
 *     .sort("name", true)                   // 排序
 *     .select("id", "name", "age");         // 列投影
 *
 * long count = users.count();
 * Object maxAge = users.max("age");
 * Map&lt;String, Table&gt; groups = users.groupBy("dept");
 * List&lt;UserDTO&gt; dtos = users.queryAs("SELECT * WHERE age > 18", UserDTO.class);
 * </pre>
 */
public class Table implements Iterable<Row> {

    private final String name;
    private final List<Row> rows;
    private final List<String> columns;
    private final Map<String, Map<Object, List<Integer>>> indexes = new HashMap<>();

    // ==================== 构造 ====================

    public Table(String name) {
        this.name = name;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
    }

    public Table(String name, List<Map<String, Object>> data) {
        this.name = name;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
        if (data != null) {
            for (Map<String, Object> map : data) {
                this.rows.add(new Row(map));
                for (String col : map.keySet()) {
                    if (!this.columns.contains(col)) {
                        this.columns.add(col);
                    }
                }
            }
        }
    }

    public Table(String name, List<Row> rows, List<String> columns) {
        this.name = name;
        this.rows = rows != null ? new ArrayList<>(rows) : new ArrayList<>();
        this.columns = columns != null ? new ArrayList<>(columns) : new ArrayList<>();
    }

    // ==================== 基础信息 ====================

    public String getName() { return name; }
    public int size() { return rows.size(); }
    public boolean isEmpty() { return rows.isEmpty(); }
    public List<String> getColumnNames() { return Collections.unmodifiableList(columns); }
    public Set<String> getColumns() { return Collections.unmodifiableSet(new LinkedHashSet<>(columns)); }

    // ==================== 行访问 ====================

    public Row getRow(int index) { return rows.get(index); }
    public List<Row> getRows() { return Collections.unmodifiableList(rows); }
    public Row first() { return rows.isEmpty() ? null : rows.get(0); }
    public Row last() { return rows.isEmpty() ? null : rows.get(rows.size() - 1); }

    // ==================== 行追加/删除 ====================

    public Table addRow(Row row) {
        rows.add(row);
        updateColumnsFromRow(row);
        return this;
    }

    public Table addRow(Object... kv) {
        return addRow(Row.of(kv));
    }

    public Table addRow(Map<String, Object> map) {
        return addRow(new Row(map));
    }

    public Table addRows(List<Row> newRows) {
        for (Row row : newRows) {
            addRow(row);
        }
        return this;
    }

    public Table removeRow(int index) {
        rows.remove(index);
        return this;
    }

    public Table removeRow(Row row) {
        rows.remove(row);
        return this;
    }

    // ==================== 列操作 ====================

    public Table addColumn(String name, Object defaultValue) {
        for (Row row : rows) {
            row.set(name, defaultValue);
        }
        ensureColumn(name);
        return this;
    }

    public Table addColumn(String name, Function<Row, Object> computer) {
        for (Row row : rows) {
            row.set(name, computer.apply(row));
        }
        ensureColumn(name);
        return this;
    }

    public Table dropColumn(String name) {
        for (Row row : rows) {
            row.remove(name);
        }
        columns.remove(name);
        return this;
    }

    public Table renameColumn(String oldName, String newName) {
        int idx = columns.indexOf(oldName);
        if (idx >= 0) {
            columns.set(idx, newName);
        }
        for (Row row : rows) {
            row.rename(oldName, newName);
        }
        return this;
    }

    // ==================== 数据操作（链式） ====================

    public Table where(Predicate<Row> predicate) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            if (predicate.test(row)) {
                result.add(row);
            }
        }
        return new Table(name, result, new ArrayList<>(columns));
    }

    public Table where(String column, Object value) {
        return where(row -> {
            Object cell = row.get(column);
            return value == null ? cell == null : value.equals(cell);
        });
    }

    public Table sort(Comparator<Row> comparator) {
        List<Row> sorted = new ArrayList<>(rows);
        sorted.sort(comparator);
        return new Table(name, sorted, new ArrayList<>(columns));
    }

    public Table sort(String column, boolean ascending) {
        return sort((a, b) -> {
            Object va = a.get(column);
            Object vb = b.get(column);
            int cmp = compareValues(va, vb);
            return ascending ? cmp : -cmp;
        });
    }

    public Table limit(int count) {
        int end = Math.min(rows.size(), count);
        return new Table(name, new ArrayList<>(rows.subList(0, end)), new ArrayList<>(columns));
    }

    public Table limit(int count, int offset) {
        int start = Math.max(0, offset);
        int end = Math.min(rows.size(), start + count);
        if (start >= rows.size()) return new Table(name, new ArrayList<>(), new ArrayList<>(columns));
        return new Table(name, new ArrayList<>(rows.subList(start, end)), new ArrayList<>(columns));
    }

    public Table distinct(String... cols) {
        Set<String> keyCols = cols.length == 0 ? new LinkedHashSet<>(columns) : new LinkedHashSet<>(Arrays.asList(cols));
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            StringBuilder sb = new StringBuilder();
            for (String col : keyCols) {
                sb.append(row.get(col)).append("\0");
            }
            if (seen.add(sb.toString())) {
                result.add(row);
            }
        }
        return new Table(name, result, new ArrayList<>(columns));
    }

    // ==================== 列投影 ====================

    public Table select(String... cols) {
        List<String> selectCols = Arrays.asList(cols);
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            Row projected = new Row();
            for (String col : selectCols) {
                projected.set(col, row.get(col));
            }
            result.add(projected);
        }
        return new Table(name, result, new ArrayList<>(selectCols));
    }

    // ==================== 映射/转换 ====================

    public <T> List<T> mapToBean(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Row row : rows) {
            result.add(row.toBean(clazz));
        }
        return result;
    }

    public <T> List<T> map(Function<Row, T> mapper) {
        List<T> result = new ArrayList<>();
        for (Row row : rows) {
            result.add(mapper.apply(row));
        }
        return result;
    }

    public List<Map<String, Object>> toMapList() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Row row : rows) {
            result.add(row.toMapCopy());
        }
        return result;
    }

    public List<Row> toList() {
        return Collections.unmodifiableList(rows);
    }

    // ==================== 聚合 ====================

    public long count() { return rows.size(); }

    public Object max(String column) {
        Object max = null;
        for (Row row : rows) {
            Object val = row.get(column);
            if (val != null && (max == null || compareValues(val, max) > 0)) {
                max = val;
            }
        }
        return max;
    }

    public Object min(String column) {
        Object min = null;
        for (Row row : rows) {
            Object val = row.get(column);
            if (val != null && (min == null || compareValues(val, min) < 0)) {
                min = val;
            }
        }
        return min;
    }

    public Object sum(String column) {
        double sum = 0;
        boolean hasValue = false;
        for (Row row : rows) {
            Object val = row.get(column);
            if (val instanceof Number) {
                sum += ((Number) val).doubleValue();
                hasValue = true;
            }
        }
        return hasValue ? sum : null;
    }

    public Object avg(String column) {
        double sum = 0;
        long count = 0;
        for (Row row : rows) {
            Object val = row.get(column);
            if (val instanceof Number) {
                sum += ((Number) val).doubleValue();
                count++;
            }
        }
        return count > 0 ? sum / count : null;
    }

    public Object aggregate(String column, String function) {
        switch (function.toUpperCase()) {
            case "COUNT": return (long) rows.size();
            case "SUM":   return sum(column);
            case "AVG":   return avg(column);
            case "MAX":   return max(column);
            case "MIN":   return min(column);
            default: throw new IllegalArgumentException("未知聚合函数: " + function);
        }
    }

    // ==================== 分组 ====================

    public Map<String, Table> groupBy(String column) {
        return groupBy(row -> String.valueOf(row.get(column)));
    }

    public Map<String, Table> groupBy(Function<Row, String> keyExtractor) {
        Map<String, List<Row>> groups = new LinkedHashMap<>();
        for (Row row : rows) {
            String key = keyExtractor.apply(row);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        Map<String, Table> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Row>> entry : groups.entrySet()) {
            result.put(entry.getKey(), new Table(name + "." + entry.getKey(), entry.getValue(), new ArrayList<>(columns)));
        }
        return result;
    }

    // ==================== 索引 ====================

    public Table createIndex(String columnName) {
        String lowerCol = columnName.toLowerCase();
        if (indexes.containsKey(lowerCol)) return this;
        Map<Object, List<Integer>> index = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            Object val = rows.get(i).get(columnName);
            if (val == null) continue;
            index.computeIfAbsent(val, k -> new ArrayList<>()).add(i);
        }
        indexes.put(lowerCol, index);
        return this;
    }

    public Table createIndex(String... columnNames) {
        for (String col : columnNames) {
            createIndex(col);
        }
        return this;
    }

    public boolean hasIndex(String columnName) {
        return columnName != null && indexes.containsKey(columnName.toLowerCase());
    }

    public List<Row> lookupByIndex(String columnName, Object value) {
        if (columnName == null || value == null) return Collections.emptyList();
        Map<Object, List<Integer>> index = indexes.get(columnName.toLowerCase());
        if (index == null) return Collections.emptyList();
        List<Integer> indices = index.get(value);
        if (indices == null) {
            // 数值类型回退
            if (value instanceof Number) {
                double target = ((Number) value).doubleValue();
                for (Map.Entry<Object, List<Integer>> entry : index.entrySet()) {
                    if (entry.getKey() instanceof Number && Double.compare(target, ((Number) entry.getKey()).doubleValue()) == 0) {
                        indices = entry.getValue();
                        break;
                    }
                }
            }
            if (indices == null) return Collections.emptyList();
        }
        List<Row> result = new ArrayList<>(indices.size());
        for (int idx : indices) {
            result.add(rows.get(idx));
        }
        return result;
    }

    // ==================== SQL 查询 ====================

    public Table query(String sql) {
        VirtualTableEngine engine = new VirtualTableEngine();
        engine.register(name, toMapList());
        String finalSql = buildQuerySql(sql);
        List<Map<String, Object>> result = engine.query(finalSql);
        return fromMaps(name, result);
    }

    /**
     * 将用户 SQL 转为完整 SELECT 语句。
     * - "SELECT * WHERE age > 30" → "SELECT * FROM users WHERE age > 30"
     * - "SELECT name FROM users WHERE age > 30" → 原样使用（含 FROM）
     * - "WHERE age > 30" → "SELECT * FROM users WHERE age > 30"
     */
    private String buildQuerySql(String sql) {
        String trimmed = sql.trim();
        String upper = trimmed.toUpperCase();

        // 已包含 FROM 子句，直接使用（替换 FROM 后的表名为本表）
        if (upper.contains(" FROM ")) {
            int fromIdx = upper.indexOf(" FROM ");
            String before = trimmed.substring(0, fromIdx + 6); // "SELECT * FROM "
            String after = trimmed.substring(fromIdx + 6).trim();
            // 如果 FROM 后的表名不是本表名，替换之
            return before + name + " " + after;
        }

        // 以 SELECT 开头但无 FROM：在 SELECT ... 后插入 FROM tableName
        if (upper.startsWith("SELECT ")) {
            // 找到 SELECT 子句的结束位置（跳过列列表）
            int selectStart = 7; // "SELECT ".length()
            int fromInsertPos = findSelectEnd(trimmed, selectStart);
            String selectPart = trimmed.substring(0, fromInsertPos);
            String wherePart = trimmed.substring(fromInsertPos).trim();
            return selectPart + " FROM " + name + (wherePart.isEmpty() ? "" : " " + wherePart);
        }

        // 非 SELECT 开头（如 "WHERE age > 30"），包装为 SELECT * FROM ...
        return "SELECT * FROM " + name + " " + trimmed;
    }

    /**
     * 找到 SELECT 子句中列列表的结束位置（即 FROM 关键字或 WHERE/ORDER BY 等的位置）。
     */
    private int findSelectEnd(String sql, int start) {
        int i = start;
        boolean inString = false;
        char stringChar = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (inString) {
                if (c == stringChar && (i + 1 >= sql.length() || sql.charAt(i + 1) != stringChar)) {
                    inString = false;
                }
            } else {
                if (isQuoteChar(c)) {
                    inString = true;
                    stringChar = c;
                } else if (c == ' ') {
                    String rest = sql.substring(i).trim().toUpperCase();
                    if (rest.startsWith("FROM ") || rest.startsWith("WHERE ") || rest.startsWith("ORDER ")
                            || rest.startsWith("GROUP ") || rest.startsWith("LIMIT ") || rest.startsWith("OFFSET ")
                            || rest.startsWith("HAVING ") || rest.startsWith("DISTINCT ")
                            || rest.isEmpty() || rest.startsWith(";")) {
                        return i;
                    }
                }
            }
            i++;
        }
        return sql.length();
    }
    
    private static boolean isQuoteChar(char c) {
        // 39 = single quote, 34 = double quote
        return c == 39 || c == 34;
    }

    public <T> List<T> queryAs(String sql, Class<T> clazz) {
        return query(sql).mapToBean(clazz);
    }

    // ==================== 格式化 ====================

    public Table head(int n) {
        return limit(n);
    }

    public Table tail(int n) {
        int start = Math.max(0, rows.size() - n);
        return new Table(name, new ArrayList<>(rows.subList(start, rows.size())), new ArrayList<>(columns));
    }

    public void print() {
        System.out.println(toPrettyString());
    }

    public String toPrettyString() {
        if (rows.isEmpty()) return name + " (empty, " + columns.size() + " columns)";

        List<String> cols = columns;
        int[] widths = new int[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            widths[i] = cols.get(i).length();
        }
        List<String[]> cellStrs = new ArrayList<>();
        for (Row row : rows) {
            String[] cells = new String[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                Object val = row.get(cols.get(i));
                cells[i] = val == null ? "NULL" : val.toString();
                widths[i] = Math.max(widths[i], cells[i].length());
            }
            cellStrs.add(cells);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Table: ").append(name).append(" (").append(rows.size()).append(" rows)\n");

        // Header
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(padRight(cols.get(i), widths[i]));
        }
        sb.append("\n");
        // Separator
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) sb.append("-+-");
            sb.append(repeat('-', widths[i]));
        }
        sb.append("\n");
        // Rows
        for (String[] cells : cellStrs) {
            for (int i = 0; i < cells.length; i++) {
                if (i > 0) sb.append(" | ");
                sb.append(padRight(cells[i], widths[i]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ==================== 互操作 ====================

    public VirtualTable toVirtualTable() {
        return new VirtualTable(toMapList());
    }

    public static Table fromVirtualTable(VirtualTable vt, String name) {
        List<Row> rows = new ArrayList<>();
        for (Map<String, Object> map : vt.getRows()) {
            rows.add(new Row(map));
        }
        return new Table(name, rows, new ArrayList<>(vt.getColumnNames()));
    }

    public static Table fromMaps(String name, List<Map<String, Object>> data) {
        return new Table(name, data);
    }

    // ==================== Iterable ====================

    @Override
    public Iterator<Row> iterator() {
        return rows.iterator();
    }

    // ==================== 内部工具 ====================

    private void updateColumnsFromRow(Row row) {
        for (String col : row.columnNames()) {
            if (!columns.contains(col)) {
                columns.add(col);
            }
        }
    }

    private void ensureColumn(String name) {
        if (!columns.contains(name)) {
            columns.add(name);
        }
    }



    private static int compareValues(Object a, Object b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }
        return a.toString().compareTo(b.toString());
    }

    private static String padRight(String s, int width) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Table{name='" + name + "', rows=" + rows.size() + ", columns=" + columns.size() + "}";
    }
}
