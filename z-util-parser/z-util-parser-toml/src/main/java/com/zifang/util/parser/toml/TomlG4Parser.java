package com.zifang.util.parser.toml;

import com.zifang.util.dsl.core.ASTNode;
import com.zifang.util.dsl.core.TokenReader;
import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.g4.DynamicParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 G4 DSL 的 TOML 解析器。
 * <p>
 * 使用 DynamicLexer + DynamicParser 加载 TomlLexer.g4 + TomlParser.g4，
 * 无任何第三方依赖。
 * <p>
 * 支持 TOML v1.0.0 子集：key=value、sections [a.b]、array of tables [[a]]、
 * 字符串（双引号/单引号）、数字、布尔、null、数组。
 */

/**
 * TomlG4Parser类。
 */
public class TomlG4Parser {

    private static final String LEXER_G4 = "TomlLexer.g4";
    private static final String PARSER_G4 = "TomlParser.g4";

    /**
     * 解析 TOML 字符串。
     */
    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return TomlDocument类型返回值
     */
    public TomlDocument parse(String content) {
        return parse(new StringReader(content));
    }

    /**
     * 解析 TOML Reader。
     */
    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return TomlDocument类型返回值
     */
    public TomlDocument parse(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return parseString(sb.toString());
        } catch (IOException e) {
            throw new TomlException("读取 TOML 失败", e);
        }
    }

    private TomlDocument parseString(String content) {
        TomlDocument document = new TomlDocument();
        TomlDocument.TomlTable rootTable = new TomlDocument.TomlTable("");
        document.addTable("", rootTable);
        TomlDocument.TomlTable currentTable = rootTable;

        if (content == null || content.trim().isEmpty()) {
            return document;
        }

        try {
            String lexerG4 = loadG4(LEXER_G4);
            String parserG4 = loadG4(PARSER_G4);

            DynamicLexer lexer = new DynamicLexer();
            lexer.loadG4(lexerG4);
            lexer.setInput(content);
            TokenReader tokenReader = lexer.getTokenReader();

            DynamicParser parser = new DynamicParser();
            parser.loadG4(parserG4);
            parser.setTokenReader(tokenReader);
            ASTNode ast = parser.parse("file");

            for (ASTNode top : ast.getChildren()) {
                if (!"topItem".equals(top.getType())) continue;
                ASTNode child = top.getChildren().isEmpty() ? null : top.getChildren().get(0);
                if (child == null) continue;
                String type = child.getType();
                if ("section".equals(type)) {
                    String path = extractDottedKey(child);
                    currentTable = navigateOrCreateTable(document, path, false);
                } else if ("tableArray".equals(type)) {
                    String path = extractDottedKey(child);
                    currentTable = navigateOrCreateTable(document, path, true);
                } else if ("entry".equals(type)) {
                    String key = extractDottedKey(child);
                    Object value = extractValue(findChildByType(child, "value"));
                    if (currentTable != null) currentTable.put(key, value);
                }
            }
            return document;
        } catch (TomlException e) {
            throw e;
        } catch (Exception e) {
            throw new TomlException("G4 TOML解析失败: " + e.getMessage(), e);
        }
    }

    // ==================== G4 加载 ====================

    private String loadG4(String name) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new TomlException("G4文件未找到: " + name);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (TomlException e) {
            throw e;
        } catch (Exception e) {
            throw new TomlException("读取G4文件失败: " + name, e);
        }
    }

    // ==================== 工具方法 ====================

    private ASTNode findChildByType(ASTNode node, String type) {
        for (ASTNode c : node.getChildren()) {
            if (type.equals(c.getType())) return c;
        }
        return null;
    }

    private String extractDottedKey(ASTNode sectionOrEntry) {
        ASTNode dk = findChildByType(sectionOrEntry, "dottedKey");
        if (dk == null) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ASTNode c : dk.getChildren()) {
            if ("KEY".equals(c.getType())) {
                if (!first) sb.append('.');
                sb.append(c.getText());
                first = false;
            }
        }
        return sb.toString();
    }

    private TomlDocument.TomlTable navigateOrCreateTable(TomlDocument doc, String path, boolean isArray) {
        if (path == null || path.isEmpty()) return doc.getRootTable();
        String[] parts = path.split("\\.");
        TomlDocument.TomlTable current = doc.getTables().get(parts[0]);
        if (current == null) {
            current = new TomlDocument.TomlTable(parts[0]);
            doc.addTable(parts[0], current);
        }
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            TomlDocument.TomlTable next = current.getSubTable(part);
            if (next == null) {
                next = new TomlDocument.TomlTable(part);
                current.addSubTable(part, next);
            }
            current = next;
        }
        return current;
    }

    private Object extractValue(ASTNode valueNode) {
        if (valueNode == null) return null;
        if (valueNode.getChildren().isEmpty()) {
            return parseScalarFromText(valueNode.getType(), valueNode.getText());
        }
        ASTNode first = valueNode.getChildren().get(0);
        String type = first.getType();
        if ("array".equals(type)) {
            List<Object> list = new ArrayList<>();
            for (ASTNode item : first.getChildren()) {
                if ("value".equals(item.getType())) {
                    list.add(extractValue(item));
                }
            }
            return list;
        }
        return parseScalarFromText(type, first.getText());
    }

    private Object parseScalarFromText(String type, String text) {
        if (text == null) return null;
        text = text.trim();
        switch (type) {
            case "STRING_DQ": {
                String inner = text.length() >= 2 ? text.substring(1, text.length() - 1) : "";
                return unescapeString(inner);
            }
            case "STRING_SQ":
                return text.length() >= 2 ? text.substring(1, text.length() - 1) : text;
            case "NUMBER":
                return parseNumber(text);
            case "BOOL":
                return Boolean.parseBoolean(text);
            case "NULL":
                return null;
            default:
                return text;
        }
    }

    private String unescapeString(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int len = s.length();
        while (i < len) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char n = s.charAt(i + 1);
                switch (n) {
                    case 'n':
                        sb.append('\n');
                        i += 2;
                        break;
                    case 't':
                        sb.append('\t');
                        i += 2;
                        break;
                    case 'r':
                        sb.append('\r');
                        i += 2;
                        break;
                    case '"':
                        sb.append('"');
                        i += 2;
                        break;
                    case '\\':
                        sb.append('\\');
                        i += 2;
                        break;
                    default:
                        sb.append(c).append(n);
                        i += 2;
                        break;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private Object parseNumber(String text) {
        try {
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                return Double.parseDouble(text);
            }
            long v = Long.parseLong(text);
            if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) return (int) v;
            return v;
        } catch (NumberFormatException e) {
            return text;
        }
    }
}
