package com.zifang.util.source.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.zifang.util.source.generator.info.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 源代码解析器
 * <p>
 * 使用 JavaParser 将 Java 源代码解析为 ClassInfo 模型。
 * 支持从文件、InputStream 或字符串解析。
 * 支持：类、接口、枚举、注解、内部类
 *
 * @author zifang
 */
public class SourceCodeParser {

    private JavaParser javaParser;

    /**
     * SourceCodeParser方法。
     */
    public SourceCodeParser() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 从文件路径解析 Java 源码
     */
    public ClassInfo parse(String filePath) throws IOException {
        return parse(new File(filePath));
    }

    /**
     * 从 File 对象解析 Java 源码
     */
    public ClassInfo parse(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return parse(fis);
        }
    }

    /**
     * 从 InputStream 解析 Java 源码
     */
    public ClassInfo parse(InputStream is) {
        CompilationUnit cu = javaParser.parse(is).getResult().orElseThrow(
                () -> new RuntimeException("Failed to parse Java source")
        );
        return convertToClassInfo(cu);
    }

    /**
     * 从字符串解析 Java 源码
     */
    public ClassInfo parseSource(String sourceCode) {
        CompilationUnit cu = javaParser.parse(sourceCode).getResult().orElseThrow(
                () -> new RuntimeException("Failed to parse Java source")
        );
        return convertToClassInfo(cu);
    }

    /**
     * 将 CompilationUnit 转换为 ClassInfo
     */
    private ClassInfo convertToClassInfo(CompilationUnit cu) {
        ClassInfo classInfo = new ClassInfo();

        // 包名
        classInfo.setPackageName(
                cu.getPackageDeclaration().map(p -> p.getName().asString()).orElse("")
        );

        // 类型声明（只取第一个）
        List<TypeDeclaration<?>> types = cu.getTypes();
        if (types.isEmpty()) {
            throw new RuntimeException("No class, interface, enum or annotation declaration found");
        }

        TypeDeclaration<?> type = types.get(0);
        convertTypeDeclaration(type, classInfo);

        // 导入语句
        List<String> imports = new ArrayList<>();
        cu.getImports().forEach(imp -> imports.add(imp.getName().asString()));
        classInfo.setImports(imports);

        return classInfo;
    }

    /**
     * 转换类型声明（支持 Class、Interface、Enum、Annotation）
     */
    private void convertTypeDeclaration(TypeDeclaration<?> type, ClassInfo classInfo) {
        classInfo.setSimpleClassName(type.getName().asString());
        classInfo.setModifiers(parseModifiers(type));

        // 解析类的注解
        List<AnnotationInfo> annotations = new ArrayList<>();
        for (AnnotationExpr annotation : type.getAnnotations()) {
            annotations.add(convertAnnotation(annotation));
        }
        classInfo.setAnnotations(annotations);

        if (type instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration classDecl = (ClassOrInterfaceDeclaration) type;
            classInfo.setInterfaceType(classDecl.isInterface());

            // 父类
            List<ClassOrInterfaceType> extendedTypes = classDecl.getExtendedTypes();
            if (!extendedTypes.isEmpty()) {
                ClassInfo superClassInfo = new ClassInfo();
                superClassInfo.setSimpleClassName(extendedTypes.get(0).getName().asString());
                superClassInfo.setPackageName("");
                classInfo.setSuperClass(superClassInfo);
            }

            // 接口列表
            List<ClassInfo> interfaces = new ArrayList<>();
            for (ClassOrInterfaceType iface : classDecl.getImplementedTypes()) {
                ClassInfo ifaceInfo = new ClassInfo();
                ifaceInfo.setSimpleClassName(iface.getName().asString());
                ifaceInfo.setPackageName("");
                interfaces.add(ifaceInfo);
            }
            classInfo.setInterfaces(interfaces);

        } else if (type instanceof EnumDeclaration) {
            EnumDeclaration enumDecl = (EnumDeclaration) type;
            classInfo.setInterfaceType(false);
            // 枚举隐式继承 java.lang.Enum

            // 实现的接口
            List<ClassInfo> interfaces = new ArrayList<>();
            for (ClassOrInterfaceType iface : enumDecl.getImplementedTypes()) {
                ClassInfo ifaceInfo = new ClassInfo();
                ifaceInfo.setSimpleClassName(iface.getName().asString());
                ifaceInfo.setPackageName("");
                interfaces.add(ifaceInfo);
            }
            classInfo.setInterfaces(interfaces);

        } else if (type instanceof AnnotationDeclaration) {
            classInfo.setInterfaceType(false);
            // 注解类型隐式继承 java.lang.annotation.Annotation
        }

        // 字段列表
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (FieldDeclaration field : type.getFields()) {
            fieldInfos.add(convertField(field));
        }

        // 枚举常量作为特殊字段添加
        if (type instanceof EnumDeclaration) {
            EnumDeclaration enumDecl = (EnumDeclaration) type;
            for (EnumConstantDeclaration constant : enumDecl.getEntries()) {
                FieldInfo constField = new FieldInfo();
                constField.setType(classInfo.getSimpleClassName());
                constField.setValue(constant.getName().asString());
                constField.setModifiers(new int[]{java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC | java.lang.reflect.Modifier.FINAL});
                constField.setInitializer("null");
                fieldInfos.add(constField);
            }
        }

        classInfo.setFields(fieldInfos);

        // 方法列表
        List<MethodInfo> methodInfos = new ArrayList<>();
        for (MethodDeclaration method : type.getMethods()) {
            methodInfos.add(convertMethod(method));
        }

        // 注解成员（AnnotationMemberDeclaration）也作为方法处理
        if (type instanceof AnnotationDeclaration) {
            AnnotationDeclaration annot = (AnnotationDeclaration) type;
            for (BodyDeclaration<?> member : annot.getMembers()) {
                if (member instanceof AnnotationMemberDeclaration) {
                    methodInfos.add(convertAnnotationMember((AnnotationMemberDeclaration) member));
                }
            }
        }

        classInfo.setMethods(methodInfos);

        // 内部类列表
        List<ClassInfo> innerClasses = new ArrayList<>();
        for (BodyDeclaration<?> member : type.getMembers()) {
            if (member instanceof TypeDeclaration) {
                ClassInfo innerClassInfo = new ClassInfo();
                convertTypeDeclaration((TypeDeclaration<?>) member, innerClassInfo);
                innerClasses.add(innerClassInfo);
            }
        }
        classInfo.setInnerClasses(innerClasses);
    }

    /**
     * 转换字段
     */
    private FieldInfo convertField(FieldDeclaration field) {
        FieldInfo fieldInfo = new FieldInfo();
        fieldInfo.setType(field.getVariables().get(0).getType().asString());
        fieldInfo.setValue(field.getVariables().get(0).getName().asString());
        fieldInfo.setModifiers(new int[]{parseModifiers(field)});

        // 初始值
        if (field.getVariables().get(0).getInitializer().isPresent()) {
            fieldInfo.setInitializer(field.getVariables().get(0).getInitializer().get().toString());
        } else {
            fieldInfo.setInitializer("null");
        }

        // 字段注解
        List<AnnotationInfo> annotations = new ArrayList<>();
        for (AnnotationExpr annotation : field.getAnnotations()) {
            annotations.add(convertAnnotation(annotation));
        }
        fieldInfo.setAnnotations(annotations);

        return fieldInfo;
    }

    /**
     * 转换方法
     */
    private MethodInfo convertMethod(MethodDeclaration method) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName(method.getName().asString());
        methodInfo.setModifier(parseModifiers(method));
        methodInfo.setReturnType(method.getType().asString());

        // 参数
        List<MethodParameterPair> params = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            MethodParameterPair pair = new MethodParameterPair();
            pair.setParamType(param.getType().asString());
            pair.setParamName(param.getName().asString());
            params.add(pair);
        }
        methodInfo.setMethodParameterPairs(params);

        // 方法体语句
        List<String> statements = new ArrayList<>();
        method.getBody().ifPresent(body -> {
            body.getStatements().forEach(stmt -> statements.add(stmt.toString()));
        });
        methodInfo.setStatements(statements);

        // 方法注解
        List<AnnotationInfo> annotations = new ArrayList<>();
        for (AnnotationExpr annotation : method.getAnnotations()) {
            annotations.add(convertAnnotation(annotation));
        }
        methodInfo.setAnnotations(annotations);

        return methodInfo;
    }

    /**
     * 转换注解成员声明
     */
    private MethodInfo convertAnnotationMember(AnnotationMemberDeclaration member) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName(member.getName().asString());
        methodInfo.setModifier(java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT);
        methodInfo.setReturnType(member.getType().asString());
        methodInfo.setMethodParameterPairs(new ArrayList<>());

        // 默认值作为方法体的一部分
        List<String> statements = new ArrayList<>();
        member.getDefaultValue().ifPresent(v -> statements.add("default " + v.toString()));
        methodInfo.setStatements(statements);

        return methodInfo;
    }

    /**
     * 转换注解
     */
    private AnnotationInfo convertAnnotation(AnnotationExpr annotation) {
        AnnotationInfo info = new AnnotationInfo();
        info.setType(annotation.getName().asString());

        if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normal = (NormalAnnotationExpr) annotation;
            for (MemberValuePair pair : normal.getPairs()) {
                info.addMember(pair.getName().asString(), pair.getValue().toString());
            }
        } else if (annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr single = (SingleMemberAnnotationExpr) annotation;
            info.addMember("", single.getMemberValue().toString());
        }

        return info;
    }

    /**
     * 解析修饰符
     */
    private int parseModifiers(TypeDeclaration<?> type) {
        int modifiers = 0;
        for (Modifier mod : type.getModifiers()) {
            modifiers |= modifierKeywordToInt(mod.getKeyword().name());
        }
        return modifiers;
    }

    /**
     * 解析字段修饰符
     */
    private int parseModifiers(FieldDeclaration field) {
        int modifiers = 0;
        for (Modifier mod : field.getModifiers()) {
            modifiers |= modifierKeywordToInt(mod.getKeyword().name());
        }
        return modifiers;
    }

    /**
     * 解析方法修饰符
     */
    private int parseModifiers(MethodDeclaration method) {
        int modifiers = 0;
        for (Modifier mod : method.getModifiers()) {
            modifiers |= modifierKeywordToInt(mod.getKeyword().name());
        }
        return modifiers;
    }

    /**
     * 将 javaparser Modifier.Keyword 名称转换为 java.lang.reflect.Modifier 常量
     */
    private int modifierKeywordToInt(String keywordName) {
        switch (keywordName) {
            case "PUBLIC":
                return java.lang.reflect.Modifier.PUBLIC;
            case "PRIVATE":
                return java.lang.reflect.Modifier.PRIVATE;
            case "PROTECTED":
                return java.lang.reflect.Modifier.PROTECTED;
            case "STATIC":
                return java.lang.reflect.Modifier.STATIC;
            case "FINAL":
                return java.lang.reflect.Modifier.FINAL;
            case "ABSTRACT":
                return java.lang.reflect.Modifier.ABSTRACT;
            case "SYNCHRONIZED":
                return java.lang.reflect.Modifier.SYNCHRONIZED;
            case "VOLATILE":
                return java.lang.reflect.Modifier.VOLATILE;
            case "TRANSIENT":
                return java.lang.reflect.Modifier.TRANSIENT;
            case "NATIVE":
                return java.lang.reflect.Modifier.NATIVE;
            case "STRICTFP":
                return java.lang.reflect.Modifier.STRICT;
            default:
                return 0;
        }
    }
}
