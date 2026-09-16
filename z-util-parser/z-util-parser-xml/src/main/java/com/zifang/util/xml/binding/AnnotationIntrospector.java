package com.zifang.util.xml.binding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Bean 注解内省工具，同时支持 JAXB 注解和自研注解（通过反射按名字查找，避免编译期依赖）。
 * <p>
 * 通过 {@code Class.forName} 动态加载 JAXB 注解类，按优先级查找：
 * 自研注解 &gt; JAXB 注解 &gt; 属性 / 字段名 fallback。
 *
 * @author zifang
 */
final class AnnotationIntrospector {

    // ===== 自研注解 =====
    private static final String SELF_ROOT = "com.zifang.util.xml.binding.annotation.XmlRootElement";
    private static final String SELF_ELEMENT = "com.zifang.util.xml.binding.annotation.XmlElement";
    private static final String SELF_WRAPPER = "com.zifang.util.xml.binding.annotation.XmlElementWrapper";
    private static final String SELF_TYPE = "com.zifang.util.xml.binding.annotation.XmlType";
    private static final String SELF_ATTR = "com.zifang.util.xml.binding.annotation.XmlAttribute";

    // ===== JAXB 注解 =====
    private static final String JAXB_ROOT = "javax.xml.bind.annotation.XmlRootElement";
    private static final String JAXB_ELEMENT = "javax.xml.bind.annotation.XmlElement";
    private static final String JAXB_WRAPPER = "javax.xml.bind.annotation.XmlElementWrapper";
    private static final String JAXB_TYPE = "javax.xml.bind.annotation.XmlType";
    private static final String JAXB_ATTR = "javax.xml.bind.annotation.XmlAttribute";

    private static final Class<?> ROOT_CLASS = tryLoad(SELF_ROOT);
    private static final Class<?> JAXB_ROOT_CLASS = tryLoad(JAXB_ROOT);
    private static final Class<?> ELEMENT_CLASS = tryLoad(SELF_ELEMENT);
    private static final Class<?> JAXB_ELEMENT_CLASS = tryLoad(JAXB_ELEMENT);
    private static final Class<?> WRAPPER_CLASS = tryLoad(SELF_WRAPPER);
    private static final Class<?> JAXB_WRAPPER_CLASS = tryLoad(JAXB_WRAPPER);
    private static final Class<?> TYPE_CLASS = tryLoad(SELF_TYPE);
    private static final Class<?> JAXB_TYPE_CLASS = tryLoad(JAXB_TYPE);
    private static final Class<?> ATTR_CLASS = tryLoad(SELF_ATTR);
    private static final Class<?> JAXB_ATTR_CLASS = tryLoad(JAXB_ATTR);

    private AnnotationIntrospector() {
    }

    // ==================== Root name ====================

    /**
     * 返回 XML 根元素名。优先级：自研注解 &gt; JAXB 注解 &gt; 简单类名。
     */
    static String findRootName(Class<?> clazz) {
        String n = getAnnotationValue(clazz, ROOT_CLASS, "name");
        if (n != null && !n.isEmpty()) {
            return n;
        }
        n = getAnnotationValue(clazz, JAXB_ROOT_CLASS, "name");
        if (n != null && !n.isEmpty()) {
            return n;
        }
        return clazz.getSimpleName();
    }

    // ==================== Element name ====================

    /**
     * 返回属性对应的 XML 子元素名。查找优先级：setter 上的注解 &gt; getter &gt; field &gt; fallback。
     */
    static String findElementName(Method setter, Method getter, Field field, String fallbackName) {
        // setter 上的注解
        if (setter != null) {
            String n = getAnnotationValue(setter, ELEMENT_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(setter, JAXB_ELEMENT_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        // getter 上的注解
        if (getter != null) {
            String n = getAnnotationValue(getter, ELEMENT_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(getter, JAXB_ELEMENT_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        // 字段上的注解
        if (field != null) {
            String n = getAnnotationValue(field, ELEMENT_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(field, JAXB_ELEMENT_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        return fallbackName;
    }

    // ==================== Wrapper name ====================

    /**
     * 返回 List 属性的外层包裹元素名，若无 @XmlElementWrapper 注解则返回 null。
     */
    static String findWrapperName(Method setter, Method getter, Field field) {
        if (setter != null) {
            String n = getAnnotationValue(setter, WRAPPER_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(setter, JAXB_WRAPPER_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        if (getter != null) {
            String n = getAnnotationValue(getter, WRAPPER_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(getter, JAXB_WRAPPER_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        if (field != null) {
            String n = getAnnotationValue(field, WRAPPER_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(field, JAXB_WRAPPER_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        return null;
    }

    // ==================== Attribute name ====================

    /**
     * 返回属性对应的 XML 属性名（非子元素），若无 @XmlAttribute 注解则返回 null。
     */
    static String findAttrName(Method setter, Method getter, Field field) {
        if (setter != null) {
            String n = getAnnotationValue(setter, ATTR_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(setter, JAXB_ATTR_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        if (getter != null) {
            String n = getAnnotationValue(getter, ATTR_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(getter, JAXB_ATTR_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        if (field != null) {
            String n = getAnnotationValue(field, ATTR_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
            n = getAnnotationValue(field, JAXB_ATTR_CLASS, "name");
            if (n != null && !n.isEmpty()) {
                return n;
            }
        }
        return null;
    }

    // ==================== propOrder ====================

    /**
     * 返回 @XmlType.propOrder()。无注解或无 propOrder 时返回 null。
     */
    static String[] findPropOrder(Class<?> clazz) {
        String[] order = getAnnotationStringArrayValue(clazz, TYPE_CLASS, "propOrder");
        if (order != null && order.length > 0) {
            return order;
        }
        order = getAnnotationStringArrayValue(clazz, JAXB_TYPE_CLASS, "propOrder");
        if (order != null && order.length > 0) {
            return order;
        }
        return null;
    }

    // ==================== Internal helpers ====================

    @SuppressWarnings("unchecked")
    private static String getAnnotationValue(Object target, Class<?> annClass, String methodName) {
        if (annClass == null) {
            return null;
        }
        Annotation ann;
        if (target instanceof Class) {
            ann = ((Class<?>) target).getAnnotation((Class<? extends Annotation>) annClass);
        } else if (target instanceof Method) {
            ann = ((Method) target).getAnnotation((Class<? extends Annotation>) annClass);
        } else if (target instanceof Field) {
            ann = ((Field) target).getAnnotation((Class<? extends Annotation>) annClass);
        } else {
            return null;
        }
        if (ann == null) {
            return null;
        }
        try {
            Object val = annClass.getMethod(methodName).invoke(ann);
            return val == null ? "" : val.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static String[] getAnnotationStringArrayValue(Class<?> clazz, Class<?> annClass, String methodName) {
        if (annClass == null || clazz == null) {
            return null;
        }
        Annotation ann = clazz.getAnnotation((Class<? extends Annotation>) annClass);
        if (ann == null) {
            return null;
        }
        try {
            Object val = annClass.getMethod(methodName).invoke(ann);
            if (val instanceof String[]) {
                return (String[]) val;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Class<?> tryLoad(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // ==================== Property discovery ====================

    /**
     * 属性元数据，描述一个 bean 属性的字段、getter、setter 以及派生信息。
     */
    static class PropertyMeta {
        final Field field;
        final Method getter;
        final Method setter;
        final String propName;   // Java 属性名（小写开头）
        final Class<?> propertyType;

        PropertyMeta(Field field, Method getter, Method setter, String propName, Class<?> type) {
            this.field = field;
            this.getter = getter;
            this.setter = setter;
            this.propName = propName;
            this.propertyType = type;
        }
    }

    /**
     * 发现 bean 类的所有可映射属性：遵循 JAXB 默认规则（getter/setter 配对 + 私有字段）。
     */
    static java.util.List<PropertyMeta> discoverProperties(Class<?> clazz) {
        java.util.List<PropertyMeta> props = new java.util.ArrayList<>();
        java.util.Map<String, Field> fieldMap = new java.util.LinkedHashMap<>();
        for (Field f : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                continue;
            }
            fieldMap.put(f.getName(), f);
        }
        java.util.Map<String, Method[]> accessors = new java.util.LinkedHashMap<>();
        for (Method m : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers())) continue;
            String methodName = m.getName();
            String propName = null;
            if (methodName.length() > 3 && methodName.startsWith("get") && m.getParameterCount() == 0) {
                propName = decapitalize(methodName.substring(3));
                if (accessors.containsKey(propName)) {
                    accessors.get(propName)[0] = m;
                } else {
                    accessors.put(propName, new Method[]{m, null});
                }
            } else if (methodName.length() > 2 && methodName.startsWith("is")
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)
                    && m.getParameterCount() == 0) {
                propName = decapitalize(methodName.substring(2));
                if (accessors.containsKey(propName)) {
                    accessors.get(propName)[0] = m;
                } else {
                    accessors.put(propName, new Method[]{m, null});
                }
            } else if (methodName.length() > 3 && methodName.startsWith("set") && m.getParameterCount() == 1) {
                propName = decapitalize(methodName.substring(3));
                if (accessors.containsKey(propName)) {
                    accessors.get(propName)[1] = m;
                } else {
                    accessors.put(propName, new Method[]{null, m});
                }
            }
        }
        // 组装 PropertyMeta
        for (java.util.Map.Entry<String, Method[]> entry : accessors.entrySet()) {
            String propName = entry.getKey();
            Method getter = entry.getValue()[0];
            Method setter = entry.getValue()[1];
            Field field = fieldMap.get(propName);
            Class<?> type = field != null ? field.getType()
                    : (setter != null ? setter.getParameterTypes()[0]
                    : (getter != null ? getter.getReturnType() : Object.class));
            if (setter == null && getter == null && field == null) continue;
            props.add(new PropertyMeta(field, getter, setter, propName, type));
        }
        // 如果某个字段没有对应 accessor，但有 public field，也要加入
        for (java.util.Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            String propName = entry.getKey();
            if (accessors.containsKey(propName)) continue;
            Field field = entry.getValue();
            if (Modifier.isPublic(field.getModifiers())) {
                props.add(new PropertyMeta(field, null, null, propName, field.getType()));
            }
        }
        return props;
    }

    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] c = name.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
