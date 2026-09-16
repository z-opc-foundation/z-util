package com.zifang.util.workflow.config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CacheEngine 类测试
 */

/**
 * CacheEngineTest类。
 */
public class CacheEngineTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        CacheEngine cacheEngine = new CacheEngine();
        assertNotNull(cacheEngine);
        assertNull(cacheEngine.getEngineType());
        assertNull(cacheEngine.getCacheEngineService());
    }

    @Test
    /**
     * testConstructorWithParameters方法。
     */
    public void testConstructorWithParameters() {
        CacheEngine cacheEngine = new CacheEngine("redis", "redis://localhost:6379");

        assertEquals("redis", cacheEngine.getEngineType());
        assertEquals("redis://localhost:6379", cacheEngine.getCacheEngineService());
    }

    @Test
    /**
     * testEngineType方法。
     */
    public void testEngineType() {
        CacheEngine cacheEngine = new CacheEngine();

        cacheEngine.setEngineType("memcached");
        assertEquals("memcached", cacheEngine.getEngineType());

        cacheEngine.setEngineType("caffeine");
        assertEquals("caffeine", cacheEngine.getEngineType());
    }

    @Test
    /**
     * testCacheEngineService方法。
     */
    public void testCacheEngineService() {
        CacheEngine cacheEngine = new CacheEngine();

        cacheEngine.setCacheEngineService("redis://cache1:6379");
        assertEquals("redis://cache1:6379", cacheEngine.getCacheEngineService());

        cacheEngine.setCacheEngineService("ehcache://localhost:40000");
        assertEquals("ehcache://localhost:40000", cacheEngine.getCacheEngineService());
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        CacheEngine cache1 = new CacheEngine("redis", "redis://localhost:6379");
        CacheEngine cache2 = new CacheEngine("redis", "redis://localhost:6379");

        assertEquals(cache1, cache2);
    }

    @Test
    /**
     * testEqualsWithDifferentEngineType方法。
     */
    public void testEqualsWithDifferentEngineType() {
        CacheEngine cache1 = new CacheEngine("redis", "redis://localhost:6379");
        CacheEngine cache2 = new CacheEngine("memcached", "redis://localhost:6379");

        assertNotEquals(cache1, cache2);
    }

    @Test
    /**
     * testEqualsWithDifferentService方法。
     */
    public void testEqualsWithDifferentService() {
        CacheEngine cache1 = new CacheEngine("redis", "redis://localhost:6379");
        CacheEngine cache2 = new CacheEngine("redis", "redis://other:6379");

        assertNotEquals(cache1, cache2);
    }

    @Test
    /**
     * testEqualsWithSameObject方法。
     */
    public void testEqualsWithSameObject() {
        CacheEngine cache = new CacheEngine();
        assertEquals(cache, cache);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        CacheEngine cache = new CacheEngine();
        assertNotEquals(cache, null);
    }

    @Test
    /**
     * testEqualsWithDifferentClass方法。
     */
    public void testEqualsWithDifferentClass() {
        CacheEngine cache = new CacheEngine();
        assertNotEquals(cache, "not a cache engine");
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        CacheEngine cache1 = new CacheEngine("redis", "redis://localhost:6379");
        CacheEngine cache2 = new CacheEngine("redis", "redis://localhost:6379");

        assertEquals(cache1.hashCode(), cache2.hashCode());
    }

    @Test
    /**
     * testHashCodeConsistency方法。
     */
    public void testHashCodeConsistency() {
        CacheEngine cache = new CacheEngine("redis", "redis://localhost:6379");
        int hashCode1 = cache.hashCode();
        int hashCode2 = cache.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        CacheEngine cacheEngine = new CacheEngine("redis", "redis://localhost:6379");
        String str = cacheEngine.toString();

        assertNotNull(str);
        assertTrue(str.contains("CacheEngine"));
        assertTrue(str.contains("redis"));
        assertTrue(str.contains("redis://localhost:6379"));
    }

    @Test
    /**
     * testToStringWithNullValues方法。
     */
    public void testToStringWithNullValues() {
        CacheEngine cacheEngine = new CacheEngine();
        String str = cacheEngine.toString();

        assertNotNull(str);
        assertTrue(str.contains("CacheEngine"));
    }

    @Test
    /**
     * testCommonCacheEngineTypes方法。
     */
    public void testCommonCacheEngineTypes() {
        // Redis
        CacheEngine redis = new CacheEngine();
        redis.setEngineType("redis");
        redis.setCacheEngineService("redis://redis-cluster:6379");
        assertEquals("redis", redis.getEngineType());

        // Memcached
        CacheEngine memcached = new CacheEngine();
        memcached.setEngineType("memcached");
        memcached.setCacheEngineService("memcached://memcached-server:11211");
        assertEquals("memcached", memcached.getEngineType());

        // Ehcache
        CacheEngine ehcache = new CacheEngine();
        ehcache.setEngineType("ehcache");
        ehcache.setCacheEngineService("ehcache://ehcache-server:40000");
        assertEquals("ehcache", ehcache.getEngineType());

        // Caffeine
        CacheEngine caffeine = new CacheEngine();
        caffeine.setEngineType("caffeine");
        caffeine.setCacheEngineService("caffeine://local");
        assertEquals("caffeine", caffeine.getEngineType());
    }
}
