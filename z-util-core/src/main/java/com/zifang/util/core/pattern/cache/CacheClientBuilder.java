package com.zifang.util.core.pattern.cache;

/**
 * 缓存客户端构建器
 * <p>
 * 用于构建配置化的CacheClient实例
 *
 * @author zifang
 */
public class CacheClientBuilder {

    private CacheProvider cacheProvider;
    private String dbNum;

    /**
     * 默认构造函数
     */
    public CacheClientBuilder() {
    }

    /**
     * 带参数的构造函数
     *
     * @param cacheProvider 缓存提供者
     * @param dbNum         数据库编号
     */
    public CacheClientBuilder(CacheProvider cacheProvider, String dbNum) {
        this.cacheProvider = cacheProvider;
        this.dbNum = dbNum;
    }

    /**
     * 获取缓存提供者
     *
     * @return 缓存提供者
     */
    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    /**
     * 设置缓存提供者
     *
     * @param cacheProvider 缓存提供者
     */
    public void setCacheProvider(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    /**
     * 获取数据库编号
     *
     * @return 数据库编号
     */
    public String getDbNum() {
        return dbNum;
    }

    /**
     * 设置数据库编号
     *
     * @param dbNum 数据库编号
     */
    public void setDbNum(String dbNum) {
        this.dbNum = dbNum;
    }

    /**
     * 构建缓存客户端实例
     *
     * @return 缓存客户端实例
     */
    public CacheClient build() {
        CacheClient cacheClient = new DefaultCacheClient();
        return cacheClient;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "CacheClientBuilder{cacheProvider=" + cacheProvider + ", dbNum=" + dbNum + "}";
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
        CacheClientBuilder that = (CacheClientBuilder) o;
        return java.util.Objects.equals(cacheProvider, that.cacheProvider) &&
                java.util.Objects.equals(dbNum, that.dbNum);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(cacheProvider, dbNum);
    }
}