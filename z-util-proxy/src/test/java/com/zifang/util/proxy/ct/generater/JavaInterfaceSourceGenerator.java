//package com.zifang.util.proxy.ct.generater;
//
//import com.github.javaparser.JavaParser;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.type.ClassOrInterfaceType;
//import com.zifang.util.core.lang.beans.classinfo.ClassInfo;
//import com.zifang.util.core.lang.beans.classinfo.FieldInfo;
//import com.zifang.util.core.lang.beans.classinfo.MethodInfo;
//import com.zifang.util.core.lang.beans.classinfo.MethodParameterPair;
//import com.zifang.util.core.lang.object.component.*;
//
//import java.util.Optional;
//
//import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
//
/// **
// * 生产接口代码的 生产者
// */
//public class JavaInterfaceSourceGenerator implements IGenerator {
//
//    // 针对一个类的信息载体
//    private ClassInfo classInfo;
//
//    private JavaParser javaParser = new JavaParser();
//
//    // 编译单元
//    private CompilationUnit compilationUnit;
//
//    // 编译单元下的主类 单元
//    private ClassOrInterfaceDeclaration classUnit;
//
//    public JavaInterfaceSourceGenerator(ClassInfo classInfo) {
//        this.classInfo = classInfo;
//    }
//
//    @Override
//    public String generateCode() {
//        this.generate();
//        return compilationUnit.toString();
//    }
//
//    private void generate() {
//        // 初始化编译单元
//        compilationUnit = new CompilationUnit();
//
//        // 设置包名
//        compilationUnit.setPackageDeclaration(classInfo.getPackageName());
//
//        // 往编译单元 添加接口类
//        classUnit = compilationUnit
//                .addInterface(classInfo.getSimpleClassName())
//                .setPublic(classInfo.isPublic());
//
//        if (classInfo.getInterfaces() != null) {
//            // 使接口继承其他接口
//            for (ClassInfo subInterface : classInfo.getInterfaces()) {
//                // 根据持有接口信息进行解析
//                Optional<ClassOrInterfaceType> classOrInterfaceType = javaParser
//                        .parseClassOrInterfaceType(subInterface.getSimpleClassName()).getResult();
//                // 添加到既有类上 使用extends方式
//                classOrInterfaceType.ifPresent(orInterfaceType -> classUnit.addExtendedType(orInterfaceType));
//            }
//        }
//
//        if (classInfo.getMethods() != null) {
//            // 处理方法相关
//            for (MethodInfo methodInfo : classInfo.getMethods()) {
//                MethodDeclaration methodDeclaration = classUnit
//                        .addMethod(methodInfo.getMethodName(), PUBLIC);
//                methodDeclaration.setType(methodInfo.getReturnType());//返回类型
//                // 设置方法入参对
//                for (MethodParameterPair methodParameterPair : methodInfo.getMethodParameterPairs()) {
//                    methodDeclaration.addAndGetParameter(methodParameterPair.getParamType(), methodParameterPair.getParamName())
//                            .setVarArgs(false);// setVarArgs(true)表达是否多参
//                }
//                // 接口 实现方法体为空
//                methodDeclaration.setBody(null);
//            }
//        }
//
//        if (classInfo.getFields() != null) {
//            for (FieldInfo fieldInfo : classInfo.getFields()) {
//                CodeGenerateHelper.addField(classUnit, fieldInfo);
//            }
//        }
//
//        // 处理所有的import 这个方法将会永远存在在最后
//        // handleImport();
//    }
//
/// /    private void handleImport() {
/// /
/// /        // 接口进来之后会增加Import
/// /        for(ClassInfo interfaceInfo : classInfo.getInterfaces()){
/// /            String importedClassName = interfaceInfo.getPackageName()+"."+interfaceInfo.getSimpleClassName();
/// /            this.classInfo.getImports().add(importedClassName);
/// /        }
/// /
/// /        for(String im : classInfo.getImports()){
/// /            compilationUnit.addImport(im);
/// /        }
/// /    }
//}
