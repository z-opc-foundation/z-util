package com.zifang.util.core.lang.serialize;

/**
 * 自定义类型序列化器 SPI。
 * <p>
 * 通过实现此接口并调用 {@link ZSerializer#register(Serializer)} 注册，
 * 可为任意 Java 类型提供高效的编解码支持（不依赖 Java Serialization）。
 * <p>
 * 实现要求：
 * <ul>
 *   <li>{@link #typeId()} 返回 {@code 0x80..0xFF} 范围内的唯一 ID</li>
 *   <li>{@link #write} 仅写入负载数据（不含类型标签）</li>
 *   <li>{@link #read} 从读取位置还原对象</li>
 *   <li>实现必须线程安全：{@link ZSerializer} 可能跨线程调用</li>
 * </ul>
 *
 * @param <T> 要序列化的对象类型
 * @author zifang
 */
public interface Serializer<T> {

    /**
     * @return 此序列化器的类型 ID（0x80..0xFF）
     */
    byte typeId();

    /**
     * @return 此序列化器处理的 Java 类型
     */
    Class<T> type();

    /**
     * 将对象写入 {@link ByteWriter}（不含类型标签）。
     */
    void write(ByteWriter out, T value);

    /**
     * 从 {@link ByteReader} 读取并还原对象（不含类型标签）。
     */
    T read(ByteReader in);
}
