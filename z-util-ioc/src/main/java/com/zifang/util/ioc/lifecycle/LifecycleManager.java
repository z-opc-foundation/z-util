package com.zifang.util.ioc.lifecycle;

import com.zifang.util.ioc.inject.Instantiator;
import com.zifang.util.ioc.metadata.BeanDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理容器生命周期回调的顺序：
 * PreDestroy 按 inverse 顺序调用（与 Spring 类似）。
 */
public class LifecycleManager {

    private final Instantiator instantiator;

    public LifecycleManager() {
        this.instantiator = null;
    }

    /**
     * 兼容旧构造器，传入 Instantiator 用于执行 PreDestroy。
     */
    public LifecycleManager(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    /**
     * 收集所有已初始化单例，按注册顺序（不含配置类本身）。
     */
    public List<Object> getInitializedSingletons(List<BeanDefinition> all) {
        List<Object> singletons = new ArrayList<>();
        for (BeanDefinition bd : all) {
            if (bd.isSingleton() && !bd.isConfiguration() && bd.getInstance() != null) {
                singletons.add(bd.getInstance());
            }
        }
        return singletons;
    }

    /**
     * 触发所有 PreDestroy（倒序）。若未提供 Instantiator 则跳过。
     */
    public void destroySingletons(List<Object> singletons) {
        if (instantiator == null) return;
        for (int i = singletons.size() - 1; i >= 0; i--) {
            instantiator.invokePreDestroy(singletons.get(i));
        }
    }
}
