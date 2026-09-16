package com.zifang.util.parser.proto;

import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.token.Token;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 基于 G4 DSL 的 Proto3 解析器。
 * <p>
 * 使用 DynamicLexer 加载 ProtoLexer.g4 做词法分析，
 * 词法层完全由 G4 grammar 动态驱动，token 流由 Java 代码组装为 ProtoDocument。
 * <p>
 * 支持 proto3 子集：syntax、package、import、message（含 nested message/enum/field）、
 * enum、service、rpc。
 */

/**
 * ProtoG4Parser类。
 */
public class ProtoG4Parser {

    public static final String LEXER_G4 = "ProtoLexer.g4";

    /**
     * 解析 Proto 字符串。
     */
    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return ProtoDocument类型返回值
     */
    public ProtoDocument parse(String content) {
        return parse(new StringReader(content));
    }

    /**
     * 解析 Proto Reader。
     */
    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return ProtoDocument类型返回值
     */
    public ProtoDocument parse(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return parseString(sb.toString());
        } catch (IOException e) {
            throw new ProtoException("读取 Proto 失败", e);
        }
    }

    private ProtoDocument parseString(String content) {
        ProtoDocument doc = new ProtoDocument();
        if (content == null || content.trim().isEmpty()) return doc;

        try {
            String lexerG4 = loadG4(LEXER_G4);
            DynamicLexer lexer = new DynamicLexer();
            lexer.loadG4(lexerG4);
            lexer.setInput(content);
            List<Token> tokens = lexer.tokenize();
            return tokensToDocument(tokens, doc);
        } catch (ProtoException e) {
            throw e;
        } catch (Exception e) {
            throw new ProtoException("G4 Proto解析失败: " + e.getMessage(), e);
        }
    }

    // ==================== G4 加载 ====================

    private String loadG4(String name) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new ProtoException("G4文件未找到: " + name);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (ProtoException e) {
            throw e;
        } catch (Exception e) {
            throw new ProtoException("读取G4文件失败: " + name, e);
        }
    }

    // ==================== Token 流 → ProtoDocument ====================

    private ProtoDocument tokensToDocument(List<Token> tokens, ProtoDocument doc) {
        int i = 0;
        while (i < tokens.size()) {
            Token t = tokens.get(i);
            String name = t.getTokenName();
            if ("SYNTAX".equals(name)) {
                // syntax = "proto3";
                i = parseSyntax(tokens, i, doc);
            } else if ("PACKAGE".equals(name)) {
                i = parsePackage(tokens, i, doc);
            } else if ("IMPORT".equals(name)) {
                i = parseImport(tokens, i, doc);
            } else if ("MESSAGE".equals(name)) {
                i = parseMessage(tokens, i, doc, null);
            } else if ("SERVICE".equals(name)) {
                i = parseService(tokens, i, doc);
            } else {
                i++;
            }
        }
        return doc;
    }

    private int parseSyntax(List<Token> tokens, int i, ProtoDocument doc) {
        // SYNTAX EQUALS STRING_LITERAL SEMI
        if (i + 3 < tokens.size()
                && "EQUALS".equals(tokens.get(i + 1).getTokenName())
                && "STRING_LITERAL".equals(tokens.get(i + 2).getTokenName())) {
            doc.setSyntax(unquote(tokens.get(i + 2).getText()));
            return i + 4; // skip SEMI
        }
        return i + 1;
    }

    private int parsePackage(List<Token> tokens, int i, ProtoDocument doc) {
        // PACKAGE (IDENTIFIER DOT)* IDENTIFIER SEMI
        StringBuilder sb = new StringBuilder();
        int j = i + 1;
        while (j < tokens.size()) {
            String tn = tokens.get(j).getTokenName();
            if ("SEMI".equals(tn)) {
                doc.setPackageName(sb.toString());
                return j + 1;
            }
            if ("DOT".equals(tn)) {
                sb.append('.');
            } else if ("IDENTIFIER".equals(tn)) {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '.') sb.append('.');
                sb.append(tokens.get(j).getText());
            }
            j++;
        }
        return j;
    }

    private int parseImport(List<Token> tokens, int i, ProtoDocument doc) {
        // IMPORT (PUBLIC | WEAK)? STRING_LITERAL SEMI
        int j = i + 1;
        while (j < tokens.size() && !"SEMI".equals(tokens.get(j).getTokenName())) {
            if ("STRING_LITERAL".equals(tokens.get(j).getTokenName())) {
                doc.addImport(unquote(tokens.get(j).getText()));
            }
            j++;
        }
        return j + 1; // skip SEMI
    }

    private int parseMessage(List<Token> tokens, int i, ProtoDocument doc, ProtoMessage parent) {
        // MESSAGE IDENTIFIER LBRACE ... RBRACE
        if (i + 2 >= tokens.size()) return i + 1;
        String msgName = tokens.get(i + 1).getText();
        ProtoMessage message = new ProtoMessage(msgName);
        int j = i + 3; // skip MESSAGE IDENTIFIER LBRACE
        while (j < tokens.size() && !"RBRACE".equals(tokens.get(j).getTokenName())) {
            String tn = tokens.get(j).getTokenName();
            if ("MESSAGE".equals(tn)) {
                j = parseMessage(tokens, j, doc, message);
            } else if ("ENUM".equals(tn)) {
                j = parseEnum(tokens, j, message);
            } else if ("SEMI".equals(tn)) {
                j++;
            } else if (isFieldStart(tn)) {
                j = parseField(tokens, j, message);
            } else {
                j++;
            }
        }
        if (parent != null) {
            parent.getMessages().add(message);
        } else {
            doc.getMessages().add(message);
        }
        return j + 1; // skip RBRACE
    }

    private int parseEnum(List<Token> tokens, int i, ProtoMessage parent) {
        // ENUM IDENTIFIER LBRACE ... RBRACE
        if (i + 2 >= tokens.size()) return i + 1;
        ProtoEnum protoEnum = new ProtoEnum(tokens.get(i + 1).getText());
        int j = i + 3; // skip ENUM IDENTIFIER LBRACE
        while (j < tokens.size() && !"RBRACE".equals(tokens.get(j).getTokenName())) {
            String tn = tokens.get(j).getTokenName();
            if ("IDENTIFIER".equals(tn) && j + 2 < tokens.size()
                    && "EQUALS".equals(tokens.get(j + 1).getTokenName())
                    && "INT_LITERAL".equals(tokens.get(j + 2).getTokenName())) {
                String name = tokens.get(j).getText();
                int val = Integer.parseInt(tokens.get(j + 2).getText());
                protoEnum.addValue(name, val);
                j += 4; // skip IDENTIFIER EQUALS INT_LITERAL SEMI
            } else {
                j++;
            }
        }
        parent.getEnums().add(protoEnum);
        return j + 1;
    }

    private int parseField(List<Token> tokens, int i, ProtoMessage message) {
        // 简化版：从当前位置向后扫，收集 type，找到 IDENTIFIER EQUALS INT_LITERAL SEMI 模式
        int j = i;
        boolean repeated = false;
        if (j < tokens.size()) {
            String tn = tokens.get(j).getTokenName();
            if ("REPEATED".equals(tn) || "OPTIONAL".equals(tn) || "REQUIRED".equals(tn)) {
                repeated = "REPEATED".equals(tn);
                j++;
            }
        }
        // 扫到 EQUALS 为止，前面是 type
        StringBuilder typeBuilder = new StringBuilder();
        while (j < tokens.size()) {
            String tn = tokens.get(j).getTokenName();
            if ("EQUALS".equals(tn)) break;
            String text = tokens.get(j).getText();
            if ("DOT".equals(tn)) {
                if (typeBuilder.length() > 0) typeBuilder.append('.');
            } else {
                if (typeBuilder.length() > 0 && !typeBuilder.toString().endsWith(".")) {
                    typeBuilder.append('.');
                }
                typeBuilder.append(text);
            }
            j++;
        }
        // j 指向 EQUALS
        if (j + 3 < tokens.size()
                && "INT_LITERAL".equals(tokens.get(j + 1).getTokenName())
                && "SEMI".equals(tokens.get(j + 2).getTokenName())) {
            // 但上面的 type 已经把 field name 也吸进去了！需要回退
            // 简单处理：如果 typeBuilder 以 IDENTIFIER 结尾，那最后一段就是 field name
            // 找到最后一个点之后的段作为 field name
            String typeStr = typeBuilder.toString();
            int lastDot = typeStr.lastIndexOf('.');
            String actualType = lastDot >= 0 ? typeStr.substring(0, lastDot) : "";
            String fieldName = lastDot >= 0 ? typeStr.substring(lastDot + 1) : typeStr;
            int tag = Integer.parseInt(tokens.get(j + 1).getText());
            message.addField(new ProtoField(actualType, fieldName, tag, repeated));
            return j + 4; // skip EQUALS INT_LITERAL SEMI + 1
        }
        return j + 1;
    }

    private int parseService(List<Token> tokens, int i, ProtoDocument doc) {
        // SERVICE IDENTIFIER LBRACE rpcDecl* RBRACE
        if (i + 2 >= tokens.size()) return i + 1;
        ProtoService service = new ProtoService(tokens.get(i + 1).getText());
        int j = i + 3; // skip SERVICE IDENTIFIER LBRACE
        while (j < tokens.size() && !"RBRACE".equals(tokens.get(j).getTokenName())) {
            if ("RPC".equals(tokens.get(j).getTokenName())) {
                j = parseRpc(tokens, j, service);
            } else {
                j++;
            }
        }
        doc.getServices().add(service);
        return j + 1;
    }

    private int parseRpc(List<Token> tokens, int i, ProtoService service) {
        java.util.List<String> idents = new java.util.ArrayList<>();
        int j = i + 1; // skip RPC
        while (j < tokens.size() && idents.size() < 3) {
            if ("SEMI".equals(tokens.get(j).getTokenName()) || "LBRACE".equals(tokens.get(j).getTokenName())) {
                break;
            }
            if ("IDENTIFIER".equals(tokens.get(j).getTokenName())) {
                idents.add(tokens.get(j).getText());
            }
            j++;
        }
        if (idents.size() >= 3) {
            service.addRpc(new ProtoRpc(idents.get(0), idents.get(1), idents.get(2)));
        }
        // skip to SEMI or matching RBRACE
        while (j < tokens.size() && !"SEMI".equals(tokens.get(j).getTokenName())
                && !"RBRACE".equals(tokens.get(j).getTokenName())) {
            j++;
        }
        if (j < tokens.size() && "SEMI".equals(tokens.get(j).getTokenName())) j++;
        return j;
    }

    private boolean isFieldStart(String tn) {
        return "REPEATED".equals(tn) || "OPTIONAL".equals(tn) || "REQUIRED".equals(tn)
                || "IDENTIFIER".equals(tn)
                || "INT32".equals(tn) || "INT64".equals(tn) || "STRING".equals(tn)
                || "BOOL".equals(tn) || "DOUBLE".equals(tn) || "FLOAT".equals(tn)
                || "BYTES".equals(tn);
    }

    private String unquote(String s) {
        if (s == null || s.length() < 2) return s;
        return s.substring(1, s.length() - 1);
    }
}
