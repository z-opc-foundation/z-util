package com.zifang.util.core.lang.dynamic;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态类解析和生成工具类。
 * <p>
 * 提供将 Class 解析为 DynamicClass 结构，以及从 DynamicClass 生成源码的能力。
 *
 * @author zifang
 * @see DynamicClass
 * @see DynamicField
 * @see DynamicMethod
 */
public class DynamicClassUtil {

    /**
     * parser方法。
     * * @param clazz Class?类型参数
     *
     * @return static DynamicClass类型返回值
     */
    public static DynamicClass parser(Class<?> clazz) {
        DynamicClass dynamicClass = new DynamicClass();
        dynamicClass.setAnnotations(parserAnnotations(Arrays.asList(clazz.getAnnotations())));
        dynamicClass.setInterface(clazz.isInterface());
        dynamicClass.setPackageName(clazz.getPackage().getName());
        dynamicClass.setClassName(clazz.getName());
        dynamicClass.setImplementClasses(parserClass(Arrays.asList(clazz.getInterfaces())));
        dynamicClass.setFields(parserFields(Arrays.asList(clazz.getDeclaredFields())));
        dynamicClass.setMethods(parserMethods(Arrays.asList(clazz.getDeclaredMethods())));
        return dynamicClass;
    }

    private static Map<String, Object> parser(Annotation annotation) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", annotation.annotationType().getName());
        Map<String, Object> attributes = new HashMap<>();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            try {
                method.setAccessible(true);
                attributes.put(method.getName(), method.invoke(annotation));
            } catch (Exception e) {
                // ignore
            }
        }
        result.put("attributes", attributes);
        return result;
    }

    /**
     * parser方法。
     * * @param field Field类型参数
     *
     * @return static DynamicField类型返回值
     */
    public static DynamicField parser(Field field) {
        DynamicField dynamicField = new DynamicField();
        dynamicField.setName(field.getName());
        dynamicField.setType(field.getType().getName());
        try {
            field.setAccessible(true);
            dynamicField.setValue(field.get(null));
        } catch (Exception e) {
            // ignore for instance fields
        }
        dynamicField.setDesc(field.getType().getName() + " " + field.getName());
        return dynamicField;
    }

    /**
     * parser方法。
     * * @param method Method类型参数
     *
     * @return static DynamicMethod类型返回值
     */
    public static DynamicMethod parser(Method method) {
        DynamicMethod dynamicMethod = new DynamicMethod();
        dynamicMethod.setMethodName(method.getName());
        dynamicMethod.setReturnType(method.getReturnType().getName());
        List<String> params = new ArrayList<>();
        for (Class<?> paramType : method.getParameterTypes()) {
            params.add(paramType.getName());
        }
        dynamicMethod.setParameters(params);
        dynamicMethod.setDesc(method.getName() + "(" + String.join(",", params) + ")");
        return dynamicMethod;
    }

    /**
     * parserClass方法。
     * * @param clazzes ListClass?类型参数
     *
     * @return static List<DynamicClass>类型返回值
     */
    public static List<DynamicClass> parserClass(List<Class<?>> clazzes) {
        return clazzes.stream().map(DynamicClassUtil::parser).collect(Collectors.toList());
    }

    /**
     * parserMethods方法。
     * * @param methods ListMethod类型参数
     *
     * @return static List<DynamicMethod>类型返回值
     */
    public static List<DynamicMethod> parserMethods(List<Method> methods) {
        return methods.stream().map(DynamicClassUtil::parser).collect(Collectors.toList());
    }

    /**
     * parserFields方法。
     * * @param fields ListField类型参数
     *
     * @return static List<DynamicField>类型返回值
     */
    public static List<DynamicField> parserFields(List<Field> fields) {
        return fields.stream().map(DynamicClassUtil::parser).collect(Collectors.toList());
    }

    private static List<?> parserAnnotations(List<Annotation> annotations) {
        return annotations.stream().map(DynamicClassUtil::parser).collect(Collectors.toList());
    }

    /**
     * parser方法。
     * * @param dynamicClass DynamicClass类型参数
     *
     * @return static Class<?>类型返回值
     */
    public static Class<?> parser(DynamicClass dynamicClass) {
        // Generating a Class from DynamicClass requires bytecode generation library
        // Return the class of DynamicClass as a placeholder
        return DynamicClass.class;
    }

    /**
     * parserAsCode方法。
     * * @param dynamicClass DynamicClass类型参数
     *
     * @return static String类型返回值
     */
    public static String parserAsCode(DynamicClass dynamicClass) {
        return generateClassSource(dynamicClass);
    }

    /**
     * generateClassSource方法。
     * * @param dynamicBean DynamicClass类型参数
     *
     * @return static String类型返回值
     */
    public static String generateClassSource(DynamicClass dynamicBean) {
        if (dynamicBean == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        // Package
        if (dynamicBean.getPackageName() != null && !dynamicBean.getPackageName().isEmpty()) {
            sb.append("package ").append(dynamicBean.getPackageName()).append(";\n\n");
        }

        // Annotations
        for (Object annotationObj : dynamicBean.getAnnotations()) {
            if (annotationObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> annotation = (Map<String, Object>) annotationObj;
                sb.append("@").append(annotation.get("name")).append("\n");
            }
        }

        // Class declaration
        String classKeyword = Boolean.TRUE.equals(dynamicBean.getInterface()) ? "interface" : "class";
        sb.append("public ").append(classKeyword).append(" ").append(dynamicBean.getClassName()).append(" {\n");

        // Fields
        for (DynamicField field : dynamicBean.getFields()) {
            sb.append("    private ").append(field.getType()).append(" ").append(field.getName()).append(";\n");
        }

        // Methods
        for (DynamicMethod method : dynamicBean.getMethods()) {
            sb.append("    public ").append(method.getReturnType()).append(" ")
                    .append(method.getMethodName()).append("(");
            @SuppressWarnings("unchecked")
            List<String> params = (List<String>) method.getParameters();
            sb.append(String.join(", ", params));
            sb.append(") {\n        ");
            if (method.getBody() != null) {
                sb.append(method.getBody());
            }
            sb.append("\n    }\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * generateClassSource方法。
     * * @param json String类型参数
     *
     * @return static List<String>类型返回值
     */
    public static List<String> generateClassSource(String json) {
        return generateClassSource(parseJsonObject(json));
    }

    /**
     * 内嵌极简 JSON 解析器（仅 object / string / number / true / false / null），
     * 避免 z-util-core 反向依赖 z-util-parser-json。
     * 如需复杂 JSON，请使用 z-util-parser-json 的 JsonUtil。
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJsonObject(String json) {
        SimpleJsonParser p = new SimpleJsonParser(json);
        p.skipWs();
        Object v = p.parseValue();
        p.skipWs();
        if (v instanceof Map) return (Map<String, Object>) v;
        return new LinkedHashMap<>();
    }

    /** 极简 JSON 解析器，仅支持 DynamicClassUtil 所需子集。 */
    private static final class SimpleJsonParser {
        private final String src;
        private int pos;

        SimpleJsonParser(String src) { this.src = src; }

        void skipWs() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }

        Object parseValue() {
            skipWs();
            if (pos >= src.length()) return null;
            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++;
            skipWs();
            if (pos < src.length() && src.charAt(pos) == '}') { pos++; return map; }
            while (true) {
                skipWs();
                if (src.charAt(pos) != '"') return map;
                String key = parseString();
                skipWs();
                if (src.charAt(pos) != ':') return map;
                pos++;
                map.put(key, parseValue());
                skipWs();
                if (pos < src.length() && src.charAt(pos) == ',') { pos++; continue; }
                if (pos < src.length() && src.charAt(pos) == '}') { pos++; return map; }
                return map;
            }
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++;
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ']') { pos++; return list; }
            while (true) {
                list.add(parseValue());
                skipWs();
                if (pos < src.length() && src.charAt(pos) == ',') { pos++; continue; }
                if (pos < src.length() && src.charAt(pos) == ']') { pos++; return list; }
                return list;
            }
        }

        private String parseString() {
            pos++;
            StringBuilder sb = new StringBuilder();
            while (pos < src.length() && src.charAt(pos) != '"') {
                char c = src.charAt(pos);
                if (c == '\\' && pos + 1 < src.length()) {
                    char next = src.charAt(pos + 1);
                    switch (next) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case '\\': sb.append('\\'); break;
                        case '"': sb.append('"'); break;
                        default: sb.append(next);
                    }
                    pos += 2;
                } else {
                    sb.append(c);
                    pos++;
                }
            }
            if (pos < src.length()) pos++;
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (src.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            return null;
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) { pos += 4; return null; }
            return null;
        }

        private Object parseNumber() {
            int start = pos;
            if (pos < src.length() && (src.charAt(pos) == '-' || src.charAt(pos) == '+')) pos++;
            while (pos < src.length() && "0123456789.eE+-".indexOf(src.charAt(pos)) >= 0) pos++;
            String num = src.substring(start, pos);
            if (num.isEmpty()) return null;
            if (num.contains(".") || num.contains("e") || num.contains("E")) return Double.valueOf(num);
            try { return Long.valueOf(num); } catch (NumberFormatException e) { return Double.valueOf(num); }
        }
    }

    /**
     * generateClassSource方法。
     * * @param map MapString,Object类型参数
     *
     * @return static List<String>类型返回值
     */
    public static List<String> generateClassSource(Map<String, Object> map) {
        List<DynamicClass> dynamicClasses = parserAsDynamicClass(map);
        return dynamicClasses.stream().map(DynamicClass::generateSourceCode).collect(Collectors.toList());
    }

    private static List<DynamicClass> parserAsDynamicClass(Map<String, Object> map) {
        return parserAsDynamicClassUnit(map).collect();
    }

    private static DynamicClassUnit parserAsDynamicClassUnit(Map<String, Object> map) {
        DynamicClassUnit dynamicClassUnit = new DynamicClassUnit();
        DynamicClass dynamicClass = new DynamicClass();
        dynamicClass.setClassName("Bean_" + System.currentTimeMillis());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            // 无值 归纳为String
            if (entry.getValue() == null) {
                dynamicClass.addField(entry.getKey(), String.class, true, true);
                continue;
            }

            if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
                DynamicClassUnit sub = parserAsDynamicClassUnit((Map<String, Object>) entry.getValue());
                dynamicClass.addField(entry.getKey(), sub.getMain().getType(), true, true);
                dynamicClassUnit.getSub().add(sub);
            }

            if (List.class.isAssignableFrom(entry.getValue().getClass())) {
                List<Object> list = (List<Object>) entry.getValue();
                Object o = list.get(0);
                if (o instanceof Map) {
                    // result.addAll(parserAsDynamicClass((Map<String, Object>) o));
                } else if (o instanceof List) {
                    // result.addAll(parserAsDynamicClass((Map<String, Object>) o));
                }
            }

            dynamicClass.addField(entry.getKey(), String.class, true, true);
        }

        dynamicClassUnit.setMain(dynamicClass);

        return dynamicClassUnit;
    }

}
