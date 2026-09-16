package com.zifang.util.ioc.core;

import com.zifang.util.ioc.metadata.BeanDefinition;
import com.zifang.util.ioc.metadata.BindingKey;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * Bean 注册表，提供 BeanDefinition 的登记与查询。
 * <p>
 * 支持按名称、按类型、按限定符等多种方式查找。
 */
public interface BeanRegistry {

    /**
     * 注册一个 Bean 定义。
     */
    void register(BeanDefinition bd);

    /**
     * 按 BindingKey 精确查找。
     */
    BeanDefinition get(BindingKey<?> key);

    /**
     * 按名称查找（兼容旧 API）。
     */
    BeanDefinition get(String name);

    /**
     * 按类型精确查找唯一绑定。
     */
    <T> BeanDefinition get(Class<T> type);

    /**
     * 按类型 + 限定符查找。
     */
    <T> BeanDefinition get(Class<T> type, Class<? extends Annotation> qualifier);

    /**
     * 按类型 + 名称查找。
     */
    <T> BeanDefinition get(Class<T> type, String name);

    /**
     * 查找一个类型的所有绑定（同一接口有多个实现）。
     */
    <T> List<BeanDefinition> getAll(Class<T> type);

    /**
     * 获取所有已注册的 Bean 定义。
     */
    Collection<BeanDefinition> getAll();

    /**
     * 是否已注册（按名称）。
     */
    boolean contains(String name);

    /**
     * 是否已注册（按 BindingKey）。
     */
    boolean contains(BindingKey<?> key);

    /**
     * 清空注册表。
     */
    void clear();

    /**
     * 用 newBd 替换 oldBd（key 相同）。主要用于 {@code in(...)} 后修改 scope。
     */
    void replace(BeanDefinition oldBd, BeanDefinition newBd);
}
