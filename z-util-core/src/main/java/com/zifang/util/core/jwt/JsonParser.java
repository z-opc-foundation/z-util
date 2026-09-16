package com.zifang.util.core.jwt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 极简 JSON 解析器（仅支持 JWT claims 需要的子集：object/array/string/number/boolean/null）。
 * <p>
 * 不引 jackson/gson/fastjson 这种三方。
 * 状态机手写，约 100 行。
 */
final class JsonParser {

    private final String s;
    private int pos;

    private JsonParser(String s) {
        this.s = s;
    }

    static Object parse(String input) {
        JsonParser p = new JsonParser(input.trim());
        p.skipWs();
        Object v = p.parseValue();
        p.skipWs();
        if (p.pos < p.s.length()) {
            throw new IllegalArgumentException("trailing chars at " + p.pos);
        }
        return v;
    }

    private Object parseValue() {
        skipWs();
        if (pos >= s.length()) throw new IllegalArgumentException("unexpected EOF");
        char c = s.charAt(pos);
        switch (c) {
            case '{':
                return parseObject();
            case '[':
                return parseArray();
            case '"':
                return parseString();
            case 't':
            case 'f':
                return parseBool();
            case 'n':
                return parseNull();
            default:
                if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();
                throw new IllegalArgumentException("unexpected char at " + pos + ": " + c);
        }
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWs();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWs();
            String key = parseString();
            skipWs();
            expect(':');
            Object value = parseValue();
            map.put(key, value);
            skipWs();
            char ch = peek();
            if (ch == ',') {
                pos++;
                continue;
            }
            if (ch == '}') {
                pos++;
                return map;
            }
            throw new IllegalArgumentException("expected , or } at " + pos);
        }
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> list = new ArrayList<>();
        skipWs();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            list.add(parseValue());
            skipWs();
            char ch = peek();
            if (ch == ',') {
                pos++;
                continue;
            }
            if (ch == ']') {
                pos++;
                return list;
            }
            throw new IllegalArgumentException("expected , or ] at " + pos);
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < s.length()) {
            char c = s.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (pos >= s.length()) throw new IllegalArgumentException("bad escape");
                char esc = s.charAt(pos++);
                switch (esc) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'u':
                        if (pos + 4 > s.length()) throw new IllegalArgumentException("bad \\u005Cu");
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default:
                        throw new IllegalArgumentException("bad escape: " + esc);
                }
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("unterminated string");
    }

    private Object parseNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < s.length() && "0123456789.eE+-".indexOf(s.charAt(pos)) >= 0) pos++;
        String num = s.substring(start, pos);
        if (num.contains(".") || num.contains("e") || num.contains("E")) {
            return Double.parseDouble(num);
        }
        long l = Long.parseLong(num);
        if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
        return l;
    }

    private Boolean parseBool() {
        if (s.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        }
        if (s.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("expected true/false at " + pos);
    }

    private Object parseNull() {
        if (s.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new IllegalArgumentException("expected null at " + pos);
    }

    private void skipWs() {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
    }

    private void expect(char c) {
        if (pos >= s.length() || s.charAt(pos) != c) {
            throw new IllegalArgumentException("expected '" + c + "' at " + pos);
        }
        pos++;
    }

    private char peek() {
        return pos >= s.length() ? '\0' : s.charAt(pos);
    }
}
