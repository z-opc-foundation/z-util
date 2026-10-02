package com.zifang.util.parser.properties;

import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.token.Token;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 基于 G4 DSL 的 Properties 解析器。
 * <p>
 * 使用 DynamicLexer 加载 PropertiesLexer.g4 做词法分析，
 * 词法层完全由 G4 grammar 动态驱动，token 流由 Java 代码组装为 PropertiesModel。
 * <p>
 * 优势：语法扩展只需修改 PropertiesLexer.g4，无需改动 Java 代码。
 */

/**
 * PropertiesG4Parser类。
 */
public class PropertiesG4Parser {

    public static final String LEXER_G4 = "PropertiesLexer.g4";

    /**
     * 解析 Properties 字符串。
     */
    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return PropertiesModel类型返回值
     */
    public PropertiesModel parse(String content) {
        return parse(new StringReader(content));
    }

    /**
     * 解析 Properties Reader。
     */
    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return PropertiesModel类型返回值
     */
    public PropertiesModel parse(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return parseString(sb.toString());
        } catch (IOException e) {
            throw new PropertiesException("读取 Properties 失败", e);
        }
    }

    private PropertiesModel parseString(String content) {
        PropertiesModel model = new PropertiesModel();
        if (content == null || content.isEmpty()) {
            return model;
        }
        try {
            String lexerG4 = loadG4(LEXER_G4);
            DynamicLexer lexer = new DynamicLexer();
            lexer.loadG4(lexerG4);
            lexer.setInput(content);
            List<Token> tokens = lexer.tokenize();
            return tokensToModel(tokens, model);
        } catch (PropertiesException e) {
            throw e;
        } catch (Exception e) {
            throw new PropertiesException("G4 Properties解析失败: " + e.getMessage(), e);
        }
    }

    // ==================== G4 加载 ====================

    private String loadG4(String name) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new PropertiesException("G4文件未找到: " + name);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (PropertiesException e) {
            throw e;
        } catch (Exception e) {
            throw new PropertiesException("读取G4文件失败: " + name, e);
        }
    }

    // ==================== Token 流 → PropertiesModel ====================

    private PropertiesModel tokensToModel(List<Token> tokens, PropertiesModel model) {
        String pendingKey = null;
        boolean afterSeparator = false;
        for (Token tok : tokens) {
            String name = tok.getTokenName();
            String text = tok.getText();
            if ("IDENTIFIER".equals(name)) {
                if (afterSeparator && pendingKey != null) {
                    // value
                    model.setProperty(pendingKey.trim(), decodeEscapeSequence(text == null ? "" : text));
                    pendingKey = null;
                    afterSeparator = false;
                } else {
                    // key
                    pendingKey = text == null ? "" : text;
                }
            } else if ("SEPARATOR".equals(name)) {
                if (pendingKey != null) {
                    afterSeparator = true;
                }
            }
            // WS / NL / COMMENT 忽略
        }
        return model;
    }

    private String decodeEscapeSequence(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder sb = new StringBuilder();
        int len = str.length();
        int i = 0;
        while (i < len) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case 't':
                        sb.append('\t');
                        i += 2;
                        break;
                    case 'n':
                        sb.append('\n');
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
                    case 'u':
                        if (i + 5 < len) {
                            try {
                                int cp = Integer.parseInt(str.substring(i + 2, i + 6), 16);
                                sb.appendCodePoint(cp);
                                i += 6;
                            } catch (NumberFormatException ex) {
                                sb.append(next);
                                i += 2;
                            }
                        } else {
                            sb.append(next);
                            i += 2;
                        }
                        break;
                    default:
                        sb.append(c).append(next);
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
}
