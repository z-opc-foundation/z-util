package com.zifang.util.parser.proto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Proto Enum 模型。
 *
 * @author zifang
 */

/**
 * ProtoEnum类。
 */
public class ProtoEnum {

    private final String name;
    private final Map<String, Integer> values;

    /**
     * ProtoEnum方法。
     * * @param name String类型参数
     */
    public ProtoEnum(String name) {
        this.name = name;
        this.values = new LinkedHashMap<>();
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
     * addValue方法。
     * * @param enumName String类型参数
     *
     * @param number int类型参数
     */
    public void addValue(String enumName, int number) {
        values.put(enumName, number);
    }

    /**
     * getValues方法。
     *
     * @return Map<String, Integer>类型返回值
     */
    public Map<String, Integer> getValues() {
        return values;
    }

    /**
     * getValue方法。
     * * @param enumName String类型参数
     *
     * @return int类型返回值
     */
    public int getValue(String enumName) {
        Integer val = values.get(enumName);
        return val != null ? val : 0;
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
        ProtoEnum protoEnum = (ProtoEnum) o;
        return Objects.equals(name, protoEnum.name) &&
                Objects.equals(values, protoEnum.values);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(name, values);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("enum ").append(name).append(" { ");
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("; ");
        }
        sb.append("}");
        return sb.toString();
    }
}
