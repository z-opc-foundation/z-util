package com.zifang.util.core.pattern.factory;

/**
 * 工厂接口，定义了通过指定键创建对应实例的通用工厂模式。
 *
 * @param <K> 用于创建实例的键（参数）类型
 * @param <T> 创建的实例类型
 * @author zifang
 */
public interface IFactory<K, T> {

    /**
     * 根据指定的键获取或创建一个实例。
     *
     * @param k 用于创建实例的键，可以为null（如果工厂支持）
     * @return 创建的实例，可能返回null（取决于具体实现）
     */
    T getInstance(K k);

}
