package com.zifang.util.xml.binding;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * XML 文本 ↔ Java 类型双向转换。
 *
 * @author zifang
 */
final class XmlConverters {

    private XmlConverters() {
    }

    /**
     * 将文本值转换为目标类型。
     *
     * @param text       XML 文本值
     * @param targetType 目标 Java 类型
     * @return 转换后的对象（目标类型为 String 时直接返回 text）
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object convert(String text, Class<?> targetType) {
        if (text == null) {
            return defaultValue(targetType);
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return defaultValue(targetType);
        }

        // 基本类型
        if (targetType == String.class) {
            return trimmed;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(trimmed);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(trimmed);
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return parseBoolean(trimmed);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(trimmed);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(trimmed);
        }
        if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(trimmed);
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(trimmed);
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(trimmed);
        }
        if (targetType == BigInteger.class) {
            return new BigInteger(trimmed);
        }
        if (targetType == char.class || targetType == Character.class) {
            return trimmed.charAt(0);
        }

        // Enum：优先 name()，大小写不敏感
        if (targetType.isEnum()) {
            Enum[] constants = (Enum[]) targetType.getEnumConstants();
            for (Enum c : constants) {
                if (c.name().equalsIgnoreCase(trimmed)) {
                    return c;
                }
            }
            throw new XmlBindingException("无法将 '" + trimmed + "' 转换为枚举 " + targetType.getSimpleName());
        }

        // Date
        if (targetType == Date.class) {
            throw new XmlBindingException("Date 类型转换暂不支持，请使用 java.time 类型");
        }

        // 未知类型：返回文本（让调用方决定）
        return trimmed;
    }

    private static boolean parseBoolean(String s) {
        return "true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s);
    }

    /**
     * 返回目标类型的零值：基本类型返回 0/false，对象返回 null。
     */
    static Object defaultValue(Class<?> targetType) {
        if (targetType == boolean.class) return false;
        if (targetType == int.class) return 0;
        if (targetType == long.class) return 0L;
        if (targetType == double.class) return 0.0;
        if (targetType == float.class) return 0.0f;
        if (targetType == short.class) return (short) 0;
        if (targetType == byte.class) return (byte) 0;
        if (targetType == char.class) return '\0';
        return null;
    }

    /**
     * 将对象的属性值转换为可输出的字符串（用于 toXml）。
     */
    static String toText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return value.toString();
    }
}
