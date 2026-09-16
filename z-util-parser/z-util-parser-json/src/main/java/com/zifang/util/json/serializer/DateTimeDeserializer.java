package com.zifang.util.json.serializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateTime 类型反序列化器。
 * 默认格式：{@code "yyyy-MM-dd HH:mm:ss"}。
 */
public class DateTimeDeserializer implements ValueDeserializer {

    @Override
    public Object deserialize(String value, Class<?> targetType, String format) throws IOException {
        if (value == null || "null".equals(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.matches("-?\\d+")) {
            try {
                return new Date(Long.parseLong(trimmed));
            } catch (NumberFormatException e) {
                throw new IOException("Invalid timestamp: " + trimmed, e);
            }
        }
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        String pattern = (format != null && !format.isEmpty()) ? format : "yyyy-MM-dd HH:mm:ss";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(trimmed);
        } catch (ParseException e) {
            throw new IOException("Invalid datetime format '" + pattern + "': " + trimmed, e);
        }
    }
}
