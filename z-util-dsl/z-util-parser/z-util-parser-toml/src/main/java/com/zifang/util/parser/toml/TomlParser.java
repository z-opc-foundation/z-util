package com.zifang.util.parser.toml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TOML v1.0.0 解析器（子集实现）
 */

/**
 * TomlParser类。
 */
public class TomlParser {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("[+-]?[0-9_]+(?:_[0-9_]+)*");
    private static final Pattern FLOAT_PATTERN = Pattern.compile("[+-]?(?:[0-9_]+(?:_[0-9_]+)*)?\\.[0-9_]+(?:_[0-9_]+)*");
    private static final Pattern INF_PATTERN = Pattern.compile("[+-]?inf", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAN_PATTERN = Pattern.compile("[+-]?nan", Pattern.CASE_INSENSITIVE);
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    private String content;
    private int pos;
    private int line;
    private int column;
    private TomlDocument document;
    private TomlDocument.TomlTable currentTable;

    /**
     * TomlParser方法。
     */
    public TomlParser() {
    }

    /**
     * 解析 TOML 格式字符串
     */
    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return TomlDocument类型返回值
     */
    public TomlDocument parse(String content) {
        return parse(new StringReader(content));
    }

    /**
     * 解析 TOML 格式 Reader
     */
    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return TomlDocument类型返回值
     */
    public TomlDocument parse(Reader reader) {
        try {
            BufferedReader br = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return parseContent(sb.toString());
        } catch (IOException e) {
            throw new TomlException("读取 TOML 内容失败", e);
        }
    }

    private TomlDocument parseContent(String content) {
        this.content = content;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.document = new TomlDocument();
        TomlDocument.TomlTable rootTable = new TomlDocument.TomlTable("");
        this.document.addTable("", rootTable);
        this.currentTable = rootTable;

        skipWhitespaceAndComments();

        while (pos < content.length()) {
            if (matchChar('[')) {
                parseTableOrArrayOfTables();
            } else {
                parseKeyValue();
            }
            skipWhitespaceAndComments();
        }

        return document;
    }

    private void skipWhitespaceAndComments() {
        while (pos < content.length()) {
            char c = content.charAt(pos);
            if (c == ' ' || c == '\t') {
                pos++;
                column++;
            } else if (c == '#') {
                skipToEndOfLine();
            } else if (c == '\n') {
                pos++;
                line++;
                column = 1;
            } else if (c == '\r') {
                pos++;
                column++;
            } else {
                break;
            }
        }
    }

    private void skipToEndOfLine() {
        while (pos < content.length() && content.charAt(pos) != '\n') {
            pos++;
        }
    }

    private boolean matchChar(char expected) {
        if (pos < content.length() && content.charAt(pos) == expected) {
            pos++;
            column++;
            return true;
        }
        return false;
    }

    private char peekChar() {
        if (pos < content.length()) {
            return content.charAt(pos);
        }
        return '\0';
    }

    private char nextChar() {
        if (pos < content.length()) {
            char c = content.charAt(pos);
            pos++;
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            return c;
        }
        return '\0';
    }

    private void parseTableOrArrayOfTables() {
        boolean isArray = matchChar('[');
        String tablePath = parseTableName();
        expectChar(']');
        if (isArray) {
            expectChar(']');
            parseArrayOfTables(tablePath);
        } else {
            parseTable(tablePath);
        }
    }

    private String parseTableName() {
        StringBuilder sb = new StringBuilder();
        skipWhitespace();
        while (pos < content.length()) {
            char c = content.charAt(pos);
            if (c == ']') {
                break;
            }
            if (c == '\n') {
                throw new TomlException("行 " + line + ": 表名未闭合，缺少 ']'");
            }
            sb.append(c);
            pos++;
            column++;
        }
        return sb.toString().trim();
    }

    private void parseTable(String tablePath) {
        String[] parts = tablePath.split("\\.");
        TomlDocument.TomlTable table = document.getTables().isEmpty()
                ? new TomlDocument.TomlTable(parts[0])
                : new TomlDocument.TomlTable(parts[parts.length - 1]);

        if (parts.length == 1) {
            document.addTable(parts[0], table);
            currentTable = table;
        } else {
            // 构建嵌套表结构
            StringBuilder pathBuilder = new StringBuilder();
            TomlDocument.TomlTable parent = null;
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                if (part.isEmpty()) {
                    throw new TomlException("行 " + line + ": 表名不能为空");
                }
                if (i > 0) {
                    pathBuilder.append(".");
                }
                pathBuilder.append(part);

                TomlDocument.TomlTable existing = document.getTable(pathBuilder.toString());
                if (existing != null) {
                    parent = existing;
                } else {
                    TomlDocument.TomlTable newTable = new TomlDocument.TomlTable(part);
                    if (parent != null) {
                        parent.addSubTable(part, newTable);
                    } else {
                        document.addTable(part, newTable);
                    }
                    parent = newTable;
                }
            }
            currentTable = parent;
        }
    }

    private void parseArrayOfTables(String tablePath) {
        String[] parts = tablePath.split("\\.");
        TomlDocument.TomlTable table = new TomlDocument.TomlTable(parts[parts.length - 1]);

        if (parts.length == 1) {
            // 顶级数组 of tables
            document.addArrayOfTables(table);
            currentTable = table;
        } else {
            // 嵌套数组 of tables - 需要找到父表
            // 先查找是否已经存在于 tables 中
            StringBuilder pathBuilder = new StringBuilder();
            TomlDocument.TomlTable parent = null;

            // 先检查 tables 中是否有父表
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) {
                    pathBuilder.append(".");
                }
                pathBuilder.append(parts[i]);
            }
            parent = document.getTable(pathBuilder.toString());

            // 如果 tables 中没有，尝试从 arrayOfTables 中找最后一个
            if (parent == null) {
                String parentName = parts[0];
                List<TomlDocument.TomlTable> arrTables = document.getArrayOfTables();
                if (!arrTables.isEmpty() && arrTables.get(arrTables.size() - 1).getName().equals(parentName)) {
                    parent = arrTables.get(arrTables.size() - 1);
                }
            }

            if (parent != null) {
                // 构建完整的嵌套路径
                TomlDocument.TomlTable current = parent;
                for (int i = 1; i < parts.length - 1; i++) {
                    TomlDocument.TomlTable sub = current.getSubTable(parts[i]);
                    if (sub == null) {
                        sub = new TomlDocument.TomlTable(parts[i]);
                        current.addSubTable(parts[i], sub);
                    }
                    current = sub;
                }
                // 最后一个部分作为子表数组添加
                TomlDocument.TomlTable finalTable = new TomlDocument.TomlTable(parts[parts.length - 1]);
                current.addSubTableArray(finalTable);
                currentTable = finalTable;
            } else {
                // 父表不存在，创建一个虚拟父表结构
                StringBuilder newPath = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    if (i > 0) {
                        newPath.append(".");
                    }
                    newPath.append(parts[i]);
                }
                // 解析这个路径的父表
                TomlDocument.TomlTable newParent = new TomlDocument.TomlTable(parts[0]);
                document.addTable(parts[0], newParent);

                TomlDocument.TomlTable current = newParent;
                for (int i = 1; i < parts.length - 1; i++) {
                    TomlDocument.TomlTable sub = new TomlDocument.TomlTable(parts[i]);
                    current.addSubTable(parts[i], sub);
                    current = sub;
                }
                TomlDocument.TomlTable finalTable = new TomlDocument.TomlTable(parts[parts.length - 1]);
                current.addSubTableArray(finalTable);
                currentTable = finalTable;
            }
        }
    }

    private void parseKeyValue() {
        String key = parseKey();
        skipWhitespace();
        expectChar('=');
        skipWhitespace();
        Object value = parseValue();
        currentTable.put(key, value);
    }

    private String parseKey() {
        StringBuilder sb = new StringBuilder();
        while (pos < content.length()) {
            char c = content.charAt(pos);
            if (c == '=' || c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                break;
            }
            // 支持 bare key 和 dotted key
            if (c == '.' && sb.length() > 0) {
                // dotted key - 暂时收集，后续处理
                sb.append(c);
                pos++;
                column++;
            } else if (isBareKeyChar(c)) {
                sb.append(c);
                pos++;
                column++;
            } else {
                break;
            }
        }
        return sb.toString().trim();
    }

    private boolean isBareKeyChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9') || c == '_' || c == '-';
    }

    private Object parseValue() {
        char c = peekChar();
        if (c == '"') {
            return parseString();
        } else if (c == '\'') {
            return parseLiteralString();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '{') {
            return parseInlineTable();
        } else if (c == 't' || c == 'T' || c == 'f' || c == 'F') {
            return parseBoolean();
        } else if (c == '+' || c == '-' || (c >= '0' && c <= '9')) {
            return parseNumber();
        } else {
            throw new TomlException("行 " + line + ": 未知的值类型 '" + c + "'");
        }
    }

    private String parseString() {
        pos++;
        column++;
        char quoteStyle = '"';
        boolean isMultiLine = false;

        // 检查多行字符串
        if (pos + 1 < content.length() && content.charAt(pos) == '"' && content.charAt(pos + 1) == '"') {
            isMultiLine = true;
            pos += 2;
            column += 2;
        }

        StringBuilder sb = new StringBuilder();

        if (isMultiLine) {
            // 多行字符串
            while (pos < content.length()) {
                if (content.charAt(pos) == '"' && pos + 2 < content.length() &&
                        content.charAt(pos + 1) == '"' && content.charAt(pos + 2) == '"') {
                    pos += 3;
                    column += 3;
                    break;
                }
                char ch = nextChar();
                if (ch == '\\') {
                    ch = nextChar();
                    switch (ch) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case '"':
                            sb.append('"');
                            break;
                        default:
                            sb.append(ch);
                            break;
                    }
                } else {
                    sb.append(ch);
                }
            }
        } else {
            // 单行字符串
            while (pos < content.length()) {
                char ch = content.charAt(pos);
                if (ch == '"') {
                    pos++;
                    column++;
                    break;
                }
                if (ch == '\n') {
                    throw new TomlException("行 " + line + ": 字符串未闭合");
                }
                if (ch == '\\') {
                    pos++;
                    column++;
                    ch = nextChar();
                    switch (ch) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case '"':
                            sb.append('"');
                            break;
                        case 'u':
                        case 'x':
                            sb.append(parseUnicodeEscape());
                            break;
                        default:
                            sb.append(ch);
                            break;
                    }
                } else {
                    sb.append(ch);
                    pos++;
                    column++;
                }
            }
        }

        return sb.toString();
    }

    private String parseUnicodeEscape() {
        if (pos + 4 > content.length()) {
            throw new TomlException("行 " + line + ": Unicode 转义序列不完整");
        }
        String hex = content.substring(pos, pos + 4);
        pos += 4;
        column += 4;
        try {
            return String.valueOf((char) Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            throw new TomlException("行 " + line + ": 无效的 Unicode 转义序列 \\u005Cu" + hex);
        }
    }

    private String parseLiteralString() {
        pos++;
        column++;
        char quoteStyle = '\'';

        StringBuilder sb = new StringBuilder();
        while (pos < content.length()) {
            char ch = content.charAt(pos);
            if (ch == '\'') {
                pos++;
                column++;
                break;
            }
            if (ch == '\n') {
                throw new TomlException("行 " + line + ": 字符串未闭合");
            }
            sb.append(ch);
            pos++;
            column++;
        }
        return sb.toString();
    }

    private List<Object> parseArray() {
        pos++;
        column++;
        List<Object> array = new ArrayList<>();

        skipWhitespaceAndComments();
        while (pos < content.length() && content.charAt(pos) != ']') {
            skipWhitespaceAndComments();
            if (content.charAt(pos) == ']') {
                break;
            }
            Object value = parseValue();
            array.add(value);
            skipWhitespaceAndComments();
            if (content.charAt(pos) == ',') {
                pos++;
                column++;
                skipWhitespaceAndComments();
            }
        }
        expectChar(']');
        return array;
    }

    private TomlDocument.TomlTable parseInlineTable() {
        pos++;
        column++;
        TomlDocument.TomlTable table = new TomlDocument.TomlTable();

        skipWhitespace();
        while (pos < content.length() && content.charAt(pos) != '}') {
            skipWhitespace();
            if (content.charAt(pos) == '}') {
                break;
            }
            String key = parseKey();
            skipWhitespace();
            expectChar('=');
            skipWhitespace();
            Object value = parseValue();
            table.put(key, value);
            skipWhitespace();
            if (content.charAt(pos) == ',') {
                pos++;
                column++;
            }
            skipWhitespace();
        }
        expectChar('}');
        return table;
    }

    private Object parseNumber() {
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;

        // 符号
        if (peekChar() == '+' || peekChar() == '-') {
            sb.append(nextChar());
        }

        // 整数部分
        while (pos < content.length() && (Character.isDigit(content.charAt(pos)) || content.charAt(pos) == '_')) {
            if (content.charAt(pos) != '_') {
                sb.append(content.charAt(pos));
            }
            pos++;
            column++;
        }

        // 小数点
        if (pos < content.length() && content.charAt(pos) == '.') {
            isFloat = true;
            sb.append(nextChar());
            while (pos < content.length() && (Character.isDigit(content.charAt(pos)) || content.charAt(pos) == '_')) {
                if (content.charAt(pos) != '_') {
                    sb.append(content.charAt(pos));
                }
                pos++;
                column++;
            }
        }

        // 指数部分
        if (pos < content.length() && (content.charAt(pos) == 'e' || content.charAt(pos) == 'E')) {
            isFloat = true;
            sb.append(nextChar());
            if (pos < content.length() && (content.charAt(pos) == '+' || content.charAt(pos) == '-')) {
                sb.append(nextChar());
            }
            while (pos < content.length() && (Character.isDigit(content.charAt(pos)) || content.charAt(pos) == '_')) {
                if (content.charAt(pos) != '_') {
                    sb.append(content.charAt(pos));
                }
                pos++;
                column++;
            }
        }

        String numStr = sb.toString();
        if (isFloat) {
            try {
                return Double.parseDouble(numStr);
            } catch (NumberFormatException e) {
                throw new TomlException("行 " + line + ": 无效的浮点数 '" + numStr + "'");
            }
        } else {
            try {
                return Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                throw new TomlException("行 " + line + ": 无效的整数 '" + numStr + "'");
            }
        }
    }

    private Boolean parseBoolean() {
        if (pos + 4 <= content.length()) {
            String word = content.substring(pos, pos + 4).toLowerCase();
            if ("true".equals(word)) {
                pos += 4;
                column += 4;
                return true;
            }
        }
        if (pos + 5 <= content.length()) {
            String word = content.substring(pos, pos + 5).toLowerCase();
            if ("false".equals(word)) {
                pos += 5;
                column += 5;
                return false;
            }
        }
        throw new TomlException("行 " + line + ": 无效的布尔值");
    }

    private void expectChar(char expected) {
        if (pos >= content.length()) {
            throw new TomlException("行 " + line + ": 缺少 '" + expected + "'");
        }
        char c = content.charAt(pos);
        if (c != expected) {
            throw new TomlException("行 " + line + ": 期望 '" + expected + "' 但遇到 '" + c + "'");
        }
        pos++;
        column++;
    }

    private void skipWhitespace() {
        while (pos < content.length() && (content.charAt(pos) == ' ' || content.charAt(pos) == '\t')) {
            pos++;
            column++;
        }
    }

    /**
     * 将 TomlDocument 写回 TOML 格式字符串
     */
    /**
     * store方法。
     * * @param doc TomlDocument类型参数
     *
     * @return String类型返回值
     */
    public String store(TomlDocument doc) {
        StringBuilder sb = new StringBuilder();

        for (TomlDocument.TomlTable table : doc.getArrayOfTables()) {
            sb.append("[[").append(table.getName()).append("]]\n");
            storeTable(table, sb);
        }

        for (java.util.Map.Entry<String, TomlDocument.TomlTable> entry : doc.getTables().entrySet()) {
            if (!entry.getKey().isEmpty()) {
                sb.append("[").append(entry.getKey()).append("]\n");
            }
            storeTable(entry.getValue(), sb);
        }

        return sb.toString();
    }

    private void storeTable(TomlDocument.TomlTable table, StringBuilder sb) {
        for (java.util.Map.Entry<String, Object> entry : table.getValues().entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(valueToString(entry.getValue())).append("\n");
        }
        for (TomlDocument.TomlTable sub : table.getSubTables().values()) {
            sb.append("[").append(sub.getPath()).append("]\n");
            storeTable(sub, sb);
        }
        for (TomlDocument.TomlTable arr : table.getSubTableArrays()) {
            sb.append("[[").append(arr.getPath()).append("]]\n");
            storeTable(arr, sb);
        }
    }

    private String valueToString(Object value) {
        if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(valueToString(list.get(i)));
                if (i < list.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        } else if (value instanceof TomlDocument.TomlTable) {
            TomlDocument.TomlTable table = (TomlDocument.TomlTable) value;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (java.util.Map.Entry<String, Object> entry : table.getValues().entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                sb.append(entry.getKey()).append(" = ").append(valueToString(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        } else if (value instanceof Double) {
            return value.toString();
        } else if (value instanceof Long) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        }
        return "\"" + value.toString() + "\"";
    }

    private String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
