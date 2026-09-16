package com.zifang.util.db.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 表数据传输对象
 */
public class TableDTO {
    public final String tableName;
    public final String entityName;
    public final String comment;
    private final List<ColumnDTO> columns = new ArrayList<>();
    public ColumnDTO primaryKey;

    /**
     * 构造表DTO
     *
     * @param tableName 表名，不能为空
     * @param comment   表备注
     * @throws IllegalArgumentException 表名为空或空白时抛出
     */
    public TableDTO(String tableName, String comment) {
        String trimmedName = Objects.requireNonNull(tableName, "表名不能为空").trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("表名不能为空白");
        }
        this.tableName = trimmedName;
        this.comment = comment == null ? "" : comment.trim();
        this.entityName = ColumnDTO.underlineToCamelUpper(trimmedName);
    }

    /**
     * 添加字段
     *
     * @param column 字段对象
     */
    public void addColumn(ColumnDTO column) {
        if (column != null) {
            columns.add(column);
            if (column.isPrimaryKey() && primaryKey == null) {
                primaryKey = column;
            }
        }
    }

    /**
     * 获取字段列表的副本
     *
     * @return 字段列表
     */
    public List<ColumnDTO> getColumns() {
        return new ArrayList<>(columns);
    }

    /**
     * 打印调试信息
     */
    public void debugPrint() {
        System.out.println("=== TableDTO调试 ===");
        System.out.println("tableName: " + tableName);
        System.out.println("entityName: " + entityName);
        System.out.println("字段数: " + columns.size());
        System.out.println("===================");
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 获取实体名（首字母大写的驼峰形式）
     *
     * @return 实体名
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * 获取表备注
     *
     * @return 表备注
     */
    public String getComment() {
        return comment;
    }

    /**
     * 获取主键字段
     *
     * @return 主键字段，没有主键返回null
     */
    public ColumnDTO getPrimaryKey() {
        return primaryKey;
    }

    /**
     * 设置主键字段
     *
     * @param primaryKey 主键字段
     */
    public void setPrimaryKey(ColumnDTO primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "TableDTO{tableName=" + tableName + ", entityName=" + entityName + ", comment=" + comment + ", columns=" + columns + ", primaryKey=" + primaryKey + "}";
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
        TableDTO tableDTO = (TableDTO) o;
        return Objects.equals(tableName, tableDTO.tableName) &&
                Objects.equals(entityName, tableDTO.entityName) &&
                Objects.equals(comment, tableDTO.comment) &&
                Objects.equals(columns, tableDTO.columns) &&
                Objects.equals(primaryKey, tableDTO.primaryKey);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(tableName, entityName, comment, columns, primaryKey);
    }
}
