package com.zifang.util.expr.sql.engine;

import com.zifang.util.expr.sql.engine.ast.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * SqlAstBuilder 解析器单元测试。
 * 验证 SQL 文本 → AST 构建的正确性。
 */
public class SqlAstBuilderTest {

    private SqlAstBuilder builder;

    @Before
    public void setUp() {
        builder = new SqlAstBuilder();
    }

    // ==================== 基础 SELECT ====================

    @Test
    public void testSelectAll() {
        SelectStmt stmt = builder.parse("SELECT * FROM users");
        assertTrue(stmt.isSelectAll());
        assertEquals("users", stmt.getTableName());
        assertNull(stmt.getWhereClause());
        assertTrue(stmt.getOrderBy().isEmpty());
    }

    @Test
    public void testSelectColumns() {
        SelectStmt stmt = builder.parse("SELECT name, age FROM users");
        assertEquals(2, stmt.getSelectItems().size());
        assertEquals("users", stmt.getTableName());
    }

    @Test
    public void testSelectWithAlias() {
        SelectStmt stmt = builder.parse("SELECT name AS username, age AS user_age FROM users");
        assertEquals(2, stmt.getSelectItems().size());
        Expression first = stmt.getSelectItems().get(0);
        assertTrue(first instanceof AliasedExpr);
        assertEquals("username", ((AliasedExpr) first).getAlias());
    }

    @Test
    public void testSelectDistinct() {
        SelectStmt stmt = builder.parse("SELECT DISTINCT dept FROM users");
        assertTrue(stmt.isDistinct());
        assertEquals("users", stmt.getTableName());
    }

    // ==================== 表别名 ====================

    @Test
    public void testTableAlias() {
        SelectStmt stmt = builder.parse("SELECT u.name FROM users AS u");
        assertEquals("users", stmt.getTableName());
        assertEquals("u", stmt.getTableAlias());
    }

    @Test
    public void testTableAliasWithoutAs() {
        SelectStmt stmt = builder.parse("SELECT u.name FROM users u");
        assertEquals("users", stmt.getTableName());
        assertEquals("u", stmt.getTableAlias());
    }

    // ==================== WHERE 子句 ====================

    @Test
    public void testWhereEquals() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE age = 30");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof BinaryExpr);
        assertEquals("=", ((BinaryExpr) stmt.getWhereClause()).getOperator());
    }

    @Test
    public void testWhereAnd() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE age > 18 AND dept = 'eng'");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof BinaryExpr);
        assertEquals("AND", ((BinaryExpr) stmt.getWhereClause()).getOperator());
    }

    @Test
    public void testWhereOr() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE dept = 'eng' OR dept = 'sales'");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof BinaryExpr);
        assertEquals("OR", ((BinaryExpr) stmt.getWhereClause()).getOperator());
    }

    @Test
    public void testWhereNot() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE NOT (age > 30)");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof UnaryExpr);
        assertEquals("NOT", ((UnaryExpr) stmt.getWhereClause()).getOperator());
    }

    @Test
    public void testWhereBetween() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE age BETWEEN 18 AND 65");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof BetweenExpr);
    }

    @Test
    public void testWhereIn() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE dept IN ('eng', 'sales')");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof InExpr);
    }

    @Test
    public void testWhereIsNull() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE email IS NULL");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof IsNullExpr);
        assertFalse(((IsNullExpr) stmt.getWhereClause()).isNegated());
    }

    @Test
    public void testWhereIsNotNull() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE email IS NOT NULL");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof IsNullExpr);
        assertTrue(((IsNullExpr) stmt.getWhereClause()).isNegated());
    }

    @Test
    public void testWhereLike() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE name LIKE 'A%'");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof BinaryExpr);
        assertEquals("LIKE", ((BinaryExpr) stmt.getWhereClause()).getOperator());
    }

    @Test
    public void testWhereParentheses() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE (age > 18) AND (dept = 'eng')");
        assertNotNull(stmt.getWhereClause());
        assertTrue(stmt.getWhereClause() instanceof BinaryExpr);
        assertEquals("AND", ((BinaryExpr) stmt.getWhereClause()).getOperator());
    }

    // ==================== 表达式列 ====================

    @Test
    public void testSelectExpression() {
        SelectStmt stmt = builder.parse("SELECT price * qty AS total FROM orders");
        assertEquals(1, stmt.getSelectItems().size());
        Expression item = stmt.getSelectItems().get(0);
        assertTrue(item instanceof AliasedExpr);
        assertEquals("total", ((AliasedExpr) item).getAlias());
        assertTrue(((AliasedExpr) item).getExpression() instanceof BinaryExpr);
    }

    @Test
    public void testSelectFunction() {
        SelectStmt stmt = builder.parse("SELECT COUNT(*) AS total FROM users");
        assertEquals(1, stmt.getSelectItems().size());
        Expression item = stmt.getSelectItems().get(0);
        assertTrue(item instanceof AliasedExpr);
        assertTrue(((AliasedExpr) item).getExpression() instanceof FunctionCall);
    }

    // ==================== ORDER BY ====================

    @Test
    public void testOrderBySingle() {
        SelectStmt stmt = builder.parse("SELECT * FROM users ORDER BY age ASC");
        assertEquals(1, stmt.getOrderBy().size());
        assertFalse(stmt.getOrderBy().get(0).isDescending());
    }

    @Test
    public void testOrderByDesc() {
        SelectStmt stmt = builder.parse("SELECT * FROM users ORDER BY age DESC");
        assertEquals(1, stmt.getOrderBy().size());
        assertTrue(stmt.getOrderBy().get(0).isDescending());
    }

    @Test
    public void testOrderByMultiple() {
        SelectStmt stmt = builder.parse("SELECT * FROM users ORDER BY dept ASC, age DESC");
        assertEquals(2, stmt.getOrderBy().size());
        assertFalse(stmt.getOrderBy().get(0).isDescending());
        assertTrue(stmt.getOrderBy().get(1).isDescending());
    }

    // ==================== GROUP BY ====================

    @Test
    public void testGroupBy() {
        SelectStmt stmt = builder.parse("SELECT dept, COUNT(*) FROM users GROUP BY dept");
        assertEquals(1, stmt.getGroupBy().size());
        assertTrue(stmt.getGroupBy().get(0) instanceof ColumnRef);
    }

    // ==================== LIMIT / OFFSET ====================

    @Test
    public void testLimit() {
        SelectStmt stmt = builder.parse("SELECT * FROM users LIMIT 10");
        assertEquals(Integer.valueOf(10), stmt.getLimit());
        assertNull(stmt.getOffset());
    }

    @Test
    public void testLimitOffset() {
        SelectStmt stmt = builder.parse("SELECT * FROM users LIMIT 10 OFFSET 20");
        assertEquals(Integer.valueOf(10), stmt.getLimit());
        assertEquals(Integer.valueOf(20), stmt.getOffset());
    }

    @Test
    public void testOffsetOnly() {
        SelectStmt stmt = builder.parse("SELECT * FROM users OFFSET 5");
        assertNull(stmt.getLimit());
        assertEquals(Integer.valueOf(5), stmt.getOffset());
    }

    // ==================== JOIN ====================

    @Test
    public void testInnerJoin() {
        SelectStmt stmt = builder.parse(
                "SELECT u.name, o.product FROM users u INNER JOIN orders o ON u.id = o.user_id");
        assertEquals(1, stmt.getJoins().size());
        assertEquals(JoinClause.JoinType.INNER, stmt.getJoins().get(0).getJoinType());
        assertEquals("orders", stmt.getJoins().get(0).getTableName());
        assertEquals("o", stmt.getJoins().get(0).getAlias());
    }

    @Test
    public void testLeftJoin() {
        SelectStmt stmt = builder.parse(
                "SELECT u.name, o.product FROM users u LEFT JOIN orders o ON u.id = o.user_id");
        assertEquals(1, stmt.getJoins().size());
        assertEquals(JoinClause.JoinType.LEFT, stmt.getJoins().get(0).getJoinType());
    }

    // ==================== 字面量 ====================

    @Test
    public void testStringLiteral() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE name = 'Alice'");
        assertNotNull(stmt.getWhereClause());
        BinaryExpr bin = (BinaryExpr) stmt.getWhereClause();
        assertTrue(bin.getRight() instanceof Literal);
        assertEquals("Alice", ((Literal) bin.getRight()).getValue());
    }

    @Test
    public void testNumericLiteral() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE age = 30");
        BinaryExpr bin = (BinaryExpr) stmt.getWhereClause();
        assertTrue(bin.getRight() instanceof Literal);
        assertEquals(30L, ((Literal) bin.getRight()).getValue());
    }

    @Test
    public void testNullLiteral() {
        SelectStmt stmt = builder.parse("SELECT * FROM users WHERE age = NULL");
        BinaryExpr bin = (BinaryExpr) stmt.getWhereClause();
        assertTrue(bin.getRight() instanceof Literal);
        assertNull(((Literal) bin.getRight()).getValue());
    }

    // ==================== 复杂查询 ====================

    @Test
    public void testComplexQuery() {
        SelectStmt stmt = builder.parse(
                "SELECT u.name, COUNT(*) AS order_count " +
                "FROM users u " +
                "INNER JOIN orders o ON u.id = o.user_id " +
                "WHERE u.age > 18 " +
                "GROUP BY u.name " +
                "HAVING COUNT(*) > 1 " +
                "ORDER BY order_count DESC " +
                "LIMIT 10 OFFSET 0");
        assertEquals("users", stmt.getTableName());
        assertEquals("u", stmt.getTableAlias());
        assertEquals(1, stmt.getJoins().size());
        assertNotNull(stmt.getWhereClause());
        assertEquals(1, stmt.getGroupBy().size());
        assertNotNull(stmt.getHavingClause());
        assertEquals(1, stmt.getOrderBy().size());
        assertEquals(Integer.valueOf(10), stmt.getLimit());
        assertEquals(Integer.valueOf(0), stmt.getOffset());
    }

    // ==================== 列引用 ====================

    @Test
    public void testColumnRef() {
        SelectStmt stmt = builder.parse("SELECT name FROM users");
        Expression item = stmt.getSelectItems().get(0);
        assertTrue(item instanceof ColumnRef);
        assertEquals("name", ((ColumnRef) item).getColumn());
        assertNull(((ColumnRef) item).getTable());
    }

    @Test
    public void testQualifiedColumnRef() {
        SelectStmt stmt = builder.parse("SELECT u.name FROM users u WHERE u.age > 18");
        Expression item = stmt.getSelectItems().get(0);
        assertTrue(item instanceof ColumnRef);
        assertEquals("u", ((ColumnRef) item).getTable());
        assertEquals("name", ((ColumnRef) item).getColumn());
    }
}
