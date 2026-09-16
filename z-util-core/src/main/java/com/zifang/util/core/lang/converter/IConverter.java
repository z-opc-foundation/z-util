package com.zifang.util.core.lang.converter;

import com.zifang.util.core.lang.tuples.Pair;

/**
 * 类型转换器接口。
 * <p>
 * 定义从类型 F 到类型 T 的转换方法。
 *
 * @param <F> 源类型
 * @param <T> 目标类型
 * @author zifang
 */
public interface IConverter<F, T> {

    default T to(F value) {
        throw new RuntimeException("未实现");
    }

    default Pair<Class<?>, Class<?>> getPair() {
        return null;
    }

    T to(F value, T defaultValue);

    default T to(F value, Class<T> clazz) {
        try {
            return to(value, clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
