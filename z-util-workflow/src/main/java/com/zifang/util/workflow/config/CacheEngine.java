package com.zifang.util.workflow.config;

/**
 * 缓存引擎配置。
 * <p>
 * 用于配置工作流的缓存机制，包括缓存引擎类型和服务标识。
 * 支持多种缓存后端，如Redis、Memcached等。
 *
 * @see Configurations
 */
public class CacheEngine {

    /**
     * 缓存引擎类型
     */
    private String engineType;

    /**
     * 缓存引擎服务标识
     */
    private String cacheEngineService;

    /**
     * 默认构造函数
     */
    public CacheEngine() {
    }

    /**
     * 全参数构造函数
     *
     * @param engineType         缓存引擎类型，如redis、memcached等
     * @param cacheEngineService 缓存引擎服务标识，用于定位具体的缓存服务
     */
    public CacheEngine(String engineType, String cacheEngineService) {
        this.engineType = engineType;
        this.cacheEngineService = cacheEngineService;
    }

    /**
     * 获取缓存引擎类型
     *
     * @return 缓存引擎类型，如redis、memcached等
     */
    public String getEngineType() {
        return engineType;
    }

    /**
     * 设置缓存引擎类型
     *
     * @param engineType 缓存引擎类型，如redis、memcached等
     */
    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    /**
     * 获取缓存引擎服务标识
     *
     * @return 缓存引擎服务标识
     */
    public String getCacheEngineService() {
        return cacheEngineService;
    }

    /**
     * 设置缓存引擎服务标识
     *
     * @param cacheEngineService 缓存引擎服务标识
     */
    public void setCacheEngineService(String cacheEngineService) {
        this.cacheEngineService = cacheEngineService;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "CacheEngine{engineType=" + engineType + ", cacheEngineService=" + cacheEngineService + "}";
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
        CacheEngine that = (CacheEngine) o;
        if (engineType != null ? !engineType.equals(that.engineType) : that.engineType != null) return false;
        return cacheEngineService != null ? cacheEngineService.equals(that.cacheEngineService) : that.cacheEngineService == null;
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        int result = engineType != null ? engineType.hashCode() : 0;
        result = 31 * result + (cacheEngineService != null ? cacheEngineService.hashCode() : 0);
        return result;
    }
}
