package com.zifang.util.json.serializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateTime 类型序列化器。
 * 默认格式：{@code "yyyy-MM-dd HH:mm:ss"}。
 * 可通过 {@code @JsonSerialize(format="...")} 覆盖。
 */
public class DateTimeSerializer implements ValueSerializer {

    @Override
    public String serialize(Object value, String format) throws IOException {
        if (value == null) {
            return "null";
        }
        long timestamp;
        if (value instanceof Date) {
            timestamp = ((Date) value).getTime();
        } else if (value instanceof Number) {
            timestamp = ((Number) value).longValue();
        } else {
            throw new IOException("DateTimeSerializer expects Date or long, got: " + value.getClass().getName());
        }

        String pattern = (format != null && !format.isEmpty()) ? format : "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return "\"" + sdf.format(new Date(timestamp)) + "\"";
    }
}
