package com.zifang.util.ioc.inject;

import com.zifang.util.ioc.core.BeanRegistry;
import com.zifang.util.ioc.metadata.BeanDefinition;

/**
 * 兼容旧 API 的依赖解析上下文。
 *
 * <p>新代码请使用 {@link com.zifang.util.ioc.Injector#getInstance(Class)}，
 * 该类保留仅为兼容旧测试。
 *
 * @deprecated 使用 {@link com.zifang.util.ioc.Injector} 替代
 */
@Deprecated
public class InjectorContext {

    private final BeanRegistry registry;
    private final Instantiator instantiator;

    public InjectorContext(BeanRegistry registry, Instantiator instantiator) {
        this.registry = registry;
        this.instantiator = instantiator;
    }

    /**
     * 根据类型解析（优先按类型精确匹配，fallback 到接口/父类匹配）。
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        BeanDefinition bd = registry.get(type);
        if (bd != null) {
            return (T) resolveInstance(bd);
        }
        for (BeanDefinition existing : registry.getAll()) {
            if (type.isAssignableFrom(existing.getBeanClass())) {
                return (T) resolveInstance(existing);
            }
        }
        return null;
    }

    private Object resolveInstance(BeanDefinition bd) {
        if (bd.isSingleton()) {
            if (bd.getInstance() == null) {
                synchronized (bd) {
                    if (bd.getInstance() == null) {
                        Object instance = instantiator.instantiate(bd, this);
                        bd.setInstance(instance);
                        bd.setCreationTime(System.currentTimeMillis());
                    }
                }
            }
            return bd.getInstance();
        } else {
            return instantiator.instantiate(bd, this);
        }
    }
}
