package com.zifang.util.parser.toml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TOML 文档数据模型
 */

/**
 * TomlDocument类。
 */
public class TomlDocument {

    private Map<String, TomlTable> tables;
    private List<TomlTable> arrayOfTables;

    /**
     * TomlDocument方法。
     */
    public TomlDocument() {
        this.tables = new LinkedHashMap<>();
        this.arrayOfTables = new ArrayList<>();
    }

    /**
     * 获取所有顶级表
     */
    /**
     * getTables方法。
     *
     * @return Map<String, TomlTable>类型返回值
     */
    public Map<String, TomlTable> getTables() {
        return tables;
    }

    /**
     * 获取数组 of tables 列表
     */
    /**
     * getArrayOfTables方法。
     *
     * @return List<TomlTable>类型返回值
     */
    public List<TomlTable> getArrayOfTables() {
        return arrayOfTables;
    }

    /**
     * 添加表
     */
    /**
     * addTable方法。
     * * @param name String类型参数
     *
     * @param table TomlTable类型参数
     */
    public void addTable(String name, TomlTable table) {
        tables.put(name, table);
    }

    /**
     * 添加数组 of tables
     */
    /**
     * addArrayOfTables方法。
     * * @param table TomlTable类型参数
     */
    public void addArrayOfTables(TomlTable table) {
        arrayOfTables.add(table);
    }

    /**
     * 根据路径获取表（支持 dotted path 如 "server.host"）
     */
    /**
     * getTable方法。
     * * @param path String类型参数
     *
     * @return TomlTable类型返回值
     */
    public TomlTable getTable(String path) {
        String[] parts = path.split("\\.");
        if (parts.length == 1) {
            return tables.get(path);
        }

        TomlTable current = tables.get(parts[0]);
        for (int i = 1; i < parts.length && current != null; i++) {
            current = current.getSubTable(parts[i]);
        }
        return current;
    }

    /**
     * 获取顶层表（用于数组 of tables 展开）
     */
    /**
     * getRootTable方法。
     *
     * @return TomlTable类型返回值
     */
    public TomlTable getRootTable() {
        return tables.get("");
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, TomlTable> entry : tables.entrySet()) {
            if (!entry.getKey().isEmpty()) {
                sb.append("[").append(entry.getKey()).append("]\n");
            }
            sb.append(entry.getValue().toString());
        }
        for (TomlTable table : arrayOfTables) {
            sb.append("[[").append(table.getName()).append("]]\n");
            sb.append(table.toString());
        }
        return sb.toString();
    }

    /**
     * TOML 表数据模型
     */
    public static class TomlTable {
        private String name;
        private Map<String, Object> values;
        private Map<String, TomlTable> subTables;
        private List<TomlTable> subTableArrays;
        private TomlTable parent;

        /**
         * TomlTable方法。
         */
        public TomlTable() {
            this(null);
        }

        /**
         * TomlTable方法。
         * * @param name String类型参数
         */
        public TomlTable(String name) {
            this.name = name;
            this.values = new LinkedHashMap<>();
            this.subTables = new LinkedHashMap<>();
            this.subTableArrays = new ArrayList<>();
        }

        /**
         * getName方法。
         *
         * @return String类型返回值
         */
        public String getName() {
            return name;
        }

        /**
         * getValues方法。
         *
         * @return Map<String, Object>类型返回值
         */
        public Map<String, Object> getValues() {
            return values;
        }

        /**
         * getSubTables方法。
         *
         * @return Map<String, TomlTable>类型返回值
         */
        public Map<String, TomlTable> getSubTables() {
            return subTables;
        }

        /**
         * getSubTableArrays方法。
         *
         * @return List<TomlTable>类型返回值
         */
        public List<TomlTable> getSubTableArrays() {
            return subTableArrays;
        }

        /**
         * getParent方法。
         *
         * @return TomlTable类型返回值
         */
        public TomlTable getParent() {
            return parent;
        }

        /**
         * setParent方法。
         * * @param parent TomlTable类型参数
         */
        public void setParent(TomlTable parent) {
            this.parent = parent;
        }

        /**
         * put方法。
         * * @param key String类型参数
         *
         * @param value Object类型参数
         */
        public void put(String key, Object value) {
            values.put(key, value);
        }

        /**
         * get方法。
         * * @param key String类型参数
         *
         * @return Object类型返回值
         */
        public Object get(String key) {
            return values.get(key);
        }

        /**
         * getString方法。
         * * @param key String类型参数
         *
         * @return String类型返回值
         */
        public String getString(String key) {
            Object value = values.get(key);
            return value != null ? value.toString() : null;
        }

        /**
         * getInteger方法。
         * * @param key String类型参数
         *
         * @return int类型返回值
         */
        public Integer getInteger(String key) {
            Object value = values.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return null;
        }

        /**
         * getLong方法。
         * * @param key String类型参数
         *
         * @return long类型返回值
         */
        public Long getLong(String key) {
            Object value = values.get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return null;
        }

        /**
         * getDouble方法。
         * * @param key String类型参数
         *
         * @return double类型返回值
         */
        public Double getDouble(String key) {
            Object value = values.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return null;
        }

        /**
         * getBoolean方法。
         * * @param key String类型参数
         *
         * @return boolean类型返回值
         */
        public Boolean getBoolean(String key) {
            Object value = values.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value instanceof String) {
                String s = ((String) value).toLowerCase();
                if ("true".equals(s)) return true;
                if ("false".equals(s)) return false;
            }
            return null;
        }

        /**
         * getList方法。
         * * @param key String类型参数
         *
         * @return List<?>类型返回值
         */
        public List<?> getList(String key) {
            Object value = values.get(key);
            if (value instanceof List) {
                return (List<?>) value;
            }
            return null;
        }

        /**
         * getSubTable方法。
         * * @param name String类型参数
         *
         * @return TomlTable类型返回值
         */
        public TomlTable getSubTable(String name) {
            return subTables.get(name);
        }

        /**
         * addSubTable方法。
         * * @param name String类型参数
         *
         * @param table TomlTable类型参数
         */
        public void addSubTable(String name, TomlTable table) {
            table.setParent(this);
            subTables.put(name, table);
        }

        /**
         * addSubTableArray方法。
         * * @param table TomlTable类型参数
         */
        public void addSubTableArray(TomlTable table) {
            table.setParent(this);
            subTableArrays.add(table);
        }

        /**
         * 获取完整路径
         */
        /**
         * getPath方法。
         *
         * @return String类型返回值
         */
        public String getPath() {
            if (parent == null || parent.name == null || parent.name.isEmpty()) {
                return name != null ? name : "";
            }
            return parent.getPath() + "." + name;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
            for (TomlTable sub : subTables.values()) {
                sb.append("[").append(sub.getPath()).append("]\n");
                sb.append(sub.toString());
            }
            for (TomlTable arr : subTableArrays) {
                sb.append("[[").append(arr.getPath()).append("]]\n");
                sb.append(arr.toString());
            }
            return sb.toString();
        }
    }
}
