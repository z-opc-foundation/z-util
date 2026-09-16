package com.zifang.util.core.lang.reflect;

import com.zifang.util.core.pattern.composite.tree.LeafWrapper;
import com.zifang.util.core.pattern.factory.IFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassParserFactory类。
 */
public class ClassParserFactory implements IFactory<Class<?>, ClassParser> {

    /**
     * 解析器缓存
     */
    private static final Map<Class<?>, ClassParser> classParserCache = new ConcurrentHashMap<>();

    /**
     * 类继承关系
     */
    private static final Map<Class<?>, LeafWrapper<Long, Long, ClassParserInfoWrapper>> classInheritableNode = new ConcurrentHashMap<>();

    /**
     * @param forceRefreshCache 是否需要强制刷新缓存
     */
    public static ClassParser getInstance(Class<?> clazz, boolean forceRefreshCache) {
        if (forceRefreshCache) {
            ClassParser classParser = new ClassParser(clazz);
            classParserCache.put(clazz, classParser);
        } else {
            if (!classParserCache.containsKey(clazz)) {
                ClassParser classParser = new ClassParser(clazz);
                classParserCache.put(clazz, classParser);
            }
        }
        return classParserCache.get(clazz);
    }

    @Override
    /**
     * getInstance方法。
     *      * @param clazz Class?类型参数
     * @return ClassParser类型返回值
     */
    public ClassParser getInstance(Class<?> clazz) {
        return getInstance(clazz, false);
    }

}
