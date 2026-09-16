//package com.zifang.util.proxy.ct.generater;
//
//import com.github.javaparser.StaticJavaParser;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.ImportDeclaration;
//import com.github.javaparser.ast.NodeList;
//import com.github.javaparser.ast.PackageDeclaration;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.FieldDeclaration;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.body.Parameter;
//import com.github.javaparser.ast.expr.SimpleName;
//import com.github.javaparser.ast.type.ClassOrInterfaceType;
//import com.zifang.util.core.lang.beans.classinfo.ClassInfo;
//import com.zifang.util.core.lang.beans.classinfo.FieldInfo;
//import com.zifang.util.core.lang.beans.classinfo.MethodInfo;
//import com.zifang.util.core.lang.beans.classinfo.MethodParameterPair;
//import com.zifang.util.core.lang.object.component.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
/// **
// * 吃掉代码字符串流
// * <p>
// * 得到ClassInfo
// */
//public class JavaCodeParser {
//
//
//    private String code;
//
//    private ClassOrInterfaceDeclaration classOrInterfaceDeclaration;
//
//    private ClassInfo classInfo;
//
//    private CompilationUnit cu;
//
//    public JavaCodeParser(String code) {
//        this.code = code;
//        this.classInfo = new ClassInfo();
//    }
//
//    public void analysis() {
//
//        // 编译单元
//        cu = StaticJavaParser.parse(code);
//
//        // 类声明
//        classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) cu.getType(0);
//
//        // 分别进行处理
//        handlePackageName(cu.getPackageDeclaration());// 处理包
//        handleImports(cu.getImports());// 得到所有的Import
//        //handleIsInterface(classOrInterfaceDeclaration.isInterface());// 是否是接口
//        handleClassName(classOrInterfaceDeclaration.getName());// 类名
//        handleInterfaces(classOrInterfaceDeclaration.getImplementedTypes());
//        handleExtendedTypes(classOrInterfaceDeclaration.getExtendedTypes()); //父亲
//        handleMethods(classOrInterfaceDeclaration.getMethods());// 解析方法
//        handleFields(classOrInterfaceDeclaration.getFields());// 得到所有的field
//    }
//
//    /**
//     * @TODO
//     */
//    private void handleInterfaces(NodeList<ClassOrInterfaceType> implementedTypes) {
//        List<String> interfaces = new ArrayList<>();
//        for (ClassOrInterfaceType classOrInterfaceType : implementedTypes) {
//            interfaces.add(classOrInterfaceType.getNameAsString());
//        }
//    }
//
//    private void handleExtendedTypes(NodeList<ClassOrInterfaceType> extendedTypes) {
//
//    }
//
//    private void handleMethods(List<MethodDeclaration> methods) {
//
//        List<MethodInfo> methodInfos = new ArrayList<>();
//        for (MethodDeclaration methodDeclaration : methods) {
//            MethodInfo methodInfo = new MethodInfo();
//            methodInfo.setMethodName(methodDeclaration.getNameAsString());
//            methodInfo.setReturnType(methodDeclaration.getTypeAsString());
//            List<MethodParameterPair> parameterPairs = new ArrayList<>();
//            for (Parameter parameter : methodDeclaration.getParameters()) {
//                String paramType = parameter.getTypeAsString();
//                String paramName = parameter.getNameAsString();
//                MethodParameterPair methodParameterPair = new MethodParameterPair(paramType, paramName);
//                parameterPairs.add(methodParameterPair);
//            }
//            methodInfo.setMethodParameterPairs(parameterPairs);
//            methodInfos.add(methodInfo);
//        }
//        classInfo.setMethods(methodInfos);
//    }
//
//    private void handleFields(List<FieldDeclaration> fields) {
//        List<FieldInfo> fieldInfos = new ArrayList<>();
//        for (FieldDeclaration fieldDeclaration : fields) {
//            String variable = fieldDeclaration.getVariables().get(0).getNameAsString();
//            String type = fieldDeclaration.getVariables().get(0).getTypeAsString();
//            fieldInfos.add(new FieldInfo(type, variable));
//        }
//        classInfo.setFields(fieldInfos);
//    }
//
//    private void handleClassName(SimpleName name) {
//        classInfo.setSimpleClassName(name.getIdentifier());
//    }
//
/// /    private void handleIsInterface(boolean anInterface) {
/// /        classInfo.setIsInterface(anInterface);
/// /    }
//
//    private void handleImports(NodeList<ImportDeclaration> imports) {
//        List<String> importsToBeSet = new ArrayList<>();
//        for (ImportDeclaration importDeclaration : imports) {
//            importsToBeSet.add(importDeclaration.getNameAsString());
//        }
//        //classInfo.setImports(importsToBeSet);
//    }
//
//    private void handlePackageName(Optional<PackageDeclaration> packageDeclaration) {
//        classInfo.setPackageName(packageDeclaration.get().getName().asString());
//    }
//
//    public ClassInfo getClassInfo() {
//
//        return this.classInfo;
//    }
//}
