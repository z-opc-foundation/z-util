package com.zifang.util.xml;

import com.zifang.util.xml.model.*;
import com.zifang.util.xml.parser.XmlG4Parser;
import com.zifang.util.xml.util.XPathQuery;
import com.zifang.util.xml.util.XmlFormatter;

import java.io.IOException;
import java.util.List;

/**
 * XML 工具类，提供解析、序列化、XPath 查询等核心功能。
 * <p>
 * 特性:
 * <ul>
 *   <li>基于 G4 DSL 动态词法（XmlLexer.g4 + DynamicLexer）</li>
 *   <li>支持 XML 声明、命名空间（保留前缀）、CDATA、注释、处理指令</li>
 *   <li>支持实体引用解码：&amp; &lt; &gt; &quot; &apos; &#nnn; &#xnnn;</li>
 *   <li>XPath 查询支持：路径、属性过滤、位置索引、通配符</li>
 *   <li>格式化输出（可配置缩进）</li>
 *   <li>对象序列化：Java Bean → XML</li>
 * </ul>
 *
 * @author zifang
 */

/**
 * XmlUtil类。
 */
public class XmlUtil {

    private static final XPathQuery XPATH_QUERY = new XPathQuery();
    private static final XmlFormatter DEFAULT_FORMATTER = new XmlFormatter(2);
    private static final XmlFormatter COMPACT_FORMATTER = new XmlFormatter(0);

    // ===== 解析 =====

    /**
     * 将 XML 字符串解析为 XDocument。
     *
     * @param xml XML 字符串
     * @return XDocument 对象
     * @throws IOException 如果解析失败
     */
    /**
     * parse方法。
     * * @param xml String类型参数
     *
     * @return static XDocument类型返回值
     */
    public static XDocument parse(String xml) throws IOException {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("XML string cannot be null or empty");
        }
        return new XmlG4Parser().parse(xml);
    }

    /**
     * 将 XML 字符串解析为 XDocument，解析失败返回 null。
     */
    /**
     * parseQuietly方法。
     * * @param xml String类型参数
     *
     * @return static XDocument类型返回值
     */
    public static XDocument parseQuietly(String xml) {
        try {
            return parse(xml);
        } catch (IOException e) {
            return null;
        }
    }

    // ===== 序列化 =====

    /**
     * 将 XDocument 格式化为 XML 字符串（2 空格缩进）。
     */
    /**
     * toXml方法。
     * * @param doc XDocument类型参数
     *
     * @return static String类型返回值
     */
    public static String toXml(XDocument doc) {
        return DEFAULT_FORMATTER.format(doc);
    }

    /**
     * 将 XElement 格式化为 XML 字符串（2 空格缩进）。
     */
    /**
     * toXml方法。
     * * @param element XElement类型参数
     *
     * @return static String类型返回值
     */
    public static String toXml(XElement element) {
        return DEFAULT_FORMATTER.format(element);
    }

    /**
     * 将 XDocument 格式化为紧凑 XML 字符串（无缩进无换行）。
     */
    /**
     * toCompactXml方法。
     * * @param doc XDocument类型参数
     *
     * @return static String类型返回值
     */
    public static String toCompactXml(XDocument doc) {
        return COMPACT_FORMATTER.format(doc);
    }

    /**
     * 将 XElement 格式化为紧凑 XML 字符串。
     */
    /**
     * toCompactXml方法。
     * * @param element XElement类型参数
     *
     * @return static String类型返回值
     */
    public static String toCompactXml(XElement element) {
        return COMPACT_FORMATTER.format(element);
    }

    /**
     * 自定义缩进格式化。
     */
    /**
     * toXml方法。
     * * @param doc XDocument类型参数
     *
     * @param indentSize int类型参数
     * @return static String类型返回值
     */
    public static String toXml(XDocument doc, int indentSize) {
        XmlFormatter formatter = new XmlFormatter(indentSize);
        return formatter.format(doc);
    }

    // ===== XPath 查询 =====

    /**
     * 在 XDocument 上执行 XPath 查询。
     *
     * @param doc  XDocument
     * @param path XPath 表达式，如 "/root/child", "//element[@attr='v']"
     * @return 匹配结果列表（元素或属性值）
     */
    /**
     * xpath方法。
     * * @param doc XDocument类型参数
     *
     * @param path String类型参数
     * @return static List<Object>类型返回值
     */
    public static List<Object> xpath(XDocument doc, String path) {
        return XPATH_QUERY.query(doc, path);
    }

    /**
     * 在 XElement 上执行 XPath 查询。
     */
    /**
     * xpath方法。
     * * @param element XElement类型参数
     *
     * @param path String类型参数
     * @return static List<Object>类型返回值
     */
    public static List<Object> xpath(XElement element, String path) {
        return XPATH_QUERY.query(element, path);
    }

    /**
     * 在 XDocument 上查询，返回第一个结果。
     */
    /**
     * xpathOne方法。
     * * @param doc XDocument类型参数
     *
     * @param path String类型参数
     * @return static Object类型返回值
     */
    public static Object xpathOne(XDocument doc, String path) {
        return XPATH_QUERY.queryOne(doc, path);
    }

    /**
     * 在 XElement 上查询，返回第一个结果。
     */
    /**
     * xpathOne方法。
     * * @param element XElement类型参数
     *
     * @param path String类型参数
     * @return static Object类型返回值
     */
    public static Object xpathOne(XElement element, String path) {
        return XPATH_QUERY.queryOne(element, path);
    }

    /**
     * 在 XDocument 上查询子元素。
     */
    /**
     * getElement方法。
     * * @param doc XDocument类型参数
     *
     * @param path String类型参数
     * @return static XElement类型返回值
     */
    public static XElement getElement(XDocument doc, String path) {
        Object result = xpathOne(doc, path);
        if (result instanceof XElement) {
            return (XElement) result;
        }
        return null;
    }

    /**
     * 在 XElement 上查询子元素。
     */
    /**
     * getElement方法。
     * * @param element XElement类型参数
     *
     * @param path String类型参数
     * @return static XElement类型返回值
     */
    public static XElement getElement(XElement element, String path) {
        Object result = xpathOne(element, path);
        if (result instanceof XElement) {
            return (XElement) result;
        }
        return null;
    }

    /**
     * 在 XElement 上查询属性值。
     */
    /**
     * getAttr方法。
     * * @param element XElement类型参数
     *
     * @param path String类型参数
     * @return static String类型返回值
     */
    public static String getAttr(XElement element, String path) {
        Object result = xpathOne(element, path);
        if (result instanceof String) {
            return (String) result;
        }
        return null;
    }

    /**
     * 在 XDocument 上查询属性值。
     */
    /**
     * getAttr方法。
     * * @param doc XDocument类型参数
     *
     * @param path String类型参数
     * @return static String类型返回值
     */
    public static String getAttr(XDocument doc, String path) {
        Object result = xpathOne(doc, path);
        if (result instanceof String) {
            return (String) result;
        }
        return null;
    }

    // ===== 元素构建（流式 API） =====

    /**
     * 创建 XElement 的快捷方法。
     *
     * @param name 元素名
     * @return 新 XElement
     */
    /**
     * element方法。
     * * @param name String类型参数
     *
     * @return static XElement类型返回值
     */
    public static XElement element(String name) {
        return new XElement(name);
    }

    /**
     * 创建带属性的 XElement。
     */
    /**
     * element方法。
     * * @param name String类型参数
     *
     * @param attrPairs String...类型参数
     * @return static XElement类型返回值
     */
    public static XElement element(String name, String... attrPairs) {
        XElement e = new XElement(name);
        for (int i = 0; i < attrPairs.length - 1; i += 2) {
            e.setAttribute(attrPairs[i], attrPairs[i + 1]);
        }
        return e;
    }

    /**
     * 创建带文本内容的 XElement。
     */
    /**
     * elementWithText方法。
     * * @param name String类型参数
     *
     * @param text String类型参数
     * @return static XElement类型返回值
     */
    public static XElement elementWithText(String name, String text) {
        XElement e = new XElement(name);
        e.setText(text);
        return e;
    }

    /**
     * 创建带子元素的 XElement。
     */
    /**
     * element方法。
     * * @param name String类型参数
     *
     * @param children XNode...类型参数
     * @return static XElement类型返回值
     */
    public static XElement element(String name, XNode... children) {
        XElement e = new XElement(name);
        for (XNode child : children) {
            if (child instanceof XText) {
                e.addChild((XText) child);
            } else if (child instanceof XElement) {
                e.addChild((XElement) child);
            } else if (child instanceof XCData) {
                e.addChild((XCData) child);
            } else if (child instanceof XComment) {
                e.addChild((XComment) child);
            }
        }
        return e;
    }

    /**
     * 创建 XDocument。
     */
    /**
     * document方法。
     * * @param root XElement类型参数
     *
     * @return static XDocument类型返回值
     */
    public static XDocument document(XElement root) {
        return new XDocument(root);
    }

    /**
     * 创建 XDocument，带声明。
     */
    /**
     * document方法。
     * * @param encoding String类型参数
     *
     * @param root XElement类型参数
     * @return static XDocument类型返回值
     */
    public static XDocument document(String encoding, XElement root) {
        XDocument doc = new XDocument(root);
        XDeclaration decl = new XDeclaration("1.0", encoding, null);
        doc.setDeclaration(decl);
        return doc;
    }

    /**
     * 创建 XDeclaration。
     */
    /**
     * declaration方法。
     * * @param version String类型参数
     *
     * @param encoding   String类型参数
     * @param standalone String类型参数
     * @return static XDeclaration类型返回值
     */
    public static XDeclaration declaration(String version, String encoding, String standalone) {
        return new XDeclaration(version, encoding, standalone);
    }

    /**
     * 创建 XAttribute。
     */
    /**
     * attr方法。
     * * @param name String类型参数
     *
     * @param value String类型参数
     * @return static XAttribute类型返回值
     */
    public static XAttribute attr(String name, String value) {
        return new XAttribute(name, value);
    }

    /**
     * 创建 XText。
     */
    /**
     * text方法。
     * * @param content String类型参数
     *
     * @return static XText类型返回值
     */
    public static XText text(String content) {
        return new XText(content);
    }

    /**
     * 创建 XCData。
     */
    /**
     * cdata方法。
     * * @param data String类型参数
     *
     * @return static XCData类型返回值
     */
    public static XCData cdata(String data) {
        return new XCData(data);
    }

    /**
     * 创建 XComment。
     */
    /**
     * comment方法。
     * * @param content String类型参数
     *
     * @return static XComment类型返回值
     */
    public static XComment comment(String content) {
        return new XComment(content);
    }

    // ===== 格式工具 =====

    /**
     * 美化 XML（保留原始声明）。
     */
    /**
     * beautify方法。
     * * @param xml String类型参数
     *
     * @return static String类型返回值
     */
    public static String beautify(String xml) throws IOException {
        XDocument doc = parse(xml);
        return toXml(doc);
    }

    /**
     * 压缩 XML（去除空白）。
     */
    /**
     * compact方法。
     * * @param xml String类型参数
     *
     * @return static String类型返回值
     */
    public static String compact(String xml) throws IOException {
        XDocument doc = parse(xml);
        return toCompactXml(doc);
    }
}
