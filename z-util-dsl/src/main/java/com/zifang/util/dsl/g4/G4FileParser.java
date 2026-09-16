package com.zifang.util.dsl.g4;

import com.zifang.util.dsl.g4.model.G4File;
import com.zifang.util.dsl.g4.model.G4Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * G4 file parser
 * Parses .g4 files, extracts lexer and parser rules
 * <p>
 * G4 format:
 * - lexer grammar Name;
 * - parser grammar Name;
 * - ruleName : body ;
 * - fragment ruleName : body ;
 * - supports -> channel(HIDDEN) actions
 */
public class G4FileParser {

    /**
     * Parse .g4 content
     */
    public static G4File parse(String g4Content) {
        G4File g4File = new G4File();
        g4File.setGrammarName(extractGrammarName(g4Content));
        List<G4Rule> rules = extractRules(g4Content);

        return g4File;
    }

    /**
     * Extract all rules from .g4 content
     * Uses character-by-character parsing instead of regex to avoid comment handling issues
     */
    public static List<G4Rule> extractRules(String g4Content) {
        List<G4Rule> rules = new ArrayList<>();

        String lexerName = extractGrammarName(g4Content, "lexer");
        String parserName = extractGrammarName(g4Content, "parser");

        // Remove block comments to avoid /**/ interfering with rule parsing
        g4Content = stripBlockComments(g4Content);

        boolean inLexer = false;
        boolean inParser = false;

        // 支持多行规则：累积非空非注释行直到遇到 `;`，合并为单行再解析
        StringBuilder buffer = new StringBuilder();

        String[] lines = g4Content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("lexer grammar")) {
                inLexer = true;
                inParser = false;
                continue;
            } else if (trimmed.startsWith("parser grammar")) {
                inLexer = false;
                inParser = true;
                continue;
            }

            // Skip empty lines, single-line comments, /* lines
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                continue;
            }

            // Skip G4 顶层声明（options { ... }、tokens { ... }、channels { ... } 等），它们不是规则定义
            if (trimmed.startsWith("options") || trimmed.startsWith("tokens") || trimmed.startsWith("channels")) {
                continue;
            }

            // Skip ANTLR4 注解（@header { ... }、@members { ... }、@parser::context 等）
            if (trimmed.startsWith("@")) {
                continue;
            }

            // Append current line to buffer
            if (buffer.length() > 0) {
                buffer.append(' ');
            }
            buffer.append(trimmed);

            // If buffer has a top-level `;`, treat the buffer as one rule
            // findSemicolon 跟踪括号/字符串，能正确判断 `;` 是否在顶层（不在括号内/字符串里）
            String buffered = buffer.toString();
            int semiPos = findSemicolon(buffered, 0);
            if (semiPos >= 0) {
                G4Rule rule = parseRuleLine(buffered, inLexer ? G4Rule.RuleType.LEXER : G4Rule.RuleType.PARSER);
                if (rule != null) {
                    rules.add(rule);
                }
                buffer.setLength(0);
            }
        }

        return rules;
    }

    /**
     * Parse a single rule line
     */
    private static G4Rule parseRuleLine(String line, G4Rule.RuleType defaultType) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*")) {
            return null;
        }

        line = removeLineComment(line);

        boolean isFragment = false;
        String name = null;
        String body = null;

        if (line.startsWith("fragment")) {
            isFragment = true;
            line = line.substring(8).trim();
        }

        int colonIdx = line.indexOf(':');
        int colon2Idx = line.indexOf("::=");

        int sepIdx = -1;
        if (colon2Idx >= 0 && (colonIdx < 0 || colon2Idx < colonIdx)) {
            sepIdx = colon2Idx;
        } else if (colonIdx >= 0) {
            sepIdx = colonIdx;
        }

        if (sepIdx < 0) {
            return null;
        }

        name = line.substring(0, sepIdx).trim();

        int semiIdx = findSemicolon(line, sepIdx);
        if (semiIdx < 0) {
            return null;
        }

        body = line.substring(sepIdx + (colon2Idx >= 0 && colon2Idx == sepIdx ? 2 : 1), semiIdx).trim();

        boolean isHidden = false;
        int actionIdx = body.indexOf("->");
        if (actionIdx >= 0) {
            String action = body.substring(actionIdx + 2).trim();
            if (action.contains("HIDDEN")) {
                isHidden = true;
            }
            body = body.substring(0, actionIdx).trim();
        }

        G4Rule.RuleType type = defaultType;

        G4Rule rule = new G4Rule(name, type, body, isFragment);
        rule.setHidden(isHidden);
        return rule;
    }

    /**
     * Remove line-end comments (not inside strings)
     */
    private static String removeLineComment(String line) {
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < line.length() - 1; i++) {
            char ch = line.charAt(i);

            if (!inString && (ch == '\'' || ch == '"')) {
                inString = true;
                stringChar = ch;
            } else if (inString && ch == '\\' && i + 1 < line.length()) {
                i++; // skip escaped char
            } else if (inString && ch == stringChar) {
                inString = false;
            } else if (!inString && ch == '/' && line.charAt(i + 1) == '/') {
                String before = line.substring(0, i).trim();
                return before.isEmpty() ? "" : before;
            }
        }

        return line;
    }

    /**
     * Remove block comments slash-star ... star-slash
     * Uses byte-level scanning to handle multi-byte characters correctly.
     * A star-slash inside [] char class is NOT a block comment end marker.
     */
    private static String stripBlockComments(String content) {
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int bracketDepth = 0;

        while (i < bytes.length - 1) {
            char ch = (char) bytes[i];

            if (ch == '\'' || ch == '"') {
                char quote = ch;
                sb.append(ch);
                i++;
                while (i < bytes.length) {
                    char c = (char) bytes[i];
                    sb.append(c);
                    if (c == '\\' && i + 1 < bytes.length) {
                        i++;
                        sb.append((char) bytes[i]);
                    } else if (c == quote) {
                        break;
                    }
                    i++;
                }
                i++;
                continue;
            }

            if (ch == '[') bracketDepth++;
            else if (ch == ']') bracketDepth = Math.max(0, bracketDepth - 1);

            // Block comment start - but not inside char class
            if (bytes[i] == '/' && i + 1 < bytes.length
                    && bytes[i + 1] == '*'
                    && bracketDepth == 0) {
                int end = findByteSequence(bytes, i + 2, new byte[]{(byte) '*', (byte) '/'});
                if (end >= 0) {
                    i = end + 2;
                    continue;
                }
            }

            sb.append(ch);
            i++;
        }
        if (i < bytes.length) sb.append((char) bytes[i]);
        return sb.toString();
    }

    /**
     * Find needle pattern in byte array starting from index. Returns start index or -1.
     */
    private static int findByteSequence(byte[] haystack, int start, byte[] needle) {
        outer:
        for (int i = start; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    /**
     * Find semicolon from given position (accounts for strings, char classes, paren nesting)
     */
    private static int findSemicolon(String s, int start) {
        int depth = 0;
        boolean inString = false;
        char stringChar = 0;
        boolean inCharClass = false;

        for (int i = start; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (!inString && !inCharClass && (ch == '\'' || ch == '"')) {
                inString = true;
                stringChar = ch;
            } else if (inString && ch == '\\' && i + 1 < s.length()) {
                i++;
            } else if (inString && ch == stringChar) {
                inString = false;
            } else if (!inString && !inCharClass && ch == '[') {
                inCharClass = true;
            } else if (!inString && inCharClass && ch == '\\' && i + 1 < s.length()) {
                i++;
            } else if (!inString && inCharClass && ch == ']') {
                inCharClass = false;
            } else if (!inString && !inCharClass) {
                if (ch == '(' || ch == '{') {
                    depth++;
                } else if (ch == ')' || ch == '}') {
                    depth--;
                } else if (ch == ';' && depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Extract grammar name of given type
     */
    private static String extractGrammarName(String content, String type) {
        Pattern pattern = Pattern.compile(
                type + "\\s+grammar\\s+(\\w+)\\s*;",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Unknown";
    }

    /**
     * Extract grammar name (auto-detect lexer or parser)
     */
    private static String extractGrammarName(String content) {
        Pattern pattern = Pattern.compile(
                "(?:lexer|parser)\\s+grammar\\s+(\\w+)\\s*;",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Unknown";
    }

    /**
     * Load and parse from file
     */
    public static G4File parseFromFile(String filePath) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            String content = new String(java.nio.file.Files.readAllBytes(path));
            return parse(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse G4 file: " + filePath, e);
        }
    }
}