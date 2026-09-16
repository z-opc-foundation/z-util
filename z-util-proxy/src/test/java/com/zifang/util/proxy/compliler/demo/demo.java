package com.zifang.util.proxy.compliler.demo;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import static com.github.javaparser.ast.Modifier.Keyword.PUBLIC;

/**
 * demo类。
 */
public class demo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        CompilationUnit compilationUnit = new CompilationUnit();
        compilationUnit.setPackageDeclaration("com.abc.def");
        compilationUnit.addImport("org.springframework.boot.SpringApplication");
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.addClass("AnyClassName").setPublic(true);
        classDeclaration.addAnnotation("AnyAnnotation");

        classDeclaration.addField("String", "title", PUBLIC);
        classDeclaration.addField("Person", "author", PUBLIC);

        classDeclaration.addConstructor(PUBLIC)
                .addParameter("String", "title")
                .addParameter("Person", "author")
                .setBody(new BlockStmt()
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), "title"),
                                new NameExpr("title"),
                                AssignExpr.Operator.ASSIGN)))
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), "author"),
                                new NameExpr("author"),
                                AssignExpr.Operator.ASSIGN))));


        // 给类增加一个方法
        MethodDeclaration methodDeclaration = classDeclaration.addMethod("anyMethodName", PUBLIC);
        methodDeclaration.setType("AnyReturnType");
        // 方法的入参
        methodDeclaration.addAndGetParameter(String.class, "args").setVarArgs(false);// setVarArgs(true)表达是否多参
        methodDeclaration.addAndGetParameter(String.class, "cc").setVarArgs(false);// setVarArgs(true)表达是否多参
        // 方法增加注解
        methodDeclaration.addAndGetAnnotation("AnyAnnotation");
        methodDeclaration.addSingleMemberAnnotation("aa", "吃饭=吃饭");

        // 增加method的代码块儿
        BlockStmt blockStmt = new BlockStmt();
        methodDeclaration.setBody(blockStmt);

        // 有实际意义的方法代码
        ExpressionStmt expressionStmt = new ExpressionStmt();

        // 声明式 表达式 代码
        VariableDeclarationExpr variableDeclarationExpr = new VariableDeclarationExpr();
        VariableDeclarator variableDeclarator = new VariableDeclarator();
        variableDeclarator.setType(new ClassOrInterfaceType("AnyVariableType")); // 声明类型
        variableDeclarator.setName("anyVariableName");// 声明类型名

        variableDeclarator.setInitializer("new AnyVariableType()");// 对应的初始化 看起来像字符串拼接

        NodeList<VariableDeclarator> variableDeclarators = new NodeList<>();
        variableDeclarators.add(variableDeclarator);
        variableDeclarationExpr.setVariables(variableDeclarators);
        expressionStmt.setExpression(variableDeclarationExpr);

        // 调用方法相关
        NameExpr nameExpr = new NameExpr("anyVariableName");
        MethodCallExpr methodCallExpr = new MethodCallExpr(nameExpr, "anyMethodName");
        methodCallExpr.addArgument("anyArgument");

        // 方法返回相关
        ReturnStmt returnStmt = new ReturnStmt();
        NameExpr returnNameExpr = new NameExpr();
        returnNameExpr.setName("anyVariableName");
        returnStmt.setExpression(returnNameExpr);

        blockStmt.addStatement(expressionStmt); // 声明
        blockStmt.addStatement(methodCallExpr); // 调用代码块
        blockStmt.addStatement(returnStmt); // 返回代码块

        String code = compilationUnit.toString();
        System.out.println(code);

    }
}
