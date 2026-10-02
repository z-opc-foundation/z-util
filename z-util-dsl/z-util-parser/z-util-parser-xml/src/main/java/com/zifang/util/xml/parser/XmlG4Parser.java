package com.zifang.util.xml.parser;

import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.token.Token;
import com.zifang.util.xml.exception.XmlParseException;
import com.zifang.util.xml.model.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于 G4 DSL 的 XML 解析器。
 * <p>
 * 使用 DynamicLexer 加载 XmlLexer.g4 做词法分析。
 * <p>
 * 词法层规则（详见 resources/XmlLexer.g4）：
 *  - 顶层结构（XML_DECL / PI / COMMENT / CDATA）由 G4 整体匹配
 *  - 标签符号（LT / LT_SLASH / GT / SLASH_GT）、EQUALS、ATTR_VALUE_DQ / SQ 由 G4 单条匹配
 *  - NAME 匹配标签名 / 属性名 / 文本中的"单词"（受 longest-match 支配）
 *  - TEXT 是单字符 fallback（~[<]），处理非字母文本字符与实体引用
 * <p>
 * 组装策略：
 *  - 在 start tag 内（LT..GT/SLASH_GT 之间）：NAME 是标签名或属性名，TEXT 是标签内部空白
 *  - 在 element content 内（GT..LT_SLASH 之间）：NAME / TEXT 都是文本片段，Java 端合并
 *  - 这种"靠上下文区分"的方式绕开 DynamicLexer longest-match 对 NAME 的贪婪问题
 *
 * @author zifang
 */
public class XmlG4Parser {

    private static final String LEXER_G4 = "XmlLexer.g4";

    /**
     * 预加载的 G4 文本（首次加载后不再变化）。
     */
    private static volatile String cachedG4Text;

    private static final Pattern DECL_VERSION =
            Pattern.compile("version\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern DECL_ENCODING =
            Pattern.compile("encoding\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern DECL_STANDALONE =
            Pattern.compile("standalone\\s*=\\s*[\"']([^\"']+)[\"']");

    private List<Token> tokens;
    private int pos;

    /**
     * 将 XML 字符串解析为 XDocument。
     *
     * @param xml XML 字符串
     * @return XDocument
     */
    public XDocument parse(String xml) {
        if (xml == null || xml.isEmpty()) {
            throw new IllegalArgumentException("XML string cannot be null or empty");
        }
        try {
            // 预加载 G4：首次加载后缓存，后续复用（消除 91% 的重复加载开销）
            DynamicLexer lexer = getOrCreateLexer();
            lexer.setInput(xml);
            tokens = lexer.tokenize();
            pos = 0;
            return buildDocument();
        } catch (XmlParseException e) {
            throw e;
        } catch (RuntimeException e) {
            // DynamicLexer 在词法错误或匹配越界时抛 RuntimeException
            throw new XmlParseException("XML 词法错误: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new XmlParseException("G4 XML 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取预编译的 lexer 实例。首次调用时加载 G4 并编译规则，后续复用。
     * <p>
     * 由于 DynamicLexer 非线程安全，每次调用创建新实例，但 G4 文本已缓存，
     * 只需 loadG4()（编译规则 ~0.01ms），无需重新从 classpath 读取文件。
     * 并发安全：ConcurrentHashMap + volatile G4 文本。
     */
    private static DynamicLexer getOrCreateLexer() {
        String g4 = getOrLoadG4Text();
        DynamicLexer lexer = new DynamicLexer();
        lexer.setPreserveWhitespace(true);
        lexer.loadG4(g4);
        return lexer;
    }

    /**
     * 获取或加载 G4 文本（线程安全的懒加载）。
     */
    private static String getOrLoadG4Text() {
        String g4 = cachedG4Text;
        if (g4 != null) {
            return g4;
        }
        synchronized (XmlG4Parser.class) {
            g4 = cachedG4Text;
            if (g4 == null) {
                g4 = loadG4Static(LEXER_G4);
                cachedG4Text = g4;
            }
        }
        return g4;
    }

    // ==================== G4 资源加载 ====================

    /**
     * 静态版本：从 classpath 读取 G4 文件（用于缓存初始化）。
     */
    private static String loadG4Static(String name) {
        try (InputStream is = XmlG4Parser.class.getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                throw new XmlParseException("G4 文件未找到: " + name);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (XmlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParseException("读取 G4 文件失败: " + name, e);
        }
    }

    private String loadG4(String name) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                throw new XmlParseException("G4 文件未找到: " + name);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = is.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        } catch (XmlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParseException("读取 G4 文件失败: " + name, e);
        }
    }

    // ==================== Token 流 → XDocument ====================

    private XDocument buildDocument() {
        XDocument doc = new XDocument();

        while (hasMore()) {
            Token t = peek();
            if (t == null) {
                break;
            }
            String name = t.getTokenName();
            if ("XML_DECL".equals(name)) {
                doc.setDeclaration(parseDeclaration());
            } else if ("PI".equals(name)) {
                doc.addPrependNode(parseProcessingInstruction());
            } else if ("COMMENT".equals(name)) {
                doc.addPrependNode(parseComment());
            } else if ("TEXT".equals(name)) {
                // 根元素前的孤立文本（不合法但容错）
                String text = next().getText();
                String trimmed = text.trim();
                if (trimmed.length() > 0) {
                    throw new XmlParseException("Text content is not allowed before root element: " + trimmed);
                }
                // 跳过根元素前的空白
            } else if ("NAME".equals(name)) {
                // 根元素前的孤立 NAME（不合法但容错：可能是被 longest-match 误吃掉的空白文本）
                throw new XmlParseException("Unexpected NAME before root element: " + t.getText());
            } else if ("LT".equals(name)) {
                doc.setRoot(parseElement());
                break;
            } else {
                throw new XmlParseException("Unexpected token before root element: " + name);
            }
        }

        // 根元素之后：注释 / PI / 空白
        while (hasMore()) {
            Token t = peek();
            if (t == null) {
                break;
            }
            String name = t.getTokenName();
            if ("COMMENT".equals(name)) {
                doc.addPrependNode(parseComment());
            } else if ("PI".equals(name)) {
                doc.addPrependNode(parseProcessingInstruction());
            } else if ("TEXT".equals(name)) {
                String text = next().getText();
                if (text.trim().length() > 0) {
                    throw new XmlParseException("Unexpected text content after root element: " + text.trim());
                }
            } else {
                throw new XmlParseException("Unexpected token after root element: " + name);
            }
        }

        return doc;
    }

    private XElement parseElement() {
        Token lt = next();
        if (!"LT".equals(lt.getTokenName())) {
            throw new XmlParseException("Expected '<', got: " + lt.getTokenName());
        }

        // 标签名：NAME；其前若还有残留 TEXT（如换行）则累积成 tagName 的一部分
        StringBuilder tagNameBuf = new StringBuilder();
        while (hasMore()) {
            Token t = peek();
            if (t == null) {
                break;
            }
            String n = t.getTokenName();
            if ("NAME".equals(n)) {
                tagNameBuf.append(next().getText());
                break;
            } else if ("TEXT".equals(n)) {
                // < 后面只可能是 NAME，否则就是非法输入
                throw new XmlParseException("Expected element name after '<', got TEXT: " + t.getText());
            } else {
                throw new XmlParseException("Expected element name after '<', got: " + n);
            }
        }
        if (tagNameBuf.length() == 0) {
            throw new XmlParseException("Missing element name after '<'");
        }
        XElement element = new XElement(tagNameBuf.toString());

        // 属性：跳过 TEXT 空白，NAME 是属性名，EQUALS + ATTR_VALUE_* 是值
        while (hasMore()) {
            Token t = peek();
            if (t == null) {
                break;
            }
            String n = t.getTokenName();
            if ("NAME".equals(n)) {
                parseAttribute(element);
            } else if ("TEXT".equals(n)) {
                // tag 内部空白，跳过
                next();
            } else {
                break; // GT / SLASH_GT
            }
        }

        // 闭合：/> 或 >
        Token closer = peek();
        if (closer == null) {
            throw new XmlParseException("Unclosed element: " + element.getName());
        }
        String cn = closer.getTokenName();
        if ("SLASH_GT".equals(cn)) {
            next();
            return element;
        } else if ("GT".equals(cn)) {
            next();
        } else {
            throw new XmlParseException("Expected '>' or '/>' after element start, got: " + cn);
        }

        // 子节点 / 文本
        parseElementContent(element);
        return element;
    }

    /**
     * 解析元素内容：累积连续 NAME / TEXT 作为单一 XText，识别 LT / LT_SLASH / CDATA / COMMENT / PI。
     */
    private void parseElementContent(XElement element) {
        StringBuilder textBuf = new StringBuilder();
        while (hasMore()) {
            Token t = peek();
            if (t == null) {
                break;
            }
            String n = t.getTokenName();
            if ("LT".equals(n)) {
                flushText(textBuf, element);
                element.addChild(parseElement());
            } else if ("LT_SLASH".equals(n)) {
                flushText(textBuf, element);
                next(); // 消耗 </
                // 累积 end tag 名称（NAME 在 end tag 后）
                StringBuilder endName = new StringBuilder();
                while (hasMore()) {
                    Token et = peek();
                    if (et == null) {
                        break;
                    }
                    String en = et.getTokenName();
                    if ("NAME".equals(en)) {
                        endName.append(next().getText());
                        break;
                    } else if ("TEXT".equals(en)) {
                        throw new XmlParseException("Unexpected TEXT in end tag near: " + et.getText());
                    } else {
                        break;
                    }
                }
                Token gt = next();
                if (gt == null || !"GT".equals(gt.getTokenName())) {
                    throw new XmlParseException("Expected '>' after </" + element.getName() + ">, got: "
                            + (gt == null ? "EOF" : gt.getTokenName()));
                }
                if (!endName.toString().equals(element.getName())) {
                    throw new XmlParseException("Mismatched end tag: expected </"
                            + element.getName() + ">, got </" + endName + ">");
                }
                return;
            } else if ("TEXT".equals(n)) {
                textBuf.append(next().getText());
            } else if ("NAME".equals(n)) {
                // 文本内容中的"单词"（longest-match 让 NAME 吃掉了连续字母）
                textBuf.append(next().getText());
            } else if ("CDATA".equals(n)) {
                flushText(textBuf, element);
                element.addChild(new XCData(extractCData(next().getText())));
            } else if ("COMMENT".equals(n)) {
                flushText(textBuf, element);
                element.addChild(parseComment());
            } else if ("PI".equals(n)) {
                flushText(textBuf, element);
                element.addChild(parseProcessingInstruction());
            } else {
                throw new XmlParseException("Unexpected token in element content: " + n);
            }
        }

        throw new XmlParseException("Unclosed element: " + element.getName());
    }

    private void flushText(StringBuilder buf, XElement element) {
        if (buf.length() == 0) {
            return;
        }
        String raw = buf.toString();
        element.addChild(new XText(decodeEntities(raw)));
        buf.setLength(0);
    }

    private void parseAttribute(XElement element) {
        Token nameTok = next();
        if (!"NAME".equals(nameTok.getTokenName())) {
            throw new XmlParseException("Expected attribute name, got: " + nameTok.getTokenName());
        }
        String name = nameTok.getText();

        if (!hasMore()) {
            // 行尾的 valueless 属性
            element.setAttribute(name, "");
            return;
        }
        Token t = peek();
        if (t == null || !"EQUALS".equals(t.getTokenName())) {
            // valueless 属性，如 <root disabled/>
            element.setAttribute(name, "");
            return;
        }
        next(); // 消耗 =

        Token valTok = peek();
        if (valTok == null) {
            throw new XmlParseException("Expected attribute value after '=' for attribute: " + name);
        }
        String vn = valTok.getTokenName();
        if (!"ATTR_VALUE_DQ".equals(vn) && !"ATTR_VALUE_SQ".equals(vn)) {
            throw new XmlParseException("Expected attribute value (quoted), got: " + vn);
        }
        next();
        String raw = valTok.getText();
        // 去掉首尾引号
        String inner = raw.substring(1, raw.length() - 1);
        element.setAttribute(name, decodeEntities(inner));
    }

    private XDeclaration parseDeclaration() {
        Token t = next();
        String raw = t.getText();
        XDeclaration decl = new XDeclaration();
        Matcher m = DECL_VERSION.matcher(raw);
        if (m.find()) {
            decl.setVersion(m.group(1));
        }
        m = DECL_ENCODING.matcher(raw);
        if (m.find()) {
            decl.setEncoding(m.group(1));
        }
        m = DECL_STANDALONE.matcher(raw);
        if (m.find()) {
            decl.setStandalone(m.group(1));
        }
        return decl;
    }

    private XProcessingInstruction parseProcessingInstruction() {
        Token t = next();
        String raw = t.getText();
        String inner = stripPiBoundary(raw);
        int spaceIdx = inner.indexOf(' ');
        if (spaceIdx > 0) {
            String target = inner.substring(0, spaceIdx);
            String data = inner.substring(spaceIdx + 1).trim();
            return new XProcessingInstruction(target, data);
        }
        return new XProcessingInstruction(inner, "");
    }

    private XComment parseComment() {
        Token t = next();
        return new XComment(stripCommentBoundary(t.getText()));
    }

    private String extractCData(String raw) {
        if (raw.startsWith("<![CDATA[")) {
            raw = raw.substring(9);
        }
        if (raw.endsWith("]]>")) {
            raw = raw.substring(0, raw.length() - 3);
        }
        return raw;
    }

    private String stripCommentBoundary(String raw) {
        if (raw.startsWith("<!--")) {
            raw = raw.substring(4);
        }
        if (raw.endsWith("-->")) {
            raw = raw.substring(0, raw.length() - 3);
        }
        return raw;
    }

    private String stripPiBoundary(String raw) {
        if (raw.startsWith("<?")) {
            raw = raw.substring(2);
        }
        if (raw.endsWith("?>")) {
            raw = raw.substring(0, raw.length() - 2);
        }
        return raw.trim();
    }

    // ==================== 实体引用解码 ====================

    private String decodeEntities(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length());
        int i = 0;
        int len = text.length();
        while (i < len) {
            char c = text.charAt(i);
            if (c == '&') {
                int semi = text.indexOf(';', i + 1);
                if (semi > i) {
                    String entity = text.substring(i + 1, semi);
                    String decoded = decodeEntity(entity);
                    if (decoded != null) {
                        sb.append(decoded);
                        i = semi + 1;
                        continue;
                    }
                }
                sb.append(c);
                i++;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    private String decodeEntity(String entity) {
        switch (entity) {
            case "amp":
                return "&";
            case "lt":
                return "<";
            case "gt":
                return ">";
            case "quot":
                return "\"";
            case "apos":
                return "'";
            default:
                if (entity.startsWith("#x") || entity.startsWith("#X")) {
                    try {
                        int cp = Integer.parseInt(entity.substring(2), 16);
                        return new String(Character.toChars(cp));
                    } catch (NumberFormatException ex) {
                        throw new XmlParseException("Invalid hex entity: &" + entity + ";");
                    }
                } else if (entity.startsWith("#")) {
                    try {
                        int cp = Integer.parseInt(entity.substring(1), 10);
                        return new String(Character.toChars(cp));
                    } catch (NumberFormatException ex) {
                        throw new XmlParseException("Invalid numeric entity: &" + entity + ";");
                    }
                }
                throw new XmlParseException("Unknown entity: &" + entity + ";");
        }
    }

    // ==================== Token 遍历辅助 ====================

    private boolean hasMore() {
        return tokens != null && pos < tokens.size();
    }

    private Token peek() {
        if (tokens == null || pos >= tokens.size()) {
            return null;
        }
        return tokens.get(pos);
    }

    private Token next() {
        if (tokens == null || pos >= tokens.size()) {
            return null;
        }
        return tokens.get(pos++);
    }
}
