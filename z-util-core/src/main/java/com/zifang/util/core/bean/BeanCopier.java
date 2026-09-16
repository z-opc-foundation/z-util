package com.zifang.util.core.bean;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 自研属性拷贝（对标 Apache BeanUtils.copyProperties / Spring BeanUtils.copyProperties / MapStruct 简化版）。
 * <p>
 * 约定：
 * <ul>
 *   <li>按字段名匹配（不依赖 getter/setter 命名约定，更宽松）</li>
 *   <li>支持基本类型 + String 包装 + 任意对象的引用赋值（仅当类型兼容）</li>
 *   <li>目标对象必须有无参构造</li>
 * </ul>
 * <p>
 * <b>不</b>做的事：嵌套拷贝、集合深拷贝、Map → Bean、注解驱动。保持精简。
 */
public final class BeanCopier {

    private static final ConcurrentMap<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private BeanCopier() {
    }

    /**
     * 把 src 对象的同名字段值拷贝到 target。target 必须有无参构造（如果 target == null）。
     */
    public static <T> T copy(Object src, Class<T> targetClass) {
        if (src == null) return null;
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyInto(src, target);
            return target;
        } catch (ReflectiveOperationException e) {
            throw new BeanCopyException("copy failed: " + targetClass, e);
        }
    }

    /**
     * 把 src 同名字段值拷到 target 实例。
     */
    public static void copyInto(Object src, Object target) {
        if (src == null || target == null) return;
        Map<String, Field> srcFields = fieldsOf(src.getClass());
        Map<String, Field> tgtFields = fieldsOf(target.getClass());
        for (Map.Entry<String, Field> e : srcFields.entrySet()) {
            Field tgt = tgtFields.get(e.getKey());
            if (tgt == null) continue;
            if (!isAssignable(e.getValue().getType(), tgt.getType())) continue;
            try {
                e.getValue().setAccessible(true);
                Object v = e.getValue().get(src);
                tgt.setAccessible(true);
                tgt.set(target, v);
            } catch (IllegalAccessException ex) {
                // 跳过
            }
        }
    }

    /**
     * Map → Bean（key 匹配字段名）。
     */
    public static <T> T fromMap(Map<String, ?> map, Class<T> targetClass) {
        if (map == null) return null;
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            Map<String, Field> tgtFields = fieldsOf(targetClass);
            for (Map.Entry<String, ?> e : map.entrySet()) {
                Field f = tgtFields.get(e.getKey());
                if (f == null) continue;
                if (!isAssignable(e.getValue() == null ? null : e.getValue().getClass(), f.getType())) continue;
                f.setAccessible(true);
                f.set(target, e.getValue());
            }
            return target;
        } catch (ReflectiveOperationException ex) {
            throw new BeanCopyException("fromMap failed: " + targetClass, ex);
        }
    }

    /**
     * Bean → Map。
     */
    public static Map<String, Object> toMap(Object src) {
        if (src == null) return new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        Map<String, Field> srcFields = fieldsOf(src.getClass());
        for (Map.Entry<String, Field> e : srcFields.entrySet()) {
            try {
                e.getValue().setAccessible(true);
                result.put(e.getKey(), e.getValue().get(src));
            } catch (IllegalAccessException ex) { /* skip */ }
        }
        return result;
    }

    private static Map<String, Field> fieldsOf(Class<?> klass) {
        return FIELD_CACHE.computeIfAbsent(klass, k -> {
            Map<String, Field> m = new HashMap<>();
            for (Class<?> c = k; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                    m.putIfAbsent(f.getName(), f);
                }
            }
            return m;
        });
    }

    private static boolean isAssignable(Class<?> from, Class<?> to) {
        if (from == null) return !to.isPrimitive();
        if (to.isAssignableFrom(from)) return true;
        // 兼容基本类型 ↔ 包装类
        Class<?> tWrap = to.isPrimitive() ? wrap(to) : to;
        Class<?> fWrap = from.isPrimitive() ? wrap(from) : from;
        return tWrap == fWrap;
    }

    private static Class<?> wrap(Class<?> p) {
        if (p == int.class) return Integer.class;
        if (p == long.class) return Long.class;
        if (p == boolean.class) return Boolean.class;
        if (p == double.class) return Double.class;
        if (p == float.class) return Float.class;
        if (p == short.class) return Short.class;
        if (p == byte.class) return Byte.class;
        if (p == char.class) return Character.class;
        return p;
    }
}
