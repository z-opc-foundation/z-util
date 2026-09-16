package com.zifang.util.yaml.facade;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * YAML 反序列化类型引用。
 */
public abstract class YamlTypeBinding<T> {

    private final Type type;

    /**
     * YamlTypeBinding方法。
     */
    /**
     * YamlTypeBinding方法。
     */
    protected YamlTypeBinding() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        } else {
            this.type = Object.class;
        }
    }

    /**
     * getType方法。
     * @return Type类型返回值
     */
    /**
     * getType方法。
     *
     * @return Type类型返回值
     */
    public Type getType() {
        return type;
    }
}