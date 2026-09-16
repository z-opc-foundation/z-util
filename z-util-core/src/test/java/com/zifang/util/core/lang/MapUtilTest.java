package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * MapUtilTest类。
 */
public class MapUtilTest {

    // --- newHashMap ---

    @Test
    /**
     * testNewHashMap_WithDefaultSize方法。
     */
    public void testNewHashMap_WithDefaultSize() {
        HashMap<String, String> map = MapUtil.newHashMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    /**
     * testNewHashMap_WithExpectedSize方法。
     */
    public void testNewHashMap_WithExpectedSize() {
        HashMap<String, String> map = MapUtil.newHashMap(10);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testNewHashMap_WithNegativeSize方法。
     */
    public void testNewHashMap_WithNegativeSize() {
        MapUtil.newHashMap(-1);
    }

    // --- newLinkedHashMap ---

    @Test
    /**
     * testNewLinkedHashMap_WithExpectedSize方法。
     */
    public void testNewLinkedHashMap_WithExpectedSize() {
        LinkedHashMap<String, String> map = MapUtil.newLinkedHashMap(10);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testNewLinkedHashMap_WithNegativeSize方法。
     */
    public void testNewLinkedHashMap_WithNegativeSize() {
        MapUtil.newLinkedHashMap(-1);
    }

    // --- isEmpty ---

    @Test
    /**
     * testIsEmpty_WithNull方法。
     */
    public void testIsEmpty_WithNull() {
        assertTrue(MapUtil.isEmpty(null));
    }

    @Test
    /**
     * testIsEmpty_WithEmptyMap方法。
     */
    public void testIsEmpty_WithEmptyMap() {
        assertTrue(MapUtil.isEmpty(new HashMap<>()));
    }

    @Test
    /**
     * testIsEmpty_WithNonEmptyMap方法。
     */
    public void testIsEmpty_WithNonEmptyMap() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertFalse(MapUtil.isEmpty(map));
    }

    // --- isNotEmpty ---

    @Test
    /**
     * testIsNotEmpty_WithNull方法。
     */
    public void testIsNotEmpty_WithNull() {
        assertFalse(MapUtil.isNotEmpty(null));
    }

    @Test
    /**
     * testIsNotEmpty_WithEmptyMap方法。
     */
    public void testIsNotEmpty_WithEmptyMap() {
        assertFalse(MapUtil.isNotEmpty(new HashMap<>()));
    }

    @Test
    /**
     * testIsNotEmpty_WithNonEmptyMap方法。
     */
    public void testIsNotEmpty_WithNonEmptyMap() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertTrue(MapUtil.isNotEmpty(map));
    }

    // --- parseValue ---

    @Test
    /**
     * testParseValue_WithNullMap方法。
     */
    public void testParseValue_WithNullMap() {
        assertNull(MapUtil.parseValue(null, "key"));
    }

    @Test
    /**
     * testParseValue_WithEmptyMap方法。
     */
    public void testParseValue_WithEmptyMap() {
        assertNull(MapUtil.parseValue(new HashMap<>(), "key"));
    }

    @Test
    /**
     * testParseValue_WithExistingKey方法。
     */
    public void testParseValue_WithExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        assertEquals("value", MapUtil.parseValue(map, "key"));
    }

    @Test
    /**
     * testParseValue_WithNonExistingKey方法。
     */
    public void testParseValue_WithNonExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        assertNull(MapUtil.parseValue(map, "nonexistent"));
    }

    // --- parseValueOrDefault ---

    @Test
    /**
     * testParseValueOrDefault_WithNullMap方法。
     */
    public void testParseValueOrDefault_WithNullMap() {
        assertEquals("default", MapUtil.parseValueOrDefault(null, "key", "default"));
    }

    @Test
    /**
     * testParseValueOrDefault_WithEmptyMap方法。
     */
    public void testParseValueOrDefault_WithEmptyMap() {
        assertEquals("default", MapUtil.parseValueOrDefault(new HashMap<>(), "key", "default"));
    }

    @Test
    /**
     * testParseValueOrDefault_WithExistingKey方法。
     */
    public void testParseValueOrDefault_WithExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        assertEquals("value", MapUtil.parseValueOrDefault(map, "key", "default"));
    }

    @Test
    /**
     * testParseValueOrDefault_WithNonExistingKey方法。
     */
    public void testParseValueOrDefault_WithNonExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        // Note: parseValueOrDefault returns null for non-existing key (not default)
        // It only returns default when map is empty
        assertNull(MapUtil.parseValueOrDefault(map, "nonexistent", "default"));
    }

    // --- parseStringValue ---

    @Test
    /**
     * testParseStringValue_WithNullMap方法。
     */
    public void testParseStringValue_WithNullMap() {
        assertNull(MapUtil.parseStringValue(null, "key"));
    }

    @Test
    /**
     * testParseStringValue_WithExistingKey方法。
     */
    public void testParseStringValue_WithExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        assertEquals("value", MapUtil.parseStringValue(map, "key"));
    }

    // --- parseByteValue ---

    @Test
    /**
     * testParseByteValue_WithNullMap方法。
     */
    public void testParseByteValue_WithNullMap() {
        assertNull(MapUtil.parseByteValue(null, "key"));
    }

    @Test
    /**
     * testParseByteValue_WithExistingKey方法。
     */
    public void testParseByteValue_WithExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", (byte) 5);
        assertEquals(Byte.valueOf((byte) 5), MapUtil.parseByteValue(map, "key"));
    }

    // --- parseShortValue ---

    @Test
    /**
     * testParseShortValue_WithNullMap方法。
     */
    public void testParseShortValue_WithNullMap() {
        assertNull(MapUtil.parseShortValue(null, "key"));
    }

    @Test
    /**
     * testParseShortValue_WithExistingKey方法。
     */
    public void testParseShortValue_WithExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", (short) 10);
        assertEquals(Short.valueOf((short) 10), MapUtil.parseShortValue(map, "key"));
    }

    // --- parseIntegerValue ---

    @Test
    /**
     * testParseIntegerValue_WithNullMap方法。
     */
    public void testParseIntegerValue_WithNullMap() {
        assertNull(MapUtil.parseIntegerValue(null, "key"));
    }

    @Test
    /**
     * testParseIntegerValue_WithExistingKey方法。
     */
    public void testParseIntegerValue_WithExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 42);
        assertEquals(Integer.valueOf(42), MapUtil.parseIntegerValue(map, "key"));
    }

    // --- parseLongValue ---

    @Test
    /**
     * testParseLongValue_WithNullMap方法。
     */
    public void testParseLongValue_WithNullMap() {
        assertNull(MapUtil.parseLongValue(null, "key"));
    }

    @Test
    /**
     * testParseLongValue_WithExistingKey方法。
     */
    public void testParseLongValue_WithExistingKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 100L);
        assertEquals(Long.valueOf(100L), MapUtil.parseLongValue(map, "key"));
    }

    // --- fromProperties ---

    @Test
    /**
     * testFromProperties_WithNull方法。
     */
    public void testFromProperties_WithNull() {
        assertNotNull(MapUtil.fromProperties(null));
        assertTrue(MapUtil.fromProperties(null).isEmpty());
    }

    @Test
    /**
     * testFromProperties_WithValidProperties方法。
     */
    public void testFromProperties_WithValidProperties() {
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");
        Map<String, String> map = MapUtil.fromProperties(props);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    // --- mergePropertiesIntoMap ---

    @Test
    /**
     * testMergePropertiesIntoMap_WithNullProperties方法。
     */
    public void testMergePropertiesIntoMap_WithNullProperties() {
        Map<String, Object> map = new HashMap<>();
        MapUtil.mergePropertiesIntoMap(null, map);
        assertTrue(map.isEmpty());
    }

    @Test
    /**
     * testMergePropertiesIntoMap_WithValidProperties方法。
     */
    public void testMergePropertiesIntoMap_WithValidProperties() {
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        Map<String, Object> map = new HashMap<>();
        MapUtil.mergePropertiesIntoMap(props, map);
        assertEquals(1, map.size());
        assertEquals("value1", map.get("key1"));
    }

    // --- replaceKey ---

    @Test
    /**
     * testReplaceKey_WithMatchingKeys方法。
     */
    public void testReplaceKey_WithMatchingKeys() {
        Map<String, String> map = new HashMap<>();
        map.put("old_key", "value");
        Map<String, String> result = MapUtil.replaceKey(map, "old", "new");
        assertEquals("value", result.get("new_key"));
        assertFalse(result.containsKey("old_key"));
    }

    @Test
    /**
     * testReplaceKey_WithNoMatchingKeys方法。
     */
    public void testReplaceKey_WithNoMatchingKeys() {
        Map<String, String> map = new HashMap<>();
        map.put("unique_key", "value");
        Map<String, String> result = MapUtil.replaceKey(map, "old", "new");
        assertEquals("value", result.get("unique_key"));
    }

    // --- trimValue ---

    @Test
    /**
     * testTrimValue_WithNull方法。
     */
    public void testTrimValue_WithNull() {
        assertNull(MapUtil.trimValue(null));
    }

    @Test
    /**
     * testTrimValue_WithEmptyMap方法。
     */
    public void testTrimValue_WithEmptyMap() {
        Map<String, String> map = new HashMap<>();
        Map<String, String> result = MapUtil.trimValue(map);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testTrimValue_WithMixedValues方法。
     */
    public void testTrimValue_WithMixedValues() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", null);
        map.put("key3", "value3");
        Map<String, String> result = MapUtil.trimValue(map);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("key1"));
        assertTrue(result.containsKey("key3"));
        assertFalse(result.containsKey("key2"));
    }

    @Test
    /**
     * testTrimValue_ModifiesOriginalMap方法。
     */
    public void testTrimValue_ModifiesOriginalMap() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", null);
        Map<String, String> result = MapUtil.trimValue(map);
        assertSame(map, result);
    }

    @Test
    /**
     * testGetCacheFirst方法。
     */
    public void testGetCacheFirst() {
        Map<String, Integer> cache = new HashMap<>();
        cache.put("a", 1);
        AtomicInteger loadCount = new AtomicInteger();
        // 命中缓存不触发加载
        assertEquals(Integer.valueOf(1), MapUtil.getCacheFirst(cache, "a", k -> {
            loadCount.incrementAndGet();
            return 99;
        }));
        assertEquals(0, loadCount.get());
        // 未命中时加载并写回缓存
        assertEquals(Integer.valueOf(2), MapUtil.getCacheFirst(cache, "b", k -> {
            loadCount.incrementAndGet();
            return 2;
        }));
        assertEquals(1, loadCount.get());
        assertEquals(Integer.valueOf(2), cache.get("b"));
        // 第二次命中缓存
        assertEquals(Integer.valueOf(2), MapUtil.getCacheFirst(cache, "b", k -> {
            loadCount.incrementAndGet();
            return 99;
        }));
        assertEquals(1, loadCount.get());
        // 加载返回null时不写缓存
        assertNull(MapUtil.getCacheFirst(cache, "c", k -> null));
        assertFalse(cache.containsKey("c"));
        // map为null时直接加载
        assertEquals(Integer.valueOf(5), MapUtil.getCacheFirst(null, "x", k -> 5));
    }
}
