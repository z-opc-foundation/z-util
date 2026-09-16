package com.zifang.util.core.lang.dynamic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zifang
 */
public class DynamicMethod {

    private String methodName;

    private String returnType;

    private String desc;

    private String body;

    private List<?> parameters = new ArrayList<>();

    /**
     * of方法。
     * * @param methodName String类型参数
     *
     * @param parameters List?类型参数
     * @param returnType String类型参数
     * @param body       String类型参数
     * @return static DynamicMethod类型返回值
     */
    public static DynamicMethod of(String methodName, List<?> parameters, String returnType, String body) {
        DynamicMethod dynamicMethod = new DynamicMethod();
        dynamicMethod.setMethodName(methodName);
        dynamicMethod.setParameters(parameters);
        dynamicMethod.setReturnType(returnType);
        dynamicMethod.setBody(body);
        return dynamicMethod;
    }

    /**
     * getMethodName方法。
     *
     * @return String类型返回值
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * setMethodName方法。
     * * @param methodName String类型参数
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * getReturnType方法。
     *
     * @return String类型返回值
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * setReturnType方法。
     * * @param returnType String类型参数
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
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
     * getBody方法。
     *
     * @return String类型返回值
     */
    public String getBody() {
        return body;
    }

    /**
     * setBody方法。
     * * @param body String类型参数
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * getParameters方法。
     *
     * @return List<?>类型返回值
     */
    public List<?> getParameters() {
        return parameters;
    }

    /**
     * setParameters方法。
     * * @param parameters List?类型参数
     */
    public void setParameters(List<?> parameters) {
        this.parameters = parameters;
    }
}
