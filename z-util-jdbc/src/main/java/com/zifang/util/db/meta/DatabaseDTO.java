package com.zifang.util.db.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 数据库数据传输对象
 */
public class DatabaseDTO {

    private String databaseName;

    private List<TableDTO> tables = new ArrayList<>();

    /**
     * 构造数据库DTO
     *
     * @param databaseName 数据库名称，不能为空
     * @throws IllegalArgumentException 数据库名为空时抛出
     */
    public DatabaseDTO(String databaseName) {
        this.databaseName = Objects.requireNonNull(databaseName, "数据库名不能为空").trim();
    }

    /**
     * 添加表（只有包含字段的表才会被添加）
     *
     * @param table 表对象
     */
    public void addTable(TableDTO table) {
        if (table != null && !table.getColumns().isEmpty()) {
            tables.add(table);
        }
    }

    /**
     * 获取表列表的副本
     *
     * @return 表列表
     */
    public List<TableDTO> getTables() {
        return new ArrayList<>(tables);
    }

    /**
     * 设置表列表
     *
     * @param tables 表列表，null时设置为空列表
     */
    public void setTables(List<TableDTO> tables) {
        this.tables = tables == null ? new ArrayList<>() : tables;
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * 设置数据库名称
     *
     * @param databaseName 数据库名称
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "DatabaseDTO{databaseName=" + databaseName + ", tables=" + tables + "}";
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
        DatabaseDTO that = (DatabaseDTO) o;
        return Objects.equals(databaseName, that.databaseName) &&
                Objects.equals(tables, that.tables);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(databaseName, tables);
    }
}
