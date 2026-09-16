package com.zifang.util.core.lang.dynamic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zifang
 */
public class DynamicClass {

    private List<String> imports = new ArrayList<>();

    private String packageName;

    private List<?> annotations = new ArrayList<>();

    private Boolean isInterface;

    private String className;

    /**
     * 实现的接口类
     */
    private List<DynamicClass> implementClasses = new ArrayList<>();

    /**
     * 继承的bean
     */
    private DynamicClass extendClass;

    /**
     * 动态bean字段
     */
    private List<DynamicField> fields = new ArrayList<>();

    /**
     * 动态方法
     */
    private List<DynamicMethod> methods = new ArrayList<>();


    /**
     * getImplementClasses方法。
     *
     * @return List<DynamicClass>类型返回值
     */
    public List<DynamicClass> getImplementClasses() {
        return implementClasses;
    }

    /**
     * setImplementClasses方法。
     * * @param implementClasses ListDynamicClass类型参数
     */
    public void setImplementClasses(List<DynamicClass> implementClasses) {
        this.implementClasses = implementClasses;
    }

    /**
     * getExtendClass方法。
     *
     * @return DynamicClass类型返回值
     */
    public DynamicClass getExtendClass() {
        return extendClass;
    }

    /**
     * setExtendClass方法。
     * * @param extendClass DynamicClass类型参数
     */
    public void setExtendClass(DynamicClass extendClass) {
        this.extendClass = extendClass;
    }

    /**
     * getFields方法。
     *
     * @return List<DynamicField>类型返回值
     */
    public List<DynamicField> getFields() {
        return fields;
    }

    /**
     * setFields方法。
     * * @param fields ListDynamicField类型参数
     */
    public void setFields(List<DynamicField> fields) {
        this.fields = fields;
    }

    /**
     * getMethods方法。
     *
     * @return List<DynamicMethod>类型返回值
     */
    public List<DynamicMethod> getMethods() {
        return methods;
    }

    /**
     * setMethods方法。
     * * @param methods ListDynamicMethod类型参数
     */
    public void setMethods(List<DynamicMethod> methods) {
        this.methods = methods;
    }

    /**
     * getPackageName方法。
     *
     * @return String类型返回值
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * setPackageName方法。
     * * @param packageName String类型参数
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
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
     * getInterface方法。
     *
     * @return boolean类型返回值
     */
    public Boolean getInterface() {
        return isInterface;
    }

    /**
     * setInterface方法。
     * * @param anInterface boolean类型参数
     */
    public void setInterface(Boolean anInterface) {
        isInterface = anInterface;
    }

    /**
     * getImports方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getImports() {
        return imports;
    }

    /**
     * setImports方法。
     * * @param imports ListString类型参数
     */
    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    /**
     * getAnnotations方法。
     *
     * @return List<?>类型返回值
     */
    public List<?> getAnnotations() {
        return annotations;
    }

    /**
     * setAnnotations方法。
     * * @param annotations List?类型参数
     */
    public void setAnnotations(List<?> annotations) {
        this.annotations = annotations;
    }

    /**
     * generateSourceCode方法。
     *
     * @return String类型返回值
     */
    public String generateSourceCode() {

        if (className == null) {
            className = "Bean_" + System.currentTimeMillis();
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("public class " + className + "{\n");

        for (DynamicField dynamicField : fields) {
            String te = "\tprivate %s %s;\n";
            stringBuffer.append(String.format(te, dynamicField.getType(), dynamicField.getName()));
        }

        for (DynamicMethod dynamicMethod : methods) {
            String te = "\tpublic %s %s(%s){\n\t\t%s\n\t}\n";
            String code = String.format(te,
                    dynamicMethod.getReturnType(),
                    dynamicMethod.getMethodName(),
                    String.join(",", dynamicMethod.getParameters().stream().map(Object::toString).toArray(String[]::new)),
                    dynamicMethod.getBody());

            stringBuffer.append(code);
        }

        stringBuffer.append("}");

        return stringBuffer.toString();
    }

    /**
     * getType方法。
     *
     * @return String类型返回值
     */
    public String getType() {
        if (packageName == null) {
            return className;
        } else {
            return packageName + "." + className;
        }
    }

    /**
     * addField方法。
     * * @param key String类型参数
     *
     * @param clazz Class?类型参数
     */
    public void addField(String key, Class<?> clazz) {
        addField(key, clazz, false, false);
    }

    /**
     * addField方法。
     * * @param key String类型参数
     *
     * @param clazz          Class?类型参数
     * @param generateGetter boolean类型参数
     * @param generateSetter boolean类型参数
     */
    public void addField(String key, Class<?> clazz, boolean generateGetter, boolean generateSetter) {
        addField(key, clazz.getName(), generateGetter, generateSetter);
    }

    /**
     * addField方法。
     * * @param key String类型参数
     *
     * @param type           String类型参数
     * @param generateGetter boolean类型参数
     * @param generateSetter boolean类型参数
     */
    public void addField(String key, String type, boolean generateGetter, boolean generateSetter) {

        fields.add(DynamicField.of(key, type));

        if (generateGetter) {
            String methodName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
            methods.add(DynamicMethod.of(
                    methodName,
                    new ArrayList<>(),
                    type, "return this." + key + ";"));
        }
    }
}
