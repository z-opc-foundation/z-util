package com.zifang.util.parser.ini;

import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.token.Token;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 基于 G4 DSL 的 INI 解析器。
 * <p>
 * 使用 DynamicLexer 加载 IniLexer.g4 做词法分析，
 * 词法层完全由 G4 grammar 动态驱动，token 流由 Java 代码组装为 IniFile。
 * <p>
 * 优势：语法扩展只需修改 IniLexer.g4，无需改动 Java 代码。
 */

/**
 * IniG4Parser类。
 */
public class IniG4Parser {

    private static final String LEXER_G4 = "IniLexer.g4";

    /**
     * 解析 INI 字符串。
     */
    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return IniFile类型返回值
     */
    public IniFile parse(String content) {
        return parse(new StringReader(content));
    }

    /**
     * 解析 INI Reader。
     */
    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return IniFile类型返回值
     */
    public IniFile parse(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return parseString(sb.toString());
        } catch (IOException e) {
            throw new IniException("读取 INI 失败", e);
        }
    }

    private IniFile parseString(String content) {
        IniFile iniFile = new IniFile();
        if (content == null || content.isEmpty()) {
            return iniFile;
        }
        try {
            String lexerG4 = loadG4(LEXER_G4);
            DynamicLexer lexer = new DynamicLexer();
            lexer.loadG4(lexerG4);
            lexer.setInput(content);
            List<Token> tokens = lexer.tokenize();
            return tokensToIniFile(tokens);
        } catch (IniException e) {
            throw e;
        } catch (Exception e) {
            throw new IniException("G4 INI解析失败: " + e.getMessage(), e);
        }
    }

    // ==================== G4 加载 ====================

    private String loadG4(String name) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new IniException("G4文件未找到: " + name);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (IniException e) {
            throw e;
        } catch (Exception e) {
            throw new IniException("读取G4文件失败: " + name, e);
        }
    }

    // ==================== Token 流 → IniFile ====================

    private IniFile tokensToIniFile(List<Token> tokens) {
        IniFile iniFile = new IniFile();
        IniSection currentSection = null;

        for (Token tok : tokens) {
            String name = tok.getTokenName();
            String text = tok.getText();
            if (!"LINE".equals(name)) continue;
            if (text == null) continue;
            String trimmed = text.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith(";") || trimmed.startsWith("#")) continue;
            // section: [name]
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                String sectionName = trimmed.substring(1, trimmed.length() - 1).trim();
                currentSection = new IniSection(sectionName);
                iniFile.addSection(currentSection);
                continue;
            }
            // entry: key = value
            int eqIdx = findUnquotedEquals(trimmed);
            if (eqIdx > 0) {
                String key = trimmed.substring(0, eqIdx).trim();
                String value = trimmed.substring(eqIdx + 1).trim();
                value = stripInlineComment(value);
                if (currentSection == null) {
                    currentSection = new IniSection();
                    iniFile.addSection(currentSection);
                }
                currentSection.put(key, value);
            }
        }
        return iniFile;
    }

    private int findUnquotedEquals(String s) {
        boolean inDq = false, inSq = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && !inSq) inDq = !inDq;
            else if (c == '\'' && !inDq) inSq = !inSq;
            else if (c == '=' && !inDq && !inSq) return i;
        }
        return -1;
    }

    private String stripInlineComment(String value) {
        if (value == null) return "";
        int semi = value.indexOf(';');
        int hash = value.indexOf('#');
        int idx = -1;
        if (semi >= 0 && hash >= 0) idx = Math.min(semi, hash);
        else if (semi >= 0) idx = semi;
        else if (hash >= 0) idx = hash;
        if (idx >= 0) value = value.substring(0, idx);
        return value.trim();
    }
}
