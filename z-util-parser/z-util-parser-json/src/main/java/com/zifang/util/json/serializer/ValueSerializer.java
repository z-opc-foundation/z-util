package com.zifang.util.json.serializer;

import java.io.IOException;

/**
 * 值级别序列化器接口。
 * 用于自定义字段的序列化逻辑。
 */
public interface ValueSerializer {

    /**
     * 将对象值序列化为字符串。
     *
     * @param value 待序列化的值
     * @param format 格式字符串（由 @JsonSerialize(format=...) 传入）
     * @return 序列化后的字符串表示
     */
    String serialize(Object value, String format) throws IOException;
}
