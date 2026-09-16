//package com.zifang.util.proxy.ct.generater;
//
//import com.github.javaparser.JavaParser;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.NodeList;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.type.ClassOrInterfaceType;
//import com.zifang.util.core.lang.beans.classinfo.ClassInfo;
//import com.zifang.util.core.lang.beans.classinfo.FieldInfo;
//import com.zifang.util.core.lang.beans.classinfo.MethodInfo;
//import com.zifang.util.core.lang.object.component.*;
//
//import java.util.Optional;
//
//import static com.github.javaparser.ast.Modifier.Keyword.PRIVATE;
//
/// **
// * 正常类的 源码生产者
// */
//public class JavaObjectSourceGenerator implements IGenerator {
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
//
//    public JavaObjectSourceGenerator(ClassInfo classInfo) {
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
//
//        // 初始化编译单元
//        compilationUnit = new CompilationUnit();
//
//        // 设置包名
//        compilationUnit.setPackageDeclaration(classInfo.getPackageName());
//        // 往编译单元 添加类 或者 接口
//
//        classUnit = compilationUnit
//                .addClass(classInfo.getSimpleClassName())
//                .setPublic(classInfo.isPublic() == null ? true : classInfo.isPublic());
//
//        // 处理 使用implements 实现接口
//        NodeList<ClassOrInterfaceType> implementedTypes = new NodeList<>();
//
//        if (classInfo.getInterfaces() != null) {
//            for (ClassInfo subInterface : classInfo.getInterfaces()) {
//                // 根据持有接口信息进行解析
//                Optional<ClassOrInterfaceType> classOrInterfaceType = javaParser
//                        .parseClassOrInterfaceType(subInterface.getSimpleClassName()).getResult();
//                // 添加到既有类上 使用implement方式
//                classOrInterfaceType.ifPresent(implementedTypes::add);
//            }
//            classUnit.setImplementedTypes(implementedTypes);
//        }
//
//        if (classInfo.getMethods() != null) {
//            // 处理method
//            for (MethodInfo methodInfo : classInfo.getMethods()) {
//                CodeGenerateHelper.addMethod(classUnit, methodInfo);
//            }
//        }
//
//        if (classInfo.getFields() != null) {
//            for (FieldInfo fieldInfo : classInfo.getFields()) {
//                classUnit.addField(fieldInfo.getType(), fieldInfo.getValue(), PRIVATE);
//            }
//        }
//
//        // 处理所有的import 这个方法将会永远存在在最后
//        // handleImport();
//    }
//
/// /    private void handleImport() {
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
//
//
//
