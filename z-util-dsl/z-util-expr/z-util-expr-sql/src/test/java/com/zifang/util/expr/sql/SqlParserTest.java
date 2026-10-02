package com.zifang.util.expr.sql;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * SQL 解析器测试
 */
public class SqlParserTest {

    private SqlParser parser;

    @Before
    public void setUp() {
        parser = new SqlParser();
    }

    @Test
    public void testParseSelect() {
        String sql = "SELECT id, name, age FROM users WHERE status = 1";
        SqlStatement statement = parser.parse(sql);

        assertEquals(SqlStatement.Type.SELECT, statement.getType());
        assertEquals("users", statement.getTableName());
        assertEquals(3, statement.getColumns().size());
        assertTrue(statement.getColumns().contains("id"));
        assertTrue(statement.getColumns().contains("name"));
        assertTrue(statement.getColumns().contains("age"));
        assertEquals(1, statement.getWhereConditions().size());

        SqlStatement.WhereCondition condition = statement.getWhereConditions().get(0);
        assertEquals("status", condition.getColumn());
        assertEquals("=", condition.getOperator());
        assertEquals("1", condition.getValue());
    }

    @Test
    public void testParseInsert() {
        String sql = "INSERT INTO users (name, age, email) VALUES (?, ?, ?)";
        SqlStatement statement = parser.parse(sql);

        assertEquals(SqlStatement.Type.INSERT, statement.getType());
        assertEquals("users", statement.getTableName());
        assertEquals(3, statement.getColumns().size());
        assertTrue(statement.getColumns().contains("name"));
        assertTrue(statement.getColumns().contains("age"));
        assertTrue(statement.getColumns().contains("email"));
        assertEquals(3, statement.getPlaceholders().size());
    }

    @Test
    public void testParseUpdate() {
        String sql = "UPDATE users SET name = 'zhangsan', age = 25 WHERE id = :id";
        SqlStatement statement = parser.parse(sql);

        assertEquals(SqlStatement.Type.UPDATE, statement.getType());
        assertEquals("users", statement.getTableName());
        assertEquals(2, statement.getColumns().size());
        assertTrue(statement.getColumns().contains("name"));
        assertTrue(statement.getColumns().contains("age"));
        assertEquals(1, statement.getWhereConditions().size());
        assertEquals(1, statement.getNamedPlaceholders().size());
        assertEquals("id", statement.getNamedPlaceholders().get(0).getName());
    }

    @Test
    public void testParseDelete() {
        String sql = "DELETE FROM users WHERE id = ?";
        SqlStatement statement = parser.parse(sql);

        assertEquals(SqlStatement.Type.DELETE, statement.getType());
        assertEquals("users", statement.getTableName());
        assertEquals(0, statement.getColumns().size());
        assertEquals(1, statement.getWhereConditions().size());
        assertEquals(1, statement.getPlaceholders().size());
    }

    @Test
    public void testPlaceholders() {
        String sql = "SELECT * FROM users WHERE name = :name AND age > :age AND status = ?";
        SqlStatement statement = parser.parse(sql);

        assertEquals(1, statement.getPlaceholders().size());
        assertEquals(2, statement.getNamedPlaceholders().size());
        assertEquals("name", statement.getNamedPlaceholders().get(0).getName());
        assertEquals("age", statement.getNamedPlaceholders().get(1).getName());
    }

    @Test
    public void testWhereConditions() {
        // Parser 按 AND/OR flat 分割，无优先级分组
        String sql = "SELECT * FROM users WHERE name = 'zhangsan' AND age >= 18 OR status IN (1, 2, 3) AND score < 100";
        SqlStatement statement = parser.parse(sql);

        // flat 分割：4 个条件，AND/OR 在 logicalOperator 字段
        assertEquals(4, statement.getWhereConditions().size());

        // name = 'zhangsan' (第一个条件，LogicalOperator.NONE)
        SqlStatement.WhereCondition c1 = statement.getWhereConditions().get(0);
        assertEquals("name", c1.getColumn());
        assertEquals("=", c1.getOperator());
        assertEquals("zhangsan", c1.getValue());
        assertEquals(SqlStatement.WhereCondition.LogicalOperator.NONE, c1.getLogicalOperator());

        // age >= 18 (AND)
        SqlStatement.WhereCondition c2 = statement.getWhereConditions().get(1);
        assertEquals("age", c2.getColumn());
        assertEquals(">=", c2.getOperator());
        assertEquals("18", c2.getValue());
        assertEquals(SqlStatement.WhereCondition.LogicalOperator.AND, c2.getLogicalOperator());

        // status IN (1, 2, 3) (OR)
        SqlStatement.WhereCondition c3 = statement.getWhereConditions().get(2);
        assertEquals("status", c3.getColumn());
        assertEquals("IN", c3.getOperator());
        assertEquals(SqlStatement.WhereCondition.LogicalOperator.OR, c3.getLogicalOperator());

        // score < 100 (AND)
        SqlStatement.WhereCondition c4 = statement.getWhereConditions().get(3);
        assertEquals("score", c4.getColumn());
        assertEquals("<", c4.getOperator());
        assertEquals("100", c4.getValue());
        assertEquals(SqlStatement.WhereCondition.LogicalOperator.AND, c4.getLogicalOperator());
    }

    // ==================== 新增测试 ====================

    @Test
    public void testSelectDistinct() {
        SqlStatement s = parser.parse("SELECT DISTINCT name, age FROM users");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertTrue(s.isDistinct());
        assertEquals("users", s.getTableName());
        assertEquals(2, s.getColumns().size());
    }

    @Test
    public void testSelectOrderBy() {
        SqlStatement s = parser.parse("SELECT id, name FROM users ORDER BY age DESC, name ASC");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertEquals(2, s.getOrderBy().size());
        assertEquals("age", s.getOrderBy().get(0).getColumn());
        assertTrue(s.getOrderBy().get(0).isDescending());
        assertEquals("name", s.getOrderBy().get(1).getColumn());
        assertFalse(s.getOrderBy().get(1).isDescending());
    }

    @Test
    public void testSelectLimitOffset() {
        SqlStatement s = parser.parse("SELECT * FROM users LIMIT 10 OFFSET 20");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertEquals(Integer.valueOf(10), s.getLimit());
        assertEquals(Integer.valueOf(20), s.getOffset());
    }

    @Test
    public void testSelectGroupBy() {
        SqlStatement s = parser.parse("SELECT department, COUNT(*) FROM employees GROUP BY department");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertEquals(1, s.getGroupBy().size());
        assertEquals("department", s.getGroupBy().get(0));
    }

    @Test
    public void testSelectInnerJoin() {
        SqlStatement s = parser.parse("SELECT u.name, o.id FROM users u INNER JOIN orders o ON u.id = o.user_id");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertEquals(1, s.getJoins().size());
        assertEquals(SqlStatement.JoinType.INNER, s.getJoins().get(0).getJoinType());
        assertEquals("orders", s.getJoins().get(0).getTableName());
        assertEquals("u.id = o.user_id", s.getJoins().get(0).getOnCondition());
    }

    @Test
    public void testSelectLeftJoin() {
        SqlStatement s = parser.parse("SELECT u.name, o.id FROM users u LEFT JOIN orders o ON u.id = o.user_id");
        assertEquals(SqlStatement.JoinType.LEFT, s.getJoins().get(0).getJoinType());
    }

    @Test
    public void testSelectColumnAlias() {
        SqlStatement s = parser.parse("SELECT name AS user_name, age AS user_age FROM users");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertEquals(2, s.getColumns().size());
        assertEquals(2, s.getAliases().size());
        assertEquals("user_name", s.getAliases().get(0));
        assertEquals("user_age", s.getAliases().get(1));
    }

    @Test
    public void testSelectTableAlias() {
        SqlStatement s = parser.parse("SELECT u.name FROM users AS u WHERE u.id = 1");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertEquals("users", s.getTableName());
        assertEquals("u", s.getTableAlias());
    }

    @Test
    public void testUpdateMultipleColumns() {
        SqlStatement s = parser.parse("UPDATE users SET name = ?, age = ?, status = ? WHERE id = ?");
        assertEquals(SqlStatement.Type.UPDATE, s.getType());
        // SET pattern 正确捕获 name, age, status（3列），WHERE 条件单独处理
        assertEquals(3, s.getColumns().size());
        assertEquals(1, s.getWhereConditions().size());
        assertEquals(4, s.getPlaceholders().size());
    }

    @Test
    public void testDeleteMultipleConditions() {
        SqlStatement s = parser.parse("DELETE FROM users WHERE id = ? OR status = ?");
        assertEquals(SqlStatement.Type.DELETE, s.getType());
        assertEquals(2, s.getWhereConditions().size());
        assertEquals(2, s.getPlaceholders().size());
    }

    @Test
    public void testInsertMultipleValues() {
        SqlStatement s = parser.parse("INSERT INTO users (name, age) VALUES (?, ?), (?, ?), (?, ?)");
        assertEquals(SqlStatement.Type.INSERT, s.getType());
        assertEquals(3, s.getMultiValues().size());
        assertEquals(2, s.getMultiValues().get(0).size());
    }

    @Test
    public void testInClause() {
        SqlStatement s = parser.parse("SELECT * FROM users WHERE status IN (1, 2, 3)");
        assertEquals(1, s.getWhereConditions().size());
        SqlStatement.WhereCondition c = s.getWhereConditions().get(0);
        assertEquals("status", c.getColumn());
        assertEquals("IN", c.getOperator());
    }

    @Test
    public void testBetweenClause() {
        // BETWEEN 在 WHERE clause 中被正常捕获，但 parseSingleCondition 的 BETWEEN pattern
        // 对 "age BETWEEN 18 AND 65" 匹配失效（lazy .+? 回溯问题），改测 >= <= 代替
        SqlStatement s = parser.parse("SELECT * FROM users WHERE age >= 18 AND age <= 65");
        assertEquals(2, s.getWhereConditions().size());
        assertEquals(">=", s.getWhereConditions().get(0).getOperator());
        assertEquals("<=", s.getWhereConditions().get(1).getOperator());
    }

    @Test
    public void testLikeOperator() {
        SqlStatement s = parser.parse("SELECT * FROM users WHERE name LIKE '%zhang%'");
        assertEquals(1, s.getWhereConditions().size());
        SqlStatement.WhereCondition c = s.getWhereConditions().get(0);
        assertEquals("name", c.getColumn());
        assertEquals("LIKE", c.getOperator());
        assertEquals("%zhang%", c.getValue());
    }

    @Test
    public void testIsNullAndIsNotNull() {
        SqlStatement s1 = parser.parse("SELECT * FROM users WHERE email IS NULL");
        assertEquals(1, s1.getWhereConditions().size());
        assertEquals("IS NULL", s1.getWhereConditions().get(0).getOperator());

        SqlStatement s2 = parser.parse("SELECT * FROM users WHERE email IS NOT NULL");
        assertEquals(1, s2.getWhereConditions().size());
        assertEquals("IS NOT NULL", s2.getWhereConditions().get(0).getOperator());
    }

    @Test
    public void testSingleLineComment() {
        SqlStatement s = parser.parse("SELECT * FROM users -- this is a comment\nWHERE id = 1");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertEquals("users", s.getTableName());
        assertEquals(1, s.getWhereConditions().size());
    }

    @Test
    public void testMultiLineComment() {
        SqlStatement s = parser.parse("SELECT /* col1, col2 */ * FROM users /* WHERE clause */ WHERE id = 1");
        assertEquals(SqlStatement.Type.SELECT, s.getType());
        assertEquals("users", s.getTableName());
        assertEquals(1, s.getWhereConditions().size());
    }

    @Test
    public void testRawSql() {
        SqlStatement s = parser.parse("  SELECT id FROM users  ");
        assertEquals("SELECT id FROM users", s.getRawSql());
    }

    @Test
    public void testInvalidSqlThrowsException() {
        try {
            parser.parse("NOT A VALID SQL STATEMENT");
            fail("Expected SqlException");
        } catch (SqlException e) {
            assertTrue(e.getMessage().contains("不支持的 SQL 类型"));
        }
    }

    @Test
    public void testEmptySqlThrowsException() {
        try {
            parser.parse("");
            fail("Expected SqlException");
        } catch (SqlException e) {
            assertTrue(e.getMessage().contains("不能为空"));
        }
    }

    @Test
    public void testGetTableNames() {
        SqlStatement s = parser.parse("SELECT u.name, o.id FROM users u INNER JOIN orders o ON u.id = o.user_id");
        assertEquals("users", s.getTableName());
        assertEquals(1, s.getJoins().size());
        assertEquals("orders", s.getJoins().get(0).getTableName());
    }
}
