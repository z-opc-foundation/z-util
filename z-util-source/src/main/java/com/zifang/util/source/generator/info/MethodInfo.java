package com.zifang.util.source.generator.info;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法信息封装类
 * <p>
 * 用于封装Java方法的完整元数据信息，
 * 包括方法修饰符、返回类型、方法名、参数列表、方法体语句等。
 * 支持方法的签名生成和相等性比较。
 *
 * @author zifang
 * @version 1.0.0
 */
public class MethodInfo {

    /**
     * 方法的可见性修饰符，使用 java.lang.reflect.Modifier 的常量值
     */
    private Integer modifier;

    /**
     * 返回参数类型全限定名
     */
    private String returnType;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 形参列表
     */
    private List<MethodParameterPair> methodParameterPairs = new ArrayList<>();

    /**
     * 方法体的语句列表
     */
    private List<String> statements;

    /**
     * 注解列表
     */
    private List<AnnotationInfo> annotations = new ArrayList<>();


    @Override
    /**
     * equals方法。
     *      * @param obj Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object obj) {
        if (obj instanceof MethodInfo) {
            MethodInfo tobeCompare = (MethodInfo) obj;
            if (returnType.equals(tobeCompare.getReturnType()) && methodName.equals(tobeCompare.getMethodName())) {
                if (methodParameterPairs.toString().equals(tobeCompare.getMethodParameterPairs().toString())) {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return false;
    }

    /**
     * 获取方法签名（不考虑修饰符）
     * <p>
     * 例如：String name(String cc, Double dd);
     *
     * @return 方法签名字符串
     */
    public String signature() {
        return returnType + " " + methodName + "(" + getParameterStr() + ")" + ";";
    }

    /**
     * 获取方法的完整签名（包含修饰符）
     * <p>
     * 例如：public String name(String cc, Double dd);
     *
     * @return 方法完整签名字符串
     */
    public String fullSignature() {
        return returnType + " " + methodName + "(" + getParameterStr() + ")" + ";";
    }

    private String getParameterStr() {
        List<String> sub = new ArrayList<>();
        if (methodParameterPairs == null) {
            return "";
        } else {
            for (MethodParameterPair methodParameterPair : methodParameterPairs) {
                sub.add(methodParameterPair.toString());
            }
            return String.join(",", sub);
        }
    }

    /**
     * 获取方法修饰符
     *
     * @return 方法修饰符
     */
    public Integer getModifier() {
        return modifier;
    }

    /**
     * 设置方法修饰符
     *
     * @param modifier 方法修饰符
     */
    public void setModifier(Integer modifier) {
        this.modifier = modifier;
    }

    /**
     * 获取返回类型
     *
     * @return 返回类型全限定名
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * 设置返回类型
     *
     * @param returnType 返回类型全限定名
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /**
     * 获取方法名称
     *
     * @return 方法名称
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 设置方法名称
     *
     * @param methodName 方法名称
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * 获取参数列表
     *
     * @return 参数列表
     */
    public List<MethodParameterPair> getMethodParameterPairs() {
        return methodParameterPairs;
    }

    /**
     * 设置参数列表
     *
     * @param methodParameterPairs 参数列表
     */
    public void setMethodParameterPairs(List<MethodParameterPair> methodParameterPairs) {
        this.methodParameterPairs = methodParameterPairs;
    }

    /**
     * 获取方法体语句列表
     *
     * @return 方法体语句列表
     */
    public List<String> getStatements() {
        return statements;
    }

    /**
     * 设置方法体语句列表
     *
     * @param statements 方法体语句列表
     */
    public void setStatements(List<String> statements) {
        this.statements = statements;
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
}
