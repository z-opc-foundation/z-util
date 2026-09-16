package com.zifang.util.xml.binding;

import com.zifang.util.xml.model.*;
import com.zifang.util.xml.util.XmlFormatter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean ↔ XML 核心绑定器（内部实现，支持 JAXB 注解与自研注解两种路径）。
 * <p>
 * <b>Marshal（toXml）</b>：将 Java Bean 递归序列化为 XDocument 树，再调用 {@link XmlFormatter} 输出。
 * <br>
 * <b>Unmarshal（fromXml）</b>：调用 {@link com.zifang.util.xml.XmlUtil#parse(String)} 解析 XML，
 * 再递归填充 Java Bean 字段 / setter。
 * <p>
 * 性能优化：使用 ClassMeta 缓存每个类的属性发现结果，避免递归调用时重复反射。
 *
 * @author zifang
 */
class XmlBinder {

    /**
     * 类属性缓存：避免对同一个 Class 重复调用 discoverProperties + 构建查找表。
     */
    private static final Map<Class<?>, ClassMeta> META_CACHE = new ConcurrentHashMap<>();

    private static final class ClassMeta {
        final List<AnnotationIntrospector.PropertyMeta> props;
        final Map<String, AnnotationIntrospector.PropertyMeta> propByElementName;
        final Map<String, AnnotationIntrospector.PropertyMeta> propByName;
        final Map<String, AnnotationIntrospector.PropertyMeta> propByWrapperName;
        final Map<String, AnnotationIntrospector.PropertyMeta> propByAttrName;
        final String[] propOrder;

        ClassMeta(List<AnnotationIntrospector.PropertyMeta> props,
                  Map<String, AnnotationIntrospector.PropertyMeta> propByElementName,
                  Map<String, AnnotationIntrospector.PropertyMeta> propByName,
                  Map<String, AnnotationIntrospector.PropertyMeta> propByWrapperName,
                  Map<String, AnnotationIntrospector.PropertyMeta> propByAttrName,
                  String[] propOrder) {
            this.props = props;
            this.propByElementName = propByElementName;
            this.propByName = propByName;
            this.propByWrapperName = propByWrapperName;
            this.propByAttrName = propByAttrName;
            this.propOrder = propOrder;
        }
    }

    private static ClassMeta getMeta(Class<?> clazz) {
        return META_CACHE.computeIfAbsent(clazz, XmlBinder::buildMeta);
    }

    private static ClassMeta buildMeta(Class<?> clazz) {
        List<AnnotationIntrospector.PropertyMeta> props = AnnotationIntrospector.discoverProperties(clazz);
        Map<String, AnnotationIntrospector.PropertyMeta> propByElementName = new LinkedHashMap<>();
        Map<String, AnnotationIntrospector.PropertyMeta> propByName = new LinkedHashMap<>();
        Map<String, AnnotationIntrospector.PropertyMeta> propByWrapperName = new LinkedHashMap<>();
        Map<String, AnnotationIntrospector.PropertyMeta> propByAttrName = new LinkedHashMap<>();
        for (AnnotationIntrospector.PropertyMeta prop : props) {
            propByName.put(prop.propName, prop);
            String elementName = AnnotationIntrospector.findElementName(
                    prop.setter, prop.getter, prop.field, prop.propName);
            propByElementName.put(elementName, prop);
            String wrapperName = AnnotationIntrospector.findWrapperName(
                    prop.setter, prop.getter, prop.field);
            if (wrapperName != null) {
                propByWrapperName.put(wrapperName, prop);
            }
            String attrName = AnnotationIntrospector.findAttrName(
                    prop.setter, prop.getter, prop.field);
            if (attrName != null) {
                propByAttrName.put(attrName, prop);
            }
        }
        String[] propOrder = AnnotationIntrospector.findPropOrder(clazz);
        return new ClassMeta(props, propByElementName, propByName, propByWrapperName, propByAttrName, propOrder);
    }

    // ==================== Marshal (Bean → XML) ====================

    /**
     * 将 bean 序列化为 XDocument。
     */
    XDocument marshal(Object obj) {
        if (obj == null) {
            throw new XmlBindingException("无法序列化 null 对象");
        }
        String rootName = AnnotationIntrospector.findRootName(obj.getClass());
        XElement root = new XElement(rootName);
        marshalBean(obj, root, obj.getClass());
        return new XDocument(root);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void marshalBean(Object bean, XElement parent, Class<?> clazz) {
        ClassMeta meta = getMeta(clazz);
        List<AnnotationIntrospector.PropertyMeta> props = meta.props;

        // propOrder 支持
        if (meta.propOrder != null) {
            Map<String, AnnotationIntrospector.PropertyMeta> propMap = new LinkedHashMap<>();
            for (AnnotationIntrospector.PropertyMeta p : props) {
                propMap.put(p.propName, p);
            }
            List<AnnotationIntrospector.PropertyMeta> ordered = new ArrayList<>();
            for (String name : meta.propOrder) {
                AnnotationIntrospector.PropertyMeta p = propMap.get(name);
                if (p != null) {
                    ordered.add(p);
                }
            }
            for (AnnotationIntrospector.PropertyMeta p : props) {
                if (!ordered.contains(p)) {
                    ordered.add(p);
                }
            }
            props = ordered;
        }

        for (AnnotationIntrospector.PropertyMeta prop : props) {
            Object value = readValue(bean, prop);
            if (value == null) {
                continue;
            }
            String elementName = AnnotationIntrospector.findElementName(
                    prop.setter, prop.getter, prop.field, prop.propName);
            String wrapperName = AnnotationIntrospector.findWrapperName(
                    prop.setter, prop.getter, prop.field);
            String attrName = AnnotationIntrospector.findAttrName(
                    prop.setter, prop.getter, prop.field);

            if (attrName != null) {
                // @XmlAttribute：输出为 XML 属性
                parent.setAttribute(attrName, XmlConverters.toText(value));
                continue;
            }

            if (value instanceof Collection && wrapperName != null) {
                // @XmlElementWrapper：包裹输出
                XElement wrapper = new XElement(wrapperName);
                Collection<?> collection = (Collection<?>) value;
                for (Object item : collection) {
                    String itemName = findItemElementName(prop);
                    if (item == null) {
                        continue;
                    }
                    if (isSimpleType(item.getClass())) {
                        XElement itemEl = new XElement(itemName);
                        itemEl.setText(XmlConverters.toText(item));
                        wrapper.addChild(itemEl);
                    } else {
                        XElement itemEl = new XElement(itemName);
                        marshalBean(item, itemEl, item.getClass());
                        wrapper.addChild(itemEl);
                    }
                }
                parent.addChild(wrapper);
                continue;
            }

            if (value instanceof Collection) {
                // 无 wrapper 的集合：每个元素直接作为同名兄弟
                Collection<?> collection = (Collection<?>) value;
                String itemName = findItemElementName(prop);
                for (Object item : collection) {
                    if (item == null) {
                        continue;
                    }
                    if (isSimpleType(item.getClass())) {
                        XElement itemEl = new XElement(itemName);
                        itemEl.setText(XmlConverters.toText(item));
                        parent.addChild(itemEl);
                    } else {
                        XElement itemEl = new XElement(itemName);
                        marshalBean(item, itemEl, item.getClass());
                        parent.addChild(itemEl);
                    }
                }
                continue;
            }

            if (isSimpleType(value.getClass())) {
                // 简单类型：叶子元素
                XElement leafEl = new XElement(elementName);
                leafEl.setText(XmlConverters.toText(value));
                parent.addChild(leafEl);
            } else {
                // 复合 Bean：递归
                XElement child = new XElement(elementName);
                marshalBean(value, child, value.getClass());
                parent.addChild(child);
            }
        }
    }

    /**
     * 找到集合 item 的 XML 元素名：getter 上 @XmlElement(name) 或属性名。
     */
    private String findItemElementName(AnnotationIntrospector.PropertyMeta prop) {
        // 集合 item 的 element name 取 prop 上的 @XmlElement
        String name = AnnotationIntrospector.findElementName(
                prop.setter, prop.getter, prop.field, prop.propName);
        return name;
    }

    private Object readValue(Object bean, AnnotationIntrospector.PropertyMeta prop) {
        try {
            if (prop.getter != null) {
                Object val = prop.getter.invoke(bean);
                return val;
            }
            if (prop.field != null) {
                prop.field.setAccessible(true);
                return prop.field.get(bean);
            }
            return null;
        } catch (Exception e) {
            throw new XmlBindingException("读取属性 " + prop.propName + " 失败", e);
        }
    }

    // ==================== Unmarshal (XML → Bean) ====================

    @SuppressWarnings({"unchecked", "rawtypes"})
    <T> T unmarshal(XDocument doc, Class<T> clazz) {
        if (doc == null || doc.getRoot() == null) {
            throw new XmlBindingException("XML 文档为空或缺少根元素");
        }
        String expectedRoot = AnnotationIntrospector.findRootName(clazz);
        if (!doc.getRoot().getName().equals(expectedRoot)) {
            throw new XmlBindingException("根元素不匹配：期望 '" + expectedRoot + "'，实际 '"
                    + doc.getRoot().getName() + "'");
        }
        T instance = createInstance(clazz);
        populateBean(doc.getRoot(), instance, clazz);
        return instance;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void populateBean(XElement parent, Object bean, Class<?> clazz) {
        ClassMeta meta = getMeta(clazz);
        List<AnnotationIntrospector.PropertyMeta> props = meta.props;

        // 处理属性（@XmlAttribute）- 使用预计算的 O(1) 查找表
        Map<String, String> attributes = parent.getAttributes();
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            AnnotationIntrospector.PropertyMeta attrProp = meta.propByAttrName.get(attr.getKey());
            if (attrProp != null) {
                Object val = XmlConverters.convert(attr.getValue(), attrProp.propertyType);
                setValue(bean, attrProp, val);
            }
        }

        // 处理子元素
        for (XNode child : parent.getChildren()) {
            if (!(child instanceof XElement)) {
                // XText：跳过空白文本
                if (child instanceof XText) {
                    String text = ((XText) child).getText();
                    if (text != null && !text.trim().isEmpty()) {
                        // 有可能是某个简单类型字段的文本值（JAXB 特殊情况：root 元素下直接放文本）
                        // 这里暂不处理，按标准 Bean 映射走子元素路径
                    }
                }
                continue;
            }
            XElement childEl = (XElement) child;
            String childName = childEl.getName();

            // 检查是否为 @XmlElementWrapper
            AnnotationIntrospector.PropertyMeta wrapperProp = meta.propByWrapperName.get(childName);
            if (wrapperProp != null) {
                // wrapper 内的每个子元素收集为 List
                Class<?> itemType = getListItemType(wrapperProp);
                Collection<Object> collection = createCollection(wrapperProp.propertyType);
                for (XNode itemNode : childEl.getChildren()) {
                    if (itemNode instanceof XElement) {
                        Object item;
                        if (isSimpleType(itemType)) {
                            item = XmlConverters.convert(((XElement) itemNode).getText(), itemType);
                        } else {
                            item = createInstance(itemType);
                            populateBean((XElement) itemNode, item, itemType);
                        }
                        collection.add(item);
                    }
                }
                setValue(bean, wrapperProp, collection);
                continue;
            }

            // 普通子元素
            AnnotationIntrospector.PropertyMeta prop = meta.propByElementName.get(childName);
            if (prop == null) {
                // 按属性名二次查找
                prop = meta.propByName.get(childName);
            }
            if (prop == null) {
                // 忽略不认识的元素（容错）
                continue;
            }

            // 集合属性（无 wrapper）：多个同名子元素追加到 List
            if (isCollectionType(prop.propertyType)) {
                Class<?> itemType = getListItemType(prop);
                Collection<Object> existing = (Collection<Object>) readValue(bean, prop);
                if (existing == null) {
                    existing = createCollection(prop.propertyType);
                }
                Object item;
                if (isSimpleType(itemType)) {
                    item = XmlConverters.convert(childEl.getText(), itemType);
                } else {
                    item = createInstance(itemType);
                    populateBean(childEl, item, itemType);
                }
                existing.add(item);
                setValue(bean, prop, existing);
                continue;
            }

            // 简单类型
            if (isSimpleType(prop.propertyType)) {
                String text = childEl.getText();
                Object val = XmlConverters.convert(text, prop.propertyType);
                setValue(bean, prop, val);
                continue;
            }

            // 复合 Bean
            Object childObj = createInstance(prop.propertyType);
            populateBean(childEl, childObj, prop.propertyType);
            setValue(bean, prop, childObj);
        }
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new XmlBindingException("类 " + clazz.getName() + " 必须有无参构造器", e);
        } catch (Exception e) {
            throw new XmlBindingException("实例化 " + clazz.getName() + " 失败", e);
        }
    }

    private void setValue(Object bean, AnnotationIntrospector.PropertyMeta prop, Object value) {
        try {
            if (prop.setter != null) {
                prop.setter.invoke(bean, value);
                return;
            }
            if (prop.field != null) {
                prop.field.setAccessible(true);
                prop.field.set(bean, value);
                return;
            }
        } catch (Exception e) {
            throw new XmlBindingException("设置属性 " + prop.propName + " 失败", e);
        }
    }

    static boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Boolean.class || type == boolean.class
                || type == Double.class || type == double.class
                || type == Float.class || type == float.class
                || type == Short.class || type == short.class
                || type == Byte.class || type == byte.class
                || type == Character.class || type == char.class
                || Number.class.isAssignableFrom(type)
                || type.isEnum();
    }

    private boolean isCollectionType(Class<?> type) {
        return Collection.class.isAssignableFrom(type)
                || type == List.class || type == ArrayList.class;
    }

    @SuppressWarnings("unchecked")
    private Class<?> getListItemType(AnnotationIntrospector.PropertyMeta prop) {
        // 尝试从 getter 返回类型的泛型参数获取
        if (prop.getter != null) {
            Type genType = prop.getter.getGenericReturnType();
            if (genType instanceof ParameterizedType) {
                Type[] args = ((ParameterizedType) genType).getActualTypeArguments();
                if (args.length > 0 && args[0] instanceof Class) {
                    return (Class<?>) args[0];
                }
            }
        }
        // 尝试从字段泛型获取
        if (prop.field != null) {
            Type genType = prop.field.getGenericType();
            if (genType instanceof ParameterizedType) {
                Type[] args = ((ParameterizedType) genType).getActualTypeArguments();
                if (args.length > 0 && args[0] instanceof Class) {
                    return (Class<?>) args[0];
                }
            }
        }
        return Object.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<Object> createCollection(Class<?> type) {
        if (type == List.class || type == ArrayList.class || Collection.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        }
        // 其他 Collection 实现：尝试实例化
        try {
            return (Collection<Object>) type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 判断对象是否为简单类型（值输出）。
     */
    static boolean isSimpleValue(Object value) {
        return value == null || value instanceof String || value instanceof Number
                || value instanceof Boolean || value instanceof Character || value.getClass().isEnum();
    }

    // ==================== XElement 子元素创建辅助 ====================

    /**
     * 构造一个带文本内容的 XElement（JAXB 格式的叶子节点）。
     * <p>
     * 内部用法：{@code new XElement(name, textValue)}。
     */
}
