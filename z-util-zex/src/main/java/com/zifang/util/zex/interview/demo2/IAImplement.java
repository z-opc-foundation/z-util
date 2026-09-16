package com.zifang.util.zex.interview.demo2;

/**
 * IA和IB接口的实现类。
 * <p>
 * 此类同时实现了IA和IB两个接口，并提供控制字符串的解析功能。
 * 控制字符串格式：className$methodName=returnValue
 *
 * @author zifang
 * @version 1.0
 */
public class IAImplement implements IA, IB {

    private String className;
    private String method;
    private String returns;

    private String controlStr;

    /**
     * IAImplement方法。
     * * @param controlStr String类型参数
     */
    public IAImplement(String controlStr) {
        this.controlStr = controlStr;
        className = controlStr.split("[$]")[0];
        method = controlStr.split("[$]")[1].split("=")[0];
        returns = controlStr.split("[$]")[1].split("=")[1];
    }

    /**
     * getClassName方法。
     *
     * @return String类型返回值
     */
    public String getClassName() {
        return className;
    }

    /**
     * setClassName方法。
     * * @param className String类型参数
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * getMethod方法。
     *
     * @return String类型返回值
     */
    public String getMethod() {
        return method;
    }

    /**
     * setMethod方法。
     * * @param method String类型参数
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * getReturns方法。
     *
     * @return String类型返回值
     */
    public String getReturns() {
        return returns;
    }

    /**
     * setReturns方法。
     * * @param returns String类型参数
     */
    public void setReturns(String returns) {
        this.returns = returns;
    }

    /**
     * getControlStr方法。
     *
     * @return String类型返回值
     */
    public String getControlStr() {
        return controlStr;
    }

    /**
     * setControlStr方法。
     * * @param controlStr String类型参数
     */
    public void setControlStr(String controlStr) {
        this.controlStr = controlStr;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "IAImplement{className=" + className + ", method=" + method + ", returns=" + returns + ", controlStr=" + controlStr + "}";
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
        IAImplement that = (IAImplement) o;
        return java.util.Objects.equals(className, that.className) && java.util.Objects.equals(method, that.method) && java.util.Objects.equals(returns, that.returns) && java.util.Objects.equals(controlStr, that.controlStr);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(className, method, returns, controlStr);
    }

    @Override
    /**
     * getName方法。
     * @return String类型返回值
     */
    public String getName() {
        return returns;
    }

    @Override
    /**
     * getYear方法。
     * @return String类型返回值
     */
    public String getYear() {
        return null;
    }

    @Override
    /**
     * getIB方法。
     *      * @param b String类型参数
     * @return IB类型返回值
     */
    public IB getIB(String b) {
        return null;
    }

    @Override
    /**
     * getIBName方法。
     * @return String类型返回值
     */
    public String getIBName() {
        return null;
    }
}
