package com.zifang.util.workflow.engine.interfaces;

import com.zifang.util.workflow.config.Engine;
import com.zifang.util.workflow.engine.java.JavaEngine;
import com.zifang.util.workflow.engine.python.PythonEngine;
import com.zifang.util.workflow.engine.spark.SparkEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * 引擎工厂类，负责创建和管理不同类型的执行引擎。
 * 支持的引擎类型包括：spark、python、java。
 * 引擎实例会被缓存以提高性能。
 *
 * @see AbstractEngine
 * @see SparkEngine
 * @see PythonEngine
 * @see JavaEngine
 */
public class EngineFactory {

    /**
     * 引擎实例缓存池
     */
    public static Map<String, AbstractEngine> engineCache = new HashMap<>();

    /**
     * 已注册的引擎类型映射表
     */
    public static Map<String, Class<? extends AbstractEngine>> registeredEngineMap = new HashMap<String, Class<? extends AbstractEngine>>() {
        {
            put("spark", SparkEngine.class);
            put("python", PythonEngine.class);
            put("java", JavaEngine.class);
        }
    };

    /**
     * 根据引擎配置获取引擎实例。
     * 如果缓存中已存在则直接返回，否则创建新实例并加入缓存。
     *
     * @param engine 引擎配置，包含类型、模式及属性信息
     * @return 引擎实例，如果类型未注册则返回null
     * @throws IllegalArgumentException 如果引擎类型未注册
     */
    public static AbstractEngine getEngine(Engine engine) {

        //得到引擎种类类型
        String type = engine.getType();

        //引擎缓存是否命中，命中则直接返回
        if (engineCache.containsKey(type)) {
            return engineCache.get(type);
        } else {
            // 初始化引擎类
            // 得到引擎的class类型
            Class<? extends AbstractEngine> clazz = registeredEngineMap.get(engine.getType());
            try {
                AbstractEngine abstractEngine = clazz.newInstance();

                //设置引擎所需的所有东西
                abstractEngine.setMode(engine.getMode());
                abstractEngine.setConfiguration(engine.getProperties());

                //让子类进行初始化操作
                abstractEngine.doInitial();

                //加入缓存
                engineCache.put(type, abstractEngine);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return engineCache.get(type);
    }
}
