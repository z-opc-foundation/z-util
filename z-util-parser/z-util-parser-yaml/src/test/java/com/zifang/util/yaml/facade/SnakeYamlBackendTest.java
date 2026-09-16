package com.zifang.util.yaml.facade;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * SnakeYamlBackend 测试。
 */
/**
 * SnakeYamlBackendTest类。
 */

/**
 * SnakeYamlBackendTest类。
 */
public class SnakeYamlBackendTest {

    private final SnakeYamlBackend backend = new SnakeYamlBackend();

    // ==================== toYaml / fromYaml ====================

    @Test
    /**
     * testToYamlFromYaml_Roundtrip方法。
     */
    /**
     * testToYamlFromYaml_Roundtrip方法。
     */
    public void testToYamlFromYaml_Roundtrip() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "zifang");
        data.put("age", 30);
        data.put("active", true);
        String yaml = backend.toYaml(data);
        assertNotNull(yaml);
        assertTrue(yaml.contains("name: zifang"));

        Map<?, ?> restored = backend.fromYaml(yaml, Map.class);
        assertEquals("zifang", restored.get("name"));
        assertEquals(30, restored.get("age"));
    }

    @Test
    /**
     * testToYaml_Null方法。
     */
    /**
     * testToYaml_Null方法。
     */
    public void testToYaml_Null() {
        assertEquals("null", backend.toYaml(null));
    }

    @Test
    /**
     * testFromYaml_NullOrEmpty方法。
     */
    /**
     * testFromYaml_NullOrEmpty方法。
     */
    public void testFromYaml_NullOrEmpty() {
        assertNull(backend.fromYaml(null, Map.class));
        assertNull(backend.fromYaml("", Map.class));
        assertNull(backend.fromYaml("   ", Map.class));
    }

    @Test
    /**
     * testFromYaml_List方法。
     */
    /**
     * testFromYaml_List方法。
     */
    public void testFromYaml_List() {
        String yaml = "- name: alice\n  age: 25\n- name: bob\n  age: 30";
        YamlTypeBinding<List<Map<String, Object>>> binding = new YamlTypeBinding<List<Map<String, Object>>>() {
        };
        List<Map<String, Object>> list = backend.fromYaml(yaml, binding);
        assertEquals(2, list.size());
        assertEquals("alice", list.get(0).get("name"));
    }

    @Test
    /**
     * testToPrettyYaml方法。
     */
    /**
     * testToPrettyYaml方法。
     */
    public void testToPrettyYaml() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "zifang");
        data.put("items", Arrays.asList(1, 2, 3));
        String pretty = backend.toPrettyYaml(data);
        assertNotNull(pretty);
        // block style 应有换行
        assertTrue(pretty.contains("\n"));
    }

    // ==================== engine ====================

    @Test
    /**
     * testEngine方法。
     */
    /**
     * testEngine方法。
     */
    public void testEngine() {
        assertEquals("SnakeYAML", backend.engine());
    }

    // ==================== isValidYaml ====================

    @Test
    /**
     * testIsValidYaml方法。
     */
    /**
     * testIsValidYaml方法。
     */
    public void testIsValidYaml() {
        assertTrue(backend.isValidYaml("name: test\nage: 30"));
        assertTrue(backend.isValidYaml("- a\n- b"));
        assertFalse(backend.isValidYaml("{invalid"));
        assertFalse(backend.isValidYaml(null));
        assertFalse(backend.isValidYaml(""));
    }
}