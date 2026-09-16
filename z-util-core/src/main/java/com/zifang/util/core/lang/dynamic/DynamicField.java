package com.zifang.util.core.lang.dynamic;

/**
 * @author zifang
 * <p>
 * /**
 * * comments
 * \*\/
 * // desc
 * private String name = "";
 *
 */
public class DynamicField {

    private String name;

    private String type;

    private Object value;

    private String desc;

    /**
     * of方法。
     * * @param name String类型参数
     *
     * @param type String类型参数
     * @return static DynamicField类型返回值
     */
    public static DynamicField of(String name, String type) {
        return DynamicField.of(name, type, null, null);
    }

    /**
     * of方法。
     * * @param name String类型参数
     *
     * @param type  String类型参数
     * @param value Object类型参数
     * @param desc  String类型参数
     * @return static DynamicField类型返回值
     */
    public static DynamicField of(String name, String type, Object value, String desc) {
        DynamicField dynamicField = new DynamicField();
        dynamicField.setName(name);
        dynamicField.setType(type);
        dynamicField.setValue(value);
        dynamicField.setDesc(desc);
        return dynamicField;
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * setName方法。
     * * @param name String类型参数
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getDesc方法。
     *
     * @return String类型返回值
     */
    public String getDesc() {
        return desc;
    }

    /**
     * setDesc方法。
     * * @param desc String类型参数
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * getValue方法。
     *
     * @return Object类型返回值
     */
    public Object getValue() {
        return value;
    }

    /**
     * setValue方法。
     * * @param value Object类型参数
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * getType方法。
     *
     * @return String类型返回值
     */
    public String getType() {
        return type;
    }

    /**
     * setType方法。
     * * @param type String类型参数
     */
    public void setType(String type) {
        this.type = type;
    }
}