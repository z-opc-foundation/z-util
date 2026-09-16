package com.zifang.util.expr.sql.engine;

import com.zifang.util.dsl.g4.DynamicLexer;
import com.zifang.util.dsl.token.Token;
import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.engine.ast.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SQL AST 构建器。
 * <p>
 * 使用 DynamicLexer 对 SQL 文本进行词法分析，
 * 然后通过递归下降解析构建 AST。
 * <p>
 * 表达式优先级（由低到高）：
 * OR → AND → NOT → 比较/LIKE/BETWEEN/IN/IS → 算术加减 → 算术乘除 → 一元 → 原子
 */
public class SqlAstBuilder {

    private static final String G4_PATH = "/g4/SQLLexer.g4";

    private Token[] tokens;
    private int pos;

    /**
     * 解析 SQL 文本，返回 SELECT 语句 AST。
     */
    public SelectStmt parse(String sql) {
        this.tokens = tokenize(sql);
        this.pos = 0;
        return parseSelect();
    }

    // ==================== 词法分析 ====================

    private Token[] tokenize(String sql) {
        try {
            DynamicLexer lexer = new DynamicLexer();
            lexer.loadG4(loadG4());
            lexer.setInput(sql);
            List<Token> rawTokens = lexer.tokenize();
            return rawTokens.toArray(new Token[0]);
        } catch (Exception e) {
            throw new SqlException("SQL 词法分析失败: " + e.getMessage(), e);
        }
    }

    private String loadG4() {
        try {
            InputStream is = getClass().getResourceAsStream(G4_PATH);
            if (is == null) throw new SqlException("G4 文件未找到: " + G4_PATH);
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            is.close();
            return new String(bytes, "UTF-8");
        } catch (SqlException e) {
            throw e;
        } catch (Exception e) {
            throw new SqlException("加载 G4 文件失败: " + e.getMessage(), e);
        }
    }

    // ==================== Token 辅助方法 ====================

    private Token peek() {
        if (pos >= tokens.length) return null;
        return tokens[pos];
    }

    private Token advance() {
        if (pos >= tokens.length) return null;
        return tokens[pos++];
    }

    private boolean check(String tokenName) {
        Token t = peek();
        return t != null && t.getTokenName().equals(tokenName);
    }

    private boolean match(String tokenName) {
        if (check(tokenName)) {
            pos++;
            return true;
        }
        return false;
    }

    private Token expect(String tokenName) {
        Token t = advance();
        if (t == null || !t.getTokenName().equals(tokenName)) {
            String found = t == null ? "<EOF>" : t.getText() + " (" + t.getTokenName() + ")";
            throw new SqlException("期望 " + tokenName + "，但遇到 " + found);
        }
        return t;
    }

    private boolean isEOF() {
        return pos >= tokens.length;
    }

    // ==================== SELECT 语句解析 ====================

    private SelectStmt parseSelect() {
        SelectStmt stmt = new SelectStmt();

        // SELECT
        expect("SELECT");

        // [DISTINCT]
        if (match("DISTINCT")) {
            stmt.setDistinct(true);
        }

        // selectItems
        stmt.setSelectItems(parseSelectItems());

        // FROM tableName [alias]
        expect("FROM");
        stmt.setTableName(expect("ID").getText());
        if (check("AS")) {
            advance(); // consume AS
            stmt.setTableAlias(expect("ID").getText());
        } else if (check("ID") && !isClauseKeyword(peek().getTokenName())) {
            stmt.setTableAlias(advance().getText());
        }

        // [JOIN ...]
        while (check("JOIN") || check("INNER") || check("LEFT") || check("RIGHT")
                || check("FULL") || check("CROSS")) {
            stmt.addJoin(parseJoin());
        }

        // [WHERE expr]
        if (match("WHERE")) {
            stmt.setWhereClause(parseOrExpr());
        }

        // [GROUP BY expr, ...]
        if (match("GROUP")) {
            expect("BY");
            stmt.setGroupBy(parseCommaSeparatedExprs());
        }

        // [HAVING expr]
        if (match("HAVING")) {
            stmt.setHavingClause(parseOrExpr());
        }

        // [ORDER BY sortKey, ...]
        if (match("ORDER")) {
            expect("BY");
            stmt.setOrderBy(parseOrderByKeys());
        }

        // [LIMIT n [OFFSET m]]
        if (match("LIMIT")) {
            stmt.setLimit(Integer.parseInt(expect("NUM").getText()));
            if (match("OFFSET")) {
                stmt.setOffset(Integer.parseInt(expect("NUM").getText()));
            }
        } else if (match("OFFSET")) {
            stmt.setOffset(Integer.parseInt(expect("NUM").getText()));
        }

        return stmt;
    }

    /**
     * 判断 token 名是否是子句关键字（非表别名）。
     */
    private boolean isClauseKeyword(String tokenName) {
        return "WHERE".equals(tokenName) || "GROUP".equals(tokenName) || "ORDER".equals(tokenName)
                || "LIMIT".equals(tokenName) || "OFFSET".equals(tokenName) || "HAVING".equals(tokenName)
                || "JOIN".equals(tokenName) || "INNER".equals(tokenName) || "LEFT".equals(tokenName)
                || "RIGHT".equals(tokenName) || "FULL".equals(tokenName) || "CROSS".equals(tokenName)
                || "ON".equals(tokenName) || "UNION".equals(tokenName);
    }

    // ==================== SELECT 列表 ====================

    private List<Expression> parseSelectItems() {
        List<Expression> items = new ArrayList<>();
        items.add(parseSelectItem());
        while (match("COMMA")) {
            items.add(parseSelectItem());
        }
        return items;
    }

    private Expression parseSelectItem() {
        // SELECT * 或 SELECT t.*
        if (check("STAR")) {
            advance();
            return new ColumnRef("*");
        }
        // SELECT table.*
        if (check("ID") && pos + 1 < tokens.length && "DOT".equals(tokens[pos + 1].getTokenName())
                && pos + 2 < tokens.length && "STAR".equals(tokens[pos + 2].getTokenName())) {
            String table = advance().getText();
            advance(); // DOT
            advance(); // STAR
            return new ColumnRef(table, "*");
        }

        Expression expr = parseExpression();
        // [AS alias]
        if (match("AS")) {
            String alias = advance().getText();
            return new AliasedExpr(expr, alias);
        }
        // 如果下一个 token 是 ID 且不是关键字，可能是隐式别名 (SELECT name username)
        // 暂不支持隐式别名，需要 AS 关键字
        return expr;
    }

    // ==================== JOIN ====================

    private JoinClause parseJoin() {
        JoinClause.JoinType joinType = JoinClause.JoinType.INNER;
        if (match("INNER")) {
            joinType = JoinClause.JoinType.INNER;
        } else if (match("LEFT")) {
            joinType = JoinClause.JoinType.LEFT;
        } else if (match("RIGHT")) {
            joinType = JoinClause.JoinType.RIGHT;
        } else if (match("FULL")) {
            joinType = JoinClause.JoinType.FULL;
        } else if (match("CROSS")) {
            joinType = JoinClause.JoinType.CROSS;
        }
        expect("JOIN");

        String tableName = expect("ID").getText();
        String alias = null;
        if (check("ID") && !isClauseKeyword(peek().getTokenName())) {
            alias = advance().getText();
        } else if (match("AS")) {
            alias = expect("ID").getText();
        }

        Expression onCondition = null;
        if (match("ON")) {
            onCondition = parseOrExpr();
        }

        return new JoinClause(joinType, tableName, alias, onCondition);
    }

    // ==================== ORDER BY ====================

    private List<SortKey> parseOrderByKeys() {
        List<SortKey> keys = new ArrayList<>();
        keys.add(parseSortKey());
        while (match("COMMA")) {
            keys.add(parseSortKey());
        }
        return keys;
    }

    private SortKey parseSortKey() {
        Expression expr = parseExpression();
        boolean descending = match("DESC");
        if (!descending) {
            match("ASC"); // 可选，忽略
        }
        return new SortKey(expr, descending);
    }

    // ==================== 逗号分隔表达式列表 ====================

    private List<Expression> parseCommaSeparatedExprs() {
        List<Expression> exprs = new ArrayList<>();
        exprs.add(parseExpression());
        while (match("COMMA")) {
            exprs.add(parseExpression());
        }
        return exprs;
    }

    // ==================== 表达式解析（Pratt / 递归下降） ====================

    /**
     * 表达式入口。
     */
    private Expression parseExpression() {
        return parseOrExpr();
    }

    // --- OR (优先级 1) ---

    private Expression parseOrExpr() {
        Expression left = parseAndExpr();
        while (match("OR")) {
            Expression right = parseAndExpr();
            left = new BinaryExpr(left, "OR", right);
        }
        return left;
    }

    // --- AND (优先级 2) ---

    private Expression parseAndExpr() {
        Expression left = parseNotExpr();
        while (match("AND")) {
            Expression right = parseNotExpr();
            left = new BinaryExpr(left, "AND", right);
        }
        return left;
    }

    // --- NOT (优先级 3) ---

    private Expression parseNotExpr() {
        if (match("NOT")) {
            Expression operand = parseComparisonExpr();
            return new UnaryExpr("NOT", operand);
        }
        return parseComparisonExpr();
    }

    // --- 比较 / BETWEEN / IN / LIKE / IS (优先级 4) ---

    private Expression parseComparisonExpr() {
        Expression left = parseAddSubExpr();

        if (left == null) return null;

        // BETWEEN / NOT BETWEEN
        if (match("BETWEEN")) {
            Expression low = parseAddSubExpr();
            expect("AND");
            Expression high = parseAddSubExpr();
            return new BetweenExpr(left, low, high, false);
        }

        // IS [NOT] NULL
        if (check("IS")) {
            advance();
            boolean negated = match("NOT");
            expect("NULL_");
            return new IsNullExpr(left, negated);
        }

        // IN (...) —— 左侧必须是原子表达式，IN 是后缀
        if (check("IN")) {
            // 确保前面不是比较运算符的结果（IN 只能跟在列引用或括号表达式后）
            if (left instanceof ColumnRef || left instanceof AliasedExpr || left instanceof FunctionCall) {
                advance();
                boolean negated = false; // NOT IN 在 NOT 层处理
                expect("LPAREN");
                List<Expression> values;
                if (check("SELECT")) {
                    // 子查询暂不支持，抛异常
                    throw new SqlException("子查询暂不支持");
                } else {
                    values = parseCommaSeparatedExprs();
                }
                expect("RPAREN");
                return new InExpr(left, values, negated);
            }
        }

        // 比较运算符
        String compOp = peekCompareOp();
        if (compOp != null) {
            advance();
            Expression right = parseAddSubExpr();
            return new BinaryExpr(left, compOp, right);
        }

        return left;
    }

    /**
     * 检查并返回当前 token 的比较运算符（消费 token）。
     */
    private String peekCompareOp() {
        Token t = peek();
        if (t == null) return null;
        String name = t.getTokenName();
        switch (name) {
            case "EQ": return "=";
            case "NEQ": return "<>";
            case "LT": return "<";
            case "GT": return ">";
            case "LE": return "<=";
            case "GE": return ">=";
            case "LIKE": return "LIKE";
            default: return null;
        }
    }

    // --- 算术加减 (优先级 5) ---

    private Expression parseAddSubExpr() {
        Expression left = parseMulDivExpr();
        while (true) {
            if (match("PLUS")) {
                Expression right = parseMulDivExpr();
                left = new BinaryExpr(left, "+", right);
            } else if (match("MINUS")) {
                Expression right = parseMulDivExpr();
                left = new BinaryExpr(left, "-", right);
            } else {
                break;
            }
        }
        return left;
    }

    // --- 算术乘除 (优先级 6) ---

    private Expression parseMulDivExpr() {
        Expression left = parseUnaryExpr();
        while (true) {
            if (match("STAR")) {
                Expression right = parseUnaryExpr();
                left = new BinaryExpr(left, "*", right);
            } else if (match("SLASH")) {
                Expression right = parseUnaryExpr();
                left = new BinaryExpr(left, "/", right);
            } else if (match("PERCENT")) {
                Expression right = parseUnaryExpr();
                left = new BinaryExpr(left, "%", right);
            } else {
                break;
            }
        }
        return left;
    }

    // --- 一元运算 (优先级 7) ---

    private Expression parseUnaryExpr() {
        if (match("MINUS")) {
            Expression operand = parseAtom();
            return new UnaryExpr("-", operand);
        }
        if (match("PLUS")) {
            return parseAtom();
        }
        return parseAtom();
    }

    // --- 原子表达式 (优先级 8) ---

    private Expression parseAtom() {
        if (isEOF()) {
            throw new SqlException("表达式意外结束");
        }

        Token t = peek();
        String name = t.getTokenName();

        // 数字
        if ("NUM".equals(name)) {
            advance();
            return Literal.ofNumber(t.getText());
        }

        // 字符串
        if ("STRING".equals(name)) {
            advance();
            // 去掉首尾引号
            String text = t.getText();
            return Literal.ofString(text.substring(1, text.length() - 1));
        }

        // NULL
        if ("NULL_".equals(name)) {
            advance();
            return Literal.ofNull();
        }

        // TRUE / FALSE
        if ("TRUE_".equals(name)) {
            advance();
            return Literal.ofBoolean(true);
        }
        if ("FALSE_".equals(name)) {
            advance();
            return Literal.ofBoolean(false);
        }

        // 括号
        if ("LPAREN".equals(name)) {
            advance();
            Expression expr = parseExpression();
            expect("RPAREN");
            return expr;
        }

        // 标识符：可能是 列引用 / table.column / 函数调用 / CAST / COUNT(*) 等
        if ("ID".equals(name)) {
            return parseIdentifierOrFunction();
        }

        // 聚合函数关键字 token（COUNT, SUM, AVG, MAX, MIN）
        if ("COUNT".equals(name) || "SUM".equals(name) || "AVG".equals(name)
                || "MAX".equals(name) || "MIN".equals(name)) {
            advance();
            return parseFunctionCall(name);
        }

        // CAST 关键字 token
        if ("CAST".equals(name)) {
            advance();
            return parseCastExpr();
        }

        // STAR 作为原子：COUNT(*) 中的 *
        if ("STAR".equals(name)) {
            advance();
            return new ColumnRef("*");
        }

        throw new SqlException("意外的 token: " + t.getText() + " (" + name + ")");
    }

    /**
     * 解析标识符开头的表达式：列引用、带表前缀的列、函数调用。
     */
    private Expression parseIdentifierOrFunction() {
        String firstName = advance().getText();

        // table.column
        if (match("DOT")) {
            String secondName = advance().getText();
            // table.*
            if (match("STAR")) {
                return new ColumnRef(firstName, "*");
            }
            // 检查是否是函数调用 table.func(...)
            if (check("LPAREN")) {
                return parseFunctionCall(firstName + "." + secondName);
            }
            return new ColumnRef(firstName, secondName);
        }

        // 检查是否是函数调用 func(...)
        if (check("LPAREN")) {
            return parseFunctionCall(firstName);
        }

        // CAST(expr AS TYPE)
        if ("CAST".equalsIgnoreCase(firstName)) {
            return parseCastExpr();
        }

        // 普通列引用
        return new ColumnRef(firstName);
    }

    // ==================== 函数调用 ====================

    private Expression parseFunctionCall(String funcName) {
        expect("LPAREN");

        // COUNT(*)
        if ("COUNT".equalsIgnoreCase(funcName) && check("STAR")) {
            advance();
            expect("RPAREN");
            return new FunctionCall("COUNT", Collections.singletonList(new ColumnRef("*")));
        }

        // [DISTINCT] args
        boolean distinct = match("DISTINCT");
        List<Expression> args;
        if (check("RPAREN")) {
            args = Collections.emptyList();
        } else {
            args = parseCommaSeparatedExprs();
        }
        expect("RPAREN");
        return new FunctionCall(funcName.toUpperCase(), args, distinct);
    }

    // ==================== CAST ====================

    private Expression parseCastExpr() {
        expect("LPAREN");
        Expression expr = parseExpression();
        expect("AS");
        // 收集类型名（可能是多 token，如 DOUBLE PRECISION，但简单情况取一个 ID）
        StringBuilder typeName = new StringBuilder();
        typeName.append(advance().getText());
        // 处理可能的多词类型如 "DOUBLE PRECISION" —— 如果后面还有 ID 且不是 RPAREN
        while (check("ID") && pos + 1 < tokens.length && "RPAREN".equals(tokens[pos + 1].getTokenName())) {
            // 不太可能，跳过
            break;
        }
        expect("RPAREN");
        return new CastExpr(expr, typeName.toString());
    }
}
