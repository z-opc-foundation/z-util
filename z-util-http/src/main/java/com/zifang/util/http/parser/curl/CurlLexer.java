package com.zifang.util.http.parser.curl;

import java.util.ArrayList;
import java.util.List;

/**
 * cURL 命令词法分析器
 * 将 cURL 命令文本分割为 token 列表
 */
public class CurlLexer {

    /**
     * 将 cURL 命令词法分析为 token 列表
     *
     * @param command cURL 命令文本
     * @return Token 列表
     */
    public static List<Token> tokenize(String command) {
        List<Token> tokens = new ArrayList<>();

        if (command == null || command.trim().isEmpty()) {
            return tokens;
        }

        // 标准化换行符并去除首尾空白
        command = command.replaceAll("\\r\\n?", "\n").trim();

        int i = 0;
        int length = command.length();

        while (i < length) {
            char ch = command.charAt(i);

            // 跳过空白字符（包括行继续符 \\n）
            if (Character.isWhitespace(ch) || (ch == '\\' && i + 1 < length && command.charAt(i + 1) == '\n')) {
                if (ch == '\\' && i + 1 < length && command.charAt(i + 1) == '\n') {
                    i += 2; // 跳过 \\n
                } else {
                    i++;
                }
                continue;
            }

            // 处理引号字符串
            if (ch == '"' || ch == '\'') {
                Token token = parseQuotedString(command, i);
                if (token != null) {
                    tokens.add(token);
                    i = token.position + token.value.length() + 2; // +2 为引号
                    continue;
                }
            }

            // 处理选项
            if (ch == '-') {
                if (i + 1 < length && command.charAt(i + 1) == '-') {
                    // 长选项 --xxx
                    Token token = parseLongOption(command, i);
                    tokens.add(token);
                    i = token.position + token.value.length();
                } else {
                    // 短选项 -x
                    Token token = parseShortOption(command, i);
                    tokens.add(token);
                    i = token.position + token.value.length();
                }
                continue;
            }

            // 处理普通值
            Token token = parseValue(command, i);
            if (token != null) {
                tokens.add(token);
                i = token.position + token.value.length();
            } else {
                i++;
            }
        }

        return classifyTokens(tokens);
    }

    /**
     * 解析引号字符串
     */
    private static Token parseQuotedString(String command, int start) {
        char quote = command.charAt(start);
        int i = start + 1;
        int length = command.length();
        StringBuilder sb = new StringBuilder();

        while (i < length) {
            char ch = command.charAt(i);
            if (ch == '\\' && i + 1 < length) {
                char next = command.charAt(i + 1);
                if (next == quote || next == '\\' || next == 'n' || next == 't' || next == 'r') {
                    if (next == 'n') sb.append('\n');
                    else if (next == 't') sb.append('\t');
                    else if (next == 'r') sb.append('\r');
                    else sb.append(next);
                    i += 2;
                    continue;
                }
            }
            if (ch == quote) {
                return new Token(TokenType.VALUE, sb.toString(), start);
            }
            sb.append(ch);
            i++;
        }

        return null; // 未闭合的引号
    }

    /**
     * 解析短选项
     */
    private static Token parseShortOption(String command, int start) {
        int i = start + 1;
        int length = command.length();

        // 跳过开头的 -
        while (i < length && command.charAt(i) == '-') {
            i++;
        }

        // 读取选项字母
        while (i < length && Character.isLetterOrDigit(command.charAt(i))) {
            i++;
        }

        String option = command.substring(start, i);
        return new Token(TokenType.OPTION_SHORT, option, start);
    }

    /**
     * 解析长选项
     */
    private static Token parseLongOption(String command, int start) {
        int i = start + 2; // 跳过 --
        int length = command.length();

        while (i < length && (Character.isLetterOrDigit(command.charAt(i)) || command.charAt(i) == '-')) {
            i++;
        }

        String option = command.substring(start, i);
        return new Token(TokenType.OPTION_LONG, option, start);
    }

    /**
     * 解析普通值
     */
    private static Token parseValue(String command, int start) {
        int i = start;
        int length = command.length();

        while (i < length) {
            char ch = command.charAt(i);
            if (Character.isWhitespace(ch) || ch == '-' && (i + 1 < length && command.charAt(i + 1) == '-')) {
                break;
            }
            if (ch == '\\' && i + 1 < length && command.charAt(i + 1) == '\n') {
                break;
            }
            i++;
        }

        if (i > start) {
            String value = command.substring(start, i);
            return new Token(TokenType.VALUE, value, start);
        }

        return null;
    }

    /**
     * 对 token 进行分类
     * 将某些 VALUE token 重新分类为其他类型
     */
    private static List<Token> classifyTokens(List<Token> tokens) {
        List<Token> classified = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.type == TokenType.VALUE) {
                // 检查是否是 URL
                if (token.value.startsWith("http://") || token.value.startsWith("https://")) {
                    classified.add(token);
                    continue;
                }

                // 检查是否是命令（第一个 token）
                if (i == 0 && token.value.equals("curl")) {
                    classified.add(new Token(TokenType.COMMAND, token.value, token.position));
                    continue;
                }
            }

            classified.add(token);
        }

        return classified;
    }

    /**
     * Token 类型
     */
    public enum TokenType {
        COMMAND,        // curl 命令本身
        OPTION_SHORT,   // 短选项，如 -X, -H
        OPTION_LONG,    // 长选项，如 --data, --header
        VALUE,          // 值（非选项参数）
        EQUALS          // = 符号
    }

    /**
     * Token
     */
    public static class Token {
        public final TokenType type;
        public final String value;
        public final int position;

        /**
         * Token方法。
         * * @param type TokenType类型参数
         *
         * @param value    String类型参数
         * @param position int类型参数
         */
        public Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return String.format("Token(%s, '%s', %d)", type, value, position);
        }
    }

}
