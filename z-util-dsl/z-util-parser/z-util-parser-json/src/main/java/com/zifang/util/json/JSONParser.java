package com.zifang.util.json;

import com.zifang.util.dsl.core.ASTNode;
import com.zifang.util.dsl.core.TokenReader;
import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.g4.DynamicParser;
import com.zifang.util.json.exception.JsonParseException;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 G4 DSL 的 JSON 解析器。
 * <p>
 * 使用 DynamicLexer + DynamicParser 加载 JsonLexer.g4 + JsonParser.g4，
 * 无任何第三方依赖。G4文件位于 src/main/resources/。
 * <p>
 * 对标 com.zifang.util.json.JSONParser，提供 fromJSON(String) 入口。
 */

/**
 * JSONParser类。
 */
public class JSONParser {

    private static final String LEXER_G4 = "JsonLexer.g4";
    private static final String PARSER_G4 = "JsonParser.g4";

    /**
     * 将 JSON 字符串解析为 Java 对象（JsonObject / JsonArray / 标量）。
     *
     * @param json JSON 字符串
     * @return JsonObject、JsonArray 或标量值（String/Number/Boolean/null）
     * @throws JsonParseException 如果格式非法
     */
    /**
     * fromJSON方法。
     * * @param json String类型参数
     *
     * @return Object类型返回值
     */
    public Object fromJSON(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JsonObject();
        }
        try {
            String lexerG4 = loadG4(LEXER_G4);
            String parserG4 = loadG4(PARSER_G4);

            DynamicLexer lexer = new DynamicLexer();
            lexer.loadG4(lexerG4);
            lexer.setInput(json);
            TokenReader tokenReader = lexer.getTokenReader();

            DynamicParser parser = new DynamicParser();
            parser.loadG4(parserG4);
            parser.setTokenReader(tokenReader);
            ASTNode ast = parser.parse("json");

            return astToJava(ast);
        } catch (JsonParseException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("EXCEPTION type: " + e.getClass().getName());
            System.out.println("EXCEPTION message: " + e.getMessage());
            System.out.println("EXCEPTION cause: " + (e.getCause() != null ? e.getCause().getClass().getName() + " " + e.getCause().getMessage() : "null"));
            e.printStackTrace();
            throw new JsonParseException("JSON解析失败: " + e.getMessage());
        }
    }

    /**
     * 将 JSON 字符串解析为 JsonObject。
     *
     * @param json JSON 字符串
     * @return JsonObject
     * @throws JsonParseException 如果根节点不是对象
     */
    /**
     * parseObject方法。
     * * @param json String类型参数
     *
     * @return JsonObject类型返回值
     */
    public JsonObject parseObject(String json) {
        Object result = fromJSON(json);
        if (result instanceof JsonObject) return (JsonObject) result;
        if (result instanceof JsonArray) {
            throw new JsonParseException("JSON is an array, not an object");
        }
        throw new JsonParseException("Unexpected root type: " + result.getClass().getSimpleName());
    }

    /**
     * 将 JSON 字符串解析为 JsonArray。
     *
     * @param json JSON 字符串
     * @return JsonArray
     * @throws JsonParseException 如果根节点不是数组
     */
    /**
     * parseArray方法。
     * * @param json String类型参数
     *
     * @return JsonArray类型返回值
     */
    public JsonArray parseArray(String json) {
        Object result = fromJSON(json);
        if (result instanceof JsonArray) return (JsonArray) result;
        if (result instanceof JsonObject) {
            throw new JsonParseException("JSON is an object, not an array");
        }
        throw new JsonParseException("Unexpected root type: " + result.getClass().getSimpleName());
    }

    // ==================== G4 加载 ====================

    private String loadG4(String name) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(name);
            if (is == null) throw new JsonParseException("G4文件未找到: " + name);
            byte[] bytes = readAllBytes(is);
            is.close();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (JsonParseException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonParseException("读取G4文件失败: " + name + ", " + e.getMessage());
        }
    }

    /** 等价于 FileUtil.readAllBytes，但避免反向依赖 z-util-core。 */
    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) != -1) baos.write(buf, 0, len);
        return baos.toByteArray();
    }

    // ==================== AST → Java 对象 ====================

    private Object astToJava(ASTNode node) {
        if (node == null) return null;
        String type = node.getType();
        String text = node.getText();
        List<ASTNode> children = node.getChildren();

        // 叶子节点：直接解析标量
        if (children.isEmpty()) {
            return parseScalar(type, text);
        }

        switch (type) {
            case "json":
            case "value":
                // value 层只有一个子节点
                return children.isEmpty() ? null : astToJava(children.get(0));
            case "object":
                return astToObject(node);
            case "array":
                return astToArray(node);
            default:
                // 未知节点类型，尝试单子节点或标量
                if (children.size() == 1) return astToJava(children.get(0));
                if (text != null && !text.isEmpty()) return parseScalar(type, text);
                return children.stream().map(this::astToJava).collect(Collectors.toList());
        }
    }

    private JsonObject astToObject(ASTNode node) {
        JsonObject obj = new JsonObject();
        for (ASTNode child : node.getChildren()) {
            String childType = child.getType();
            if ("pair".equals(childType)) {
                // pair 有两个子节点：key (string) 和 value
                List<ASTNode> pairChildren = child.getChildren();
                Object key = null;
                Object value = null;
                for (ASTNode pairChild : pairChildren) {
                    String pt = pairChild.getType();
                    if ("string".equals(pt)) {
                        key = extractStringValue(pairChild);
                    } else if ("value".equals(pt)) {
                        value = astToJava(pairChild);
                    }
                }
                if (key != null) {
                    obj.put(String.valueOf(key), value);
                }
            }
            // 跳过 LBrace/RBrace 等括号终端节点
        }
        return obj;
    }

    private JsonArray astToArray(ASTNode node) {
        JsonArray arr = new JsonArray();
        for (ASTNode child : node.getChildren()) {
            String childType = child.getType();
            // 跳过 LBracket/RBracket 等括号终端节点，只把实际 value 节点加进数组
            if (isBracketTerminal(childType)) {
                continue;
            }
            arr.add(astToJava(child));
        }
        return arr;
    }

    private boolean isBracketTerminal(String type) {
        return "LBrace".equals(type) || "RBrace".equals(type)
                || "LBracket".equals(type) || "RBracket".equals(type)
                || "Comma".equals(type);
    }

    private String extractStringValue(ASTNode stringNode) {
        List<ASTNode> children = stringNode.getChildren();
        for (ASTNode child : children) {
            if ("StringLiteral".equals(child.getType())) {
                String t = child.getText();
                if (t != null && t.length() >= 2) {
                    // 去掉首尾引号 + 反转义 JSON 转义序列
                    return unescapeJsonString(t.substring(1, t.length() - 1));
                }
            }
        }
        // fallback：直接用 text
        String t = stringNode.getText();
        if (t != null && t.length() >= 2) {
            return unescapeJsonString(t.substring(1, t.length() - 1));
        }
        return t;
    }

    private Object parseScalar(String type, String text) {
        if (text == null || text.trim().isEmpty()) return null;
        text = text.trim();

        switch (type) {
            case "null":
            case "Null":
                return null;
            case "bool":
            case "Bool":
                if ("true".equals(text)) return Boolean.TRUE;
                if ("false".equals(text)) return Boolean.FALSE;
                break;
            case "number":
            case "Number":
                if (text.contains(".") || text.contains("e") || text.contains("E")) {
                    try {
                        return Double.parseDouble(text);
                    } catch (NumberFormatException ignored) {
                    }
                }
                try {
                    long v = Long.parseLong(text);
                    if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) {
                        return (int) v;
                    }
                    return v;
                } catch (NumberFormatException ignored) {
                }
                break;
            case "string":
            case "StringLiteral":
                // 去掉引号 + 反转义 JSON 转义序列
                if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
                    return unescapeJsonString(text.substring(1, text.length() - 1));
                }
                return text;
            case "terminal":
                // 处理各种终结符
                if ("null".equals(text)) return null;
                if ("true".equals(text)) return Boolean.TRUE;
                if ("false".equals(text)) return Boolean.FALSE;
                return text;
            default:
                break;
        }
        return text;
    }

    /**
     * 反转义 JSON 字符串内容。
     * <p>支持标准的 JSON 字符串转义序列（含 Unicode escape 和 UTF-16 代理对）。
     * <p>遇到无法识别的转义序列时保留原样（与多数 JSON 库一致）。
     */
    private String unescapeJsonString(String s) {
        if (s == null || s.indexOf('\\') < 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) {
                sb.append(c);
                i++;
                continue;
            }
            char next = s.charAt(i + 1);
            switch (next) {
                case '"':  sb.append('"');  i += 2; break;
                case '\\': sb.append('\\'); i += 2; break;
                case '/':  sb.append('/');  i += 2; break;
                case 'b':  sb.append('\b'); i += 2; break;
                case 'f':  sb.append('\f'); i += 2; break;
                case 'n':  sb.append('\n'); i += 2; break;
                case 'r':  sb.append('\r'); i += 2; break;
                case 't':  sb.append('\t'); i += 2; break;
                case 'u':
                    if (i + 5 < s.length()) {
                        String hex = s.substring(i + 2, i + 6);
                        int cp;
                        try {
                            cp = Integer.parseInt(hex, 16);
                        } catch (NumberFormatException ex) {
                            sb.append(c).append(next);
                            i += 2;
                            break;
                        }
                        // 高位代理：合并后续的 low surrogate 形成 supplementary code point
                        if (Character.isHighSurrogate((char) cp)
                                && i + 11 < s.length()
                                && s.charAt(i + 6) == '\\' && s.charAt(i + 7) == 'u') {
                            String lowHex = s.substring(i + 8, i + 12);
                            try {
                                int low = Integer.parseInt(lowHex, 16);
                                if (Character.isLowSurrogate((char) low)) {
                                    sb.appendCodePoint(Character.toCodePoint((char) cp, (char) low));
                                    i += 12;
                                    break;
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        sb.append((char) cp);
                        i += 6;
                    } else {
                        // 末尾残缺 unicode escape 不足 4 位：原样保留
                        sb.append(c).append(next);
                        i += 2;
                    }
                    break;
                default:
                    // 未知转义：保留 \ 与下一字符
                    sb.append(c).append(next);
                    i += 2;
            }
        }
        return sb.toString();
    }
}