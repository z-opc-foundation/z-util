package com.zifang.util.wf.kernel.engine.interfaces;

import com.zifang.util.wf.kernel.config.Engine;

import java.util.HashMap;
import java.util.Map;

/**
 * 引擎工厂类，负责创建和管理不同类型的执行引擎。
 * <p>
 * 注册模型：kernel 不强依赖任何 executor 子模块。executor 子模块在自己的静态初始化块中调用
 * {@link #register(String, Class)} 把引擎类挂进来，避免 reactor cycle。
 *
 * @see AbstractEngine
 */
public class EngineFactory {

    /**
     * 引擎实例缓存池（type -> 已初始化实例）
     */
    public static Map<String, AbstractEngine> engineCache = new HashMap<>();

    /**
     * 已注册的引擎类型映射表（type -> engine class）。由各 executor 子模块自注册。
     */
    private static final Map<String, Class<? extends AbstractEngine>> registeredEngineMap = new HashMap<>();

    /**
     * 注册一种引擎实现。重复注册后者覆盖前者。
     */
    public static void register(String type, Class<? extends AbstractEngine> clazz) {
        registeredEngineMap.put(type, clazz);
    }

    /**
     * 根据引擎配置获取引擎实例。
     *
     * @param engine 引擎配置
     * @return 引擎实例，如果类型未注册返回 null
     */
    public static AbstractEngine getEngine(Engine engine) {
        String type = engine.getType();
        if (engineCache.containsKey(type)) {
            return engineCache.get(type);
        }
        Class<? extends AbstractEngine> clazz = registeredEngineMap.get(type);
        if (clazz == null) {
            return null;
        }
        try {
            AbstractEngine abstractEngine = clazz.newInstance();
            abstractEngine.setMode(engine.getMode());
            abstractEngine.setConfiguration(engine.getProperties());
            abstractEngine.doInitial();
            engineCache.put(type, abstractEngine);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return engineCache.get(type);
    }
}
