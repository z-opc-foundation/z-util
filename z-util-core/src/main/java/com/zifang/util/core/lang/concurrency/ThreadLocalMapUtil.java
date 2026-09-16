package com.zifang.util.core.lang.concurrency;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: zifang
 * @time: 2021-04-10 17:54:00
 * @description: 线程工具
 * @version: JDK 1.8
 */
public class ThreadLocalMapUtil {

    private static final ThreadLocal<Map<Object, Object>> THREAD_LOCAL_MAP = new InheritableThreadLocal<>();

    /**
     * getThreadLocal方法。
     *
     * @return static Map<Object, Object>类型返回值
     */
    public static Map<Object, Object> getThreadLocal() {
        return THREAD_LOCAL_MAP.get();
    }

    /**
     * get方法。
     * * @param key Object类型参数
     *
     * @return static <T> T类型返回值
     */
    public static <T> T get(Object key) {
        Map<Object, Object> map = THREAD_LOCAL_MAP.get();
        if (map == null) {
            return null;
        }
        return (T) map.get(key);
    }

    /**
     * get方法。
     * * @param key Object类型参数
     *
     * @param defaultValue T类型参数
     * @return static <T> T类型返回值
     */
    public static <T> T get(Object key, T defaultValue) {
        Map<Object, Object> map = THREAD_LOCAL_MAP.get();
        if (map == null) {
            return null;
        }
        return map.get(key) == null ? defaultValue : (T) map.get(key);
    }

    /**
     * set方法。
     * * @param key Object类型参数
     *
     * @param value Object类型参数
     * @return static void类型返回值
     */
    public static void set(Object key, Object value) {
        Map<Object, Object> map = THREAD_LOCAL_MAP.get();
        if (null == map) {
            map = new HashMap<>(4);
        }
        map.put(key, value);
        THREAD_LOCAL_MAP.set(map);
    }

    /**
     * set方法。
     * * @param keyValueMap MapObject,类型参数
     *
     * @return static void类型返回值
     */
    public static void set(Map<Object, Object> keyValueMap) {
        Map<Object, Object> map = THREAD_LOCAL_MAP.get();
        if (null == map) {
            map = new HashMap<>();
        }
        map.putAll(keyValueMap);
        THREAD_LOCAL_MAP.set(map);
    }

    /**
     * remove方法。
     *
     * @return static void类型返回值
     */
    public static void remove() {
        THREAD_LOCAL_MAP.remove();
    }

    /**
     * fetchValuesByPrefix方法。
     * * @param prefix String类型参数
     *
     * @return static <T> Map<Object, T>类型返回值
     */
    public static <T> Map<Object, T> fetchValuesByPrefix(String prefix) {
        Map<Object, T> values = new HashMap<>();
        if (prefix == null) {
            return values;
        }
        Map<Object, Object> map = THREAD_LOCAL_MAP.get();
        map.forEach((key, value) -> {
            String keyStr = String.valueOf(key);
            if (keyStr != null) {
                if (keyStr.startsWith(prefix)) {
                    values.put(key, (T) value);
                }
            }
        });

        return values;
    }

    /**
     * remove方法。
     * * @param key String类型参数
     *
     * @return static <T> T类型返回值
     */
    public static <T> T remove(String key) {
        Map<Object, Object> map = THREAD_LOCAL_MAP.get();
        if (map == null) {
            return null;
        }
        return (T) map.remove(key);
    }

    /**
     * clear方法。
     * * @param prefix String类型参数
     *
     * @return static void类型返回值
     */
    public static void clear(String prefix) {
        if (prefix == null) {
            return;
        }
        Map<Object, Object> map = THREAD_LOCAL_MAP.get();
        if (map == null) {
            return;
        }

        map.forEach((key, value) -> {
            String keyStr = String.valueOf(key);
            if (keyStr != null) {
                if (keyStr.startsWith(prefix)) {
                    map.remove(key);
                }
            }
        });
    }

}
