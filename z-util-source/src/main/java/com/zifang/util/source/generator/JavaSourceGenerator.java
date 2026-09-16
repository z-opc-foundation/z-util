package com.zifang.util.source.generator;

import com.zifang.util.core.lang.StringUtil;
import com.zifang.util.source.generator.info.*;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Java 源代码生成器
 * <p>
 * 根据 ClassInfo 模型生成可读的 Java 源代码。
 * 支持生成完整的类定义，包括包声明、导入语句、类注释、方法实现等。
 * 支持：类、接口、枚举、注解、内部类
 *
 * @author zifang
 */
public class JavaSourceGenerator {

    /**
     * 从 ClassInfo 生成完整的 Java 源码
     */
    public String generate(ClassInfo classInfo) {
        StringBuilder sb = new StringBuilder();

        // 包声明
        if (classInfo.getPackageName() != null && !classInfo.getPackageName().isEmpty()) {
            sb.append("package ").append(classInfo.getPackageName()).append(";\n\n");
        }

        sb.append(generateClass(classInfo, 0));
        return sb.toString();
    }

    /**
     * 生成类源码（带缩进级别）
     */
    private String generateClass(ClassInfo classInfo, int indentLevel) {
        StringBuilder sb = new StringBuilder();
        String indent = StringUtil.repeat("    ", indentLevel);

        // 类注解
        if (classInfo.getAnnotations() != null) {
            for (AnnotationInfo annotation : classInfo.getAnnotations()) {
                sb.append(indent).append(annotation.toString()).append("\n");
            }
        }

        // 类修饰符 + 类名
        sb.append(indent).append(modifiersToString(classInfo.getModifiers(), classInfo.getInterfaceType()));

        // 判断类型
        Boolean isInterface = classInfo.getInterfaceType();
        if (isInterface != null && isInterface) {
            sb.append("interface ").append(classInfo.getSimpleClassName());
        } else {
            sb.append("class ").append(classInfo.getSimpleClassName());
        }

        // 父类
        if (classInfo.getSuperClass() != null) {
            String superName = getSimpleName(classInfo.getSuperClass().getName());
            sb.append(" extends ").append(superName);
        }

        // 接口
        List<ClassInfo> interfaces = classInfo.getInterfaces();
        if (interfaces != null && !interfaces.isEmpty()) {
            String ifaceNames = interfaces.stream()
                    .map(i -> getSimpleName(i.getName()))
                    .collect(Collectors.joining(", "));
            sb.append(" implements ").append(ifaceNames);
        }

        sb.append(" {\n");

        // 字段
        List<FieldInfo> fields = classInfo.getFields();
        if (fields != null) {
            for (FieldInfo field : fields) {
                sb.append(indent).append("    ");
                // 字段注解
                if (field.getAnnotations() != null) {
                    for (AnnotationInfo annotation : field.getAnnotations()) {
                        sb.append(annotation.toString()).append(" ");
                    }
                }
                sb.append(modifiersToString(field.getModifiers(), false));
                sb.append(field.getType()).append(" ");
                sb.append(field.getValue());
                if (!"null".equals(field.getInitializer())) {
                    sb.append(" = ").append(field.getInitializer());
                }
                sb.append(";\n");
            }
            if (!fields.isEmpty()) {
                sb.append("\n");
            }
        }

        // 方法
        List<MethodInfo> methods = classInfo.getMethods();
        if (methods != null) {
            for (MethodInfo method : methods) {
                sb.append(indent).append("    ");
                // 方法注解
                if (method.getAnnotations() != null) {
                    for (AnnotationInfo annotation : method.getAnnotations()) {
                        sb.append(annotation.toString()).append(" ");
                    }
                }
                sb.append(modifiersToString(method.getModifier(), false));
                sb.append(method.getReturnType()).append(" ");
                sb.append(method.getMethodName()).append("(");

                // 参数
                List<MethodParameterPair> params = method.getMethodParameterPairs();
                if (params != null && !params.isEmpty()) {
                    for (int i = 0; i < params.size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(params.get(i).toString());
                    }
                }

                sb.append(")");

                // 方法体
                List<String> statements = method.getStatements();
                if (statements != null && !statements.isEmpty()) {
                    sb.append(" {\n");
                    for (String stmt : statements) {
                        sb.append(indent).append("        ").append(stmt).append("\n");
                    }
                    sb.append(indent).append("    }\n\n");
                } else {
                    sb.append(";\n\n");
                }
            }
        }

        // 内部类
        List<ClassInfo> innerClasses = classInfo.getInnerClasses();
        if (innerClasses != null) {
            for (ClassInfo innerClass : innerClasses) {
                sb.append(generateClass(innerClass, indentLevel + 1));
                sb.append("\n");
            }
        }

        sb.append(indent).append("}\n");
        return sb.toString();
    }

    /**
     * 将修饰符 int 转换为字符串
     */
    private String modifiersToString(int modifiers, boolean isInterface) {
        return modifiersToString(new int[]{modifiers}, isInterface);
    }

    /**
     * 将修饰符 int[] 转换为字符串
     */
    private String modifiersToString(int[] modifiers, boolean isInterface) {
        int mod = 0;
        if (modifiers != null) {
            for (int m : modifiers) {
                mod |= m;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (Modifier.isPublic(mod)) sb.append("public ");
        if (Modifier.isPrivate(mod)) sb.append("private ");
        if (Modifier.isProtected(mod)) sb.append("protected ");
        if (Modifier.isStatic(mod)) sb.append("static ");
        if (Modifier.isFinal(mod)) sb.append("final ");
        if (Modifier.isAbstract(mod)) sb.append("abstract ");
        if (Modifier.isSynchronized(mod)) sb.append("synchronized ");
        if (Modifier.isVolatile(mod)) sb.append("volatile ");
        if (Modifier.isTransient(mod)) sb.append("transient ");
        if (Modifier.isStrict(mod)) sb.append("strictfp ");
        if (isInterface && !Modifier.isAbstract(mod)) sb.append("interface ");
        return sb.toString();
    }

    /**
     * 获取简单类名（去掉包路径）
     */
    private String getSimpleName(String fullName) {
        if (fullName == null) return "";
        int lastDot = fullName.lastIndexOf('.');
        return lastDot >= 0 ? fullName.substring(lastDot + 1) : fullName;
    }
}
