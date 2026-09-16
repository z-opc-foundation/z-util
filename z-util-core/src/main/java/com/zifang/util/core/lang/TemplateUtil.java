package com.zifang.util.core.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板参数替换工具类。
 * <p>
 * 占位符以 {@code {}} 包围，支持「字段名」与「字段名, 格式」两种形式，
 * 取值来源支持 Map 与普通对象（反射读取字段，沿继承链查找）。
 * 日期类型（LocalDateTime、LocalDate、Date）可指定格式化模板，
 * 其余类型直接取 {@code toString()}；
 * 字段不存在或取值为 null 时占位符保持原样，日期格式化模板对非日期值不生效。
 * <p>
 * 示例：模板 {@code "{year, yyyy}/{code}_{time, HHmmss}.pdf"} 在
 * 参数 {@code {year=LocalDateTime.of(2022,4,22,12,30,0), code="123456", time=同year}}
 * 作用下得到 {@code "2022/20220422/123456_123000.pdf"}。
 */
public final class TemplateUtil {

    /**
     * 占位符匹配模式：内容为不含花括号的非空串，形如 name 或 name, pattern
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^{}]+)}");

    private TemplateUtil() {
    }

    /**
     * 渲染模板，将占位符替换为参数对象中对应字段的值。
     *
     * @param template 模板字符串，为 null 时返回 null
     * @param obj      参数来源，Map 按键取值，普通对象反射读取字段；为 null 时不替换
     * @return 渲染后的字符串；字段不存在或值为 null 的占位符保持原样
     */
    public static String render(String template, Object obj) {
        if (template == null || template.isEmpty() || obj == null) {
            return template;
        }
        Matcher matcher = PARAM_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer(template.length() + 16);
        while (matcher.find()) {
            String token = matcher.group(1);
            int commaIndex = token.indexOf(',');
            String fieldName = commaIndex >= 0 ? token.substring(0, commaIndex).trim() : token.trim();
            String pattern = commaIndex >= 0 ? token.substring(commaIndex + 1).trim() : null;
            if (fieldName.isEmpty()) {
                continue;
            }
            Object value = resolveValue(obj, fieldName);
            if (value == null) {
                continue;
            }
            String replacement = formatValue(value, pattern);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * 渲染模板，使用自定义定界符将占位符替换为 Map 中对应的值。
     * <p>
     * 占位符形如 {@code prefix + key + suffix}（如 {@code ${name}}），
     * Map 中不存在对应键或值为 null 时占位符保持原样；替换后的内容不再参与后续匹配。
     *
     * @param template 模板字符串，为 null 时返回 null
     * @param params   参数 Map，为 null 或空时不替换
     * @param prefix   占位符前缀（为 null 时按 "{" 处理）
     * @param suffix   占位符后缀（为 null 时按 "}" 处理）
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, ?> params, String prefix, String suffix) {
        if (template == null || template.isEmpty() || params == null || params.isEmpty()) {
            return template;
        }
        String open = prefix == null ? "{" : prefix;
        String close = suffix == null ? "}" : suffix;
        StringBuilder builder = new StringBuilder(template);
        int startIndex = builder.indexOf(open);
        while (startIndex != -1) {
            int endIndex = builder.indexOf(close, startIndex + open.length());
            if (endIndex == -1) {
                break;
            }
            String key = builder.substring(startIndex + open.length(), endIndex);
            int nextIndex = endIndex + close.length();
            Object value = params.get(key);
            if (value != null) {
                String replacement = String.valueOf(value);
                builder.replace(startIndex, endIndex + close.length(), replacement);
                nextIndex = startIndex + replacement.length();
            }
            startIndex = builder.indexOf(open, nextIndex);
        }
        return builder.toString();
    }

    /**
     * 渲染模板，将占位符按出现顺序依次替换为 args 数组中的值。
     * <p>
     * 占位符形如 {@code openToken + ... + closeToken}，第 n 个占位符替换为
     * {@code args[n]}；args 用尽后剩余占位符保持原样，多余的 args 被忽略。
     * 替换后的内容不再参与后续匹配。
     *
     * @param template   模板字符串，为 null 时返回 null
     * @param openToken  占位符起始标记（为 null 时按 "{" 处理）
     * @param closeToken 占位符结束标记（为 null 时按 "}" 处理）
     * @param args       依次替换的参数，为 null 或空数组时不替换
     * @return 渲染后的字符串
     */
    public static String renderByOrder(String template, String openToken, String closeToken, Object... args) {
        if (template == null || template.isEmpty() || args == null || args.length == 0) {
            return template;
        }
        String open = openToken == null ? "{" : openToken;
        String close = closeToken == null ? "}" : closeToken;
        StringBuilder builder = new StringBuilder(template);
        int argIndex = 0;
        int searchFrom = 0;
        while (argIndex < args.length) {
            int startIndex = builder.indexOf(open, searchFrom);
            if (startIndex == -1) {
                break;
            }
            int endIndex = builder.indexOf(close, startIndex + open.length());
            if (endIndex == -1) {
                break;
            }
            String replacement = String.valueOf(args[argIndex++]);
            builder.replace(startIndex, endIndex + close.length(), replacement);
            searchFrom = startIndex + replacement.length();
        }
        return builder.toString();
    }

    /**
     * 从参数对象中解析字段值。
     * Map 按键取值；普通对象反射读取字段，沿继承链查找，找不到返回 null。
     *
     * @param obj       参数对象
     * @param fieldName 字段名
     * @return 字段值，不存在或不可读时返回 null
     */
    private static Object resolveValue(Object obj, String fieldName) {
        if (obj instanceof Map) {
            Object value = ((Map<?, ?>) obj).get(fieldName);
            return value;
        }
        Field field = findField(obj.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 沿继承链查找指定名称的实例字段。
     *
     * @param clazz     起始类型
     * @param fieldName 字段名
     * @return 对应 Field（已置为可访问），找不到返回 null
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(fieldName);
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    return field;
                }
            } catch (NoSuchFieldException ignored) {
                // 当前类不存在该字段，继续向父类查找
            }
        }
        return null;
    }

    /**
     * 按类型格式化取到的值。
     * 日期类型在给定格式模板时按模板格式化；其余类型取 toString。
     *
     * @param value   字段值，非 null
     * @param pattern 格式模板，可为 null（不格式化）
     * @return 替换用的字符串
     */
    private static String formatValue(Object value, String pattern) {
        if (pattern != null && !pattern.isEmpty()) {
            if (value instanceof LocalDateTime) {
                return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(pattern));
            }
            if (value instanceof LocalDate) {
                return ((LocalDate) value).format(DateTimeFormatter.ofPattern(pattern));
            }
            if (value instanceof Date) {
                return new SimpleDateFormat(pattern).format((Date) value);
            }
        }
        return String.valueOf(value);
    }
}
