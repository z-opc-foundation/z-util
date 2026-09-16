package com.zifang.util.db.respository;

import java.util.Map;
import java.util.Objects;

/**
 * 绑定SQL类，用于存储原始SQL和参数绑定信息
 *
 * @author zifang
 */
public class BoundSql {

    private String originSql;

    private String transformSql;

    private Map<Integer, String> indexName;

    private Map<Integer, Object> indexValue;

    private Map<Integer, Object> indexValueInsert;

    /**
     * 获取原始SQL
     *
     * @return 原始SQL语句
     */
    public String getOriginSql() {
        return originSql;
    }

    /**
     * 设置原始SQL
     *
     * @param originSql 原始SQL语句
     */
    public void setOriginSql(String originSql) {
        this.originSql = originSql;
    }

    /**
     * 获取转换后的SQL（占位符形式）
     *
     * @return 转换后的SQL语句
     */
    public String getTransformSql() {
        return transformSql;
    }

    /**
     * 设置转换后的SQL
     *
     * @param transformSql 转换后的SQL语句
     */
    public void setTransformSql(String transformSql) {
        this.transformSql = transformSql;
    }

    /**
     * 获取参数索引到名称的映射
     *
     * @return 参数索引到名称的映射
     */
    public Map<Integer, String> getIndexName() {
        return indexName;
    }

    /**
     * 设置参数索引到名称的映射
     *
     * @param indexName 参数索引到名称的映射
     */
    public void setIndexName(Map<Integer, String> indexName) {
        this.indexName = indexName;
    }

    /**
     * 获取参数索引到值的映射
     *
     * @return 参数索引到值的映射
     */
    public Map<Integer, Object> getIndexValue() {
        return indexValue;
    }

    /**
     * 设置参数索引到值的映射
     *
     * @param indexValue 参数索引到值的映射
     */
    public void setIndexValue(Map<Integer, Object> indexValue) {
        this.indexValue = indexValue;
    }

    /**
     * 获取插入参数的索引到值的映射
     *
     * @return 插入参数的映射
     */
    public Map<Integer, Object> getIndexValueInsert() {
        return indexValueInsert;
    }

    /**
     * 设置插入参数的索引到值的映射
     *
     * @param indexValueInsert 插入参数的映射
     */
    public void setIndexValueInsert(Map<Integer, Object> indexValueInsert) {
        this.indexValueInsert = indexValueInsert;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "BoundSql{originSql=" + originSql + ", transformSql=" + transformSql + ", indexName=" + indexName + ", indexValue=" + indexValue + ", indexValueInsert=" + indexValueInsert + "}";
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
        BoundSql boundSql = (BoundSql) o;
        return Objects.equals(originSql, boundSql.originSql) &&
                Objects.equals(transformSql, boundSql.transformSql) &&
                Objects.equals(indexName, boundSql.indexName) &&
                Objects.equals(indexValue, boundSql.indexValue) &&
                Objects.equals(indexValueInsert, boundSql.indexValueInsert);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(originSql, transformSql, indexName, indexValue, indexValueInsert);
    }
}
