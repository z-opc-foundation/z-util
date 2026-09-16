package com.zifang.util.source.generator.info;

/**
 * 方法参数信息封装类
 * <p>
 * 用于封装Java方法的参数（形参）的类型和名称信息。
 * 是MethodInfo中参数列表的基本组成单元。
 *
 * @author zifang
 * @version 1.0.0
 */
public class MethodParameterPair {

    /**
     * 参数类型全限定名
     */
    private String paramType;

    /**
     * 参数名称
     */
    private String paramName;


    /**
     * getParamType方法。
     *
     * @return String类型返回值
     */
    public String getParamType() {
        return paramType;
    }

    /**
     * setParamType方法。
     * * @param paramType String类型参数
     */
    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    /**
     * getParamName方法。
     *
     * @return String类型返回值
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * setParamName方法。
     * * @param paramName String类型参数
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return paramType + " " + paramName;
    }
}
