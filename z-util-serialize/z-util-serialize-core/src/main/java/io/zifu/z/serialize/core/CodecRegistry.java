package io.zifu.z.serialize.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 已注册 Codec 的全局注册表。
 *
 * <p>由 codegen 生成的 {@code {ClassName}Codec} 在 static initializer 中调用
 * {@link #register(Class, Codec)} 自动注册。用户代码通过
 * {@link #get(Class)} 获取已注册的 Codec。</p>
 *
 * <p>也可手动调用 {@link #register(Class, Codec)} 注入自定义 Codec。</p>
 *
 * <p><b>线程安全：</b>基于 ConcurrentHashMap，无锁。</p>
 */
public final class CodecRegistry {

    private static final Map<Class<?>, Codec> REGISTRY = new ConcurrentHashMap<>();

    private CodecRegistry() {}

    /**
     * 注册一个 Codec 到全局注册表。
     *
     * @param messageClass 被序列化的 message 类
     * @param codec        编解码器实例
     */
    public static void register(Class<?> messageClass, Codec codec) {
        if (messageClass == null || codec == null) {
            throw new IllegalArgumentException("messageClass and codec must not be null");
        }
        REGISTRY.put(messageClass, codec);
    }

    /**
     * 获取已注册的 Codec。
     *
     * @return 已注册的 Codec，若未注册则返回 null
     */
    public static Codec get(Class<?> messageClass) {
        return REGISTRY.get(messageClass);
    }

    /**
     * 判断是否已注册。
     */
    public static boolean isRegistered(Class<?> messageClass) {
        return REGISTRY.containsKey(messageClass);
    }

    /**
     * 清空所有注册（测试用）。
     */
    public static void clear() {
        REGISTRY.clear();
    }

    /**
     * 已注册 Codec 数量。
     */
    public static int size() {
        return REGISTRY.size();
    }
}
