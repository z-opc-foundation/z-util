package com.zifang.util.db.meta;

import com.zifang.util.core.meta.Description;

import java.util.Objects;

/**
 * 数据源表字段数据传输对象
 * 用于描述数据源中某个表的字段信息
 *
 * @author zifang
 */
public class DataSourceTableColumnDTO {

    @Description("列id")
    private Long datasourceTableColumnId;

    @Description("数据标记")
    private String datasourceCode;

    @Description("当前字段归属表名称")
    private String tableName;

    @Description("列名称")
    private String columnName;

    @Description("列类型")
    private String columnType;

    @Description("列长度")
    private String columnLength;

    @Description("列的注解")
    private String columnComment;

    /**
     * nativeSignature方法。
     *
     * @return String类型返回值
     */
    public String nativeSignature() {
        return tableName + ":" + columnType + ":" + columnComment;
    }

    /**
     * getDatasourceTableColumnId方法。
     *
     * @return long类型返回值
     */
    public Long getDatasourceTableColumnId() {
        return datasourceTableColumnId;
    }

    /**
     * setDatasourceTableColumnId方法。
     * * @param datasourceTableColumnId long类型参数
     */
    public void setDatasourceTableColumnId(Long datasourceTableColumnId) {
        this.datasourceTableColumnId = datasourceTableColumnId;
    }

    /**
     * getDatasourceCode方法。
     *
     * @return String类型返回值
     */
    public String getDatasourceCode() {
        return datasourceCode;
    }

    /**
     * setDatasourceCode方法。
     * * @param datasourceCode String类型参数
     */
    public void setDatasourceCode(String datasourceCode) {
        this.datasourceCode = datasourceCode;
    }

    /**
     * getTableName方法。
     *
     * @return String类型返回值
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * setTableName方法。
     * * @param tableName String类型参数
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * getColumnName方法。
     *
     * @return String类型返回值
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * setColumnName方法。
     * * @param columnName String类型参数
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * getColumnType方法。
     *
     * @return String类型返回值
     */
    public String getColumnType() {
        return columnType;
    }

    /**
     * setColumnType方法。
     * * @param columnType String类型参数
     */
    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    /**
     * getColumnLength方法。
     *
     * @return String类型返回值
     */
    public String getColumnLength() {
        return columnLength;
    }

    /**
     * setColumnLength方法。
     * * @param columnLength String类型参数
     */
    public void setColumnLength(String columnLength) {
        this.columnLength = columnLength;
    }

    /**
     * getColumnComment方法。
     *
     * @return String类型返回值
     */
    public String getColumnComment() {
        return columnComment;
    }

    /**
     * setColumnComment方法。
     * * @param columnComment String类型参数
     */
    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "DataSourceTableColumnDTO{datasourceTableColumnId=" + datasourceTableColumnId + ", datasourceCode=" + datasourceCode + ", tableName=" + tableName + ", columnName=" + columnName + ", columnType=" + columnType + ", columnLength=" + columnLength + ", columnComment=" + columnComment + "}";
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
        DataSourceTableColumnDTO that = (DataSourceTableColumnDTO) o;
        return Objects.equals(datasourceTableColumnId, that.datasourceTableColumnId) &&
                Objects.equals(datasourceCode, that.datasourceCode) &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(columnName, that.columnName) &&
                Objects.equals(columnType, that.columnType) &&
                Objects.equals(columnLength, that.columnLength) &&
                Objects.equals(columnComment, that.columnComment);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(datasourceTableColumnId, datasourceCode, tableName, columnName, columnType, columnLength, columnComment);
    }
}
