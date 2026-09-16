package com.zifang.util.parser.proto;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Proto 文件解析器（proto3 子集，纯 JDK 手写）。
 * <p>
 * 支持的特性：
 * <ul>
 *   <li>syntax = "proto3";</li>
 *   <li>package 包名;</li>
 *   <li>import "文件";</li>
 *   <li>message Name { ... }</li>
 *   <li>字段: [repeated] type name = tag;</li>
 *   <li>enum Name { NAME = 0; }</li>
 *   <li>service Name { rpc Method(Request) returns (Response); }</li>
 *   <li>// and Slash-Star comment</li>
 * </ul>
 *
 * @author zifang
 */

/**
 * ProtoParser类。
 */
public class ProtoParser {

    // ==================== Token types ====================

    private List<Token> tokens;

    // ==================== Token ====================
    private int pos = 0;

    // ==================== Lexer ====================

    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return ProtoDocument类型返回值
     */
    public ProtoDocument parse(String content) {
        return parse(new CharSequenceReader(content));
    }

    // ==================== Parser ====================

    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return ProtoDocument类型返回值
     */
    public ProtoDocument parse(Reader reader) {
        try {
            Lexer lexer = new Lexer(reader);
            tokens = lexer.tokenize();
            pos = 0;
            return parseDocument();
        } catch (IOException e) {
            throw new ProtoException("Failed to parse proto", e);
        }
    }

    private Token peek() {
        if (pos < tokens.size()) {
            return tokens.get(pos);
        }
        return new Token(TokenType.EOF, "", 0, 0);
    }

    private Token next() {
        if (pos < tokens.size()) {
            return tokens.get(pos++);
        }
        return new Token(TokenType.EOF, "", 0, 0);
    }

    private boolean match(TokenType... types) {
        Token current = peek();
        for (TokenType type : types) {
            if (current.type == type) {
                return true;
            }
        }
        return false;
    }

    private Token expect(TokenType type) {
        Token current = next();
        if (current.type != type) {
            throw new ProtoException("Expected " + type + " but got " + current.type + " at " + current.line + ":" + current.column);
        }
        return current;
    }

    private ProtoDocument parseDocument() {
        ProtoDocument doc = new ProtoDocument();

        while (!match(TokenType.EOF)) {
            if (match(TokenType.SYNTAX)) {
                doc.setSyntax(parseSyntax());
            } else if (match(TokenType.PACKAGE)) {
                doc.setPackageName(parsePackage());
            } else if (match(TokenType.IMPORT)) {
                doc.addImport(parseImport());
            } else if (match(TokenType.MESSAGE)) {
                doc.addMessage(parseMessage());
            } else if (match(TokenType.SERVICE)) {
                doc.addService(parseService());
            } else if (match(TokenType.ENUM)) {
                // Top-level enum - treat as message with just enum inside
                ProtoMessage msgWithEnum = parseTopLevelEnum();
                // Enum at top level is unusual, but we can add it as a message with enum
                doc.addMessage(msgWithEnum);
            } else {
                throw new ProtoException("Unexpected token: " + peek() + " at " + peek().line + ":" + peek().column);
            }
        }

        return doc;
    }

    private String parseSyntax() {
        expect(TokenType.SYNTAX);
        expect(TokenType.EQ);
        Token str = expect(TokenType.STRING);
        expect(TokenType.SEMICOLON);
        return str.value;
    }

    private String parsePackage() {
        expect(TokenType.PACKAGE);
        StringBuilder sb = new StringBuilder();
        sb.append(expect(TokenType.IDENTIFIER).value);
        // Handle dotted package names like com.example.package
        while (match(TokenType.SEMICOLON) == false && peek().type == TokenType.IDENTIFIER) {
            sb.append(".").append(expect(TokenType.IDENTIFIER).value);
        }
        expect(TokenType.SEMICOLON);
        return sb.toString();
    }

    private String parseImport() {
        expect(TokenType.IMPORT);
        Token str = expect(TokenType.STRING);
        expect(TokenType.SEMICOLON);
        return str.value;
    }

    private ProtoMessage parseMessage() {
        expect(TokenType.MESSAGE);
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.LBRACE);

        ProtoMessage message = new ProtoMessage(name.value);

        while (!match(TokenType.RBRACE) && !match(TokenType.EOF)) {
            if (match(TokenType.ENUM)) {
                message.addEnum(parseEnum());
            } else if (match(TokenType.MESSAGE)) {
                message.addMessage(parseMessage());
            } else if (match(TokenType.RPC)) {
                throw new ProtoException("RPC must be inside service, not message at " + peek().line + ":" + peek().column);
            } else {
                // Field
                message.addField(parseField());
            }
        }

        expect(TokenType.RBRACE);
        return message;
    }

    private ProtoMessage parseTopLevelEnum() {
        expect(TokenType.ENUM);
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.LBRACE);

        ProtoMessage message = new ProtoMessage(name.value + "_enum_wrapper");

        while (!match(TokenType.RBRACE) && !match(TokenType.EOF)) {
            ProtoEnum e = parseEnumBody(name.value);
            message.addEnum(e);
        }

        expect(TokenType.RBRACE);
        return message;
    }

    private ProtoEnum parseEnum() {
        expect(TokenType.ENUM);
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.LBRACE);

        ProtoEnum protoEnum = new ProtoEnum(name.value);

        while (!match(TokenType.RBRACE) && !match(TokenType.EOF)) {
            Token enumName = expect(TokenType.IDENTIFIER);
            expect(TokenType.EQ);
            Token num = expect(TokenType.NUMBER);
            expect(TokenType.SEMICOLON);

            protoEnum.addValue(enumName.value, Integer.parseInt(num.value));
        }

        expect(TokenType.RBRACE);
        return protoEnum;
    }

    private ProtoEnum parseEnumBody(String enumName) {
        ProtoEnum protoEnum = new ProtoEnum(enumName);

        while (!match(TokenType.RBRACE) && !match(TokenType.EOF)) {
            Token name = expect(TokenType.IDENTIFIER);
            expect(TokenType.EQ);
            Token num = expect(TokenType.NUMBER);
            expect(TokenType.SEMICOLON);

            protoEnum.addValue(name.value, Integer.parseInt(num.value));
        }

        return protoEnum;
    }

    private ProtoField parseField() {
        boolean repeated = false;
        if (match(TokenType.REPEATED)) {
            next();
            repeated = true;
        }

        Token type = expect(TokenType.IDENTIFIER);
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.EQ);
        Token tag = expect(TokenType.NUMBER);
        expect(TokenType.SEMICOLON);

        return new ProtoField(type.value, name.value, Integer.parseInt(tag.value), repeated);
    }

    private ProtoService parseService() {
        expect(TokenType.SERVICE);
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.LBRACE);

        ProtoService service = new ProtoService(name.value);

        while (!match(TokenType.RBRACE) && !match(TokenType.EOF)) {
            if (match(TokenType.RPC)) {
                service.addRpc(parseRpc());
            } else {
                // Skip unknown tokens inside service
                throw new ProtoException("Unexpected token in service: " + peek() + " at " + peek().line + ":" + peek().column);
            }
        }

        expect(TokenType.RBRACE);
        return service;
    }

    private ProtoRpc parseRpc() {
        expect(TokenType.RPC);
        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.LPAREN);
        Token inputType = expect(TokenType.IDENTIFIER);
        expect(TokenType.RPAREN);
        expect(TokenType.RETURNS);
        expect(TokenType.LPAREN);
        Token outputType = expect(TokenType.IDENTIFIER);
        expect(TokenType.RPAREN);
        expect(TokenType.SEMICOLON);

        return new ProtoRpc(name.value, inputType.value, outputType.value);
    }

    /**
     * toProto方法。
     * * @param doc ProtoDocument类型参数
     *
     * @return String类型返回值
     */
    public String toProto(ProtoDocument doc) {
        StringBuilder sb = new StringBuilder();

        if (doc.getSyntax() != null) {
            sb.append("syntax = \"").append(doc.getSyntax()).append("\";\n");
        }

        if (doc.getPackageName() != null) {
            sb.append("package ").append(doc.getPackageName()).append(";\n");
        }

        for (String imp : doc.getImports()) {
            sb.append("import \"").append(imp).append("\";\n");
        }

        for (ProtoMessage msg : doc.getMessages()) {
            sb.append(messageToString(msg)).append("\n");
        }

        for (ProtoService svc : doc.getServices()) {
            sb.append(serviceToString(svc)).append("\n");
        }

        return sb.toString();
    }

    private String messageToString(ProtoMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("message ").append(msg.getName()).append(" {");

        for (ProtoEnum e : msg.getEnums()) {
            sb.append(" enum ").append(e.getName()).append(" {");
            for (java.util.Map.Entry<String, Integer> entry : e.getValues().entrySet()) {
                sb.append(" ").append(entry.getKey()).append(" = ").append(entry.getValue()).append(";");
            }
            sb.append(" }");
        }

        for (ProtoMessage nested : msg.getMessages()) {
            sb.append(" ").append(messageToString(nested));
        }

        for (ProtoField f : msg.getFields()) {
            sb.append(" ");
            if (f.isRepeated()) {
                sb.append("repeated ");
            }
            sb.append(f.getType()).append(" ").append(f.getName()).append(" = ").append(f.getTag()).append(";");
        }

        sb.append(" }");
        return sb.toString();
    }

    private String serviceToString(ProtoService svc) {
        StringBuilder sb = new StringBuilder();
        sb.append("service ").append(svc.getName()).append(" {");

        for (ProtoRpc rpc : svc.getRpcs()) {
            sb.append(" rpc ").append(rpc.getName()).append("(")
                    .append(rpc.getInputType()).append(") returns (")
                    .append(rpc.getOutputType()).append(");");
        }

        sb.append(" }");
        return sb.toString();
    }

    // Round-trip: convert ProtoDocument back to proto string

    private enum TokenType {
        SYNTAX, PACKAGE, IMPORT,
        MESSAGE, ENUM, SERVICE, RPC,
        REPEATED, RETURNS,
        IDENTIFIER, STRING,
        LBRACE, RBRACE, LPAREN, RPAREN,
        SEMICOLON, EQ, LT, GT,
        NUMBER,
        COMMENT, WHITESPACE,
        EOF
    }

    private static class Token {
        final TokenType type;
        final String value;
        final int line;
        final int column;

        Token(TokenType type, String value, int line, int column) {
            this.type = type;
            this.value = value;
            this.line = line;
            this.column = column;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "Token{" + type + ", '" + value + "', " + line + ":" + column + "}";
        }
    }

    private static class Lexer {
        private final Reader reader;
        private final List<Token> tokens = new ArrayList<>();
        private int pos = 0;
        private int line = 1;
        private int column = 1;
        private int charBuffer = -1;

        Lexer(Reader reader) {
            this.reader = reader;
        }

        private int read() throws IOException {
            if (charBuffer != -1) {
                int c = charBuffer;
                charBuffer = -1;
                return c;
            }
            int c = reader.read();
            if (c == '\n') {
                line++;
                column = 1;
            } else if (c != -1) {
                column++;
            }
            pos++;
            return c;
        }

        private void unread(int c) {
            charBuffer = c;
            pos--;
            if (c == '\n') {
                line--;
            } else {
                column--;
            }
        }

        private boolean isAlpha(int c) {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
        }

        private boolean isDigit(int c) {
            return c >= '0' && c <= '9';
        }

        private boolean isWhitespace(int c) {
            return c == ' ' || c == '\t' || c == '\n' || c == '\r';
        }

        private void nextToken() throws IOException {
            int c = read();

            if (c == -1) {
                tokens.add(new Token(TokenType.EOF, "", line, column));
                return;
            }

            // Whitespace
            if (isWhitespace(c)) {
                StringBuilder sb = new StringBuilder();
                sb.append((char) c);
                while (true) {
                    c = read();
                    if (c == -1 || !isWhitespace(c)) {
                        if (c != -1) unread(c);
                        break;
                    }
                    sb.append((char) c);
                }
                tokens.add(new Token(TokenType.WHITESPACE, sb.toString(), line, column));
                return;
            }

            // Single-line comment
            if (c == '/') {
                int next = read();
                if (next == '/') {
                    StringBuilder sb = new StringBuilder();
                    sb.append("//");
                    while (true) {
                        c = read();
                        if (c == -1 || c == '\n') {
                            if (c != -1) unread(c);
                            break;
                        }
                        sb.append((char) c);
                    }
                    tokens.add(new Token(TokenType.COMMENT, sb.toString(), line, column));
                    return;
                } else if (next == '*') {
                    StringBuilder sb = new StringBuilder();
                    sb.append("/*");
                    while (true) {
                        c = read();
                        if (c == -1) {
                            break;
                        }
                        sb.append((char) c);
                        if (c == '*') {
                            int nextChar = read();
                            if (nextChar == '/') {
                                sb.append('/');
                                break;
                            } else {
                                unread(nextChar);
                            }
                        }
                    }
                    tokens.add(new Token(TokenType.COMMENT, sb.toString(), line, column));
                    return;
                } else {
                    unread(next);
                    tokens.add(new Token(TokenType.SEMICOLON, "/", line, column));
                    return;
                }
            }

            // String
            if (c == '"') {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    c = read();
                    if (c == -1) {
                        throw new ProtoException("Unterminated string at line " + line);
                    }
                    if (c == '"') {
                        break;
                    }
                    if (c == '\\') {
                        int next = read();
                        if (next == -1) {
                            throw new ProtoException("Unterminated string escape at line " + line);
                        }
                        switch (next) {
                            case 'n':
                                sb.append('\n');
                                break;
                            case 't':
                                sb.append('\t');
                                break;
                            case 'r':
                                sb.append('\r');
                                break;
                            case '\\':
                                sb.append('\\');
                                break;
                            case '"':
                                sb.append('"');
                                break;
                            default:
                                sb.append((char) next);
                                break;
                        }
                    } else {
                        sb.append((char) c);
                    }
                }
                tokens.add(new Token(TokenType.STRING, sb.toString(), line, column));
                return;
            }

            // Braces and parens
            if (c == '{') {
                tokens.add(new Token(TokenType.LBRACE, "{", line, column));
                return;
            }
            if (c == '}') {
                tokens.add(new Token(TokenType.RBRACE, "}", line, column));
                return;
            }
            if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", line, column));
                return;
            }
            if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", line, column));
                return;
            }
            if (c == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, ";", line, column));
                return;
            }
            if (c == '=') {
                tokens.add(new Token(TokenType.EQ, "=", line, column));
                return;
            }

            // Number
            if (isDigit(c) || (c == '-' && peekNextDigit())) {
                StringBuilder sb = new StringBuilder();
                if (c == '-') {
                    sb.append('-');
                    c = read();
                }
                while (isDigit(c)) {
                    sb.append((char) c);
                    c = read();
                }
                unread(c);
                tokens.add(new Token(TokenType.NUMBER, sb.toString(), line, column));
                return;
            }

            // Identifier or keyword
            if (isAlpha(c)) {
                StringBuilder sb = new StringBuilder();
                sb.append((char) c);
                while (true) {
                    c = read();
                    if (c == -1 || !isAlpha(c) && !isDigit(c)) {
                        if (c != -1) unread(c);
                        break;
                    }
                    sb.append((char) c);
                }
                String ident = sb.toString();
                switch (ident) {
                    case "syntax":
                        tokens.add(new Token(TokenType.SYNTAX, ident, line, column));
                        break;
                    case "package":
                        tokens.add(new Token(TokenType.PACKAGE, ident, line, column));
                        break;
                    case "import":
                        tokens.add(new Token(TokenType.IMPORT, ident, line, column));
                        break;
                    case "message":
                        tokens.add(new Token(TokenType.MESSAGE, ident, line, column));
                        break;
                    case "enum":
                        tokens.add(new Token(TokenType.ENUM, ident, line, column));
                        break;
                    case "service":
                        tokens.add(new Token(TokenType.SERVICE, ident, line, column));
                        break;
                    case "rpc":
                        tokens.add(new Token(TokenType.RPC, ident, line, column));
                        break;
                    case "repeated":
                        tokens.add(new Token(TokenType.REPEATED, ident, line, column));
                        break;
                    case "returns":
                        tokens.add(new Token(TokenType.RETURNS, ident, line, column));
                        break;
                    default:
                        tokens.add(new Token(TokenType.IDENTIFIER, ident, line, column));
                        break;
                }
                return;
            }

            // Skip unknown characters
            // (should not happen in valid proto)
        }

        private boolean peekNextDigit() throws IOException {
            int c = read();
            unread(c);
            return isDigit(c);
        }

        List<Token> tokenize() throws IOException {
            while (true) {
                nextToken();
                Token last = tokens.get(tokens.size() - 1);
                if (last.type == TokenType.EOF) {
                    break;
                }
            }
            // Remove whitespace and comment tokens
            List<Token> meaningful = new ArrayList<>();
            for (Token t : tokens) {
                if (t.type != TokenType.WHITESPACE && t.type != TokenType.COMMENT) {
                    meaningful.add(t);
                }
            }
            return meaningful;
        }
    }

    /**
     * 用于将 CharSequence 转换为 Reader 的内部类。
     */
    private static class CharSequenceReader extends Reader {
        private final CharSequence charSequence;
        private int position = 0;

        CharSequenceReader(CharSequence charSequence) {
            this.charSequence = charSequence;
        }

        @Override
        /**
         * read方法。
         *      * @param cbuf char[]类型参数
         * @param off int类型参数
         * @param len int类型参数
         * @return int类型返回值
         */
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (position >= charSequence.length()) {
                return -1;
            }
            int count = 0;
            int end = Math.min(charSequence.length(), position + len);
            for (int i = position; i < end; i++) {
                cbuf[off + count] = charSequence.charAt(i);
                count++;
            }
            position += count;
            return count;
        }

        @Override
        /**
         * close方法。
         */
        public void close() throws IOException {
        }
    }
}
