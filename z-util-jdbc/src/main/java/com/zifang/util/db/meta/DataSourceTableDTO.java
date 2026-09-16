package com.zifang.util.db.meta;

import com.zifang.util.core.meta.Description;

import java.util.List;
import java.util.Objects;

/**
 * 数据源表信息传输对象
 *
 * @author zifang
 */
public class DataSourceTableDTO {

    @Description("数据源下表 id")
    private Long datasourceTableId;

    @Description("数据标识")
    private String datasourceCode;

    @Description("表名称")
    private String tableName;

    @Description("表描述")
    private String descriptions;

    @Description("表下的字段信息")
    private List<DataSourceTableColumnDTO> columns;

    /**
     * 获取组件标识（数据标识:表名）
     *
     * @return 组件标识
     */
    public String getComponentCode() {
        return datasourceCode + ":" + tableName;
    }

    /**
     * 获取数据源表id
     *
     * @return 数据源表id
     */
    public Long getDatasourceTableId() {
        return datasourceTableId;
    }

    /**
     * 设置数据源表id
     *
     * @param datasourceTableId 数据源表id
     */
    public void setDatasourceTableId(Long datasourceTableId) {
        this.datasourceTableId = datasourceTableId;
    }

    /**
     * 获取数据标识
     *
     * @return 数据标识
     */
    public String getDatasourceCode() {
        return datasourceCode;
    }

    /**
     * 设置数据标识
     *
     * @param datasourceCode 数据标识
     */
    public void setDatasourceCode(String datasourceCode) {
        this.datasourceCode = datasourceCode;
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名称
     *
     * @param tableName 表名称
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获取表描述
     *
     * @return 表描述
     */
    public String getDescriptions() {
        return descriptions;
    }

    /**
     * 设置表描述
     *
     * @param descriptions 表描述
     */
    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public List<DataSourceTableColumnDTO> getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(List<DataSourceTableColumnDTO> columns) {
        this.columns = columns;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "DataSourceTableDTO{datasourceTableId=" + datasourceTableId + ", datasourceCode=" + datasourceCode + ", tableName=" + tableName + ", descriptions=" + descriptions + ", columns=" + columns + "}";
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
        DataSourceTableDTO that = (DataSourceTableDTO) o;
        return Objects.equals(datasourceTableId, that.datasourceTableId) &&
                Objects.equals(datasourceCode, that.datasourceCode) &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(descriptions, that.descriptions) &&
                Objects.equals(columns, that.columns);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(datasourceTableId, datasourceCode, tableName, descriptions, columns);
    }
}
