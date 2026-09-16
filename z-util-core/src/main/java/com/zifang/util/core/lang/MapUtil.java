package com.zifang.util.core.lang;

import com.zifang.util.core.lang.primitive.ByteUtil;
import com.zifang.util.core.lang.primitive.IntegerUtil;
import com.zifang.util.core.lang.primitive.LongUtil;
import com.zifang.util.core.lang.primitive.ShortUtil;

import java.util.*;
import java.util.function.Function;

/**
 * @author: zifang
 * @time: 2021-12-02 20:08:00
 * @description: map util
 * @version: JDK 1.8
 */
public class MapUtil {

    /**
     * <<方法。
     * * @param 2 Integer.SIZE类型参数
     *
     * @return static final int MAX_POWER_OF_TWO = 1类型返回值
     */
    public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

    /**
     * 将Properties中的键值对合并到Map中
     *
     * @param props 要合并的Properties对象
     * @param map   目标Map对象
     */
    public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
        if (props != null) {
            for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                Object value = props.get(key);
                if (value == null) {
                    // Allow for defaults fallback or potentially overridden accessor...
                    value = props.getProperty(key);
                }
                map.put((K) key, (V) value);
            }
        }
    }

    /**
     * 根据预期大小创建一个新的HashMap，自动计算合适的初始容量
     *
     * @param expectedSize 预期的键值对数量
     * @return 新的HashMap实例
     */
    public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
        return new HashMap<>(capacity(expectedSize));
    }

    /**
     * 创建一个默认大小的HashMap（初始容量为16）
     *
     * @return 新的HashMap实例
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return newHashMap(16);
    }


    /**
     * 根据预期大小创建一个新的LinkedHashMap，自动计算合适的初始容量
     *
     * @param expectedSize 预期的键值对数量
     * @return 新的LinkedHashMap实例
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int expectedSize) {
        return new LinkedHashMap<>(capacity(expectedSize));
    }

    /**
     * 根据预期大小计算HashMap合适的初始容量
     *
     * @param expectedSize 预期的键值对数量
     * @return 计算后的初始容量
     */
    protected static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            checkNonNegative(expectedSize, "expectedSize");
            return expectedSize + 1;
        }
        if (expectedSize < MAX_POWER_OF_TWO) {
            // This is the calculation used in JDK8 to resize when a putAll
            // happens; it seems to be the most conservative calculation we
            // can make.  0.75 is the default load factor.
            return (int) ((float) expectedSize / 0.75F + 1.0F);
        }
        return Integer.MAX_VALUE;
    }

    private static int checkNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
        }
        return value;
    }

    /**
     * 判断Map是否为空
     *
     * @param map 要检查的Map对象
     * @return 如果Map为null或为空返回true，否则返回false
     */
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * 判断Map是否非空
     *
     * @param map 要检查的Map对象
     * @return 如果Map不为null且不为空返回true，否则返回false
     */
    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    /**
     * 从Map中根据key获取对应的值
     *
     * @param map Map对象
     * @param key 键
     * @return 如果Map为空返回null，否则返回对应的值
     */
    public static <K, V> V parseValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return map.get(key);
    }

    /**
     * 从Map中根据key获取对应的值，并转换为String类型
     *
     * @param map Map对象
     * @param key 键
     * @return 如果Map为空返回null，否则返回对应的String值
     */
    public static <K, V> String parseStringValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return StringUtil.parseString(map.get(key));
    }

    /**
     * 从Map中根据key获取对应的值，并转换为Byte类型
     *
     * @param map Map对象
     * @param key 键
     * @return 如果Map为空返回null，否则返回对应的Byte值
     */
    public static <K, V> Byte parseByteValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return ByteUtil.parseByte(map.get(key));
    }

    /**
     * 从Map中根据key获取对应的值，并转换为Short类型
     *
     * @param map Map对象
     * @param key 键
     * @return 如果Map为空返回null，否则返回对应的Short值
     */
    public static <K, V> Short parseShortValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return ShortUtil.parseShort(map.get(key));
    }

    /**
     * 从Map中根据key获取对应的值，并转换为Integer类型
     *
     * @param map Map对象
     * @param key 键
     * @return 如果Map为空返回null，否则返回对应的Integer值
     */
    public static <K, V> Integer parseIntegerValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return IntegerUtil.parseInteger(map.get(key));
    }


    /**
     * 从Map中根据key获取对应的值，并转换为Long类型
     *
     * @param map Map对象
     * @param key 键
     * @return 如果Map为空返回null，否则返回对应的Long值
     */
    public static <K, V> Long parseLongValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return LongUtil.parseLong(map.get(key));
    }

    /**
     * 从Map中根据key获取对应的值，如果值不存在或Map为空则返回默认值
     *
     * @param map          Map对象
     * @param key          键
     * @param defaultValue 默认值
     * @return 如果Map非空且包含key则返回对应的值，否则返回默认值
     */
    public static <K, V> V parseValueOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (isEmpty(map)) {
            return defaultValue;
        }
        return map.get(key);
    }


    /**
     * 将Properties对象转换为Map
     *
     * @param properties Properties对象
     * @return 转换后的Map，如果properties为null则返回空Map
     */
    public static Map<String, String> fromProperties(Properties properties) {
        if (null == properties) {
            return Collections.emptyMap();
        }
        Map<String, String> map = newHashMap(properties.size());
        Enumeration<?> enumeration = properties.propertyNames();

        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            map.put(key, properties.getProperty(key));
        }

        return map;
    }

    /**
     * 替换Map中所有键中的指定字符串
     *
     * @param map     原Map对象
     * @param search  要查找替换的字符串
     * @param replace 替换后的字符串
     * @return 键被替换后的新Map
     */
    public static <T> Map<String, T> replaceKey(Map<String, T> map, String search, String replace) {
        Map<String, T> newMap = MapUtil.newHashMap(map.size());
        map.forEach((key, value) -> {
            String newKey;
            if (key.contains(search)) {
                newKey = StringUtil.replace(key, search, replace);
            } else {
                newKey = key;
            }
            newMap.put(newKey, value);
        });
        return newMap;
    }

    /**
     * 剔除Map中值为null的键值对
     *
     * @param sourceMap 原Map对象
     * @return 剔除null值后的Map
     */
    public static <T> Map<String, T> trimValue(Map<String, T> sourceMap) {
        if (isEmpty(sourceMap)) {
            return sourceMap;
        }
        List<String> removeKeyList = new ArrayList<>();
        sourceMap.forEach((key, value) -> {
            if (null == value) {
                removeKeyList.add(key);
            }
        });
        for (String key : removeKeyList) {
            sourceMap.remove(key);
        }
        return sourceMap;
    }

    /**
     * 缓存优先取值：先从Map中取值，命中非空直接返回；
     * 未命中时执行加载函数，加载结果非空时写回Map缓存。
     * <p>
     * 与{@code Map.computeIfAbsent}的区别：加载函数返回null时不写入缓存，
     * 后续调用可再次触发加载。
     *
     * @param map    缓存Map，为null时直接执行加载函数且不缓存
     * @param key    缓存key
     * @param loader 缓存未命中时的加载函数
     * @param <K>    key类型
     * @param <V>    value类型
     * @return 缓存或加载的值，可能为null
     */
    public static <K, V> V getCacheFirst(Map<K, V> map, K key, Function<K, V> loader) {
        if (map == null) {
            return loader.apply(key);
        }
        V value = map.get(key);
        if (value != null) {
            return value;
        }
        value = loader.apply(key);
        if (value != null) {
            map.put(key, value);
        }
        return value;
    }

}
