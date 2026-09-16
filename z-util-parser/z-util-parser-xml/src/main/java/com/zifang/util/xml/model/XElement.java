package com.zifang.util.xml.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML 元素节点，对应标准 DOM 中的 Element。
 * <p>
 * 支持:
 * <ul>
 *   <li>名称、属性、子节点（元素/文本/CDATA/注释）</li>
 *   <li>命名空间前缀解析</li>
 *   <li>XPath 风格子元素查找</li>
 * </ul>
 *
 * @author zifang
 */
public class XElement implements XNode {

    private String name;

    private Map<String, String> attributes = new HashMap<>();

    private List<XNode> children = new ArrayList<>();

    private XElement parent;

    private String namespace;

    private String prefix;

    /**
     * XElement方法。
     * * @param name String类型参数
     */
    public XElement(String name) {
        this.name = name;
    }

    /**
     * XElement方法。
     * * @param prefix String类型参数
     *
     * @param name String类型参数
     */
    public XElement(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * getPrefix方法。
     *
     * @return String类型返回值
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * getQualifiedName方法。
     *
     * @return String类型返回值
     */
    public String getQualifiedName() {
        return prefix == null ? name : prefix + ":" + name;
    }

    /**
     * getNamespace方法。
     *
     * @return String类型返回值
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * setNamespace方法。
     * * @param namespace String类型参数
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    // ===== 属性操作 =====

    /**
     * setAttribute方法。
     * * @param name String类型参数
     *
     * @param value String类型参数
     */
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    /**
     * setAttribute方法。
     * * @param prefix String类型参数
     *
     * @param name  String类型参数
     * @param value String类型参数
     */
    public void setAttribute(String prefix, String name, String value) {
        String key = prefix == null ? name : prefix + ":" + name;
        attributes.put(key, value);
    }

    /**
     * getAttribute方法。
     * * @param name String类型参数
     *
     * @return String类型返回值
     */
    public String getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * hasAttribute方法。
     * * @param name String类型参数
     *
     * @return boolean类型返回值
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * removeAttribute方法。
     * * @param name String类型参数
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * getAttributes方法。
     *
     * @return Map<String, String>类型返回值
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    // ===== 子节点操作 =====

    /**
     * addChild方法。
     * * @param child XNode类型参数
     */
    public void addChild(XNode child) {
        children.add(child);
        if (child instanceof XElement) {
            ((XElement) child).setParent(this);
        }
    }

    /**
     * getChildren方法。
     *
     * @return List<XNode>类型返回值
     */
    public List<XNode> getChildren() {
        return children;
    }

    /**
     * setChildren方法。
     * * @param children ListXNode类型参数
     */
    public void setChildren(List<XNode> children) {
        this.children = children;
        for (XNode child : children) {
            if (child instanceof XElement) {
                ((XElement) child).setParent(this);
            }
        }
    }

    /**
     * removeChild方法。
     * * @param child XNode类型参数
     */
    public void removeChild(XNode child) {
        children.remove(child);
    }

    /**
     * getParent方法。
     *
     * @return XElement类型返回值
     */
    public XElement getParent() {
        return parent;
    }

    /**
     * setParent方法。
     * * @param parent XElement类型参数
     */
    public void setParent(XElement parent) {
        this.parent = parent;
    }

    /**
     * getChildElements方法。
     *
     * @return List<XElement>类型返回值
     */
    public List<XElement> getChildElements() {
        List<XElement> result = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof XElement) {
                result.add((XElement) child);
            }
        }
        return result;
    }

    /**
     * 查找所有直接子元素，名称精确匹配。
     */
    /**
     * getChildElements方法。
     * * @param name String类型参数
     *
     * @return List<XElement>类型返回值
     */
    public List<XElement> getChildElements(String name) {
        List<XElement> result = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof XElement) {
                XElement e = (XElement) child;
                if (e.getName().equals(name) || e.getQualifiedName().equals(name)) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    /**
     * 获取直接子元素，名称精确匹配，返回第一个。
     */
    /**
     * getChildElement方法。
     * * @param name String类型参数
     *
     * @return XElement类型返回值
     */
    public XElement getChildElement(String name) {
        List<XElement> list = getChildElements(name);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 获取所有后代元素（深度优先），名称精确匹配。
     */
    /**
     * getDescendantElements方法。
     * * @param name String类型参数
     *
     * @return List<XElement>类型返回值
     */
    public List<XElement> getDescendantElements(String name) {
        List<XElement> result = new ArrayList<>();
        collectDescendants(name, result);
        return result;
    }

    private void collectDescendants(String name, List<XElement> result) {
        for (XNode child : children) {
            if (child instanceof XElement) {
                XElement e = (XElement) child;
                if (e.getName().equals(name) || e.getQualifiedName().equals(name)) {
                    result.add(e);
                }
                e.collectDescendants(name, result);
            }
        }
    }

    /**
     * 获取第一个后代元素。
     */
    /**
     * getDescendantElement方法。
     * * @param name String类型参数
     *
     * @return XElement类型返回值
     */
    public XElement getDescendantElement(String name) {
        for (XNode child : children) {
            if (child instanceof XElement) {
                XElement e = (XElement) child;
                if (e.getName().equals(name) || e.getQualifiedName().equals(name)) {
                    return e;
                }
                XElement found = e.getDescendantElement(name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有文本子节点拼接后的字符串。
     */
    /**
     * getText方法。
     *
     * @return String类型返回值
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (XNode child : children) {
            if (child instanceof XText) {
                sb.append(((XText) child).getText());
            } else if (child instanceof XElement) {
                sb.append(((XElement) child).getText());
            } else if (child instanceof XCData) {
                sb.append(((XCData) child).getData());
            }
        }
        return sb.toString();
    }

    /**
     * 获取所有文本子节点拼接后的字符串，trim 后返回。
     */

    /**
     * setText方法。
     * * @param text String类型参数
     */
    public void setText(String text) {
        children.clear();
        children.add(new XText(text));
    }

    /**
     * 设置文本内容（替换所有子节点为单个文本节点）。
     */

    /**
     * getTextTrim方法。
     *
     * @return String类型返回值
     */
    public String getTextTrim() {
        return getText().trim();
    }

    /**
     * 获取纯文本子节点列表。
     */

    /**
     * getTextNodes方法。
     *
     * @return List<XText>类型返回值
     */
    public List<XText> getTextNodes() {
        List<XText> result = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof XText) {
                result.add((XText) child);
            }
        }
        return result;
    }

    /**
     * 获取 CDATA 子节点。
     */
    /**
     * getCDataNodes方法。
     *
     * @return List<XCData>类型返回值
     */
    public List<XCData> getCDataNodes() {
        List<XCData> result = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof XCData) {
                result.add((XCData) child);
            }
        }
        return result;
    }

    // ===== 深度克隆 =====

    /**
     * deepClone方法。
     *
     * @return XElement类型返回值
     */
    public XElement deepClone() {
        XElement clone = new XElement(this.prefix, this.name);
        clone.namespace = this.namespace;
        for (Map.Entry<String, String> attr : this.attributes.entrySet()) {
            clone.attributes.put(attr.getKey(), attr.getValue());
        }
        for (XNode child : this.children) {
            if (child instanceof XText) {
                clone.children.add(new XText(((XText) child).getText()));
            } else if (child instanceof XCData) {
                clone.children.add(new XCData(((XCData) child).getData()));
            } else if (child instanceof XComment) {
                clone.children.add(new XComment(((XComment) child).getContent()));
            } else if (child instanceof XElement) {
                clone.children.add(((XElement) child).deepClone());
            }
        }
        return clone;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "XElement{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                ", children=" + children.size() +
                '}';
    }
}
