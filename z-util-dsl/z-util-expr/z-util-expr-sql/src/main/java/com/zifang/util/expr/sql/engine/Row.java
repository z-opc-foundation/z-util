package com.zifang.util.expr.sql.engine;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 行包装类，封装 {@code Map<String, Object>}，提供类型化访问和链式操作。
 * <p>
 * 实现 {@link Map} 接口，与现有 {@code Map<String, Object>} 代码完全兼容。
 *
 * <pre>
 * Row row = Row.of("id", 1, "name", "Alice", "age", 30);
 * int id = row.getInt("id");
 * String name = row.getString("name");
 * row.set("dept", "eng").set("salary", 100000);  // 链式写入
 * UserDTO dto = row.toBean(UserDTO.class);
 * </pre>
 */
public class Row implements Map<String, Object> {

    private final Map<String, Object> data;

    // ==================== 构造 ====================

    public Row() {
        this.data = new LinkedHashMap<>();
    }

    public Row(Map<String, Object> data) {
        this.data = data != null ? data : new LinkedHashMap<>();
    }

    public Row(Object... kv) {
        this.data = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            data.put(kv[i].toString(), kv[i + 1]);
        }
    }

    // ==================== 类型化访问器 ====================

    @Override
    public Object get(Object key) {
        return data.get(key);
    }

    /**
     * 获取字符串值。
     */
    public String getString(Object key) {
        Object val = data.get(key);
        return val != null ? val.toString() : null;
    }

    /**
     * 获取字符串值，为空时返回默认值。
     */
    public String getString(Object key, String defaultValue) {
        Object val = data.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    /**
     * 获取 int 值。
     */
    public int getInt(Object key) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val != null) return Integer.parseInt(val.toString());
        throw new NoSuchElementException("列 '" + key + "' 不存在或为 null");
    }

    /**
     * 获取 int 值，为空时返回默认值。
     */
    public int getInt(Object key, int defaultValue) {
        Object val = data.get(key);
        if (val == null) return defaultValue;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取 long 值。
     */
    public long getLong(Object key) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        if (val != null) return Long.parseLong(val.toString());
        throw new NoSuchElementException("列 '" + key + "' 不存在或为 null");
    }

    /**
     * 获取 long 值，为空时返回默认值。
     */
    public long getLong(Object key, long defaultValue) {
        Object val = data.get(key);
        if (val == null) return defaultValue;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取 double 值。
     */
    public double getDouble(Object key) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val != null) return Double.parseDouble(val.toString());
        throw new NoSuchElementException("列 '" + key + "' 不存在或为 null");
    }

    /**
     * 获取 double 值，为空时返回默认值。
     */
    public double getDouble(Object key, double defaultValue) {
        Object val = data.get(key);
        if (val == null) return defaultValue;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取 float 值。
     */
    public float getFloat(Object key) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number) val).floatValue();
        if (val != null) return Float.parseFloat(val.toString());
        throw new NoSuchElementException("列 '" + key + "' 不存在或为 null");
    }

    /**
     * 获取 float 值，为空时返回默认值。
     */
    public float getFloat(Object key, float defaultValue) {
        Object val = data.get(key);
        if (val == null) return defaultValue;
        if (val instanceof Number) return ((Number) val).floatValue();
        try {
            return Float.parseFloat(val.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取 boolean 值。
     */
    public boolean getBoolean(Object key) {
        Object val = data.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).doubleValue() != 0;
        if (val != null) return Boolean.parseBoolean(val.toString());
        throw new NoSuchElementException("列 '" + key + "' 不存在或为 null");
    }

    /**
     * 获取 boolean 值，为空时返回默认值。
     */
    public boolean getBoolean(Object key, boolean defaultValue) {
        Object val = data.get(key);
        if (val == null) return defaultValue;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).doubleValue() != 0;
        return Boolean.parseBoolean(val.toString());
    }

    /**
     * 通用类型转换获取。
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(Object key, Class<T> type) {
        Object val = data.get(key);
        if (val == null) return null;
        if (type.isInstance(val)) return (T) val;
        if (type == String.class) return (T) val.toString();
        if (type == Integer.class || type == int.class) return (T) Integer.valueOf(toInt(val));
        if (type == Long.class || type == long.class) return (T) Long.valueOf(toLong(val));
        if (type == Double.class || type == double.class) return (T) Double.valueOf(toDouble(val));
        if (type == Float.class || type == float.class) return (T) Float.valueOf(toFloat(val));
        if (type == Boolean.class || type == boolean.class) return (T) Boolean.valueOf(toBool(val));
        if (type == Short.class || type == short.class) return (T) Short.valueOf(((Number) val).shortValue());
        if (type == Byte.class || type == byte.class) return (T) Byte.valueOf(((Number) val).byteValue());
        return (T) val;
    }

    // ==================== 判空/存在 ====================

    /**
     * 检查指定列的值是否为 null。
     */
    public boolean isNull(Object key) {
        return !data.containsKey(key) || data.get(key) == null;
    }

    /**
     * 检查指定列的值是否非 null。
     */
    public boolean isNotNull(Object key) {
        return data.containsKey(key) && data.get(key) != null;
    }

    /**
     * 检查是否包含指定列（别名 containsKey）。
     */
    public boolean has(Object key) {
        return data.containsKey(key);
    }

    // ==================== 链式写入 ====================

    /**
     * 链式 put，返回 this。
     */
    public Row putRow(String key, Object value) {
        data.put(key, value);
        return this;
    }

    /**
     * set 是 put 的语义别名，返回 this 支持链式。
     */
    public Row set(String key, Object value) {
        data.put(key, value);
        return this;
    }

    /**
     * 批量 put（链式版本），返回 this。
     */
    public Row putAllRows(Map<? extends String, ?> map) {
        data.putAll(map);
        return this;
    }

    // ==================== 列操作 ====================

    /**
     * 只保留指定列，删除其他列。返回 this。
     */
    public Row keep(String... keys) {
        Set<String> keepSet = new LinkedHashSet<>(Arrays.asList(keys));
        data.keySet().retainAll(keepSet);
        return this;
    }

    /**
     * 重命名列。返回 this。
     */
    public Row rename(String oldKey, String newKey) {
        if (data.containsKey(oldKey) && !oldKey.equals(newKey)) {
            Object val = data.remove(oldKey);
            // 保持原位置：重建 LinkedHashMap
            LinkedHashMap<String, Object> newData = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                newData.put(entry.getKey(), entry.getValue());
                if (entry.getKey().equals(newKey)) {
                    // newKey 已存在则跳过，oldKey 已被移除
                }
            }
            data.clear();
            // 重新插入，确保 oldKey 位置被 newKey 替代
            boolean inserted = false;
            for (Map.Entry<String, Object> entry : newData.entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }
            if (!data.containsKey(newKey)) {
                data.put(newKey, val);
            }
        }
        return this;
    }

    // ==================== 转换 ====================

    /**
     * 返回底层 Map（非拷贝）。
     */
    public Map<String, Object> toMap() {
        return data;
    }

    /**
     * 返回浅拷贝。
     */
    public Map<String, Object> toMapCopy() {
        return new LinkedHashMap<>(data);
    }

    /**
     * 转为 Bean。
     */
    public <T> T toBean(Class<T> clazz) {
        return BeanConverter.toBean(data, clazz);
    }

    /**
     * 获取所有列名。
     */
    public Set<String> columnNames() {
        return Collections.unmodifiableSet(data.keySet());
    }

    // ==================== 遍历 ====================

    /**
     * 遍历所有列，BiConsumer(列名, 值)。委托给 Map.forEach。
     */
    public void forEachColumn(BiConsumer<String, Object> action) {
        data.forEach(action);
    }

    // ==================== Map 接口实现 ====================

    @Override public int size() { return data.size(); }
    @Override public boolean isEmpty() { return data.isEmpty(); }
    @Override public boolean containsKey(Object key) { return data.containsKey(key); }
    @Override public boolean containsValue(Object value) { return data.containsValue(value); }
    @Override public Object remove(Object key) { return data.remove(key); }
    @Override public void clear() { data.clear(); }
    @Override public Set<String> keySet() { return data.keySet(); }
    @Override public Collection<Object> values() { return data.values(); }
    @Override public Set<Map.Entry<String, Object>> entrySet() { return data.entrySet(); }

    @Override
    public Object put(String key, Object value) {
        data.put(key, value);
        return value;
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        data.putAll(m);
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Row)) return false;
        return data.equals(((Row) o).data);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    // ==================== 静态工厂 ====================

    /**
     * 创建 Row，传入 key-value 对。
     */
    public static Row of(Object... kv) {
        return new Row(kv);
    }

    /**
     * 从 Map 创建 Row。
     */
    public static Row fromMap(Map<String, Object> map) {
        return new Row(map);
    }

    /**
     * 合并两行（用于 JOIN）。right 的值覆盖 left 的同名列。
     */
    public static Row merge(Row left, Row right) {
        Row result = new Row(left.data);
        result.data.putAll(right.data);
        return result;
    }

    /**
     * 创建空行。
     */
    public static Row empty() {
        return new Row();
    }

    // ==================== 内部工具 ====================

    private static int toInt(Object val) {
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }

    private static long toLong(Object val) {
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private static double toDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }

    private static float toFloat(Object val) {
        if (val instanceof Number) return ((Number) val).floatValue();
        return Float.parseFloat(val.toString());
    }

    private static boolean toBool(Object val) {
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).doubleValue() != 0;
        return Boolean.parseBoolean(val.toString());
    }
}
