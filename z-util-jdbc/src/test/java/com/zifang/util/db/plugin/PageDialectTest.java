package com.zifang.util.db.plugin;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * PageDialectTest类。
 */
public class PageDialectTest {

    @Test
    /**
     * testResolve_byProductName方法。
     */
    public void testResolve_byProductName() throws SQLException {
        // LIMIT方言
        assertEquals(PageDialect.LIMIT, PageDialect.resolve("MySQL"));
        assertEquals(PageDialect.LIMIT, PageDialect.resolve("MariaDB"));
        assertEquals(PageDialect.LIMIT, PageDialect.resolve("PostgreSQL"));
        assertEquals(PageDialect.LIMIT, PageDialect.resolve("H2"));
        assertEquals(PageDialect.LIMIT, PageDialect.resolve("SQLite"));
        assertEquals(PageDialect.LIMIT, PageDialect.resolve("HSQL Database Engine"));
        // ROWNUM方言
        assertEquals(PageDialect.ROWNUM, PageDialect.resolve("Oracle"));
        // FETCH方言
        assertEquals(PageDialect.FETCH, PageDialect.resolve("Microsoft SQL Server"));
        assertEquals(PageDialect.FETCH, PageDialect.resolve("Apache Derby"));
        assertEquals(PageDialect.FETCH, PageDialect.resolve("DB2/NT"));
        // 未识别的产品默认LIMIT
        assertEquals(PageDialect.LIMIT, PageDialect.resolve("SomeUnknownDB"));
        assertEquals(PageDialect.LIMIT, PageDialect.resolve((String) null));
        assertEquals(PageDialect.LIMIT, PageDialect.resolve((java.sql.Connection) null));
    }

    @Test
    /**
     * testBuildPageSql_limit方法。
     */
    public void testBuildPageSql_limit() {
        assertEquals("SELECT * FROM t LIMIT 10 OFFSET 20",
                PageDialect.LIMIT.buildPageSql("SELECT * FROM t", 20, 10));
    }

    @Test
    /**
     * testBuildPageSql_rownum方法。
     */
    public void testBuildPageSql_rownum() {
        String sql = PageDialect.ROWNUM.buildPageSql("SELECT * FROM t", 20, 10);
        assertTrue(sql.contains("ROWNUM <= 30"));
        assertTrue(sql.contains("rownum_ > 20"));
        assertTrue(sql.contains("SELECT * FROM t"));
    }

    @Test
    /**
     * testBuildPageSql_fetch方法。
     */
    public void testBuildPageSql_fetch() {
        assertEquals("SELECT * FROM t OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY",
                PageDialect.FETCH.buildPageSql("SELECT * FROM t", 20, 10));
    }

    @Test
    /**
     * testBuildPageSql_firstPage方法。
     */
    public void testBuildPageSql_firstPage() {
        assertEquals("SELECT * FROM t LIMIT 10 OFFSET 0",
                PageDialect.LIMIT.buildPageSql("SELECT * FROM t", 0, 10));
    }
}
