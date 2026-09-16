package com.zifang.util.ioc.core;

import com.zifang.util.ioc.exception.BindingException;
import com.zifang.util.ioc.metadata.BeanDefinition;
import com.zifang.util.ioc.metadata.BindingKey;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 ConcurrentHashMap 的 Bean 注册表实现。
 * <p>
 * 同时维护按 BindingKey、类型（精确 + 父类/接口）、名称的多维索引。
 */
public class DefaultBeanRegistry implements BeanRegistry {

    private final Map<BindingKey<?>, BeanDefinition> registry = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<BindingKey<?>>> typeIndex = new ConcurrentHashMap<>();

    @Override
    public void register(BeanDefinition bd) {
        BindingKey<?> key = bd.getKey();
        if (registry.containsKey(key)) {
            throw new BindingException("Duplicate binding for key: " + key);
        }
        registry.put(key, bd);
        // 按 bd.getBeanClass() 建立类型索引（支持精确匹配）
        typeIndex.computeIfAbsent(bd.getBeanClass(), k -> new ArrayList<>()).add(key);
        // 若绑定 key 的类型与 beanClass 不同（例如 bind(Interface.class).to(Impl.class)），
        // 也要为 key.type 建立索引以便按类型查找
        if (key.getType() != bd.getBeanClass()) {
            typeIndex.computeIfAbsent(key.getType(), k -> new ArrayList<>()).add(key);
        }
    }

    @Override
    public BeanDefinition get(BindingKey<?> key) {
        return registry.get(key);
    }

    @Override
    public BeanDefinition get(String name) {
        for (BeanDefinition bd : registry.values()) {
            if (name.equals(bd.getName())) {
                return bd;
            }
        }
        return null;
    }

    @Override
    public <T> BeanDefinition get(Class<T> type) {
        return get(type, (Class<? extends Annotation>) null);
    }

    @Override
    public <T> BeanDefinition get(Class<T> type, Class<? extends Annotation> qualifier) {
        BindingKey<?> key = BindingKey.of(type, qualifier);
        return registry.get(key);
    }

    @Override
    public <T> BeanDefinition get(Class<T> type, String name) {
        BindingKey<?> key = BindingKey.of(type, name);
        return registry.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<BeanDefinition> getAll(Class<T> type) {
        List<BeanDefinition> result = new ArrayList<>();
        List<BindingKey<?>> exactKeys = typeIndex.get(type);
        if (exactKeys != null) {
            for (BindingKey<?> k : exactKeys) {
                BeanDefinition bd = registry.get(k);
                if (bd != null) {
                    result.add(bd);
                }
            }
        }
        // 接口/父类匹配
        for (BeanDefinition bd : registry.values()) {
            if (bd.getBeanClass() != type && type.isAssignableFrom(bd.getBeanClass())) {
                if (!result.contains(bd)) {
                    result.add(bd);
                }
            }
        }
        return result;
    }

    @Override
    public Collection<BeanDefinition> getAll() {
        return Collections.unmodifiableCollection(registry.values());
    }

    @Override
    public boolean contains(String name) {
        return get(name) != null;
    }

    @Override
    public boolean contains(BindingKey<?> key) {
        return registry.containsKey(key);
    }

    @Override
    public void clear() {
        registry.clear();
        typeIndex.clear();
    }

    @Override
    public void replace(BeanDefinition oldBd, BeanDefinition newBd) {
        // 通过 key 找到 oldBd 并替换
        BindingKey<?> key = oldBd.getKey();
        BeanDefinition current = registry.get(key);
        if (current == oldBd) {
            registry.put(key, newBd);
            // typeIndex 不需要变
        } else {
            // key 不匹配或 oldBd 不在 registry 中：直接 put
            registry.put(key, newBd);
        }
    }
}
