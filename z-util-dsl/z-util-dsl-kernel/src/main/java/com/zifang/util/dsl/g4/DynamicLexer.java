package com.zifang.util.dsl.g4;

import com.zifang.util.dsl.core.Lexer;
import com.zifang.util.dsl.core.TokenReader;
import com.zifang.util.dsl.g4.model.G4Rule;
import com.zifang.util.dsl.g4.model.TokenDefinition;
import com.zifang.util.dsl.token.SimpleToken;
import com.zifang.util.dsl.token.Token;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态词法分析器
 * 根据.g4文件动态生成词法分析器
 * <p>
 * 支持的G4词法规则格式:
 * - 单引号包围的字面量: 'abc' 表示精确匹配 "abc"
 * - 字符类: [a-z] [^abc] - 字符类以[开头，支持^取反
 * - G4取反: ~[a-z] 表示不在a-z中的字符 (转换为 [^a-z])
 * - 组合: 空格分隔表示连接, | 表示或
 * - 后缀: * + ? 表示重复
 * - 括号: (expr) 用于分组
 * - 点号: . 表示任意字符
 */
public class DynamicLexer implements Lexer {

    private static final String ANY_CHAR_PLACEHOLDER = "\u0001ANYCHAR\u0001";
    private final List<TokenDefinition> tokenDefinitions;
    private final Map<String, Pattern> compiledPatterns;
    // Token类型映射
    private final Map<String, Integer> tokenTypeMap;
    // Fragment规则（不直接产生Token）
    private final Map<String, String> fragmentRules;
    // HIDDEN channel的token名称集合
    private final Set<String> hiddenTokenNames;
    private String input;
    private char[] chars;
    private int pos;
    private int line;
    private int column;
    private int nextTypeId;
    // 解析结果
    private List<Token> tokens;
    // 是否保留空白（关闭词法层对 ' '、'\t'、'\r' 的预跳过，默认 false 保持向后兼容）
    private boolean preserveWhitespace;

    /**
     * DynamicLexer方法。
     */
    public DynamicLexer() {
        this.tokenDefinitions = new ArrayList<>();
        this.compiledPatterns = new LinkedHashMap<>();
        this.tokenTypeMap = new HashMap<>();
        this.fragmentRules = new HashMap<>();
        this.hiddenTokenNames = new HashSet<>();
        this.nextTypeId = 1;
    }

    /**
     * 加载G4文件并初始化
     */
    public void loadG4(String g4Content) {
        // 解析G4文件
        List<G4Rule> rules = G4FileParser.extractRules(g4Content);

        // 先收集所有fragment规则
        for (G4Rule rule : rules) {
            if (rule.isFragment()) {
                fragmentRules.put(rule.getName(), rule.getBody());
            }
        }

        // 处理规则
        for (G4Rule rule : rules) {
            if (rule.isFragment()) {
                // Fragment规则不产生Token
                continue;
            }
            // 词法分析器只处理 LEXER 规则，跳过 PARSER 规则
            if (rule.getType() != G4Rule.RuleType.LEXER) {
                continue;
            }

            // 特殊处理：空模式 (例如 EOF: ;) 视为文件结束标记，用一个永远不在
            // 输入中匹配成功的哨兵正则（(?!)），让 longest-match 走完后由 nextToken 主动发射
            String rawBody = rule.getBody().trim();
            String pattern;
            if (rawBody.isEmpty()) {
                pattern = "(?!)";
            } else {
                pattern = convertToRegex(rawBody);
            }

            TokenDefinition def = new TokenDefinition(
                    rule.getName(),
                    pattern,
                    tokenDefinitions.size()
            );
            def.setFragment(false);
            def.setHidden(rule.isHidden());
            if (rule.isHidden()) {
                hiddenTokenNames.add(rule.getName());
            }
            tokenDefinitions.add(def);

            // 编译正则表达式
            try {
                compiledPatterns.put(rule.getName(), Pattern.compile(pattern));
            } catch (Exception e) {
                throw new RuntimeException("Failed to compile pattern for rule: " + rule.getName() + ", pattern: " + pattern, e);
            }

            tokenTypeMap.put(rule.getName(), nextTypeId++);
        }
    }

    /**
     * 从文件加载G4
     */
    public void loadG4File(String filePath) {
        try {
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
            loadG4(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load G4 file: " + filePath, e);
        }
    }

    /**
     * 将G4规则体转换为Java正则表达式
     */
    private String convertToRegex(String body) {
        String regex = body.trim();
        String result = convertExpr(regex).result;
        // G4中 . 匹配任意字符包括\n，但Java regex的.默认不匹配\n
        // 用 ANY_CHAR 替换不在字符类内的 .
        result = replaceDotOutsideCharClass(result);
        // ANY_CHAR -> 任意字符（包括换行符）
        // 用 [^\0] 匹配除NULL外的所有字符（等价于G4的.）
        char NULL = '\0';
        return result.replace(ANY_CHAR_PLACEHOLDER, "[^" + NULL + "]");
    }

    /**
     * 替换 . 为占位符，但跳过字符类 [...] 内的 .（那里是字面量）
     */
    private String replaceDotOutsideCharClass(String s) {
        StringBuilder sb = new StringBuilder();
        boolean inCharClass = false;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i + 1 < s.length()) {
                // 转义序列，原样保留
                sb.append(ch);
                sb.append(s.charAt(++i));
            } else if (ch == '[') {
                inCharClass = true;
                sb.append(ch);
            } else if (ch == ']') {
                inCharClass = false;
                sb.append(ch);
            } else if (ch == '.' && !inCharClass) {
                // G4的.匹配任意字符，包括换行符
                sb.append(ANY_CHAR_PLACEHOLDER);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * 递归解析G4表达式
     */
    private ConvertResult convertExpr(String s) {
        return convertExpr(s, 0);
    }

    private ConvertResult convertExpr(String s, int start) {
        StringBuilder result = new StringBuilder();
        int i = start;

        while (i < s.length()) {
            char ch = s.charAt(i);

            if (ch == '\'') {
                // 字面量
                int end = findMatchingQuote(s, i + 1, s.length());
                if (end == -1) throw new RuntimeException("Unclosed string literal at " + i);
                String literal = s.substring(i + 1, end);
                result.append(escapeRegex(literal));
                i = end + 1;
            } else if (ch == '[') {
                // 字符类: [abc] 或 [^abc] 或 ~[abc]
                int end = findMatchingBracket(s, i);
                if (end == -1) throw new RuntimeException("Unclosed bracket at " + i);
                String charClass = s.substring(i, end + 1);
                result.append(convertCharClass(charClass));
                i = end + 1;
            } else if (ch == '(') {
                int end = findMatchingParen(s, i);
                if (end == -1) throw new RuntimeException("Unclosed paren at " + i);
                String inner = s.substring(i + 1, end);
                // 处理后缀
                int suffixEnd = end + 1;
                while (suffixEnd < s.length() && "*+?".indexOf(s.charAt(suffixEnd)) >= 0) {
                    suffixEnd++;
                }
                String suffix = s.substring(end + 1, suffixEnd);

                result.append("(?:");
                result.append(convertExpr(inner, 0).result);
                result.append(")");
                result.append(suffix);
                i = suffixEnd;
            } else if (ch == '|') {
                result.append("|");
                i++;
            } else if (ch == '*' || ch == '+' || ch == '?') {
                result.append(ch);
                i++;
            } else if (ch == '~') {
                // G4取反: ~[abc] -> [^abc]
                i++;
                while (i < s.length() && s.charAt(i) == ' ') i++; // skip ws
                if (i < s.length() && s.charAt(i) == '[') {
                    int end = findMatchingBracket(s, i);
                    if (end == -1) throw new RuntimeException("Unclosed bracket after ~ at " + i);
                    String charClass = s.substring(i, end + 1);
                    // charClass like ~["\\r\n]
                    String inner = charClass.substring(1, charClass.length() - 1); // strip ~[ and ]
                    result.append("[^").append(escapeCharClass(inner)).append("]");
                    i = end + 1;
                } else {
                    throw new RuntimeException("~ must be followed by [ at " + i);
                }
            } else if (ch == '.') {
                result.append(".");
                i++;
            } else if (Character.isLetterOrDigit(ch) || ch == '_') {
                int start2 = i;
                while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_')) {
                    i++;
                }
                String ref = s.substring(start2, i);
                if (fragmentRules.containsKey(ref)) {
                    result.append("(?:");
                    result.append(convertExpr(fragmentRules.get(ref), 0).result);
                    result.append(")");
                }
                // else: unknown ref, pass through (might be parser token ref in combined grammar)
            } else if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                i++;
            } else {
                result.append(ch);
                i++;
            }
        }

        return new ConvertResult(result.toString(), i);
    }

    /**
     * 转换G4字符类为Java正则
     * 处理: [abc], [a-z], [^abc], [\t\r\n] 等
     */
    private String convertCharClass(String charClass) {
        // charClass is like [abc] or [^abc]
        if (charClass.length() < 2) return charClass;

        boolean negated = false;
        String inner;

        if (charClass.charAt(1) == '^') {
            negated = true;
            inner = charClass.substring(2, charClass.length() - 1);
        } else {
            inner = charClass.substring(1, charClass.length() - 1);
        }

        String escaped = escapeCharClass(inner);
        return negated ? "[^" + escaped + "]" : "[" + escaped + "]";
    }

    /**
     * 转义字符类内容 (用于Java正则的字符类[]内)
     * G4字符类中需要转义的字符: ] \ ^ - (在某些位置)
     */
    private String escapeCharClass(String inner) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char ch = inner.charAt(i);
            if (ch == '\\' && i + 1 < inner.length()) {
                // G4转义序列 -> 转为真实字符
                // Java regex在字符类[]中: \t=tab, \r=CR, \n=LF, \\=backslash
                char next = inner.charAt(i + 1);
                if (next == 't') {
                    sb.append('\t');
                    i++;
                } else if (next == 'r') {
                    sb.append('\r');
                    i++;
                } else if (next == 'n') {
                    sb.append('\n');
                    i++;
                } else if (next == '\\') {
                    sb.append("\\\\");
                    i++;
                } else if (next == '"') {
                    sb.append('"');
                    i++;
                } else if (next == '\'') {
                    sb.append('\'');
                    i++;
                } else if (next == '[') {
                    sb.append('[');
                    i++;
                } else if (next == ']') {
                    sb.append(']');
                    i++;
                } else if (next == '^') {
                    sb.append('^');
                    i++;
                } else if (next == '-') {
                    sb.append('-');
                    i++;
                } else {
                    sb.append(ch);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private int findMatchingQuote(String s, int start, int maxPos) {
        for (int i = start; i < maxPos; i++) {
            if (s.charAt(i) == '\\' && i + 1 < s.length()) {
                i++; // skip escaped char
            } else if (s.charAt(i) == '\'') {
                return i;
            }
        }
        return -1;
    }

    private int findMatchingBracket(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '\\' && i + 1 < s.length()) {
                i++;
                continue;
            }
            if (s.charAt(i) == '[') depth++;
            else if (s.charAt(i) == ']') {
                if (depth == 1) return i;
                if (depth > 0) depth--;
            }
        }
        return -1;
    }

    private int findMatchingParen(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '\\' && i + 1 < s.length()) {
                i++;
                continue;
            }
            if (s.charAt(i) == '\'') {
                int end = findMatchingQuote(s, i + 1, s.length());
                if (end != -1) i = end;
                continue;
            }
            if (s.charAt(i) == '[') {
                int end = findMatchingBracket(s, i);
                if (end != -1) i = end;
                continue;
            }
            if (s.charAt(i) == '(') depth++;
            else if (s.charAt(i) == ')') {
                if (depth == 0) return i;  // unmatched ), error
                depth--;
                if (depth == 0) return i;  // just closed the outermost paren
            }
        }
        return -1;
    }

    private String escapeRegex(String literal) {
        // 先处理G4转义序列，再转义正则特殊字符
        String processed = unescapeG4(literal);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < processed.length(); i++) {
            char ch = processed.charAt(i);
            if ("\\^$.|?*+()[]{}-".indexOf(ch) >= 0) {
                sb.append('\\');
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * 将G4转义序列还原为真实字符
     * G4中: \\ -> \  \t -> tab  \r -> CR  \n -> LF  \" -> "  \' -> '
     */
    private String unescapeG4(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                if (next == 't') {
                    sb.append('\t');
                    i++;
                } else if (next == 'r') {
                    sb.append('\r');
                    i++;
                } else if (next == 'n') {
                    sb.append('\n');
                    i++;
                } else if (next == '\\') {
                    sb.append('\\');
                    i++;
                } else if (next == '"') {
                    sb.append('"');
                    i++;
                } else if (next == '\'') {
                    sb.append('\'');
                    i++;
                } else {
                    sb.append(ch);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    @Override
    /**
     * setInput方法。
     *      * @param input String类型参数
     */
    public void setInput(String input) {
        this.input = input;
        this.chars = input.toCharArray();
        this.pos = 0;
        this.line = 1;
        this.column = 1;
    }

    /**
     * 设置是否在词法阶段预跳过空白字符（' '、'\t'、'\r'）。
     * <p>
     * 默认 false：行为与历史版本一致，词法层在每次取 token 前会跳过空白，
     * 适合 JSON / YAML / INI / Properties 等以行为单位的格式。
     * <p>
     * 设置为 true：关闭预跳过，由 G4 词法规则自行决定如何处理空白，
     * 适合需要保留文本中原始空白（如 XML 文本节点前后空格）的场景。
     *
     * @param preserve 是否保留空白
     */
    public void setPreserveWhitespace(boolean preserve) {
        this.preserveWhitespace = preserve;
    }

    /**
     * 当前是否启用保留空白模式。
     *
     * @return 是否在词法层保留空白
     */
    public boolean isPreserveWhitespace() {
        return preserveWhitespace;
    }

    @Override
    /**
     * setInput方法。
     *      * @param input char[]类型参数
     */
    public void setInput(char[] input) {
        this.chars = input;
        this.input = new String(input);
        this.pos = 0;
        this.line = 1;
        this.column = 1;
    }

    @Override
    /**
     * tokenize方法。
     * @return List<Token>类型返回值
     */
    public List<Token> tokenize() {
        tokens = new ArrayList<>();
        pos = 0;
        line = 1;
        column = 1;

        while (pos < chars.length) {
            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }
        // 输入末尾再调一次 nextToken，让它有机会发射 EOF 等哨兵令牌
        while (true) {
            Token token = nextToken();
            if (token == null) break;
            tokens.add(token);
            // 如果是 EOF，nextToken 会一直发射同一个 EOF，循环会爆。
            // 哨兵 EOF 的 text 为空且不再推进 pos，所以最多发一次即可。
            break;
        }

        return tokens;
    }

    /**
     * 获取下一个Token
     */
    private Token nextToken() {
        // 跳过空白字符（不包括换行符 LF，换行符留给词法规则匹配）。
        // 当调用方开启 preserveWhitespace 时（如 XML），跳过该循环以便 G4
        // 词法规则捕获完整的文本内容（含前后空格）。
        if (!preserveWhitespace) {
            while (pos < chars.length && isWhitespace(chars[pos])) {
                if (chars[pos] == '\n') {
                    // 永远不会进来：isWhitespace 不再匹配 \n
                    line++;
                    column = 1;
                } else {
                    column++;
                }
                pos++;
            }
        }

        if (pos >= chars.length) {
            // 输入末尾：发射 EOF（如果声明了 EOF 规则），否则返回 null
            if (tokenTypeMap.containsKey("EOF")) {
                SimpleToken token = new SimpleToken();
                token.setType(tokenTypeMap.get("EOF"));
                token.setText("");
                token.setLine(line);
                token.setColumn(column);
                token.setTokenName("EOF");
                return token;
            }
            return null;
        }

        // 尝试所有Token定义，找最长匹配
        String bestMatch = null;
        String bestTokenType = null;
        int bestEndPos = pos;
        int bestLine = line;
        int bestColumn = column;

        for (Map.Entry<String, Pattern> entry : compiledPatterns.entrySet()) {
            String tokenType = entry.getKey();
            Pattern pattern = entry.getValue();

            // 从当前位置匹配
            Matcher matcher = pattern.matcher(input.substring(pos));
            if (matcher.lookingAt()) {
                String matched = matcher.group();
                // 找最长匹配；长度相等时保留先出现的规则（保持keyword优先于同长度Identifier）
                if (bestMatch == null || matched.length() > bestMatch.length()) {
                    bestMatch = matched;
                    bestTokenType = tokenType;
                    bestEndPos = pos + matched.length();
                }
            }
        }

        if (bestMatch != null) {
            // 创建Token
            SimpleToken token = new SimpleToken();
            token.setType(tokenTypeMap.get(bestTokenType));
            token.setText(bestMatch);
            token.setLine(bestLine);
            token.setColumn(bestColumn);
            token.setTokenName(bestTokenType);

            // 移动位置
            for (int i = 0; i < bestMatch.length(); i++) {
                if (bestMatch.charAt(i) == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
            }
            pos = bestEndPos;

            // HIDDEN channel的token不加入结果
            if (hiddenTokenNames.contains(bestTokenType)) {
                return nextToken();
            }

            return token;
        }

        // 无法识别，报告错误
        throw new RuntimeException("Unexpected character: '" + chars[pos] + "' (code=" + (int) chars[pos] + ") at line " + line + ", column " + column);
    }

    private boolean isWhitespace(char ch) {
        // 只跳过空格、Tab 和回车符（CR），不跳过换行符（LF）。
        // 换行符是 G4 语法中常见的 token（如 NL、NEWLINE），必须留给词法规则去匹配。
        return ch == ' ' || ch == '\t' || ch == '\r';
    }

    @Override
    /**
     * getTokenReader方法。
     * @return TokenReader类型返回值
     */
    public TokenReader getTokenReader() {
        if (tokens == null) {
            tokenize();
        }
        return new SimpleTokenReader(tokens);
    }

    /**
     * 表达式转换结果
     */
    private static class ConvertResult {
        String result;
        int pos;

        ConvertResult(String r, int p) {
            result = r;
            pos = p;
        }
    }

    /**
     * 简单的Token读取器实现
     */
    private static class SimpleTokenReader implements TokenReader {
        private final List<Token> tokens;
        private int index;

        /**
         * SimpleTokenReader方法。
         * * @param tokens ListToken类型参数
         */
        public SimpleTokenReader(List<Token> tokens) {
            this.tokens = tokens;
            this.index = 0;
        }

        @Override
        /**
         * read方法。
         * @return Token类型返回值
         */
        public Token read() {
            if (index < tokens.size()) {
                return tokens.get(index++);
            }
            return null;
        }

        @Override
        /**
         * peek方法。
         * @return Token类型返回值
         */
        public Token peek() {
            if (index < tokens.size()) {
                return tokens.get(index);
            }
            return null;
        }

        @Override
        /**
         * advance方法。
         */
        public void advance() {
            if (index < tokens.size()) {
                index++;
            }
        }

        @Override
        /**
         * get方法。
         *      * @param offset int类型参数
         * @return Token类型返回值
         */
        public Token get(int offset) {
            int idx = index + offset;
            if (idx >= 0 && idx < tokens.size()) {
                return tokens.get(idx);
            }
            return null;
        }

        @Override
        /**
         * hasNext方法。
         * @return boolean类型返回值
         */
        public boolean hasNext() {
            return index < tokens.size();
        }

        @Override
        /**
         * reset方法。
         */
        public void reset() {
            index = 0;
        }

        @Override
        /**
         * close方法。
         */
        public void close() {
            // nothing to close
        }
    }
}