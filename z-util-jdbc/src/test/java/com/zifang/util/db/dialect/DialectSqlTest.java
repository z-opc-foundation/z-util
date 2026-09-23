package com.zifang.util.db.dialect;

import com.zifang.util.core.lang.exception.BusinessException;
import com.zifang.util.db.meta.DataSourceDTO;
import com.zifang.util.db.support.Identifiers;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 方言层：引用符、URL、分页与类型归一的纯文本断言，不需要真实实例。
 */
public class DialectSqlTest {

    private static DataSourceDTO def(String type, String host, int port, String db) {
        DataSourceDTO dto = new DataSourceDTO();
        dto.setDatasourceCode("ds-" + type);
        dto.setDatasourceType(type);
        dto.setDatasourceUrl(host);
        dto.setPortNumber(port);
        dto.setSchemaMark(db);
        dto.setUserName("sa");
        dto.setPw("");
        return dto;
    }

    @Test
    public void quoteIdentifiersPerDialect() {
        assertEquals("`t_order`", new MySqlDialect().quote("t_order"));
        assertEquals("`db1`.`t_order`", new MySqlDialect().quote("db1.t_order"));
        assertEquals("\"t_order\"", new PostgresDialect().quote("t_order"));
        assertEquals("\"public\".\"t_order\"", new PostgresDialect().quote("public.t_order"));
        assertEquals("\"T_ORDER\"", new H2Dialect().quote("T_ORDER"));
    }

    @Test
    public void rejectUnsafeIdentifiers() {
        String[] evil = {"t_order; DROP TABLE x", "t order", "1=1", "t.a.b", ""};
        for (String name : evil) {
            try {
                new MySqlDialect().quote(name);
                fail("应拒绝标识符: " + name);
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("标识符"));
            }
        }
        assertFalse(Identifiers.isSafeQualifiedName("a b"));
        assertTrue(Identifiers.isSafeQualifiedName("db1.t_order"));
    }

    @Test
    public void buildUrls() {
        String mysql = new MySqlDialect().buildUrl(def("mysql", "10.0.0.1", 3306, "z_report"));
        assertTrue(mysql, mysql.startsWith("jdbc:mysql://10.0.0.1:3306/z_report?"));
        assertTrue(mysql, mysql.contains("useUnicode=true"));

        assertEquals("jdbc:postgresql://10.0.0.2:5432/z_report",
                new PostgresDialect().buildUrl(def("postgres", "10.0.0.2", 5432, "z_report")));

        assertEquals("jdbc:h2:mem:z_report;DB_CLOSE_DELAY=-1",
                new H2Dialect().buildUrl(def("h2", "", 0, "z_report")));
        assertEquals("jdbc:h2:tcp://db-host:9092/z_report",
                new H2Dialect().buildUrl(def("h2", "db-host", 9092, "z_report")));
    }

    @Test
    public void missingHostFailsForServerDialects() {
        try {
            new MySqlDialect().buildUrl(def("mysql", " ", 3306, "z_report"));
            fail("MySQL 缺 host 应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("datasourceUrl"));
        }
    }

    @Test
    public void paginationAndCountClauses() {
        Dialect mysql = new MySqlDialect();
        assertEquals(" LIMIT 20", mysql.limitClause(0, 20));
        assertEquals(" LIMIT 20 OFFSET 40", mysql.limitClause(40, 20));
        assertEquals("", mysql.limitClause(0, -1));
        assertEquals("SELECT COUNT(*) FROM (SELECT * FROM t) z_cnt", mysql.countSql("SELECT * FROM t"));
        assertEquals("SELECT 1", mysql.validationQuery());
        try {
            mysql.limitClause(-1, 10);
            fail("负 offset 应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("offset"));
        }
    }

    @Test
    public void resolveByIdUrlAndMetadata() {
        assertEquals(MySqlDialect.ID, Dialects.resolve("MySQL").id());
        assertEquals(MySqlDialect.ID, Dialects.resolve(" mysql ").id());
        assertEquals(PostgresDialect.ID, Dialects.resolve("postgresql").id());
        assertEquals(PostgresDialect.ID, Dialects.resolve("pg").id());
        assertEquals(H2Dialect.ID, Dialects.resolve("h2").id());
        assertEquals(MySqlDialect.ID, Dialects.forJdbcUrl("jdbc:mysql://h:3306/d").id());
        assertEquals(H2Dialect.ID, Dialects.forJdbcUrl("jdbc:h2:mem:x").id());
        assertTrue(Dialects.ids().contains("postgres"));
    }

    @Test
    public void unknownTypeAndUrlFail() {
        try {
            Dialects.resolve("oracle");
            fail("未知方言应报错");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("不支持的数据源类型"));
        }
        try {
            Dialects.resolve(null);
            fail("空方言应报错");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("datasourceType"));
        }
        try {
            Dialects.forJdbcUrl("mysql://h/d");
            fail("非法 URL 应报错");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("JDBC URL"));
        }
    }

    @Test
    public void sqlTypeNormalization() {
        assertEquals(SqlType.STRING, SqlType.of(Types.VARCHAR));
        assertEquals(SqlType.STRING, SqlType.of(Types.LONGVARCHAR));
        assertEquals(SqlType.INTEGER, SqlType.of(Types.TINYINT));
        assertEquals(SqlType.INTEGER, SqlType.of(Types.INTEGER));
        assertEquals(SqlType.LONG, SqlType.of(Types.BIGINT));
        assertEquals(SqlType.DOUBLE, SqlType.of(Types.DOUBLE));
        assertEquals(SqlType.DECIMAL, SqlType.of(Types.DECIMAL));
        assertEquals(SqlType.BOOLEAN, SqlType.of(Types.BOOLEAN));
        assertEquals(SqlType.DATE, SqlType.of(Types.DATE));
        assertEquals(SqlType.TIMESTAMP, SqlType.of(Types.TIMESTAMP));
        assertEquals(SqlType.BINARY, SqlType.of(Types.BLOB));
        assertEquals(SqlType.OTHER, SqlType.of(Types.ARRAY));
    }
}
