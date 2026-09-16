package com.zifang.util.workflow.engine.interfaces;

import java.util.Map;

/**
 * 工作流执行引擎的抽象基类。
 * 定义了引擎的基本属性和需要子类实现的注册机制。
 * 支持多种执行引擎类型：Spark、Python、Java等。
 *
 * @see AbstractEngineService
 * @see com.zifang.util.workflow.engine.spark.SparkEngine
 * @see com.zifang.util.workflow.engine.python.PythonEngine
 * @see com.zifang.util.workflow.engine.java.JavaEngine
 */
public abstract class AbstractEngine {

    /**
     * 引擎运行模式
     */
    protected String mode;

    /**
     * 引擎配置属性
     */
    protected Map<String, String> properties;

    /**
     * 获取已注册的引擎服务映射表。
     *
     * @return 服务单元名称到服务类类型的映射
     */
    public abstract Map<String, Class<? extends AbstractEngineService>> getRegisteredEngineServiceMap();

    /**
     * 根据服务单元名称获取对应的引擎服务实例。
     *
     * @param serviceUnit 服务单元名称
     * @return 引擎服务实例，如果未找到则返回null
     */
    public abstract AbstractEngineService getRegisteredEngineService(String serviceUnit);

    /**
     * 注册引擎服务。
     *
     * @param name          服务单元名称
     * @param engineService 服务类类型
     */
    public abstract void register(String name, Class<? extends AbstractEngineService> engineService);

    /**
     * 设置引擎运行模式。
     *
     * @param mode 模式名称
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * 设置引擎配置属性。
     *
     * @param properties 配置属性映射
     */
    public void setConfiguration(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * 引擎初始化方法，子类实现具体的初始化逻辑。
     */
    public abstract void doInitial();
}
