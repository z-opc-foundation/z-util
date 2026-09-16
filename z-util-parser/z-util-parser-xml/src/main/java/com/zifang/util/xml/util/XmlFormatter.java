package com.zifang.util.xml.util;

import com.zifang.util.xml.model.*;

import java.util.List;

/**
 * XML 格式化与序列化工具。
 *
 * @author zifang
 */

/**
 * XmlFormatter类。
 */
public class XmlFormatter {

    private int indentSize = 2;

    private boolean preserveWhitespace = false;

    /**
     * XmlFormatter方法。
     */
    public XmlFormatter() {
    }

    /**
     * XmlFormatter方法。
     * * @param indentSize int类型参数
     */
    public XmlFormatter(int indentSize) {
        this.indentSize = indentSize;
    }

    /**
     * escapeXmlText方法。
     * * @param text String类型参数
     *
     * @return static String类型返回值
     */
    public static String escapeXmlText(String text) {
        if (text == null) {
            return null;
        }
        // Must escape & FIRST to avoid double-escaping &amp; etc.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&') {
                sb.append("&amp;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * escapeXmlAttr方法。
     * * @param value String类型参数
     *
     * @return static String类型返回值
     */
    public static String escapeXmlAttr(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '&') {
                sb.append("&amp;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将 XDocument 序列化为格式化的 XML 字符串。
     */

    /**
     * wrapCData方法。
     * * @param data String类型参数
     *
     * @return static String类型返回值
     */
    public static String wrapCData(String data) {
        if (data == null) {
            return null;
        }
        // 如果包含 ]]>，分段处理
        if (data.contains("]]>")) {
            StringBuilder sb = new StringBuilder();
            String[] parts = data.split("]]>");
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    sb.append("]]><![CDATA[>");
                }
                sb.append(parts[i]);
            }
            return sb.toString();
        }
        return data;
    }

    /**
     * 将 XElement 序列化为格式化的 XML 字符串（不带 declaration）。
     */

    /**
     * setIndentSize方法。
     * * @param indentSize int类型参数
     */
    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    /**
     * setPreserveWhitespace方法。
     * * @param preserveWhitespace boolean类型参数
     */
    public void setPreserveWhitespace(boolean preserveWhitespace) {
        this.preserveWhitespace = preserveWhitespace;
    }

    /**
     * format方法。
     * * @param doc XDocument类型参数
     *
     * @return String类型返回值
     */
    public String format(XDocument doc) {
        StringBuilder sb = new StringBuilder();
        writeDocument(doc, sb, 0);
        return sb.toString();
    }

    /**
     * format方法。
     * * @param element XElement类型参数
     *
     * @return String类型返回值
     */
    public String format(XElement element) {
        StringBuilder sb = new StringBuilder();
        writeElement(element, sb, 0);
        return sb.toString();
    }

    private void writeDocument(XDocument doc, StringBuilder sb, int depth) {
        if (doc.getDeclaration() != null) {
            writeDeclaration(doc.getDeclaration(), sb);
            if (indentSize > 0 && (!doc.getPrependNodes().isEmpty() || doc.getRoot() != null)) {
                sb.append('\n');
            }
        } else {
            // 默认加上 XML 声明
            writeDeclaration(new XDeclaration("1.0", null, null), sb);
            if (indentSize > 0 && doc.getRoot() != null) {
                sb.append('\n');
            }
        }

        for (XNode node : doc.getPrependNodes()) {
            writeNode(node, sb, 0);
            if (indentSize > 0) {
                sb.append('\n');
            }
        }

        if (doc.getRoot() != null) {
            writeElement(doc.getRoot(), sb, 0);
        }
    }

    private void writeDeclaration(XDeclaration decl, StringBuilder sb) {
        sb.append("<?xml version=\"");
        sb.append(decl.getVersion());
        sb.append("\"");
        if (decl.getEncoding() != null) {
            sb.append(" encoding=\"");
            sb.append(escapeXmlAttr(decl.getEncoding()));
            sb.append("\"");
        }
        if (decl.getStandalone() != null) {
            sb.append(" standalone=\"");
            sb.append(escapeXmlAttr(decl.getStandalone()));
            sb.append("\"");
        }
        sb.append("?>");
    }

    private void writeElement(XElement element, StringBuilder sb, int depth) {
        indent(sb, depth);
        sb.append('<');
        sb.append(element.getQualifiedName());
        writeAttributes(element, sb);
        List<XNode> children = element.getChildren();

        if (children.isEmpty()) {
            sb.append("/>");
            return;
        }

        // 判断是否为纯文本子元素（避免格式化成 <tag>text</tag> 而非 <tag>text</tag>）
        if (isSimpleTextElement(element)) {
            sb.append('>');
            for (XNode child : children) {
                if (child instanceof XText) {
                    sb.append(escapeXmlText(((XText) child).getText()));
                } else if (child instanceof XCData) {
                    sb.append("<![CDATA[");
                    sb.append(((XCData) child).getData());
                    sb.append("]]>");
                } else if (child instanceof XElement) {
                    writeElement((XElement) child, sb, 0);
                }
            }
            sb.append("</");
            sb.append(element.getQualifiedName());
            sb.append('>');
            return;
        }

        sb.append('>');
        if (indentSize > 0) {
            sb.append('\n');
        }
        for (XNode child : children) {
            writeNode(child, sb, depth + 1);
        }
        if (indentSize > 0) {
            indent(sb, depth);
        }
        sb.append("</");
        sb.append(element.getQualifiedName());
        sb.append('>');
        if (indentSize > 0) {
            sb.append('\n');
        }
    }

    private boolean isSimpleTextElement(XElement element) {
        List<XNode> children = element.getChildren();
        if (children.isEmpty()) {
            return false;
        }
        for (XNode child : children) {
            if (!(child instanceof XText || child instanceof XCData)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 对 XML 元素内容进行转义。
     */

    private void writeNode(XNode node, StringBuilder sb, int depth) {
        if (node instanceof XElement) {
            writeElement((XElement) node, sb, depth);
        } else if (node instanceof XText) {
            String text = ((XText) node).getText();
            if (preserveWhitespace || !text.trim().isEmpty()) {
                sb.append(escapeXmlText(text));
                if (indentSize > 0) {
                    sb.append('\n');
                }
            }
        } else if (node instanceof XCData) {
            if (indentSize > 0) {
                indent(sb, depth);
            }
            sb.append("<![CDATA[");
            sb.append(((XCData) node).getData());
            sb.append("]]>");
            if (indentSize > 0) {
                sb.append('\n');
            }
        } else if (node instanceof XComment) {
            if (indentSize > 0) {
                indent(sb, depth);
            }
            sb.append("<!--");
            sb.append(((XComment) node).getContent());
            sb.append("-->");
            if (indentSize > 0) {
                sb.append('\n');
            }
        } else if (node instanceof XProcessingInstruction) {
            if (indentSize > 0) {
                indent(sb, depth);
            }
            sb.append("<?");
            XProcessingInstruction pi = (XProcessingInstruction) node;
            sb.append(pi.getTarget());
            if (!pi.getData().isEmpty()) {
                sb.append(' ').append(pi.getData());
            }
            sb.append("?>");
            if (indentSize > 0) {
                sb.append('\n');
            }
        }
    }

    /**
     * 对 XML 属性值进行转义。
     */

    private void writeAttributes(XElement element, StringBuilder sb) {
        for (java.util.Map.Entry<String, String> attr : element.getAttributes().entrySet()) {
            sb.append(' ');
            sb.append(attr.getKey());
            if (attr.getValue() != null && !attr.getValue().isEmpty()) {
                sb.append("=\"");
                sb.append(escapeXmlAttr(attr.getValue()));
                sb.append('"');
            }
        }
    }

    /**
     * 对 CDATA 内容进行安全封装（检测是否含有 ]]>）。
     */

    private void indent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth * indentSize; i++) {
            sb.append(' ');
        }
    }
}
