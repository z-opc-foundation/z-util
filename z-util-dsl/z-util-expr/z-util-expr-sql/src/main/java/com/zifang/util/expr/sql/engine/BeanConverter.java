package com.zifang.util.expr.sql.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map → Bean 批量转换工具。
 * <p>
 * 基于反射实现，将 List&lt;Map&lt;String, Object&gt;&gt; 转换为 List&lt;T&gt;，
 * 支持字段名与 Map key 的大小写不敏感匹配。
 */
public final class BeanConverter {

    private BeanConverter() {
    }

    /**
     * 将 Map 列表转换为 Bean 列表。
     *
     * @param data      Map 列表
     * @param beanClass 目标 Bean 类
     * @param <T>       Bean 类型
     * @return Bean 列表
     */
    public static <T> List<T> toBeans(List<Map<String, Object>> data, Class<T> beanClass) {
        List<T> result = new ArrayList<>(data.size());
        for (Map<String, Object> row : data) {
            result.add(toBean(row, beanClass));
        }
        return result;
    }

    /**
     * 将单个 Map 转换为 Bean。
     *
     * @param map       Map 数据
     * @param beanClass 目标 Bean 类
     * @param <T>       Bean 类型
     * @return Bean 实例
     */
    public static <T> T toBean(Map<String, Object> map, Class<T> beanClass) {
        if (map == null) return null;
        try {
            T bean = beanClass.getDeclaredConstructor().newInstance();
            Map<String, Field> fields = getAllFields(beanClass);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 精确匹配
                Field field = fields.get(key);
                // 大小写不敏感匹配
                if (field == null) {
                    field = fields.get(key.toLowerCase());
                }
                if (field == null) {
                    // 驼峰转换匹配：Map key "user_name" → Bean field "userName"
                    field = fields.get(camelCase(key));
                }

                if (field != null) {
                    setFieldValue(field, bean, value);
                }
            }
            return bean;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Bean 转换失败: " + beanClass.getName(), e);
        }
    }

    /**
     * 获取类的所有字段（包括父类），建立名称索引。
     */
    private static Map<String, Field> getAllFields(Class<?> clazz) {
        Map<String, Field> fields = new LinkedHashMap<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                    continue;
                }
                f.setAccessible(true);
                // 存储原始名和小写名
                fields.put(f.getName(), f);
                fields.put(f.getName().toLowerCase(), f);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * 设置字段值，自动处理类型转换。
     */
    private static void setFieldValue(Field field, Object bean, Object value) {
        if (value == null) return;
        Class<?> targetType = field.getType();
        try {
            if (targetType.isInstance(value)) {
                field.set(bean, value);
            } else if (value instanceof Number) {
                Number num = (Number) value;
                if (targetType == int.class || targetType == Integer.class) {
                    field.set(bean, num.intValue());
                } else if (targetType == long.class || targetType == Long.class) {
                    field.set(bean, num.longValue());
                } else if (targetType == double.class || targetType == Double.class) {
                    field.set(bean, num.doubleValue());
                } else if (targetType == float.class || targetType == Float.class) {
                    field.set(bean, num.floatValue());
                } else if (targetType == short.class || targetType == Short.class) {
                    field.set(bean, num.shortValue());
                } else if (targetType == byte.class || targetType == Byte.class) {
                    field.set(bean, num.byteValue());
                } else if (targetType == String.class) {
                    field.set(bean, value.toString());
                } else {
                    field.set(bean, value);
                }
            } else if (targetType == String.class) {
                field.set(bean, value.toString());
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                if (value instanceof Number) {
                    field.set(bean, ((Number) value).doubleValue() != 0);
                } else {
                    field.set(bean, Boolean.parseBoolean(value.toString()));
                }
            } else {
                field.set(bean, value);
            }
        } catch (IllegalAccessException e) {
            // 跳过无法设置的字段
        }
    }

    /**
     * 下划线命名转驼峰：user_name → userName
     */
    private static String camelCase(String name) {
        if (name == null || !name.contains("_")) return name;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }
}
