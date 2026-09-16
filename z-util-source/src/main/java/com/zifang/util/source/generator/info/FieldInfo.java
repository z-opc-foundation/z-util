package com.zifang.util.source.generator.info;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段信息封装类
 * <p>
 * 用于封装Java类的字段（Field）的完整元数据信息，
 * 包括字段类型、名称、修饰符、初始值等属性。
 * 支持字段的构建、比较、哈希计算和字符串表示。
 *
 * @author zifang
 * @version 1.0.0
 */
public class FieldInfo {

    /**
     * 字段类型全限定名
     */
    private String type;

    /**
     * 字段名称
     */
    private String value;

    /**
     * 修饰符数组，使用 java.lang.reflect.Modifier 的常量值
     */
    private int[] modifiers = new int[]{};

    /**
     * 初始值表达式字符串，默认为 "null"
     */
    private String initializer = "null";

    /**
     * 注解列表
     */
    private List<AnnotationInfo> annotations = new ArrayList<>();

    /**
     * 默认构造函数，创建一个空的FieldInfo实例
     */
    public FieldInfo() {
    }

    /**
     * 构造函数，创建指定类型和名称的FieldInfo实例
     *
     * @param type  字段类型全限定名，如 "java.lang.String"
     * @param value 字段名称
     */
    public FieldInfo(String type, String value) {
        this.type = type;
        this.value = value;
        this.modifiers = new int[]{};
        this.initializer = "null";
    }

    /**
     * 构造函数，创建完整配置的FieldInfo实例
     *
     * @param type        字段类型全限定名
     * @param value       字段名称
     * @param modifiers   修饰符数组，使用 java.lang.reflect.Modifier 的常量值
     * @param initializer 初始值表达式字符串
     */
    public FieldInfo(String type, String value, int[] modifiers, String initializer) {
        this.type = type;
        this.value = value;
        this.modifiers = modifiers;
        this.initializer = initializer;
    }

    /**
     * 获取字段类型
     *
     * @return 字段类型全限定名
     */
    public String getType() {
        return type;
    }

    /**
     * 设置字段类型
     *
     * @param type 字段类型全限定名
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取字段名称
     *
     * @return 字段名称
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置字段名称
     *
     * @param value 字段名称
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取修饰符数组
     *
     * @return 修饰符数组
     */
    public int[] getModifiers() {
        return modifiers;
    }

    /**
     * 设置修饰符数组
     *
     * @param modifiers 修饰符数组
     */
    public void setModifiers(int[] modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * 获取初始值表达式
     *
     * @return 初始值表达式字符串
     */
    public String getInitializer() {
        return initializer;
    }

    /**
     * 设置初始值表达式
     *
     * @param initializer 初始值表达式字符串
     */
    public void setInitializer(String initializer) {
        this.initializer = initializer;
    }

    /**
     * 设置修饰符
     *
     * @param modifier 可变参数形式的修饰符
     */
    public void setModifier(int... modifier) {
        modifiers = modifier;
    }

    /**
     * getAnnotations方法。
     *
     * @return List<AnnotationInfo>类型返回值
     */
    public List<AnnotationInfo> getAnnotations() {
        return annotations;
    }

    /**
     * setAnnotations方法。
     * * @param annotations ListAnnotationInfo类型参数
     */
    public void setAnnotations(List<AnnotationInfo> annotations) {
        this.annotations = annotations;
    }

    @Override
    /**
     * equals方法。
     *      * @param obj Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object obj) {
        return value.equals(((FieldInfo) obj).value);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (modifiers != null ? java.util.Arrays.hashCode(modifiers) : 0);
        result = 31 * result + (initializer != null ? initializer.hashCode() : 0);
        return result;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return type + " " + value + " = " + initializer + ";";
    }
}
