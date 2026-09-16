package com.zifang.util.yaml.define;

/**
 * 类型引用，用于在运行时保留泛型类型信息。
 * <p>
 * 类似 Gson's TypeToken，解决 Java 泛型擦除问题。
 *
 * @param <T> 目标类型
 * @author zifang
 */
/**
 * TypeReference类。
 */

/**
 * TypeReference类。
 */
public class TypeReference<T> {

    private final Class<? super T> rawType;
    private final java.lang.reflect.Type type;

    @SuppressWarnings("unchecked")
    /**
     * TypeReference方法。
     */
    /**
     * TypeReference方法。
     */
    protected TypeReference() {
        java.lang.reflect.Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType parameterized = (java.lang.reflect.ParameterizedType) superclass;
            type = parameterized.getActualTypeArguments()[0];
            rawType = (Class<? super T>) ((java.lang.reflect.ParameterizedType) type).getRawType();
        } else {
            type = Object.class;
            rawType = (Class<? super T>) Object.class;
        }
    }

    /**
     * getRawType方法。
     * @return Class<? super T>类型返回值
     */
    /**
     * getRawType方法。
     *
     * @return Class<? super T>类型返回值
     */
    public Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * getType方法。
     * @return java.lang.reflect.Type类型返回值
     */
    /**
     * getType方法。
     *
     * @return java.lang.reflect.Type类型返回值
     */
    public java.lang.reflect.Type getType() {
        return type;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "TypeReference{" +
                "type=" + type +
                ", rawType=" + rawType +
                '}';
    }
}
