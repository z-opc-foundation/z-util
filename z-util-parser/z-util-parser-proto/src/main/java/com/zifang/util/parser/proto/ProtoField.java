package com.zifang.util.parser.proto;

import java.util.Objects;

/**
 * Proto 字段模型。
 *
 * @author zifang
 */

/**
 * ProtoField类。
 */
public class ProtoField {

    /**
     * 字段类型：int32, string, bool, 等
     */
    private final String type;

    /**
     * 字段名称
     */
    private final String name;

    /**
     * 字段标签号
     */
    private final int tag;

    /**
     * 是否为 repeated（数组）修饰符
     */
    private final boolean repeated;

    /**
     * ProtoField方法。
     * * @param type String类型参数
     *
     * @param name     String类型参数
     * @param tag      int类型参数
     * @param repeated boolean类型参数
     */
    public ProtoField(String type, String name, int tag, boolean repeated) {
        this.type = type;
        this.name = name;
        this.tag = tag;
        this.repeated = repeated;
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
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * getTag方法。
     *
     * @return int类型返回值
     */
    public int getTag() {
        return tag;
    }

    /**
     * isRepeated方法。
     *
     * @return boolean类型返回值
     */
    public boolean isRepeated() {
        return repeated;
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtoField protoField = (ProtoField) o;
        return tag == protoField.tag &&
                repeated == protoField.repeated &&
                Objects.equals(type, protoField.type) &&
                Objects.equals(name, protoField.name);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(type, name, tag, repeated);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (repeated) {
            sb.append("repeated ");
        }
        sb.append(type).append(" ").append(name).append(" = ").append(tag).append(";");
        return sb.toString();
    }
}
