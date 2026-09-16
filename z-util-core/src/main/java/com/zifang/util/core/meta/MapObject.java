package com.zifang.util.core.meta;

import java.util.HashMap;
import java.util.Optional;

/**
 * 泛型 Map 对象，提供便捷的类型转换取值方法。
 * <p>
 * 继承自 HashMap<String, Object>，内部存储 Object 类型值，
 * 提供类型安全的 getString、getLong、getInteger 等方法，
 * 避免手动类型转换和 NullPointerException。
 *
 * @author zifang
 * @see HashMap
 */
public class MapObject extends HashMap<String, Object> {

    private static final long serialVersionUID = 5070417008655524317L;

    /**
     * getString方法。
     * * @param key String类型参数
     *
     * @return String类型返回值
     */
    public String getString(String key) {
        return Optional.ofNullable(get(key)).map(Object::toString).orElse(null);
    }

    /**
     * getLong方法。
     * * @param key String类型参数
     *
     * @return long类型返回值
     */
    public Long getLong(String key) {
        return Optional.ofNullable(get(key)).map(value -> Long.parseLong(value.toString()))
                .orElse(null);
    }

    /**
     * getInteger方法。
     * * @param key String类型参数
     *
     * @return int类型返回值
     */
    public Integer getInteger(String key) {
        return Optional.ofNullable(get(key)).map(value -> Integer.parseInt(value.toString()))
                .orElse(null);
    }

    /**
     * getShort方法。
     * * @param key String类型参数
     *
     * @return short类型返回值
     */
    public Short getShort(String key) {
        return Optional.ofNullable(get(key)).map(value -> Short.parseShort(value.toString()))
                .orElse(null);
    }

    /**
     * getByte方法。
     * * @param key String类型参数
     *
     * @return byte类型返回值
     */
    public Byte getByte(String key) {
        return Optional.ofNullable(get(key)).map(value -> Byte.parseByte(value.toString()))
                .orElse(null);
    }

    /**
     * getDouble方法。
     * * @param key String类型参数
     *
     * @return double类型返回值
     */
    public Double getDouble(String key) {
        return Optional.ofNullable(get(key)).map(value -> Double.parseDouble(value.toString()))
                .orElse(null);
    }

    /**
     * getFloat方法。
     * * @param key String类型参数
     *
     * @return float类型返回值
     */
    public Float getFloat(String key) {
        return Optional.ofNullable(get(key)).map(value -> Float.parseFloat(value.toString()))
                .orElse(null);
    }

    /**
     * getObject方法。
     * * @param key String类型参数
     *
     * @return <T> T类型返回值
     */
    public <T> T getObject(String key) {
        return (T) Optional.ofNullable(get(key)).orElse(null);
    }

}
