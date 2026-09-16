package com.zifang.util.workflow.config;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Engine 类测试
 */

/**
 * EngineTest类。
 */
public class EngineTest {

    @Test
    /**
     * testEngineCreation方法。
     */
    public void testEngineCreation() {
        Engine engine = new Engine();
        assertNotNull(engine);
    }

    @Test
    /**
     * testEngineType方法。
     */
    public void testEngineType() {
        Engine engine = new Engine();
        engine.setType("java");
        assertEquals("java", engine.getType());

        engine.setType("python");
        assertEquals("python", engine.getType());

        engine.setType("spark");
        assertEquals("spark", engine.getType());
    }

    @Test
    /**
     * testEngineMode方法。
     */
    public void testEngineMode() {
        Engine engine = new Engine();
        engine.setMode("local");
        assertEquals("local", engine.getMode());

        engine.setMode("cluster");
        assertEquals("cluster", engine.getMode());

        engine.setMode("distributed");
        assertEquals("distributed", engine.getMode());
    }

    @Test
    /**
     * testEngineProperties方法。
     */
    public void testEngineProperties() {
        Engine engine = new Engine();

        Map<String, String> properties = new HashMap<>();
        properties.put("memory", "4g");
        properties.put("cores", "4");
        properties.put("timeout", "3600");

        engine.setProperties(properties);

        Map<String, String> retrievedProperties = engine.getProperties();
        assertNotNull(retrievedProperties);
        assertEquals("4g", retrievedProperties.get("memory"));
        assertEquals("4", retrievedProperties.get("cores"));
        assertEquals("3600", retrievedProperties.get("timeout"));
    }

    @Test
    /**
     * testEngineWithEmptyProperties方法。
     */
    public void testEngineWithEmptyProperties() {
        Engine engine = new Engine();
        engine.setProperties(new HashMap<>());

        Map<String, String> properties = engine.getProperties();
        assertNotNull(properties);
        assertTrue(properties.isEmpty());
    }

    @Test
    /**
     * testEngineWithNullProperties方法。
     */
    public void testEngineWithNullProperties() {
        Engine engine = new Engine();
        engine.setProperties(null);

        Map<String, String> properties = engine.getProperties();
        assertNull(properties);
    }

    @Test
    /**
     * testEngineEquality方法。
     */
    public void testEngineEquality() {
        Engine engine1 = new Engine();
        engine1.setType("java");
        engine1.setMode("local");

        Engine engine2 = new Engine();
        engine2.setType("java");
        engine2.setMode("local");

        // 测试两个 Engine 的属性是否相等
        assertEquals(engine1.getType(), engine2.getType());
        assertEquals(engine1.getMode(), engine2.getMode());
    }

    @Test
    /**
     * testEngineComplexConfiguration方法。
     */
    public void testEngineComplexConfiguration() {
        Engine engine = new Engine();

        // 设置复杂的配置
        engine.setType("spark");
        engine.setMode("cluster");

        Map<String, String> properties = new HashMap<>();
        properties.put("spark.executor.memory", "8g");
        properties.put("spark.executor.cores", "4");
        properties.put("spark.driver.memory", "4g");
        properties.put("spark.sql.adaptive.enabled", "true");
        properties.put("spark.sql.adaptive.coalescePartitions.enabled", "true");

        engine.setProperties(properties);

        // 验证配置
        assertEquals("spark", engine.getType());
        assertEquals("cluster", engine.getMode());
        assertEquals("8g", engine.getProperties().get("spark.executor.memory"));
        assertEquals("true", engine.getProperties().get("spark.sql.adaptive.enabled"));
    }
}
