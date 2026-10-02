package com.zifang.util.expr.el;

import java.util.ArrayList;
import java.util.List;

/**
 * 自研 EL（Expression Language）解析器与求值器。
 * <p>
 * 设计目标：替代 SpEL（Spring Expression Language），覆盖本项目所需的子集。
 *
 * <p>完整实现约 700 行，recursive-descent 解析 + 反射求值。
 */
public final class ElExpression {

    /** 求值上下文：变量表 + 可选 root object。 */
    public static final class Context {
        private final java.util.Map<String, Object> vars = new java.util.HashMap<>();
        private Object rootObject;

        public Context() {}

        public Context(java.util.Map<String, Object> initial) {
            if (initial != null) vars.putAll(initial);
        }

        public Object get(String name) { return vars.get(name); }
        public void set(String name, Object value) { vars.put(name, value); }
        public boolean contains(String name) { return vars.containsKey(name); }
        public java.util.Map<String, Object> asMap() { return new java.util.HashMap<>(vars); }
        public void putAll(java.util.Map<String, Object> m) { if (m != null) vars.putAll(m); }

        public Object getRoot() { return rootObject; }
        public void setRoot(Object root) { this.rootObject = root; }
    }

    // ==================== AST 节点 ====================

    interface Node { Object eval(Context ctx); }

    static final class NumberNode implements Node {
        final Number value;
        NumberNode(Number v) { this.value = v; }
        public Object eval(Context ctx) {
            // 优先返回 Integer（小数值），与 SpEL 行为一致
            if (value instanceof Long) {
                long l = value.longValue();
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
            }
            return value;
        }
    }

    static final class StringNode implements Node {
        final String value;
        StringNode(String v) { this.value = v; }
        public Object eval(Context ctx) { return value; }
    }

    static final class BoolNode implements Node {
        final boolean value;
        BoolNode(boolean v) { this.value = v; }
        public Object eval(Context ctx) { return value; }
    }

    static final class NullNode implements Node {
        public Object eval(Context ctx) { return null; }
    }

    /** 变量引用。 */
    static final class VariableNode implements Node {
        final String name;
        VariableNode(String n) { this.name = n; }
        public Object eval(Context ctx) {
            if (ctx.contains(name)) return ctx.get(name);
            // 找不到时尝试访问 rootObject 的属性（SpEL 的 root reference 语义）
            Object root = ctx.getRoot();
            if (root != null) {
                return accessProperty(root, name);
            }
            return null;
        }
    }

    static final class UnaryMinusNode implements Node {
        final Node operand;
        UnaryMinusNode(Node op) { this.operand = op; }
        public Object eval(Context ctx) {
            Object v = operand.eval(ctx);
            if (v instanceof Integer) return -((Integer) v);
            if (v instanceof Long) return -((Long) v);
            if (v instanceof Double) return -((Double) v);
            if (v instanceof Float) return -((Float) v);
            throw new ElException("Unary minus on non-numeric: " + v);
        }
    }

    static final class BinaryOpNode implements Node {
        final Node left, right;
        final String op;
        BinaryOpNode(Node l, String op, Node r) { this.left = l; this.op = op; this.right = r; }
        public Object eval(Context ctx) { return ElExpression.evalBinaryOp(left.eval(ctx), op, right.eval(ctx)); }
    }

    static final class NotNode implements Node {
        final Node operand;
        NotNode(Node op) { this.operand = op; }
        public Object eval(Context ctx) { return !ElExpression.truthy(operand.eval(ctx)); }
    }

    static final class TernaryNode implements Node {
        final Node cond, trueExpr, falseExpr;
        TernaryNode(Node c, Node t, Node f) { this.cond = c; this.trueExpr = t; this.fExpr(f); this.falseExpr = f; }
        private void fExpr(Node f) {}
        public Object eval(Context ctx) { return truthy(cond.eval(ctx)) ? trueExpr.eval(ctx) : falseExpr.eval(ctx); }
    }

    static final class ChainNode implements Node {
        final Node target;
        final List<Step> steps;
        ChainNode(Node t, List<Step> s) { this.target = t; this.steps = s; }
        public Object eval(Context ctx) {
            Object current = target.eval(ctx);
            for (Step step : steps) {
                current = step.apply(current);
            }
            return current;
        }
    }

    static final class Step {
        enum Kind { PROPERTY, METHOD, INDEX }
        final Kind kind;
        final String name;
        final List<Node> args;
        final Node index;

        Step(Kind k, String n, List<Node> a, Node i) { this.kind = k; this.name = n; this.args = a; this.index = i; }

        Object apply(Object target) {
            if (target == null) {
                throw new ElException("Cannot access property/method on null");
            }
            switch (kind) {
                case PROPERTY: return accessProperty(target, name);
                case METHOD: return invokeMethod(target, name, args);
                case INDEX: return accessIndex(target, index.eval(null));
                default: throw new ElException("Unknown step: " + kind);
            }
        }
    }

    // ==================== 编译与求值 ====================

    private final Node root;

    private ElExpression(Node root) { this.root = root; }

    public static ElExpression compile(String expression) {
        Parser p = new Parser(expression);
        Node n = p.parseExpression();
        p.skipWs();
        if (p.pos < p.src.length()) {
            throw new ElException("Unexpected trailing characters at " + p.pos + ": '" + p.src.substring(p.pos) + "'");
        }
        return new ElExpression(n);
    }

    public Object evaluate(Context ctx) {
        try {
            return root.eval(ctx);
        } catch (ElException e) {
            throw e;
        } catch (Exception e) {
            throw new ElException("Failed to evaluate expression: " + e.getMessage(), e);
        }
    }

    // ==================== 求值原语 ====================

    static boolean truthy(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).doubleValue() != 0.0;
        if (v instanceof String) return !((String) v).isEmpty();
        if (v instanceof java.util.Collection) return !((java.util.Collection<?>) v).isEmpty();
        if (v instanceof java.util.Map) return !((java.util.Map<?, ?>) v).isEmpty();
        if (v.getClass().isArray()) return java.lang.reflect.Array.getLength(v) > 0;
        return true;
    }

    static Object evalBinaryOp(Object l, String op, Object r) {
        switch (op) {
            case "&&": return truthy(l) && truthy(r);
            case "||": return truthy(l) || truthy(r);
            case "+": return add(l, r);
            case "-": return subtract(l, r);
            case "*": return multiply(l, r);
            case "/": return divide(l, r);
            case "%": return mod(l, r);
            case "==": return equalsLoose(l, r);
            case "!=": return !equalsLoose(l, r);
            case "<": return compare(l, r) < 0;
            case ">": return compare(l, r) > 0;
            case "<=": return compare(l, r) <= 0;
            case ">=": return compare(l, r) >= 0;
            default: throw new ElException("Unknown operator: " + op);
        }
    }

    private static Object add(Object l, Object r) {
        if (l instanceof String || r instanceof String) return toStr(l) + toStr(r);
        return arith(l, r, '+');
    }

    private static Object subtract(Object l, Object r) { return arith(l, r, '-'); }
    private static Object multiply(Object l, Object r) { return arith(l, r, '*'); }
    private static Object divide(Object l, Object r) { return arith(l, r, '/'); }

    /**
     * 类型保留算术：整数 + 整数 = 整数（long），其他 = double。
     */
    private static Object arith(Object l, Object r, char op) {
        if (isInteger(l) && isInteger(r)) {
            long a = ((Number) l).longValue();
            long b = ((Number) r).longValue();
            long res;
            switch (op) {
                case '+': res = a + b; break;
                case '-': res = a - b; break;
                case '*': res = a * b; break;
                case '/': res = a / b; break;
                default: throw new ElException("Unknown op: " + op);
            }
            if (res >= Integer.MIN_VALUE && res <= Integer.MAX_VALUE) return (int) res;
            return res;
        }
        double a = toDouble(l), b = toDouble(r);
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return a / b;
            default: throw new ElException("Unknown op: " + op);
        }
    }

    private static Object mod(Object l, Object r) {
        if (isInteger(l) && isInteger(r)) {
            long a = ((Number) l).longValue(), b = ((Number) r).longValue();
            long res = a % b;
            if (res >= Integer.MIN_VALUE && res <= Integer.MAX_VALUE) return (int) res;
            return res;
        }
        return toDouble(l) % toDouble(r);
    }

    private static boolean isInteger(Object o) {
        return o instanceof Integer || o instanceof Long;
    }

    static boolean equalsLoose(Object l, Object r) {
        if (l == r) return true;
        if (l == null || r == null) return false;
        if (l instanceof Number && r instanceof Number) {
            return ((Number) l).doubleValue() == ((Number) r).doubleValue();
        }
        if (l instanceof Boolean && r instanceof Boolean) return l.equals(r);
        return l.equals(r);
    }

    static int compare(Object l, Object r) {
        if (l == r) return 0;
        if (l instanceof Number && r instanceof Number) {
            return Double.compare(((Number) l).doubleValue(), ((Number) r).doubleValue());
        }
        if (l instanceof String && r instanceof String) {
            return ((String) l).compareTo((String) r);
        }
        if (l == null) return -1;
        if (r == null) return 1;
        throw new ElException("Cannot compare " + l.getClass().getSimpleName() + " and " + r.getClass().getSimpleName());
    }

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof String) {
            try { return Double.parseDouble((String) o); } catch (NumberFormatException e) { throw new ElException("Not a number: " + o); }
        }
        if (o instanceof Boolean) return (Boolean) o ? 1.0 : 0.0;
        throw new ElException("Not a number: " + o + " (" + o.getClass().getSimpleName() + ")");
    }

    private static String toStr(Object o) {
        if (o == null) return "null";
        return o.toString();
    }

    // ==================== 属性 / 方法 / 索引 ====================

    private static Object accessProperty(Object target, String name) {
        Class<?> cls = target.getClass();
        if (target instanceof java.util.Map) {
            return ((java.util.Map<?, ?>) target).get(name);
        }
        if (target instanceof java.util.List) {
            if ("size".equals(name) || "length".equals(name)) return ((java.util.List<?>) target).size();
            if ("empty".equals(name)) return ((java.util.List<?>) target).isEmpty();
        }
        if (cls.isArray()) {
            if ("length".equals(name)) return java.lang.reflect.Array.getLength(target);
        }
        if (target instanceof String) {
            return invokeStringMethod((String) target, name, java.util.Collections.emptyList());
        }
        if (target instanceof java.util.Collection) {
            if ("size".equals(name)) return ((java.util.Collection<?>) target).size();
            if ("empty".equals(name)) return ((java.util.Collection<?>) target).isEmpty();
        }
        // 字符串方法通过 Object.invokeMethod 也能找到，这里只处理 bean 属性
        String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        String isser = "is" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            java.lang.reflect.Method m = cls.getMethod(getter);
            if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                try { return m.invoke(target); } catch (java.lang.reflect.InvocationTargetException | IllegalAccessException ite) { if (ite.getCause() instanceof RuntimeException) throw (RuntimeException) ite.getCause(); }
            }
        } catch (NoSuchMethodException ignored) {}
        try {
            java.lang.reflect.Method m = cls.getMethod(isser);
            if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                try { return m.invoke(target); } catch (java.lang.reflect.InvocationTargetException | IllegalAccessException ite) { if (ite.getCause() instanceof RuntimeException) throw (RuntimeException) ite.getCause(); }
            }
        } catch (NoSuchMethodException ignored) {}
        try {
            java.lang.reflect.Field f = cls.getField(name);
            return f.get(target);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        try {
            java.lang.reflect.Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        throw new ElException("Property '" + name + "' not found on " + cls.getSimpleName());
    }

    private static Object invokeMethod(Object target, String name, List<Node> argNodes) {
        Class<?> cls = target.getClass();
        List<Object> args = new java.util.ArrayList<>();
        for (Node n : argNodes) args.add(n.eval(null));
        if (target instanceof String) {
            Object r = invokeStringMethod((String) target, name, args);
            if (r != INVOKE_NOT_FOUND) return r;
        }
        if (target instanceof java.util.List) {
            Object r = invokeCollectionMethod((java.util.List<?>) target, name, args);
            if (r != INVOKE_NOT_FOUND) return r;
        }
        if (target instanceof java.util.Collection) {
            Object r = invokeCollectionMethod((java.util.Collection<?>) target, name, args);
            if (r != INVOKE_NOT_FOUND) return r;
        }
        if (target instanceof java.util.Map) {
            Object r = invokeMapMethod((java.util.Map<?, ?>) target, name, args);
            if (r != INVOKE_NOT_FOUND) return r;
        }
        for (java.lang.reflect.Method m : cls.getMethods()) {
            if (!m.getName().equals(name)) continue;
            if (m.getParameterTypes().length != args.size()) continue;
            try {
                return m.invoke(target, args.toArray());
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new ElException("Method " + name + " threw: " + cause.getMessage(), cause);
            } catch (IllegalAccessException ignored) {}
        }
        throw new ElException("Method '" + name + "(" + args.size() + " args)' not found on " + cls.getSimpleName());
    }

    private static final Object INVOKE_NOT_FOUND = new Object();

    private static Object invokeCollectionMethod(java.util.Collection<?> target, String name, List<Object> args) {
        if ("size".equals(name) && args.size() == 0) return target.size();
        if ("empty".equals(name) && args.size() == 0) return target.isEmpty();
        if ("isEmpty".equals(name) && args.size() == 0) return target.isEmpty();
        if ("contains".equals(name) && args.size() == 1) return target.contains(args.get(0));
        return INVOKE_NOT_FOUND;
    }

    private static Object invokeMapMethod(java.util.Map<?, ?> target, String name, List<Object> args) {
        if ("size".equals(name) && args.size() == 0) return target.size();
        if ("empty".equals(name) && args.size() == 0) return target.isEmpty();
        if ("isEmpty".equals(name) && args.size() == 0) return target.isEmpty();
        if ("containsKey".equals(name) && args.size() == 1) return target.containsKey(args.get(0));
        if ("containsValue".equals(name) && args.size() == 1) return target.containsValue(args.get(0));
        return INVOKE_NOT_FOUND;
    }

    private static Object invokeStringMethod(String s, String name, List<Object> args) {
        if ("length".equals(name) && args.size() == 0) return s.length();
        if ("size".equals(name) && args.size() == 0) return s.length();
        if ("empty".equals(name) && args.size() == 0) return s.isEmpty();
        if ("toUpperCase".equals(name) && args.size() == 0) return s.toUpperCase();
        if ("toLowerCase".equals(name) && args.size() == 0) return s.toLowerCase();
        if ("trim".equals(name) && args.size() == 0) return s.trim();
        if ("contains".equals(name) && args.size() == 1) return s.contains(String.valueOf(args.get(0)));
        if ("startsWith".equals(name) && args.size() == 1) return s.startsWith(String.valueOf(args.get(0)));
        if ("endsWith".equals(name) && args.size() == 1) return s.endsWith(String.valueOf(args.get(0)));
        if ("equals".equals(name) && args.size() == 1) return s.equals(String.valueOf(args.get(0)));
        if ("substring".equals(name) && args.size() == 1) return s.substring(((Number) args.get(0)).intValue());
        if ("substring".equals(name) && args.size() == 2) return s.substring(((Number) args.get(0)).intValue(), ((Number) args.get(1)).intValue());
        if ("indexOf".equals(name) && args.size() == 1) return s.indexOf(String.valueOf(args.get(0)));
        if ("charAt".equals(name) && args.size() == 1) return s.charAt(((Number) args.get(0)).intValue());
        return INVOKE_NOT_FOUND;
    }

    private static Object accessIndex(Object target, Object index) {
        if (target instanceof java.util.List) {
            int i = ((Number) index).intValue();
            java.util.List<?> list = (java.util.List<?>) target;
            if (i < 0) i += list.size();
            if (i < 0 || i >= list.size()) return null;
            return list.get(i);
        }
        if (target instanceof java.util.Map) {
            return ((java.util.Map<?, ?>) target).get(String.valueOf(index));
        }
        if (target.getClass().isArray()) {
            int i = ((Number) index).intValue();
            return java.lang.reflect.Array.get(target, i);
        }
        if (target instanceof String) {
            int i = ((Number) index).intValue();
            String str = (String) target;
            if (i < 0 || i >= str.length()) return null;
            return String.valueOf(str.charAt(i));
        }
        throw new ElException("Cannot index " + target.getClass().getSimpleName());
    }

    // ==================== 解析器 ====================

    static final class Parser {
        final String src;
        int pos;

        Parser(String src) { this.src = src; }

        void skipWs() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }

        boolean peek(String s) {
            if (pos + s.length() > src.length()) return false;
            for (int i = 0; i < s.length(); i++) {
                if (src.charAt(pos + i) != s.charAt(i)) return false;
            }
            return true;
        }

        boolean consume(String s) {
            if (peek(s)) { pos += s.length(); return true; }
            return false;
        }

        /**
         * 顶层入口：仅做"消耗全部输入"的强约束。
         * 内部递归调用应使用 parseTernary/parseOr/parseAnd 等。
         */
        Node parseExpression() {
            skipWs();
            return parseTernary();
        }

        Node parseTernary() {
            Node cond = parseOr();
            skipWs();
            if (consume("?")) {
                // 内部不要再做 trailing check，否则嵌套三目无法解析
                Node t = parseOr();
                skipWs();
                if (!consume(":")) throw new ElException("Expected ':' in ternary");
                Node f = parseOr();
                return new TernaryNode(cond, t, f);
            }
            return cond;
        }

        Node parseOr() {
            Node left = parseAnd();
            skipWs();
            while (consume("||")) {
                Node right = parseAnd();
                left = new BinaryOpNode(left, "||", right);
                skipWs();
            }
            return left;
        }

        Node parseAnd() {
            Node left = parseNot();
            skipWs();
            while (consume("&&")) {
                Node right = parseNot();
                left = new BinaryOpNode(left, "&&", right);
                skipWs();
            }
            return left;
        }

        Node parseNot() {
            skipWs();
            if (consume("!")) {
                return new NotNode(parseNot());
            }
            return parseComparison();
        }

        Node parseComparison() {
            Node left = parseAdditive();
            skipWs();
            for (String op : new String[]{"<=", ">=", "==", "!=", "<", ">"}) {
                if (consume(op)) {
                    Node right = parseAdditive();
                    return new BinaryOpNode(left, op, right);
                }
            }
            return left;
        }

        Node parseAdditive() {
            Node left = parseMultiplicative();
            skipWs();
            while (true) {
                if (consume("+")) {
                    Node right = parseMultiplicative();
                    left = new BinaryOpNode(left, "+", right);
                } else if (consume("-")) {
                    Node right = parseMultiplicative();
                    left = new BinaryOpNode(left, "-", right);
                } else {
                    return left;
                }
                skipWs();
            }
        }

        Node parseMultiplicative() {
            Node left = parseUnary();
            skipWs();
            while (true) {
                if (consume("*")) {
                    Node right = parseUnary();
                    left = new BinaryOpNode(left, "*", right);
                } else if (consume("/")) {
                    Node right = parseUnary();
                    left = new BinaryOpNode(left, "/", right);
                } else if (consume("%")) {
                    Node right = parseUnary();
                    left = new BinaryOpNode(left, "%", right);
                } else {
                    return left;
                }
                skipWs();
            }
        }

        Node parseUnary() {
            skipWs();
            if (consume("-")) return new UnaryMinusNode(parseUnary());
            if (consume("+")) return parseUnary();
            return parseChain();
        }

        Node parseChain() {
            Node head = parsePrimary();
            // 如果 head 是裸 VariableNode 且紧跟 '('，自动提升为 root 对象上的方法调用
            if (head instanceof VariableNode && pos < src.length() && src.charAt(pos) == '(') {
                pos++;
                List<Node> args = readArgList();
                List<Step> steps = new ArrayList<>();
                steps.add(new Step(Step.Kind.METHOD, ((VariableNode) head).name, args, null));
                head = new ChainNode(new RootReferenceNode(), steps);
            }
            List<Step> steps = new ArrayList<>();
            while (true) {
                skipWs();
                if (pos >= src.length()) break;
                char c = src.charAt(pos);
                if (c == '.') {
                    pos++;
                    String name = readIdentifier();
                    if (name == null) throw new ElException("Expected identifier after '.' at " + pos);
                    skipWs();
                    List<Node> args = null;
                    if (pos < src.length() && src.charAt(pos) == '(') {
                        pos++;
                        args = readArgList();
                    }
                    if (args != null) steps.add(new Step(Step.Kind.METHOD, name, args, null));
                    else steps.add(new Step(Step.Kind.PROPERTY, name, null, null));
                } else if (c == '[') {
                    pos++;
                    Node idx = parseOr();
                    skipWs();
                    if (!consume("]")) throw new ElException("Expected ']' at " + pos);
                    steps.add(new Step(Step.Kind.INDEX, null, null, idx));
                } else {
                    break;
                }
            }
            if (steps.isEmpty()) return head;
            // 如果 head 是 ChainNode（已经处理过的裸方法调用），直接返回
            if (head instanceof ChainNode) {
                ChainNode existing = (ChainNode) head;
                existing.steps.addAll(0, steps);
                return existing;
            }
            return new ChainNode(head, steps);
        }

        /** 引用 root object 的节点，用于实现 `getName()` 这种隐式 root 调用。 */
        static class RootReferenceNode implements Node {
            public Object eval(Context ctx) { return ctx.getRoot(); }
        }

        List<Node> readArgList() {
            List<Node> args = new ArrayList<>();
            skipWs();
            if (pos < src.length() && src.charAt(pos) == ')') { pos++; return args; }
            args.add(parseOr());
            skipWs();
            while (consume(",")) {
                args.add(parseOr());
                skipWs();
            }
            if (!consume(")")) throw new ElException("Expected ')' at " + pos);
            return args;
        }

        Node parsePrimary() {
            skipWs();
            if (pos >= src.length()) throw new ElException("Unexpected end of expression");
            char c = src.charAt(pos);
            if (c == '(') {
                pos++;
                // 括号内允许完整三目表达式
                Node e = parseTernary();
                skipWs();
                if (!consume(")")) throw new ElException("Expected ')' at " + pos);
                return e;
            }
            if (c == '\'') return new StringNode(readString('\''));
            if (c == '"') return new StringNode(readString('"'));
            if (c == '#') {
                pos++;
                String name = readIdentifier();
                if (name == null) throw new ElException("Expected identifier after '#' at " + pos);
                return new VariableNode(name);
            }
            if (Character.isDigit(c)) return new NumberNode(readNumber());
            String ident = readIdentifier();
            if (ident == null) throw new ElException("Unexpected char '" + c + "' at " + pos);
            if ("true".equals(ident)) return new BoolNode(true);
            if ("false".equals(ident)) return new BoolNode(false);
            if ("null".equals(ident)) return new NullNode();
            return new VariableNode(ident);
        }

        String readString(char quote) {
            pos++;
            StringBuilder sb = new StringBuilder();
            while (pos < src.length() && src.charAt(pos) != quote) {
                char c = src.charAt(pos);
                if (c == '\\' && pos + 1 < src.length()) {
                    char next = src.charAt(pos + 1);
                    switch (next) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case '\\': sb.append('\\'); break;
                        case '\'': sb.append('\''); break;
                        case '"': sb.append('"'); break;
                        default: sb.append(next);
                    }
                    pos += 2;
                } else {
                    sb.append(c);
                    pos++;
                }
            }
            if (pos >= src.length()) throw new ElException("Unterminated string");
            pos++;
            return sb.toString();
        }

        String readIdentifier() {
            int start = pos;
            if (pos < src.length() && (Character.isLetter(src.charAt(pos)) || src.charAt(pos) == '_' || src.charAt(pos) == '$')) {
                pos++;
                while (pos < src.length()
                        && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_' || src.charAt(pos) == '$')) {
                    pos++;
                }
                return src.substring(start, pos);
            }
            return null;
        }

        Number readNumber() {
            int start = pos;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            boolean isFloat = false;
            if (pos < src.length() && src.charAt(pos) == '.') {
                isFloat = true;
                pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            }
            if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                isFloat = true;
                pos++;
                if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            }
            String num = src.substring(start, pos);
            if (isFloat) return Double.valueOf(num);
            try { return Long.valueOf(num); }
            catch (NumberFormatException e) { return Double.valueOf(num); }
        }
    }
}
