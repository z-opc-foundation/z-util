package com.zifang.util.expression.dynamic;

import com.zifang.util.dsl.core.ASTNode;
import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.token.Token;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 动态G4解析器测试
 */
public class DynamicG4Test {

    /**
     * 测试1: 解析JS.g4文件（用Java解析g4文件本身）
     */
    @Test
    public void testParseG4File() throws Exception {
        String g4Path = "src/main/resources/g4/JSParser.g4";
        String content = new String(Files.readAllBytes(Paths.get(g4Path)));

        System.out.println("=== G4文件内容预览 ===");
        System.out.println(content.substring(0, Math.min(500, content.length())) + "...");
        System.out.println();
    }

    /**
     * 测试2: 使用动态词法分析器（只加载lexer规则）
     */
    @Test
    public void testDynamicLexer() throws Exception {
        // 只提取lexer部分
        String g4Content = loadG4Lexer();

        DynamicLexer lexer = new DynamicLexer();
        lexer.loadG4(g4Content);

        // 测试词法分析
        String jsCode = "var age = 45; if (age > 18) { return true; }";
        System.out.println("=== 测试代码 ===");
        System.out.println(jsCode);
        System.out.println();

        lexer.setInput(jsCode);
        List<Token> tokens = lexer.tokenize();

        System.out.println("=== Token列表 ===");
        System.out.printf("%-20s %-15s %-10s %-10s%n", "TEXT", "TYPE", "LINE", "COLUMN");
        System.out.println("----------------------------------------------------");
        for (Token token : tokens) {
            System.out.printf("%-20s %-15s %-10d %-10d%n",
                    token.getText(), token.getTokenName(), token.getLine(), token.getColumn());
        }
    }

    /**
     * 测试3: 完整流程 - 加载G4文件 -> 词法分析 -> 语法分析 -> AST
     */
    @Test
    public void testFullPipeline() throws Exception {
        String g4Content = loadG4Lexer();

        // 1. 词法分析
        DynamicLexer lexer = new DynamicLexer();
        lexer.loadG4(g4Content);
        lexer.setInput("var x = 100;");
        List<Token> tokens = lexer.tokenize();

        System.out.println("=== 词法分析结果 ===");
        for (Token token : tokens) {
            System.out.println(token);
        }
        System.out.println();
    }

    /**
     * 测试4: 测试多种JS代码片段
     */
    @Test
    public void testMultipleSamples() throws Exception {
        String g4Content = loadG4Lexer();
        DynamicLexer lexer = new DynamicLexer();
        lexer.loadG4(g4Content);

        String[] samples = {
                "var name = \"Alice\";",
                "let x = 10 + 20 * 30;",
                "if (x > 0) return true; else return false;",
                "function add(a, b) { return a + b; }",
                "// 这是注释\nvar x = 100; /* 多行注释 */",
                "x = x > 100 ? 100 : x;",
        };

        for (String sample : samples) {
            System.out.println("========================================");
            System.out.println("代码: " + sample);
            lexer.setInput(sample);
            List<Token> tokens = lexer.tokenize();
            for (Token token : tokens) {
                System.out.printf("  %s [%s] @%d:%d%n",
                        token.getText(), token.getTokenName(), token.getLine(), token.getColumn());
            }
            System.out.println();
        }
    }

    /**
     * 提取G4文件中的lexer部分
     */
    private String loadG4Lexer() throws Exception {
        String g4Path = "src/main/resources/g4/JSParser.g4";
        String content = new String(Files.readAllBytes(Paths.get(g4Path)));

        // 提取lexer grammar部分
        int lexerStart = content.indexOf("lexer grammar");
        int parserStart = content.indexOf("parser grammar");

        if (lexerStart == -1) {
            throw new RuntimeException("No lexer grammar found");
        }

        if (parserStart == -1) {
            return content.substring(lexerStart);
        }

        return content.substring(lexerStart, parserStart);
    }

    /**
     * 打印AST树（用于调试）
     */
    private void dumpAST(ASTNode node, int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) sb.append("  ");
        sb.append(node.getType());
        if (node.getText() != null && !node.getText().isEmpty()) {
            sb.append(": ").append(node.getText());
        }
        System.out.println(sb);
        for (ASTNode child : node.getChildren()) {
            dumpAST(child, indent + 1);
        }
    }
}