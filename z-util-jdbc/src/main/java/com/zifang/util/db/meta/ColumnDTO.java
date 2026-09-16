package com.zifang.util.db.meta;

import java.util.Objects;


/**
 * 字段数据传输对象
 */
public class ColumnDTO {
    public final String javaType;
    private final String columnName;
    private final String javaFieldName;
    private final String dataType;
    private final int columnSize;
    private final boolean nullable;
    private final boolean isPrimaryKey;
    private final String comment;

    /**
     * 构造字段DTO
     *
     * @param columnName   字段名，不能为空
     * @param dataType     数据库类型
     * @param columnSize   字段长度
     * @param nullable     是否可为空
     * @param isPrimaryKey 是否为主键
     * @param comment      字段备注
     */
    public ColumnDTO(String columnName, String dataType, int columnSize, boolean nullable, boolean isPrimaryKey, String comment) {
        this.columnName = Objects.requireNonNull(columnName, "字段名不能为空").trim();
        this.dataType = dataType == null ? "VARCHAR" : dataType.trim();
        this.columnSize = Math.max(columnSize, 0);
        this.nullable = nullable;
        this.isPrimaryKey = isPrimaryKey;
        this.comment = comment == null ? "" : comment.trim();
        this.javaFieldName = underlineToCamel(this.columnName);
        this.javaType = mapDbTypeToJavaType(this.dataType);
    }

    private static String underlineToCamel(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                result.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return result.toString();
    }

    /**
     * underlineToCamelUpper方法。
     * * @param str String类型参数
     *
     * @return static String类型返回值
     */
    public static String underlineToCamelUpper(String str) {
        String camel = underlineToCamel(str);
        if (camel == null || camel.isEmpty()) {
            return camel;
        }
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    private static String mapDbTypeToJavaType(String dbType) {
        if (dbType == null) {
            return "Object";
        }
        String upper = dbType.toUpperCase();
        if (upper.contains("INT")) return "Integer";
        if (upper.contains("BIGINT")) return "Long";
        if (upper.contains("FLOAT") || upper.contains("DOUBLE") || upper.contains("DECIMAL")) return "Double";
        if (upper.contains("BOOLEAN")) return "Boolean";
        if (upper.contains("DATE") || upper.contains("TIME")) return "java.util.Date";
        if (upper.contains("TIMESTAMP")) return "java.sql.Timestamp";
        if (upper.contains("BLOB") || upper.contains("BINARY")) return "byte[]";
        if (upper.contains("CLOB") || upper.contains("TEXT")) return "String";
        return "String";
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * 获取Java字段名（驼峰形式）
     *
     * @return Java字段名
     */
    public String getJavaFieldName() {
        return javaFieldName;
    }

    /**
     * 获取数据库类型
     *
     * @return 数据库类型
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * 获取Java类型
     *
     * @return Java类型
     */
    public String getJavaType() {
        return this.javaType == null ? "Object" : this.javaType;
    }

    /**
     * 获取字段长度
     *
     * @return 字段长度
     */
    public int getColumnSize() {
        return columnSize;
    }

    /**
     * 获取是否可为空
     *
     * @return 是否可为空
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * 获取是否为主键
     *
     * @return 是否为主键
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * 获取字段备注
     *
     * @return 字段备注
     */
    public String getComment() {
        return comment;
    }

    // --- 缺失的工具方法 ---

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ColumnDTO{columnName=" + columnName + ", javaFieldName=" + javaFieldName + ", dataType=" + dataType + ", javaType=" + javaType + ", columnSize=" + columnSize + ", nullable=" + nullable + ", isPrimaryKey=" + isPrimaryKey + ", comment=" + comment + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnDTO that = (ColumnDTO) o;
        return columnSize == that.columnSize &&
                nullable == that.nullable &&
                isPrimaryKey == that.isPrimaryKey &&
                Objects.equals(columnName, that.columnName) &&
                Objects.equals(javaFieldName, that.javaFieldName) &&
                Objects.equals(dataType, that.dataType) &&
                Objects.equals(javaType, that.javaType) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(columnName, javaFieldName, dataType, javaType, columnSize, nullable, isPrimaryKey, comment);
    }
}
