package com.zifang.util.parser.proto;

import java.util.Objects;

/**
 * Proto RPC 方法模型。
 *
 * @author zifang
 */

/**
 * ProtoRpc类。
 */
public class ProtoRpc {

    private final String name;
    private final String inputType;
    private final String outputType;

    /**
     * ProtoRpc方法。
     * * @param name String类型参数
     *
     * @param inputType  String类型参数
     * @param outputType String类型参数
     */
    public ProtoRpc(String name, String inputType, String outputType) {
        this.name = name;
        this.inputType = inputType;
        this.outputType = outputType;
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
     * getInputType方法。
     *
     * @return String类型返回值
     */
    public String getInputType() {
        return inputType;
    }

    /**
     * getOutputType方法。
     *
     * @return String类型返回值
     */
    public String getOutputType() {
        return outputType;
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
        ProtoRpc protoRpc = (ProtoRpc) o;
        return Objects.equals(name, protoRpc.name) &&
                Objects.equals(inputType, protoRpc.inputType) &&
                Objects.equals(outputType, protoRpc.outputType);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(name, inputType, outputType);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "rpc " + name + "(" + inputType + ") returns (" + outputType + ");";
    }
}
