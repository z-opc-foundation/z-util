package com.zifang.util.json.serializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date 类型反序列化器。
 * 支持时间戳（数字）和字符串两种格式。
 */
public class DateDeserializer implements ValueDeserializer {

    @Override
    public Object deserialize(String value, Class<?> targetType, String format) throws IOException {
        if (value == null || "null".equals(value)) {
            return null;
        }
        String trimmed = value.trim();
        // 纯数字字符串 → 当作时间戳
        if (trimmed.matches("-?\\d+")) {
            try {
                return new Date(Long.parseLong(trimmed));
            } catch (NumberFormatException e) {
                throw new IOException("Invalid timestamp: " + trimmed, e);
            }
        }
        // 去除首尾引号
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        String pattern = (format != null && !format.isEmpty()) ? format : "yyyy-MM-dd";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(trimmed);
        } catch (ParseException e) {
            throw new IOException("Invalid date format '" + pattern + "': " + trimmed, e);
        }
    }
}
