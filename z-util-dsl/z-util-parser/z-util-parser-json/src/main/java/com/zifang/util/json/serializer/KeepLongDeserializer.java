package com.zifang.util.json.serializer;

import java.io.IOException;

/**
 * Long 类型反序列化器。
 * 将 JSON 中的数字或字符串形式的 long 值反序列化为 Long。
 */
public class KeepLongDeserializer implements ValueDeserializer {

    @Override
    public Object deserialize(String value, Class<?> targetType, String format) throws IOException {
        if (value == null || "null".equals(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Invalid long value: " + value, e);
        }
    }
}
