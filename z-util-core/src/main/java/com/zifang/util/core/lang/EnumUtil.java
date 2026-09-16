package com.zifang.util.core.lang;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

/**
 * 枚举工具类。
 * <p>
 * 提供枚举相关的常用操作方法。
 *
 * @author zifang
 */
public class EnumUtil {

    /**
     * 根据枚举类获取指定名称的枚举实例。
     *
     * @param enumClass 枚举类
     * @param name      枚举名称（不区分大小写）
     * @param <E>       枚举类型
     * @return 对应的枚举实例，不存在则返回 Optional.empty()
     */
    public static <E extends Enum<E>> Optional<E> getEnumIgnoreCase(Class<E> enumClass, String name) {
        if (enumClass == null || name == null) {
            return Optional.empty();
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * 安全获取枚举值，如果名称不匹配则返回默认值。
     *
     * @param enumClass    枚举类
     * @param name         枚举名称
     * @param defaultValue 默认值
     * @param <E>          枚举类型
     * @return 对应的枚举实例或默认值
     */
    public static <E extends Enum<E>> E getEnumOrDefault(Class<E> enumClass, String name, E defaultValue) {
        return getEnumIgnoreCase(enumClass, name).orElse(defaultValue);
    }

    /**
     * 获取枚举的所有名称。
     *
     * @param enumClass 枚举类
     * @param <E>       枚举类型
     * @return 枚举名称数组
     */
    public static <E extends Enum<E>> String[] getEnumNames(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    /**
     * 获取枚举的所有值。
     *
     * @param enumClass 枚举类
     * @param <E>       枚举类型
     * @return 枚举值数组
     */
    public static <E extends Enum<E>> E[] getEnumValues(Class<E> enumClass) {
        return enumClass.getEnumConstants();
    }

    /**
     * 根据枚举属性值查找枚举实例。
     * <p>
     * 遍历枚举的所有实例，将属性提取函数的结果与给定值按 equals 比较，
     * 返回首个匹配的枚举实例；常用于按 code/name 等自定义属性反查枚举。
     *
     * @param enumClass       枚举类
     * @param attributeMapper 枚举属性提取函数
     * @param value           目标属性值，null 时不匹配任何枚举
     * @param <E>             枚举类型
     * @return 首个匹配的枚举实例；不存在匹配或入参非法时返回 null
     */
    public static <E extends Enum<E>, V> E query(Class<E> enumClass, Function<E, V> attributeMapper, Object value) {
        if (enumClass == null || attributeMapper == null || value == null) {
            return null;
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> value.equals(attributeMapper.apply(e)))
                .findFirst()
                .orElse(null);
    }
}