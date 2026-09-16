package com.zifang.util.expr.sql.expression;

import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.SqlFunctionDef;
import com.zifang.util.expr.sql.SqlFunctionRegistry;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * SQL 表达式求值器。
 * 采用两阶段架构：
 * 1. 解析阶段（parse）→ 返回 AST（延迟求值节点）
 * 2. 求值阶段（eval）→ 对 AST 做行级求值
 * <p>
 * 这样 column ref 在解析时保留为 AST 节点，在求值时才从 row 中取值。
 */
public class ExpressionEvaluator {

    private final SqlFunctionRegistry registry;

    public ExpressionEvaluator() {
        this.registry = SqlFunctionRegistry.get();
    }

    private static Object doArith(Object a, String op, Object b) {
        if (a == null || b == null) return null;
        double av = toDoubleStatic(a), bv = toDoubleStatic(b);
        switch (op) {
            case "+":
                return av + bv;
            case "-":
                return av - bv;
            case "*":
                return av * bv;
            case "/":
                return bv == 0 ? null : av / bv;
            case "%":
                return bv == 0 ? null : av % bv;
        }
        return null;
    }

    private static Object staticDoArith(Object a, String op, Object b) {
        return doArith(a, op, b);
    }

    // -------------------------------------------------------------------------
    // 公开 API
    // -------------------------------------------------------------------------

    private static Object doCompare(Object a, String op, Object b) {
        if (a == null && b == null) return "=".equals(op) || "==".equals(op) ? Boolean.TRUE : Boolean.FALSE;
        if (a == null || b == null) return "!=".equals(op) || "<>".equals(op) ? Boolean.TRUE : Boolean.FALSE;
        if ("=".equals(op) || "==".equals(op)) return Objects.equals(a, b) ? Boolean.TRUE : Boolean.FALSE;
        if ("<>".equals(op) || "!=".equals(op)) return !Objects.equals(a, b) ? Boolean.TRUE : Boolean.FALSE;
        try {
            double av = toDoubleStatic(a), bv = toDoubleStatic(b);
            switch (op) {
                case "<":
                    return av < bv ? Boolean.TRUE : Boolean.FALSE;
                case ">":
                    return av > bv ? Boolean.TRUE : Boolean.FALSE;
                case "<=":
                    return av <= bv ? Boolean.TRUE : Boolean.FALSE;
                case ">=":
                    return av >= bv ? Boolean.TRUE : Boolean.FALSE;
                case "LIKE":
                    return matchLikeStatic(a.toString(), b.toString()) ? Boolean.TRUE : Boolean.FALSE;
            }
        } catch (NumberFormatException e) {
            int cmp = a.toString().compareTo(b.toString());
            switch (op) {
                case "<":
                    return cmp < 0 ? Boolean.TRUE : Boolean.FALSE;
                case ">":
                    return cmp > 0 ? Boolean.TRUE : Boolean.FALSE;
                case "<=":
                    return cmp <= 0 ? Boolean.TRUE : Boolean.FALSE;
                case ">=":
                    return cmp >= 0 ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    private static Object staticDoCompare(Object a, String op, Object b) {
        return doCompare(a, op, b);
    }

    private static boolean matchLikeStatic(String text, String pattern) {
        String regex = pattern.replace(".", "\\.").replace("%", ".*").replace("_", ".");
        return Pattern.matches(regex, text);
    }

    // -------------------------------------------------------------------------
    // 解析阶段
    // -------------------------------------------------------------------------

    private static boolean toBoolStatic(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).doubleValue() != 0;
        String s = v.toString().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "t".equals(s);
    }

    // -------------------------------------------------------------------------
    // AST 节点层次
    // -------------------------------------------------------------------------

    private static double toDoubleStatic(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            return Double.parseDouble(v.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    public ExpressionEvaluator register(String name, SqlFunctionRegistry.SqlUdf udf) {
        registry.register(name, udf);
        return this;
    }

    public ExpressionEvaluator registerBuiltin() {
        registry.registerBuiltin();
        return this;
    }

    /**
     * 常量折叠：优化表达式中的常量部分。
     * 仅当表达式不包含列引用时才求值，否则返回 AST。
     */
    public Object optimize(String expr) {
        Node ast = parse(expr);
        if (!containsColumnRef(ast)) {
            return eval(ast, Collections.emptyMap());
        }
        return ast;
    }

    /**
     * 行级求值。
     */
    public Object evaluate(String expr, Map<String, Object> row) {
        Node ast = parse(expr);
        return eval(ast, row);
    }

    /**
     * 提取列引用。
     */
    public Set<String> extractColumns(String expr) {
        Set<String> cols = new HashSet<>();
        collectColumns(parse(expr), cols);
        return cols;
    }

    private Node parse(String expr) {
        try {
            Tokenizer t = new Tokenizer(expr.trim());
            Token[] tokens = t.tokenize();
            if (tokens.length == 0) return new ConstNode(null);
            Parser p = new Parser(tokens);
            return p.parseExpr();
        } catch (Exception e) {
            throw new SqlException("Failed to parse expression: " + expr, e);
        }
    }

    private boolean containsColumnRef(Node n) {
        if (n == null) return false;
        if (n instanceof ColNode) return true;
        if (n instanceof ConstNode) return false;
        if (n instanceof StrNode) return false;
        if (n instanceof FuncNode) {
            for (Node arg : ((FuncNode) n).args) if (containsColumnRef(arg)) return true;
            return false;
        }
        if (n instanceof CompareNode) {
            CompareNode c = (CompareNode) n;
            return containsColumnRef(c.left) || containsColumnRef(c.right);
        }
        if (n instanceof ArithNode) {
            ArithNode a = (ArithNode) n;
            return containsColumnRef(a.left) || containsColumnRef(a.right);
        }
        if (n instanceof AndNode) {
            AndNode a = (AndNode) n;
            return containsColumnRef(a.left) || containsColumnRef(a.right);
        }
        if (n instanceof OrNode) {
            OrNode o = (OrNode) n;
            return containsColumnRef(o.left) || containsColumnRef(o.right);
        }
        if (n instanceof NotNode) return containsColumnRef(((NotNode) n).child);
        if (n instanceof NegNode) return containsColumnRef(((NegNode) n).child);
        return false;
    }

    private void collectColumns(Node n, Set<String> cols) {
        if (n == null) return;
        if (n instanceof ColNode) cols.add(((ColNode) n).name);
        else if (n instanceof FuncNode) for (Node arg : ((FuncNode) n).args) collectColumns(arg, cols);
        else if (n instanceof CompareNode) {
            CompareNode c = (CompareNode) n;
            collectColumns(c.left, cols);
            collectColumns(c.right, cols);
        } else if (n instanceof ArithNode) {
            ArithNode a = (ArithNode) n;
            collectColumns(a.left, cols);
            collectColumns(a.right, cols);
        } else if (n instanceof AndNode) {
            AndNode a = (AndNode) n;
            collectColumns(a.left, cols);
            collectColumns(a.right, cols);
        } else if (n instanceof OrNode) {
            OrNode o = (OrNode) n;
            collectColumns(o.left, cols);
            collectColumns(o.right, cols);
        } else if (n instanceof NotNode) collectColumns(((NotNode) n).child, cols);
        else if (n instanceof NegNode) collectColumns(((NegNode) n).child, cols);
    }

    private Object eval(Node n, Map<String, Object> row) {
        return n == null ? null : n.eval(row);
    }

    enum TokenType {COL, NUM, STR, BOOL, NULL_LIT, OP, LPAREN, RPAREN, COMMA, FUNC, EOF}

    // -------------------------------------------------------------------------
    // 辅助方法
    // -------------------------------------------------------------------------

    abstract static class Node {
        abstract Object eval(Map<String, Object> row);
    }

    static class ConstNode extends Node {
        final Object value;

        ConstNode(Object v) {
            this.value = v;
        }

        Object eval(Map<String, Object> row) {
            return value;
        }

        @Override
        public String toString() {
            return "Const(" + value + ")";
        }
    }

    /**
     * 列引用
     */
    static class ColNode extends Node {
        final String name;

        ColNode(String n) {
            this.name = n;
        }

        Object eval(Map<String, Object> row) {
            if (row.containsKey(name)) return row.get(name);
            if (row.containsKey(name.toLowerCase())) return row.get(name.toLowerCase());
            if (row.containsKey(name.toUpperCase())) return row.get(name.toUpperCase());
            return null;
        }

        @Override
        public String toString() {
            return "Col(" + name + ")";
        }
    }

    /**
     * 函数调用
     */
    static class FuncNode extends Node {
        final String name;
        final Node[] args;

        FuncNode(String n, Node[] a) {
            this.name = n;
            this.args = a;
        }

        Object eval(Map<String, Object> row) {
            SqlFunctionDef def = SqlFunctionRegistry.get().find(name);
            if (def == null) throw new SqlException("Unknown function: " + name);
            Object[] resolvedArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) resolvedArgs[i] = args[i].eval(row);
            return def.exec(row, resolvedArgs);
        }

        @Override
        public String toString() {
            return name + "(" + Arrays.toString(args) + ")";
        }
    }

    /**
     * 字符串字面量
     */
    static class StrNode extends Node {
        final String value;

        StrNode(String v) {
            this.value = v;
        }

        Object eval(Map<String, Object> row) {
            return value;
        }

        @Override
        public String toString() {
            return "Str('" + value + "')";
        }
    }

    /**
     * 比较运算
     */
    static class CompareNode extends Node {
        final Node left, right;
        final String op; // = <> != < > <= >= LIKE

        CompareNode(Node l, String o, Node r) {
            this.left = l;
            this.op = o;
            this.right = r;
        }

        Object eval(Map<String, Object> row) {
            Object a = left.eval(row), b = right.eval(row);
            return staticDoCompare(a, op, b);
        }

        @Override
        public String toString() {
            return left + " " + op + " " + right;
        }
    }

    /**
     * 算术运算
     */
    static class ArithNode extends Node {
        final Node left, right;
        final String op; // + - * / %

        ArithNode(Node l, String o, Node r) {
            this.left = l;
            this.op = o;
            this.right = r;
        }

        Object eval(Map<String, Object> row) {
            Object a = left.eval(row), b = right.eval(row);
            if (a == null || b == null) return null;
            return staticDoArith(a, op, b);
        }

        @Override
        public String toString() {
            return left + " " + op + " " + right;
        }
    }

    /**
     * 逻辑 AND
     */
    static class AndNode extends Node {
        final Node left, right;

        AndNode(Node l, Node r) {
            this.left = l;
            this.right = r;
        }

        Object eval(Map<String, Object> row) {
            return toBoolStatic(left.eval(row)) && toBoolStatic(right.eval(row)) ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        public String toString() {
            return left + " AND " + right;
        }
    }

    /**
     * 逻辑 OR
     */
    static class OrNode extends Node {
        final Node left, right;

        OrNode(Node l, Node r) {
            this.left = l;
            this.right = r;
        }

        Object eval(Map<String, Object> row) {
            return toBoolStatic(left.eval(row)) || toBoolStatic(right.eval(row)) ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        public String toString() {
            return left + " OR " + right;
        }
    }

    /**
     * 逻辑 NOT
     */
    static class NotNode extends Node {
        final Node child;

        NotNode(Node c) {
            this.child = c;
        }

        Object eval(Map<String, Object> row) {
            return !toBoolStatic(child.eval(row)) ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        public String toString() {
            return "NOT " + child;
        }
    }

    // -------------------------------------------------------------------------
    // Tokenizer
    // -------------------------------------------------------------------------

    /**
     * 一元负号
     */
    static class NegNode extends Node {
        final Node child;

        NegNode(Node c) {
            this.child = c;
        }

        Object eval(Map<String, Object> row) {
            Object v = child.eval(row);
            if (v == null) return null;
            if (v instanceof Number) return -((Number) v).doubleValue();
            return null;
        }

        @Override
        public String toString() {
            return "-" + child;
        }
    }

    static class Token {
        final TokenType type;
        final String value;
        final int precedence;

        Token(TokenType t, String v) {
            this(t, v, 0);
        }

        Token(TokenType t, String v, int p) {
            this.type = t;
            this.value = v;
            this.precedence = p;
        }

        static Token eof() {
            return new Token(TokenType.EOF, "");
        }
    }

    private class Tokenizer {
        private final String s;
        private int pos = 0;

        Tokenizer(String s) {
            this.s = s;
        }

        Token next() {
            if (pos >= s.length()) return Token.eof();
            skipWs();
            if (pos >= s.length()) return Token.eof();
            char c = s.charAt(pos);
            if (c == '\'' || c == '"') return readString(c);
            if (c == '(') {
                pos++;
                return new Token(TokenType.LPAREN, "(");
            }
            if (c == ')') {
                pos++;
                return new Token(TokenType.RPAREN, ")");
            }
            if (c == ',') {
                pos++;
                return new Token(TokenType.COMMA, ",");
            }
            if (Character.isDigit(c) || (c == '.' && pos + 1 < s.length() && Character.isDigit(s.charAt(pos + 1)))) {
                return readNumber();
            }
            return readIdentOrOp();
        }

        Token[] tokenize() {
            List<Token> list = new ArrayList<>();
            Token tok;
            while ((tok = next()).type != TokenType.EOF) list.add(tok);
            return list.toArray(new Token[0]);
        }

        private void skipWs() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        private Token readString(char quote) {
            int start = ++pos;
            StringBuilder sb = new StringBuilder();
            while (pos < s.length() && s.charAt(pos) != quote) {
                if (s.charAt(pos) == '\\' && pos + 1 < s.length()) pos++;
                sb.append(s.charAt(pos++));
            }
            if (pos < s.length()) pos++;
            return new Token(TokenType.STR, sb.toString());
        }

        private Token readNumber() {
            int start = pos;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.' || s.charAt(pos) == 'e' || s.charAt(pos) == 'E' || s.charAt(pos) == '+' || s.charAt(pos) == '-'))
                pos++;
            return new Token(TokenType.NUM, s.substring(start, pos));
        }

        private Token readIdentOrOp() {
            if (pos + 1 < s.length()) {
                String two = s.substring(pos, pos + 2);
                if (two.equals("<>") || two.equals("!=") || two.equals("<=") || two.equals(">=")) {
                    pos += 2;
                    return new Token(TokenType.OP, two, 7);
                }
            }
            if (pos < s.length() && "=<>!".indexOf(s.charAt(pos)) >= 0) {
                return new Token(TokenType.OP, String.valueOf(s.charAt(pos++)), 7);
            }
            if (pos < s.length() && "+-*%".indexOf(s.charAt(pos)) >= 0) {
                char c = s.charAt(pos++);
                int prec = (c == '+' || c == '-') ? 8 : 9;
                return new Token(TokenType.OP, String.valueOf(c), prec);
            }
            int start = pos;
            while (pos < s.length() && (Character.isLetterOrDigit(s.charAt(pos)) || s.charAt(pos) == '_' || s.charAt(pos) == '.'))
                pos++;
            String word = s.substring(start, pos).toUpperCase();
            if (word.equals("NULL")) return new Token(TokenType.NULL_LIT, "NULL");
            if (word.equals("TRUE")) return new Token(TokenType.BOOL, "TRUE");
            if (word.equals("FALSE")) return new Token(TokenType.BOOL, "FALSE");
            if (word.equals("AND")) return new Token(TokenType.OP, "AND", 3);
            if (word.equals("OR")) return new Token(TokenType.OP, "OR", 2);
            if (word.equals("NOT")) return new Token(TokenType.OP, "NOT", 5);
            if (word.equals("LIKE")) return new Token(TokenType.OP, "LIKE", 6);
            if (word.equals("AS")) return new Token(TokenType.OP, "AS", 6);
            int peek = pos;
            while (peek < s.length() && Character.isWhitespace(s.charAt(peek))) peek++;
            if (peek < s.length() && s.charAt(peek) == '(') {
                return new Token(TokenType.FUNC, word);
            }
            return new Token(TokenType.COL, word);
        }
    }

    // -------------------------------------------------------------------------
    // 递归下降解析 → 返回 AST Node
    // -------------------------------------------------------------------------

    private class Parser {
        private final Token[] tokens;
        private int pos = 0;

        Parser(Token[] tokens) {
            this.tokens = tokens;
        }

        Node parseExpr() {
            return parseLogicalOr();
        }

        private Node parseLogicalOr() {
            Node left = parseLogicalAnd();
            while (matchOp("OR")) {
                pos++;
                left = new OrNode(left, parseLogicalAnd());
            }
            return left;
        }

        private Node parseLogicalAnd() {
            Node left = parseNot();
            while (matchOp("AND")) {
                pos++;
                left = new AndNode(left, parseNot());
            }
            return left;
        }

        private Node parseNot() {
            if (matchOp("NOT")) {
                pos++;
                return new NotNode(parseComparison());
            }
            return parseComparison();
        }

        private Node parseComparison() {
            Node left = parseAddSub();
            while (pos < tokens.length && tokens[pos].type == TokenType.OP && tokens[pos].precedence >= 7) {
                String op = tokens[pos].value;
                pos++;
                Node right = parseAddSub();
                left = new CompareNode(left, op, right);
            }
            return left;
        }

        private Node parseAddSub() {
            Node left = parseMulDiv();
            while (pos < tokens.length && tokens[pos].type == TokenType.OP && (tokens[pos].value.equals("+") || tokens[pos].value.equals("-"))) {
                String op = tokens[pos].value;
                pos++;
                Node right = parseMulDiv();
                left = new ArithNode(left, op, right);
            }
            return left;
        }

        private Node parseMulDiv() {
            Node left = parseUnary();
            while (pos < tokens.length && tokens[pos].type == TokenType.OP && (tokens[pos].value.equals("*") || tokens[pos].value.equals("/") || tokens[pos].value.equals("%"))) {
                String op = tokens[pos].value;
                pos++;
                Node right = parseUnary();
                left = new ArithNode(left, op, right);
            }
            return left;
        }

        private Node parseUnary() {
            if (pos < tokens.length && tokens[pos].type == TokenType.OP && tokens[pos].value.equals("-")) {
                pos++;
                return new NegNode(parseAtom());
            }
            if (pos < tokens.length && tokens[pos].type == TokenType.OP && tokens[pos].value.equals("+")) {
                pos++;
                return parseAtom();
            }
            return parseAtom();
        }

        private Node parseAtom() {
            if (pos >= tokens.length) return new ConstNode(null);
            Token tok = tokens[pos++];
            switch (tok.type) {
                case NUM:
                    try {
                        String v = tok.value;
                        if (v.contains(".") || v.contains("e") || v.contains("E")) {
                            return new ConstNode(Double.parseDouble(v));
                        }
                        return new ConstNode(Long.parseLong(v));
                    } catch (NumberFormatException e) {
                        return new ConstNode(new BigDecimal(tok.value));
                    }
                case STR:
                    return new StrNode(tok.value);
                case BOOL:
                    return new ConstNode(Boolean.parseBoolean(tok.value));
                case NULL_LIT:
                    return new ConstNode(null);
                case FUNC:
                    return parseFuncall(tok.value);
                case LPAREN: {
                    Node result = parseExpr();
                    if (pos < tokens.length && tokens[pos].type == TokenType.RPAREN) pos++;
                    return result;
                }
                case COL:
                    return new ColNode(tok.value);
                default:
                    return new ConstNode(tok.value);
            }
        }

        private Node parseFuncall(String name) {
            if (pos < tokens.length && tokens[pos].type == TokenType.LPAREN) pos++;
            List<Node> args = new ArrayList<>();
            if (pos < tokens.length && tokens[pos].type != TokenType.RPAREN && tokens[pos].type != TokenType.EOF) {
                args.add(parseExpr());
                // 特殊处理 CAST(expr AS TYPE) 语法：遇到 AS 后把剩余部分当作类型串解析
                if (pos < tokens.length && tokens[pos].type == TokenType.OP && tokens[pos].value.equalsIgnoreCase("AS")) {
                    // 跳过 AS 关键字，收集类型相关 token 直到 RPAREN
                    pos++;
                    List<Token> typeTokens = new ArrayList<>();
                    while (pos < tokens.length && tokens[pos].type != TokenType.RPAREN) {
                        typeTokens.add(tokens[pos++]);
                    }
                    // 用类型串构造一个虚假的 StrNode，CAST 函数自行解析
                    StringBuilder typeSb = new StringBuilder();
                    for (Token t : typeTokens) typeSb.append(t.value).append(" ");
                    args.add(new StrNode(typeSb.toString().trim()));
                    // RPAREN 已在 while 中被消费，不再重复消费
                    return new FuncNode(name.toUpperCase(), args.toArray(new Node[0]));
                }
                while (pos < tokens.length && tokens[pos].type == TokenType.COMMA) {
                    pos++;
                    args.add(parseExpr());
                }
            }
            if (pos < tokens.length && tokens[pos].type == TokenType.RPAREN) pos++;
            return new FuncNode(name.toUpperCase(), args.toArray(new Node[0]));
        }

        private boolean matchOp(String op) {
            return pos < tokens.length && tokens[pos].type == TokenType.OP && tokens[pos].value.equals(op);
        }
    }
}