package com.zifang.util.yaml;

import com.zifang.util.core.io.FileUtil;
import com.zifang.util.dsl.core.ASTNode;
import com.zifang.util.yaml.exception.YamlParseException;
import com.zifang.util.yaml.model.YamlArray;
import com.zifang.util.yaml.model.YamlMap;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 基于 G4 DSL 的 YAML 解析器。
 * 使用 DynamicLexer + DynamicParser 加载 YamlLexer.g4 + YamlParser.g4，
 * 无任何第三方依赖。
 */
/**
 * YamlG4Parser类。
 */

/**
 * YamlG4Parser类。
 */
public class YamlG4Parser {

    private static final String LEXER_G4 = "YamlLexer.g4";
    private static final String PARSER_G4 = "YamlParser.g4";

    /**
     * 将 YAML 字符串解析为 Java 对象（Map/List/标量）。
     */
    /**
     * parse方法。
     *      * @param yaml String类型参数
     * @return Object类型返回值
     */
    /**
     * parse方法。
     * * @param yaml String类型参数
     *
     * @return Object类型返回值
     */
    public Object parse(String yaml) {
        return new SimpleYamlParser().parse(yaml);
    }

    /**
     * parseMap方法。
     *      * @param yaml String类型参数
     * @return YamlMap类型返回值
     */
    /**
     * parseMap方法。
     * * @param yaml String类型参数
     *
     * @return YamlMap类型返回值
     */
    public YamlMap parseMap(String yaml) {
        return new SimpleYamlParser().parseMap(yaml);
    }

    /**
     * parseArray方法。
     *      * @param yaml String类型参数
     * @return YamlArray类型返回值
     */
    /**
     * parseArray方法。
     * * @param yaml String类型参数
     *
     * @return YamlArray类型返回值
     */
    public YamlArray parseArray(String yaml) {
        Object result = parse(yaml);
        if (result instanceof YamlArray) return (YamlArray) result;
        throw new YamlParseException("根节点不是序列类型");
    }

    // 调试：计算 lexer 规则数
    private int countLexerRules(String g4) {
        int count = 0;
        boolean inLexer = false;
        for (String line : g4.split("\n")) {
            line = line.trim();
            if (line.startsWith("lexer grammar")) {
                inLexer = true;
                continue;
            }
            if (line.startsWith("parser grammar")) {
                inLexer = false;
                continue;
            }
            if (line.startsWith("options") || line.startsWith("//") || line.isEmpty()) continue;
            if (inLexer && line.contains(":")) count++;
        }
        return count;
    }

    // 调试：计算 parser 规则数
    private int countParserRules(String g4) {
        int count = 0;
        boolean inParser = false;
        for (String line : g4.split("\n")) {
            line = line.trim();
            if (line.startsWith("parser grammar")) {
                inParser = true;
                continue;
            }
            if (line.startsWith("lexer grammar")) {
                inParser = false;
                continue;
            }
            if (line.startsWith("options") || line.startsWith("//") || line.isEmpty()) continue;
            if (inParser && line.contains(":")) count++;
        }
        return count;
    }

    // ==================== G4 加载 ====================

    private String loadG4(String name) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(name);
            if (is == null) throw new YamlParseException("G4文件未找到: " + name);
            byte[] bytes = FileUtil.readAllBytes(is);
            is.close();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (YamlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new YamlParseException("读取G4文件失败: " + name, e);
        }
    }

    // ==================== AST → Java 对象 ====================

    private Object astToJava(ASTNode node) {
        if (node == null) {
            System.err.println("[DBG-YAML] astToJava got null node");
            return null;
        }
        String type = node.getType();
        String text = node.getText();
        List<ASTNode> children = node.getChildren();
        if (children == null) {
            System.err.println("[DBG-YAML] children is null for type=" + type);
            return null;
        }
        if (type == null) {
            return null;
        }
        if (type.equals("pair")) {
            System.err.println("[DBG-YAML] pair type, children=" + children.size());
            for (ASTNode c : children) {
                System.err.println("  child: type=" + c.getType() + " text='" + c.getText() + "' children=" + (c.getChildren() == null ? "NULL" : c.getChildren().size()));
                if (c.getChildren() != null) {
                    for (ASTNode cc : c.getChildren()) {
                        System.err.println("    sub: type=" + cc.getType() + " text='" + cc.getText() + "'");
                    }
                }
            }
        }

        if (children.isEmpty() && (text == null || text.isEmpty())) return null;
        if (children.isEmpty()) return parseScalar(type, text);
        switch (type) {
            case "yaml": {
                YamlMap m = new YamlMap();
                List<ASTNode> kids = node.getChildren();
                return m;
            }
            case "document":
            case "blockMap":
            case "map":
                return astToMap(node);
            case "blockSeq":
            case "seq":
                return astToSeq(node);
            case "blockScalar":
                return text != null ? text.trim() : null;
            case "pair":
                return astToPair(node);
            case "mapPair":
            case "keyScalar":
                return !children.isEmpty() ? astToJava(children.get(0)) : null;
            case "valueToken":
                // valueToken 可能是 Colon（空值），跳过它
                for (ASTNode child : children) {
                    String ct = child.getType();
                    if (!ct.equals("Colon") && !ct.equals("EOL") && !ct.equals("Space")) {
                        return astToJava(child);
                    }
                }
                return null;
            default:
                if (children.size() == 1) return astToJava(children.get(0));
                if (text != null && !text.isEmpty()) return parseScalar(type, text);
                return children.stream().map(this::astToJava).collect(java.util.stream.Collectors.toList());
        }
    }

    private YamlMap astToMap(ASTNode node) {
        YamlMap map = new YamlMap();
        for (ASTNode child : node.getChildren()) {
            Object key = extractKey(child);
            Object val = extractValue(child);
            if (key != null) map.put(String.valueOf(key), val);
        }
        return map;
    }

    /**
     * 提取 pair 的 key 和 value，返回 Map.Entry 或 Pair 对象
     * pair = keyScalar Colon valueToken | keyScalar Colon LBrace ... | ...
     * children = [keyScalar, Colon, valueToken] or [keyScalar, Colon]
     */
    private Object astToPair(ASTNode pairNode) {
        Object key = null;
        Object value = null;
        for (ASTNode child : pairNode.getChildren()) {
            String ctype = child.getType();
            if (ctype.equals("Colon") || ctype.equals("EOL") || ctype.equals("Space")) continue;
            if (ctype.equals("Scalar") || ctype.equals("DqString") || ctype.equals("SqString")
                    || ctype.equals("Number") || ctype.equals("Bool") || ctype.equals("Null")
                    || ctype.equals("keyScalar")) {
                if (key == null) {
                    key = parseScalar(ctype, child.getText());
                } else {
                    value = astToJava(child);
                }
            } else {
                if (key == null) {
                    key = child.getType();
                } else {
                    value = astToJava(child);
                }
            }
        }
        // 返回简单 pair 的情况
        return key;
    }

    private YamlArray astToSeq(ASTNode node) {
        YamlArray arr = new YamlArray();
        for (ASTNode child : node.getChildren()) arr.add(astToJava(child));
        return arr;
    }

    private Object extractKey(ASTNode pairNode) {
        for (ASTNode child : pairNode.getChildren()) {
            if (isKeyLike(child)) {
                String text = child.getText();
                return text != null ? text.trim() : astToJava(child);
            }
        }
        return null;
    }

    private Object extractValue(ASTNode pairNode) {
        // pair = [keyScalar, valueToken]，直接取第二个子节点
        List<ASTNode> children = pairNode.getChildren();
        if (children != null && children.size() >= 2) {
            return astToJava(children.get(1));
        }
        return null;
    }

    private boolean isKeyLike(ASTNode node) {
        String t = node.getType();
        if (t == null) return false;
        return t.equals("keyNode") || t.equals("blockKey") || t.equals("plainScalar")
                || t.equals("DqString") || t.equals("SqString") || t.equals("keyScalar")
                || t.equals("Scalar");
    }

    private Object parseScalar(String type, String text) {
        if (text == null) return null;
        text = text.trim();
        if (text.isEmpty() || text.equals("null") || text.equals("~")) return null;
        if (text.equals("true") || text.equals("True")) return Boolean.TRUE;
        if (text.equals("false") || text.equals("False")) return Boolean.FALSE;
        try {
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
        }
        return text;
    }
}