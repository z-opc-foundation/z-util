package com.zifang.util.db.respository;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * BoundSql 类测试
 */

/**
 * BoundSqlTest类。
 */
public class BoundSqlTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        BoundSql boundSql = new BoundSql();
        assertNotNull(boundSql);
        assertNull(boundSql.getOriginSql());
        assertNull(boundSql.getTransformSql());
        assertNull(boundSql.getIndexName());
        assertNull(boundSql.getIndexValue());
        assertNull(boundSql.getIndexValueInsert());
    }

    @Test
    /**
     * testOriginSql方法。
     */
    public void testOriginSql() {
        BoundSql boundSql = new BoundSql();
        boundSql.setOriginSql("SELECT * FROM user WHERE id = ?");

        assertEquals("SELECT * FROM user WHERE id = ?", boundSql.getOriginSql());
    }

    @Test
    /**
     * testTransformSql方法。
     */
    public void testTransformSql() {
        BoundSql boundSql = new BoundSql();
        boundSql.setTransformSql("SELECT * FROM user WHERE id = #{id}");

        assertEquals("SELECT * FROM user WHERE id = #{id}", boundSql.getTransformSql());
    }

    @Test
    /**
     * testIndexName方法。
     */
    public void testIndexName() {
        BoundSql boundSql = new BoundSql();
        Map<Integer, String> indexName = new HashMap<>();
        indexName.put(1, "id");
        indexName.put(2, "name");

        boundSql.setIndexName(indexName);

        assertEquals(2, boundSql.getIndexName().size());
        assertEquals("id", boundSql.getIndexName().get(1));
        assertEquals("name", boundSql.getIndexName().get(2));
    }

    @Test
    /**
     * testIndexValue方法。
     */
    public void testIndexValue() {
        BoundSql boundSql = new BoundSql();
        Map<Integer, Object> indexValue = new HashMap<>();
        indexValue.put(1, 100);
        indexValue.put(2, "test");

        boundSql.setIndexValue(indexValue);

        assertEquals(2, boundSql.getIndexValue().size());
        assertEquals(100, boundSql.getIndexValue().get(1));
        assertEquals("test", boundSql.getIndexValue().get(2));
    }

    @Test
    /**
     * testIndexValueInsert方法。
     */
    public void testIndexValueInsert() {
        BoundSql boundSql = new BoundSql();
        Map<Integer, Object> indexValueInsert = new HashMap<>();
        indexValueInsert.put(1, "value1");
        indexValueInsert.put(2, "value2");

        boundSql.setIndexValueInsert(indexValueInsert);

        assertEquals(2, boundSql.getIndexValueInsert().size());
        assertEquals("value1", boundSql.getIndexValueInsert().get(1));
        assertEquals("value2", boundSql.getIndexValueInsert().get(2));
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        BoundSql sql1 = new BoundSql();
        sql1.setOriginSql("SELECT 1");

        BoundSql sql2 = new BoundSql();
        sql2.setOriginSql("SELECT 1");

        assertEquals(sql1, sql2);
    }

    @Test
    /**
     * testEqualsWithDifferentSql方法。
     */
    public void testEqualsWithDifferentSql() {
        BoundSql sql1 = new BoundSql();
        sql1.setOriginSql("SELECT 1");

        BoundSql sql2 = new BoundSql();
        sql2.setOriginSql("SELECT 2");

        assertNotEquals(sql1, sql2);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        BoundSql sql = new BoundSql();
        assertNotEquals(sql, null);
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        BoundSql sql1 = new BoundSql();
        sql1.setOriginSql("SELECT 1");

        BoundSql sql2 = new BoundSql();
        sql2.setOriginSql("SELECT 1");

        assertEquals(sql1.hashCode(), sql2.hashCode());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        BoundSql sql = new BoundSql();
        sql.setOriginSql("SELECT 1");
        String str = sql.toString();

        assertNotNull(str);
        assertTrue(str.contains("BoundSql"));
        assertTrue(str.contains("SELECT 1"));
    }

    @Test
    /**
     * testFullConfiguration方法。
     */
    public void testFullConfiguration() {
        BoundSql boundSql = new BoundSql();
        boundSql.setOriginSql("SELECT * FROM user WHERE id = ? AND name = ?");
        boundSql.setTransformSql("SELECT * FROM user WHERE id = #{id} AND name = #{name}");

        Map<Integer, String> indexName = new HashMap<>();
        indexName.put(1, "id");
        indexName.put(2, "name");
        boundSql.setIndexName(indexName);

        Map<Integer, Object> indexValue = new HashMap<>();
        indexValue.put(1, 100);
        indexValue.put(2, "test");
        boundSql.setIndexValue(indexValue);

        Map<Integer, Object> indexValueInsert = new HashMap<>();
        indexValueInsert.put(1, "insert1");
        indexValueInsert.put(2, "insert2");
        boundSql.setIndexValueInsert(indexValueInsert);

        assertEquals("SELECT * FROM user WHERE id = ? AND name = ?", boundSql.getOriginSql());
        assertEquals("SELECT * FROM user WHERE id = #{id} AND name = #{name}", boundSql.getTransformSql());
        assertEquals(2, boundSql.getIndexName().size());
        assertEquals(2, boundSql.getIndexValue().size());
        assertEquals(2, boundSql.getIndexValueInsert().size());
    }
}