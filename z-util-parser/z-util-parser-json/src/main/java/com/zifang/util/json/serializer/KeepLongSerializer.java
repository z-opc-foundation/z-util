package com.zifang.util.json.serializer;

import java.io.IOException;

/**
 * Long 类型序列化器。
 * 避免 JavaScript 大数字精度丢失，将 long 序列化为字符串。
 * <p>
 * 使用场景：前端 JSON.parse 时，整数超过 2^53-1 会丢失精度。
 * 将 long 序列化为字符串，前端按字符串处理或使用 decimal.js。
 * <p>
 * 示例：{@code @JsonSerialize(using = KeepLongSerializer.class)}
 */
public class KeepLongSerializer implements ValueSerializer {

    @Override
    public String serialize(Object value, String format) throws IOException {
        if (value == null) {
            return "null";
        }
        if (value instanceof Long) {
            return String.valueOf(value);
        }
        if (value instanceof Number) {
            return String.valueOf(((Number) value).longValue());
        }
        throw new IOException("KeepLongSerializer expects Long, got: " + value.getClass().getName());
    }
}
