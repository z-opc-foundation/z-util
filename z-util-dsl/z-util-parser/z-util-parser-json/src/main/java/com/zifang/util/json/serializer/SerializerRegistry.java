package com.zifang.util.json.serializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器注册表。
 * 管理 ValueSerializer / ValueDeserializer 子类的单例实例，
 * 支持自定义序列化器的注册与查找。
 * <p>
 * 内置注册：
 * <ul>
 *   <li>{@link KeepLongSerializer} / {@link KeepLongDeserializer} — Long 序列化为字符串</li>
 *   <li>{@link DateSerializer} / {@link DateDeserializer} — Date 格式化</li>
 *   <li>{@link DateTimeSerializer} / {@link DateTimeDeserializer} — DateTime 格式化</li>
 * </ul>
 */
public class SerializerRegistry {

    private static final SerializerRegistry INSTANCE = new SerializerRegistry();

    public static SerializerRegistry getInstance() {
        return INSTANCE;
    }

    private final Map<Class<? extends ValueSerializer>, ValueSerializer> serializers = new ConcurrentHashMap<>();
    private final Map<Class<? extends ValueDeserializer>, ValueDeserializer> deserializers = new ConcurrentHashMap<>();

    private SerializerRegistry() {
        // 注册内置序列化器
        register(KeepLongSerializer.class, new KeepLongSerializer());
        register(DateSerializer.class, new DateSerializer());
        register(DateTimeSerializer.class, new DateTimeSerializer());
        // 注册内置反序列化器
        register(KeepLongDeserializer.class, new KeepLongDeserializer());
        register(DateDeserializer.class, new DateDeserializer());
        register(DateTimeDeserializer.class, new DateTimeDeserializer());
    }

    public <S extends ValueSerializer> void register(Class<S> clazz, S instance) {
        serializers.put(clazz, instance);
    }

    public <D extends ValueDeserializer> void register(Class<D> clazz, D instance) {
        deserializers.put(clazz, instance);
    }

    public ValueSerializer getSerializer(Class<? extends ValueSerializer> clazz) {
        if (clazz == ValueSerializer.class) return null;
        ValueSerializer s = serializers.get(clazz);
        if (s != null) return s;
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate serializer: " + clazz.getName(), e);
        }
    }

    public ValueDeserializer getDeserializer(Class<? extends ValueDeserializer> clazz) {
        if (clazz == ValueDeserializer.class) return null;
        ValueDeserializer d = deserializers.get(clazz);
        if (d != null) return d;
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate deserializer: " + clazz.getName(), e);
        }
    }
}
