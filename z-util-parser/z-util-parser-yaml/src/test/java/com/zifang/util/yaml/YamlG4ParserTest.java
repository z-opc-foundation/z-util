package com.zifang.util.yaml;

import com.zifang.util.yaml.model.YamlMap;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * YamlG4Parser 测试。
 * 验证 G4 DSL 加载 + YAML 解析链路。
 */
/**
 * YamlG4ParserTest类。
 */

/**
 * YamlG4ParserTest类。
 */
public class YamlG4ParserTest {

    private final YamlG4Parser parser = new YamlG4Parser();

    @Test
    /**
     * testLoadG4Files方法。
     */
    /**
     * testLoadG4Files方法。
     */
    public void testLoadG4Files() {
        // 能加载 G4 文件不抛异常即通过
        YamlMap result = parser.parseMap("key: value");
        assertEquals("value", result.get("key"));
    }

    @Test
    /**
     * testSimpleMap方法。
     */
    /**
     * testSimpleMap方法。
     */
    public void testSimpleMap() {
        String yaml = "name: zifang\nage: 30";
        YamlMap map = parser.parseMap(yaml);
        assertEquals("zifang", map.get("name"));
        assertEquals(30.0, map.get("age"));
    }

    @Test
    /**
     * testNestedMap方法。
     */
    /**
     * testNestedMap方法。
     */
    public void testNestedMap() {
        String yaml = "server:\n  host: localhost\n  port: 8080";
        YamlMap map = parser.parseMap(yaml);
        assertNotNull(map.get("server"));
        assertTrue(map.get("server") instanceof YamlMap);
    }

    @Test
    /**
     * testSequence方法。
     */
    /**
     * testSequence方法。
     */
    public void testSequence() {
        String yaml = "- item1\n- item2\n- item3";
        Object result = parser.parse(yaml);
        assertTrue(result instanceof com.zifang.util.yaml.model.YamlArray);
        com.zifang.util.yaml.model.YamlArray arr = (com.zifang.util.yaml.model.YamlArray) result;
        assertEquals(3, arr.size());
    }
}