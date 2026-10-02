package com.zifang.util.json.serializer;

import java.io.IOException;

/**
 * 值级别反序列化器接口。
 * 用于自定义字段的反序列化逻辑。
 */
public interface ValueDeserializer {

    /**
     * 从字符串反序列化为对象值。
     *
     * @param value JSON 中的原始字符串值
     * @param targetType 目标类型
     * @param format 格式字符串（由 @JsonDeserialize(format=...) 传入）
     * @return 反序列化后的对象
     */
    Object deserialize(String value, Class<?> targetType, String format) throws IOException;
}
