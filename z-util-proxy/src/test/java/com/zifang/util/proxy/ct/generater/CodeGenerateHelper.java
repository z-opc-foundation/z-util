//package com.zifang.util.proxy.ct.generater;
//
//import com.github.javaparser.StaticJavaParser;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.FieldDeclaration;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.body.VariableDeclarator;
//import com.github.javaparser.ast.expr.NameExpr;
//import com.github.javaparser.ast.stmt.BlockStmt;
//import com.github.javaparser.ast.stmt.ReturnStmt;
//import com.zifang.util.core.lang.beans.classinfo.FieldInfo;
//import com.zifang.util.core.lang.beans.classinfo.MethodInfo;
//import com.zifang.util.core.lang.beans.classinfo.MethodParameterPair;
//
//import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;
//
/// **
// * 针对一个编译单元 增加类元素的 帮助类
// */
//public class CodeGenerateHelper {
//
//    public static void addMethod(ClassOrInterfaceDeclaration classDeclaration, MethodInfo methodInfo) {
//
//        boolean isInterface = classDeclaration.isInterface();
//
//        if (isInterface) {
//            // 给类增加一个方法
//            MethodDeclaration methodDeclaration = classDeclaration.addMethod(methodInfo.getMethodName(), PUBLIC);
//            methodDeclaration.setType(methodInfo.getReturnType());
//            // 设置方法入参对
//            for (MethodParameterPair methodParameterPair : methodInfo.getMethodParameterPairs()) {
//                methodDeclaration.addAndGetParameter(methodParameterPair.getParamType(), methodParameterPair.getParamName())
//                        .setVarArgs(false);// setVarArgs(true)表达是否多参
//            }
//            methodDeclaration.setBody(null);
//        } else {
//            // 给类增加一个方法
//            MethodDeclaration methodDeclaration = classDeclaration.addMethod(methodInfo.getMethodName(), PUBLIC);
//            methodDeclaration.setType(methodInfo.getReturnType());
//            // 设置方法入参对
//            for (MethodParameterPair methodParameterPair : methodInfo.getMethodParameterPairs()) {
//                methodDeclaration.addAndGetParameter(methodParameterPair.getParamType(), methodParameterPair.getParamName())
//                        .setVarArgs(false);// setVarArgs(true)表达是否多参
//            }
//            if ("void".equals(methodInfo.getReturnType())) { // 方法返回是void的时候 就干干净净的空方法
//                // 增加method的代码块儿
//                BlockStmt blockStmt = new BlockStmt();
//                methodDeclaration.setBody(blockStmt);
//            } else {
//
//                // 增加method的代码块儿
//                BlockStmt blockStmt = new BlockStmt();
//                methodDeclaration.setBody(blockStmt);
//
//                ReturnStmt returnStmt = new ReturnStmt();
//                NameExpr returnNameExpr = new NameExpr();
//                returnNameExpr.setName(methodInfo.getReturnStr());
//                returnStmt.setExpression(returnNameExpr);
//                blockStmt.addStatement(returnStmt); // 返回代码块
//            }
//        }
//
//    }
//
//    public static void addField(ClassOrInterfaceDeclaration classDeclaration, FieldInfo fieldInfo) {
//
//        FieldDeclaration fieldDeclaration = new FieldDeclaration();
//        VariableDeclarator variable = new VariableDeclarator(StaticJavaParser.parseType(fieldInfo.getType()), fieldInfo.getValue());
//        variable.setInitializer(new NameExpr(fieldInfo.getInitializer()));
//        fieldDeclaration.getVariables().add(variable);
//        //fieldDeclaration.setModifiers(fieldInfo.getModifiers());
//        classDeclaration.getMembers().add(fieldDeclaration);
//    }
//}
