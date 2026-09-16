package com.zifang.util.workflow.config;

import java.util.Map;

/**
 * 全局配置信息类。
 * 包含工作流引擎配置、缓存引擎配置、个人环境参数和运行时参数。
 *
 * @see Engine
 * @see CacheEngine
 */
public class Configurations {

    /**
     * 唯一的标识出配置id , 这个值与工作流上下文的id绑定
     */
    private Integer workflowConfigurationId;

    /**
     * 全局配置的执行引擎配置
     */
    private Engine engine;

    /**
     * 配置的作为每步缓存的执行引擎
     */
    private CacheEngine cacheEngine;

    /**
     * 为每个操作者提供专属的参数
     */
    private Map<String, String> personalEnvironment;

    /**
     * 运行时参数设置
     */
    private Map<String, String> runtimeParameter;

    /**
     * 默认构造函数
     */
    public Configurations() {
    }

    /**
     * 获取工作流配置ID
     *
     * @return 工作流配置ID，与工作流上下文的ID绑定
     */
    public Integer getWorkflowConfigurationId() {
        return workflowConfigurationId;
    }

    /**
     * 设置工作流配置ID
     *
     * @param workflowConfigurationId 工作流配置ID，与工作流上下文的ID绑定
     */
    public void setWorkflowConfigurationId(Integer workflowConfigurationId) {
        this.workflowConfigurationId = workflowConfigurationId;
    }

    /**
     * 获取执行引擎配置
     *
     * @return 执行引擎配置对象
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * 设置执行引擎配置
     *
     * @param engine 执行引擎配置对象
     */
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    /**
     * 获取缓存引擎配置
     *
     * @return 缓存引擎配置对象
     */
    public CacheEngine getCacheEngine() {
        return cacheEngine;
    }

    /**
     * 设置缓存引擎配置
     *
     * @param cacheEngine 缓存引擎配置对象
     */
    public void setCacheEngine(CacheEngine cacheEngine) {
        this.cacheEngine = cacheEngine;
    }

    /**
     * 获取个人环境参数
     *
     * @return 个人环境参数Map，为每个操作者提供专属的参数
     */
    public Map<String, String> getPersonalEnvironment() {
        return personalEnvironment;
    }

    /**
     * 设置个人环境参数
     *
     * @param personalEnvironment 个人环境参数Map，为每个操作者提供专属的参数
     */
    public void setPersonalEnvironment(Map<String, String> personalEnvironment) {
        this.personalEnvironment = personalEnvironment;
    }

    /**
     * 获取运行时参数
     *
     * @return 运行时参数Map
     */
    public Map<String, String> getRuntimeParameter() {
        return runtimeParameter;
    }

    /**
     * 设置运行时参数
     *
     * @param runtimeParameter 运行时参数Map
     */
    public void setRuntimeParameter(Map<String, String> runtimeParameter) {
        this.runtimeParameter = runtimeParameter;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Configurations{workflowConfigurationId=" + workflowConfigurationId + ", engine=" + engine + ", cacheEngine=" + cacheEngine + ", personalEnvironment=" + personalEnvironment + ", runtimeParameter=" + runtimeParameter + "}";
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
        Configurations that = (Configurations) o;
        if (workflowConfigurationId != null ? !workflowConfigurationId.equals(that.workflowConfigurationId) : that.workflowConfigurationId != null)
            return false;
        if (engine != null ? !engine.equals(that.engine) : that.engine != null) return false;
        if (cacheEngine != null ? !cacheEngine.equals(that.cacheEngine) : that.cacheEngine != null) return false;
        if (personalEnvironment != null ? !personalEnvironment.equals(that.personalEnvironment) : that.personalEnvironment != null)
            return false;
        return runtimeParameter != null ? runtimeParameter.equals(that.runtimeParameter) : that.runtimeParameter == null;
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        int result = workflowConfigurationId != null ? workflowConfigurationId.hashCode() : 0;
        result = 31 * result + (engine != null ? engine.hashCode() : 0);
        result = 31 * result + (cacheEngine != null ? cacheEngine.hashCode() : 0);
        result = 31 * result + (personalEnvironment != null ? personalEnvironment.hashCode() : 0);
        result = 31 * result + (runtimeParameter != null ? runtimeParameter.hashCode() : 0);
        return result;
    }
}
