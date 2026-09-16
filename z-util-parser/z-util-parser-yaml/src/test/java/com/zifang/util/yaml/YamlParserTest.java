package com.zifang.util.yaml;

import com.zifang.util.yaml.define.YamlNodeType;
import com.zifang.util.yaml.dsl.YamlPathParser;
import com.zifang.util.yaml.exception.YamlParseException;
import com.zifang.util.yaml.model.YamlArray;
import com.zifang.util.yaml.model.YamlMap;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * YamlParserTest类。
 */

/**
 * YamlParserTest类。
 */
public class YamlParserTest {

    private final YamlParser parser = new YamlParser();

    // --- parse(String) tests ---

    @Test
    /**
     * testParse_NullString方法。
     */
    /**
     * testParse_NullString方法。
     */
    public void testParse_NullString() {
        assertNull(parser.parse(null));
    }

    @Test
    /**
     * testParse_EmptyString方法。
     */
    /**
     * testParse_EmptyString方法。
     */
    public void testParse_EmptyString() {
        assertNull(parser.parse(""));
    }

    @Test
    /**
     * testParse_ScalarString方法。
     */
    /**
     * testParse_ScalarString方法。
     */
    public void testParse_ScalarString() {
        Object result = parser.parse("hello");
        assertEquals("hello", result);
    }

    @Test
    /**
     * testParse_ScalarNumber方法。
     */
    /**
     * testParse_ScalarNumber方法。
     */
    public void testParse_ScalarNumber() {
        Object result = parser.parse("42");
        assertEquals(42, result);
    }

    @Test
    /**
     * testParse_ScalarFloat方法。
     */
    /**
     * testParse_ScalarFloat方法。
     */
    public void testParse_ScalarFloat() {
        Object result = parser.parse("3.14");
        assertEquals(3.14, result);
    }

    @Test
    /**
     * testParse_BooleanTrue方法。
     */
    /**
     * testParse_BooleanTrue方法。
     */
    public void testParse_BooleanTrue() {
        Object result = parser.parse("true");
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    /**
     * testParse_BooleanFalse方法。
     */
    /**
     * testParse_BooleanFalse方法。
     */
    public void testParse_BooleanFalse() {
        Object result = parser.parse("false");
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    /**
     * testParse_Null方法。
     */
    /**
     * testParse_Null方法。
     */
    public void testParse_Null() {
        Object result = parser.parse("null");
        assertNull(result);
    }

    @Test
    /**
     * testParse_SimpleMap方法。
     */
    /**
     * testParse_SimpleMap方法。
     */
    public void testParse_SimpleMap() {
        String yaml = "name: zifang\nage: 18";
        Object result = parser.parse(yaml);
        assertTrue(result instanceof YamlMap);
        YamlMap map = (YamlMap) result;
        assertEquals("zifang", map.getString("name"));
        assertEquals(18, map.get("age"));
    }

    @Test
    /**
     * testParse_NestedMap方法。
     */
    /**
     * testParse_NestedMap方法。
     */
    public void testParse_NestedMap() {
        String yaml = "config:\n  database:\n    host: localhost\n    port: 3306";
        Object result = parser.parse(yaml);
        assertTrue(result instanceof YamlMap);
        YamlMap root = (YamlMap) result;
        YamlMap config = root.getMap("config");
        assertNotNull(config);
        YamlMap database = config.getMap("database");
        assertEquals("localhost", database.getString("host"));
        assertEquals(3306, database.get("port"));
    }

    @Test
    /**
     * testParse_SimpleArray方法。
     */
    /**
     * testParse_SimpleArray方法。
     */
    public void testParse_SimpleArray() {
        String yaml = "- apple\n- banana\n- cherry";
        Object result = parser.parse(yaml);
        assertTrue(result instanceof YamlArray);
        YamlArray array = (YamlArray) result;
        assertEquals(3, array.size());
        assertEquals("apple", array.get(0));
        assertEquals("banana", array.get(1));
        assertEquals("cherry", array.get(2));
    }

    @Test
    /**
     * testParse_ArrayOfMaps方法。
     */
    /**
     * testParse_ArrayOfMaps方法。
     */
    public void testParse_ArrayOfMaps() {
        String yaml = "- name: Alice\n  age: 20\n- name: Bob\n  age: 30";
        Object result = parser.parse(yaml);
        assertTrue(result instanceof YamlArray);
        YamlArray array = (YamlArray) result;
        assertEquals(2, array.size());
        YamlMap first = (YamlMap) array.get(0);
        assertEquals("Alice", first.getString("name"));
        assertEquals(20, first.get("age"));
    }

    @Test
    /**
     * testParse_MixedStructure方法。
     */
    /**
     * testParse_MixedStructure方法。
     */
    public void testParse_MixedStructure() {
        String yaml = "name: test\nitems:\n  - id: 1\n    value: foo\n  - id: 2\n    value: bar";
        Object result = parser.parse(yaml);
        assertTrue(result instanceof YamlMap);
        YamlMap root = (YamlMap) result;
        assertEquals("test", root.getString("name"));
        YamlArray items = root.getArray("items");
        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test(expected = YamlParseException.class)
    /**
     * testParse_InvalidYaml方法。
     */
    /**
     * testParse_InvalidYaml方法。
     */
    public void testParse_InvalidYaml() {
        parser.parse("  invalid:\nyaml: [");
    }

    // --- toYaml(Object) tests ---

    @Test
    /**
     * testToYaml_Map方法。
     */
    /**
     * testToYaml_Map方法。
     */
    public void testToYaml_Map() {
        YamlMap map = new YamlMap();
        map.put("name", "test");
        map.put("value", 123);
        String yaml = parser.toYaml(map);
        assertNotNull(yaml);
        assertTrue(yaml.contains("name: test"));
    }

    @Test
    /**
     * testToYaml_List方法。
     */
    /**
     * testToYaml_List方法。
     */
    public void testToYaml_List() {
        YamlArray array = new YamlArray();
        array.add("a");
        array.add("b");
        String yaml = parser.toYaml(array);
        assertNotNull(yaml);
        assertTrue("YAML should contain list items: " + yaml, yaml.contains("a") && yaml.contains("b"));
    }

    @Test
    /**
     * testToYaml_Null方法。
     */
    /**
     * testToYaml_Null方法。
     */
    public void testToYaml_Null() {
        assertEquals("null\n", parser.toYaml(null));
    }

    // --- toPrettyYaml(Object) tests ---

    @Test
    /**
     * testToPrettyYaml_Map方法。
     */
    /**
     * testToPrettyYaml_Map方法。
     */
    public void testToPrettyYaml_Map() {
        YamlMap map = new YamlMap();
        map.put("name", "test");
        String yaml = parser.toPrettyYaml(map);
        assertNotNull(yaml);
        assertTrue(yaml.contains("name:"));
    }

    // --- parse(String, Class) tests ---

    @Test
    /**
     * testParseToClass方法。
     */
    /**
     * testParseToClass方法。
     */
    public void testParseToClass() {
        String yaml = "name: Alice\nage: 30";
        @SuppressWarnings("unchecked")
        Map<String, Object> map = parser.parse(yaml, Map.class);
        assertNotNull(map);
        assertEquals("Alice", map.get("name"));
    }

    // --- toString() of model classes ---

    @Test
    /**
     * testYamlMapToString方法。
     */
    /**
     * testYamlMapToString方法。
     */
    public void testYamlMapToString() {
        YamlMap map = new YamlMap();
        map.put("key", "value");
        assertTrue(map.toString().contains("key"));
    }

    @Test
    /**
     * testYamlArrayToString方法。
     */
    /**
     * testYamlArrayToString方法。
     */
    public void testYamlArrayToString() {
        YamlArray array = new YamlArray();
        array.add("item");
        assertTrue(array.toString().contains("item"));
    }

    @Test
    /**
     * testYamlMapNodeType方法。
     */
    /**
     * testYamlMapNodeType方法。
     */
    public void testYamlMapNodeType() {
        assertEquals(YamlNodeType.MAP, new YamlMap().getNodeType());
    }

    @Test
    /**
     * testYamlArrayNodeType方法。
     */
    /**
     * testYamlArrayNodeType方法。
     */
    public void testYamlArrayNodeType() {
        assertEquals(YamlNodeType.SEQUENCE, new YamlArray().getNodeType());
    }

    // --- YamlPathParser integration ---

    @Test
    /**
     * testYamlPathParser_SimpleKey方法。
     */
    /**
     * testYamlPathParser_SimpleKey方法。
     */
    public void testYamlPathParser_SimpleKey() {
        String yaml = "name: zifang\nage: 18";
        List<Object> results = new YamlPathParser().query(yaml, "$.name");
        assertEquals(1, results.size());
        assertEquals("zifang", results.get(0));
    }

    @Test
    /**
     * testYamlPathParser_NestedKey方法。
     */
    /**
     * testYamlPathParser_NestedKey方法。
     */
    public void testYamlPathParser_NestedKey() {
        String yaml = "config:\n  host: localhost";
        List<Object> results = new YamlPathParser().query(yaml, "$.config.host");
        assertEquals(1, results.size());
        assertEquals("localhost", results.get(0));
    }

    @Test
    /**
     * testYamlPathParser_ArrayIndex方法。
     */
    /**
     * testYamlPathParser_ArrayIndex方法。
     */
    public void testYamlPathParser_ArrayIndex() {
        String yaml = "items:\n  - apple\n  - banana";
        List<Object> results = new YamlPathParser().query(yaml, "$.items[0]");
        assertEquals(1, results.size());
        assertEquals("apple", results.get(0));
    }

    @Test
    /**
     * testYamlPathParser_ArrayWildcard方法。
     */
    /**
     * testYamlPathParser_ArrayWildcard方法。
     */
    public void testYamlPathParser_ArrayWildcard() {
        String yaml = "items:\n  - a\n  - b";
        List<Object> results = new YamlPathParser().query(yaml, "$.items[*]");
        assertEquals(2, results.size());
    }

    @Test
    /**
     * testYamlPathParser_InvalidPath方法。
     */
    /**
     * testYamlPathParser_InvalidPath方法。
     */
    public void testYamlPathParser_InvalidPath() {
        String yaml = "name: test";
        List<Object> results = new YamlPathParser().query(yaml, "$.nonexistent");
        assertTrue(results.isEmpty());
    }
}
