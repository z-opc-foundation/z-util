package com.zifang.util.parser.csv;

import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.token.Token;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 G4 DSL 的 CSV 解析器。
 * <p>
 * 使用 DynamicLexer 加载 CsvLexer.g4 做词法分析，
 * 词法层完全由 G4 grammar 动态驱动，token 流由 Java 代码组装为 List<String[]>。
 * <p>
 * 优势：语法扩展只需修改 CsvLexer.g4，无需改动 Java 代码。
 * <p>
 * 注：本解析器固定使用逗号作为分隔符。如需自定义分隔符，
 * 请使用 {@link CsvParser}（手写状态机版本）。
 */

/**
 * CsvG4Parser类。
 */
public class CsvG4Parser {

    private static final String LEXER_G4 = "CsvLexer.g4";

    /**
     * 解析 CSV 字符串为 List<String[]>。
     */
    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return List<String[]>类型返回值
     */
    public List<String[]> parse(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        if (content.charAt(0) == '\uFEFF') {
            content = content.substring(1);
        }
        try {
            String lexerG4 = loadG4(LEXER_G4);
            DynamicLexer lexer = new DynamicLexer();
            lexer.loadG4(lexerG4);
            lexer.setInput(content);
            List<Token> tokens = lexer.tokenize();
            return tokensToList(tokens);
        } catch (CsvException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvException("G4 CSV解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 CSV Reader。
     */
    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return List<String[]>类型返回值
     */
    public List<String[]> parse(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return parse(sb.toString());
        } catch (IOException e) {
            throw new CsvException("读取 CSV 失败", e);
        }
    }

    // ==================== G4 加载 ====================

    private String loadG4(String name) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new CsvException("G4文件未找到: " + name);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (CsvException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvException("读取G4文件失败: " + name, e);
        }
    }

    // ==================== Token 流 → List<String[]> ====================

    private List<String[]> tokensToList(List<Token> tokens) {
        List<String[]> result = new ArrayList<>();
        List<String> current = new ArrayList<>();
        boolean hasField = false;
        for (Token tok : tokens) {
            String name = tok.getTokenName();
            String text = tok.getText();
            if ("COMMA".equals(name)) {
                if (!hasField) current.add("");
                hasField = false;
            } else if ("NL".equals(name)) {
                if (!hasField) current.add("");
                if (!(current.size() == 1 && current.get(0).isEmpty())) {
                    result.add(current.toArray(new String[0]));
                }
                current = new ArrayList<>();
                hasField = false;
            } else if ("QUOTED".equals(name)) {
                String inner = text == null ? "" : text;
                if (inner.length() >= 2) inner = inner.substring(1, inner.length() - 1);
                current.add(inner.replace("\"\"", "\""));
                hasField = true;
            } else if ("TEXT".equals(name)) {
                current.add(text == null ? "" : text);
                hasField = true;
            }
            // WS 等隐藏 token 忽略
        }
        // 处理最后一条记录
        if (hasField || !current.isEmpty()) {
            if (!hasField) current.add("");
            if (!(current.size() == 1 && current.get(0).isEmpty())) {
                result.add(current.toArray(new String[0]));
            }
        }
        return result;
    }
}
