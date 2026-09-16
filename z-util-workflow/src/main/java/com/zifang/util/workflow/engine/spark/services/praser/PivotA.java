package com.zifang.util.workflow.engine.spark.services.praser;

import java.util.List;
import java.util.Map;

/**
 * Pivot操作的配置参数类。
 * <p>
 * 用于定义数据透视表（Pivot Table）的配置信息，
 * 包括需要透视的列定义以及透视后的列名映射。
 * <p>
 * 使用示例：
 * <pre>
 * PivotA pivotA = new PivotA();
 * pivotA.setPivotColumnDefinations(pivotColumnDefinations);
 * pivotA.setColumnMap(columnMap);
 * </pre>
 *
 * @see PivotANode
 */
public class PivotA {

    /**
     * 对需要进行pivot处理的列的所有定义。
     */
    List<PivotANode> pivotColumnDefinations;

    /**
     * 透视后列名的新映射关系。
     */
    Map<String, String> columnMap;

    /**
     * 获取Pivot列定义列表。
     *
     * @return Pivot列定义列表
     */
    public List<PivotANode> getPivotColumnDefinations() {
        return pivotColumnDefinations;
    }

    /**
     * 设置Pivot列定义列表。
     *
     * @param pivotColumnDefinations Pivot列定义列表
     */
    public void setPivotColumnDefinations(List<PivotANode> pivotColumnDefinations) {
        this.pivotColumnDefinations = pivotColumnDefinations;
    }

    /**
     * 获取列名映射表。
     *
     * @return 列名映射表
     */
    public Map<String, String> getColumnMap() {
        return columnMap;
    }

    /**
     * 设置列名映射表。
     *
     * @param columnMap 列名映射表
     */
    public void setColumnMap(Map<String, String> columnMap) {
        this.columnMap = columnMap;
    }
}