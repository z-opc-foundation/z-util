package com.zifang.util.xml.util;

import com.zifang.util.xml.model.XDocument;
import com.zifang.util.xml.model.XElement;
import com.zifang.util.xml.model.XNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XPath 查询引擎，支持常用路径表达式。
 * <p>
 * 支持语法:
 * <ul>
 *   <li>{@code /root/child} - 绝对/相对路径</li>
 *   <li>{@code //descendant} - 任意后代</li>
 *   <li>{@code *} - 通配符</li>
 *   <li>{@code [@attr='value']} - 属性过滤</li>
 *   <li>{@code [n]} - 位置索引（从 1 开始）</li>
 *   <li>{@code @attr} - 属性选择</li>
 * </ul>
 *
 * @author zifang
 */

/**
 * XPathQuery类。
 */
public class XPathQuery {

    // ===== 公开 API =====

    /**
     * 在 document 上执行 XPath 查询。
     */
    /**
     * query方法。
     * * @param doc XDocument类型参数
     *
     * @param path String类型参数
     * @return List<Object>类型返回值
     */
    public List<Object> query(XDocument doc, String path) {
        if (doc.getRoot() == null) {
            return new ArrayList<>();
        }
        return queryNode(doc, path);
    }

    /**
     * 在 element 上执行 XPath 查询。
     */
    /**
     * query方法。
     * * @param element XElement类型参数
     *
     * @param path String类型参数
     * @return List<Object>类型返回值
     */
    public List<Object> query(XElement element, String path) {
        return queryNode(element, path);
    }

    /**
     * 查询，返回第一个结果。
     */
    /**
     * queryOne方法。
     * * @param doc XDocument类型参数
     *
     * @param path String类型参数
     * @return Object类型返回值
     */
    public Object queryOne(XDocument doc, String path) {
        List<Object> results = query(doc, path);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * queryOne方法。
     * * @param element XElement类型参数
     *
     * @param path String类型参数
     * @return Object类型返回值
     */
    public Object queryOne(XElement element, String path) {
        List<Object> results = query(element, path);
        return results.isEmpty() ? null : results.get(0);
    }

    // ===== 内部查询逻辑 =====

    private List<Object> queryNode(XNode node, String path) {
        List<Object> results = new ArrayList<>();
        if (path.startsWith("//")) {
            // Descendant-or-self axis
            String rest = path.substring(2);
            collectDescendants(node, rest, results);
        } else if (path.startsWith("/")) {
            // Absolute path
            String rest = path.substring(1);
            if (node instanceof XDocument) {
                XElement root = ((XDocument) node).getRoot();
                if (root == null) {
                    return results;
                }
                if (rest.isEmpty()) {
                    // Query is just "/" - return root element
                    results.add(root);
                } else {
                    String[] parts = splitPath(rest);
                    // First part should match the root element itself
                    if (matchesName(root, parts[0])) {
                        if (parts.length == 1) {
                            results.add(root);
                        } else {
                            followPath(root, parts, 1, results);
                        }
                    }
                }
            } else if (node instanceof XElement) {
                followPath((XElement) node, splitPath(rest), 0, results);
            }
        } else {
            // Relative path
            if (node instanceof XDocument) {
                XElement root = ((XDocument) node).getRoot();
                if (root != null) {
                    followPath(root, splitPath(path), 0, results);
                }
            } else if (node instanceof XElement) {
                followPath((XElement) node, splitPath(path), 0, results);
            }
        }
        return results;
    }

    private void collectDescendants(XNode node, String rest, List<Object> results) {
        if (node instanceof XElement) {
            XElement element = (XElement) node;
            String[] parts = splitPath(rest);
            if (parts.length > 0) {
                followPath(element, parts, 0, results);
            } else {
                // //element - match any descendant named rest
                if (matchesName(element, rest)) {
                    results.add(element);
                }
            }
            for (XNode child : element.getChildren()) {
                collectDescendants(child, rest, results);
            }
        } else if (node instanceof XDocument) {
            XElement root = ((XDocument) node).getRoot();
            if (root != null) {
                collectDescendants(root, rest, results);
            }
        }
    }

    private void followPath(XElement element, String[] parts, int idx, List<Object> results) {
        if (idx >= parts.length) {
            results.add(element);
            return;
        }
        String part = parts[idx];
        if (part.isEmpty()) {
            followPath(element, parts, idx + 1, results);
            return;
        }

        // Attribute selection: @attr
        if (part.startsWith("@")) {
            String attrName = part.substring(1);
            if (attrName.equals("*")) {
                for (java.util.Map.Entry<String, String> attr : element.getAttributes().entrySet()) {
                    results.add(attr.getValue());
                }
            } else {
                String val = element.getAttribute(attrName);
                if (val != null) {
                    results.add(val);
                }
            }
            return;
        }

        // Predicate: [n] or [@attr='value']
        String name;
        int predicateIdx = part.indexOf('[');
        int predicateResultIdx = -1;
        String predicateAttrName = null;
        String predicateAttrValue = null;
        int positionalIdx = -1;

        if (predicateIdx >= 0) {
            name = part.substring(0, predicateIdx);
            String predicate = part.substring(predicateIdx + 1, part.length() - 1);
            if (predicate.startsWith("@")) {
                // [@attr='value']
                Pattern p = Pattern.compile("@([^=]+)='([^']*)'");
                Matcher m = p.matcher(predicate);
                if (m.matches()) {
                    predicateAttrName = m.group(1);
                    predicateAttrValue = m.group(2);
                }
            } else {
                // [n]
                positionalIdx = Integer.parseInt(predicate) - 1;
            }
        } else {
            name = part;
        }

        List<XElement> candidates = new ArrayList<>();

        if (name.equals("*")) {
            candidates.addAll(element.getChildElements());
        } else {
            candidates.addAll(element.getChildElements(name));
        }

        for (int i = 0; i < candidates.size(); i++) {
            XElement child = candidates.get(i);
            boolean match = true;

            if (predicateAttrName != null) {
                String val = child.getAttribute(predicateAttrName);
                match = predicateAttrValue.equals(val);
            }
            if (match && positionalIdx >= 0) {
                match = (i == positionalIdx);
            }

            if (match) {
                followPath(child, parts, idx + 1, results);
            }
        }
    }

    private String[] splitPath(String path) {
        // 简单分割，支持 [] predicates
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int bracketDepth = 0;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '[') {
                bracketDepth++;
                current.append(c);
            } else if (c == ']') {
                bracketDepth--;
                current.append(c);
            } else if (c == '/' && bracketDepth == 0) {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts.toArray(new String[0]);
    }

    private boolean matchesName(XElement element, String name) {
        return element.getName().equals(name) || element.getQualifiedName().equals(name);
    }
}
