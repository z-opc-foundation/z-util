package com.zifang.util.workflow.config;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Configurations 类测试
 */

/**
 * ConfigurationsTest类。
 */
public class ConfigurationsTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        Configurations configurations = new Configurations();
        assertNotNull(configurations);
        assertNull(configurations.getWorkflowConfigurationId());
        assertNull(configurations.getEngine());
        assertNull(configurations.getCacheEngine());
        assertNull(configurations.getPersonalEnvironment());
        assertNull(configurations.getRuntimeParameter());
    }

    @Test
    /**
     * testWorkflowConfigurationId方法。
     */
    public void testWorkflowConfigurationId() {
        Configurations configurations = new Configurations();

        configurations.setWorkflowConfigurationId(100);
        assertEquals(100, configurations.getWorkflowConfigurationId().intValue());

        configurations.setWorkflowConfigurationId(0);
        assertEquals(0, configurations.getWorkflowConfigurationId().intValue());
    }

    @Test
    /**
     * testEngine方法。
     */
    public void testEngine() {
        Configurations configurations = new Configurations();

        Engine engine = new Engine();
        engine.setType("spark");
        engine.setMode("cluster");

        configurations.setEngine(engine);

        assertNotNull(configurations.getEngine());
        assertEquals("spark", configurations.getEngine().getType());
        assertEquals("cluster", configurations.getEngine().getMode());
    }

    @Test
    /**
     * testEngineCanBeNull方法。
     */
    public void testEngineCanBeNull() {
        Configurations configurations = new Configurations();
        configurations.setEngine(null);
        assertNull(configurations.getEngine());
    }

    @Test
    /**
     * testCacheEngine方法。
     */
    public void testCacheEngine() {
        Configurations configurations = new Configurations();

        CacheEngine cacheEngine = new CacheEngine();
        cacheEngine.setEngineType("redis");
        cacheEngine.setCacheEngineService("redis://localhost:6379");

        configurations.setCacheEngine(cacheEngine);

        assertNotNull(configurations.getCacheEngine());
        assertEquals("redis", configurations.getCacheEngine().getEngineType());
        assertEquals("redis://localhost:6379", configurations.getCacheEngine().getCacheEngineService());
    }

    @Test
    /**
     * testCacheEngineCanBeNull方法。
     */
    public void testCacheEngineCanBeNull() {
        Configurations configurations = new Configurations();
        configurations.setCacheEngine(null);
        assertNull(configurations.getCacheEngine());
    }

    @Test
    /**
     * testPersonalEnvironment方法。
     */
    public void testPersonalEnvironment() {
        Configurations configurations = new Configurations();

        Map<String, String> env = new HashMap<>();
        env.put("user", "admin");
        env.put("role", "developer");

        configurations.setPersonalEnvironment(env);

        assertNotNull(configurations.getPersonalEnvironment());
        assertEquals("admin", configurations.getPersonalEnvironment().get("user"));
        assertEquals("developer", configurations.getPersonalEnvironment().get("role"));
    }

    @Test
    /**
     * testPersonalEnvironmentCanBeNull方法。
     */
    public void testPersonalEnvironmentCanBeNull() {
        Configurations configurations = new Configurations();
        configurations.setPersonalEnvironment(null);
        assertNull(configurations.getPersonalEnvironment());
    }

    @Test
    /**
     * testRuntimeParameter方法。
     */
    public void testRuntimeParameter() {
        Configurations configurations = new Configurations();

        Map<String, String> runtimeParams = new HashMap<>();
        runtimeParams.put("mode", "production");
        runtimeParams.put("debug", "false");

        configurations.setRuntimeParameter(runtimeParams);

        assertNotNull(configurations.getRuntimeParameter());
        assertEquals("production", configurations.getRuntimeParameter().get("mode"));
        assertEquals("false", configurations.getRuntimeParameter().get("debug"));
    }

    @Test
    /**
     * testRuntimeParameterCanBeNull方法。
     */
    public void testRuntimeParameterCanBeNull() {
        Configurations configurations = new Configurations();
        configurations.setRuntimeParameter(null);
        assertNull(configurations.getRuntimeParameter());
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        Configurations config1 = new Configurations();
        config1.setWorkflowConfigurationId(1);

        Configurations config2 = new Configurations();
        config2.setWorkflowConfigurationId(1);

        assertEquals(config1, config2);
    }

    @Test
    /**
     * testEqualsWithDifferentIds方法。
     */
    public void testEqualsWithDifferentIds() {
        Configurations config1 = new Configurations();
        config1.setWorkflowConfigurationId(1);

        Configurations config2 = new Configurations();
        config2.setWorkflowConfigurationId(2);

        assertNotEquals(config1, config2);
    }

    @Test
    /**
     * testEqualsWithSameObject方法。
     */
    public void testEqualsWithSameObject() {
        Configurations config = new Configurations();
        assertEquals(config, config);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        Configurations config = new Configurations();
        assertNotEquals(config, null);
    }

    @Test
    /**
     * testEqualsWithDifferentClass方法。
     */
    public void testEqualsWithDifferentClass() {
        Configurations config = new Configurations();
        assertNotEquals(config, "not a configurations");
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        Configurations config1 = new Configurations();
        config1.setWorkflowConfigurationId(1);

        Configurations config2 = new Configurations();
        config2.setWorkflowConfigurationId(1);

        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    /**
     * testHashCodeConsistency方法。
     */
    public void testHashCodeConsistency() {
        Configurations config = new Configurations();
        int hashCode1 = config.hashCode();
        int hashCode2 = config.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        Configurations configurations = new Configurations();
        String str = configurations.toString();

        assertNotNull(str);
        assertTrue(str.contains("Configurations"));
    }

    @Test
    /**
     * testComplexConfigurations方法。
     */
    public void testComplexConfigurations() {
        Configurations configurations = new Configurations();
        configurations.setWorkflowConfigurationId(42);

        // 设置 Engine
        Engine engine = new Engine();
        engine.setType("spark");
        engine.setMode("cluster");
        Map<String, String> engineProps = new HashMap<>();
        engineProps.put("spark.executor.memory", "8g");
        engineProps.put("spark.executor.cores", "4");
        engine.setProperties(engineProps);
        configurations.setEngine(engine);

        // 设置 CacheEngine
        CacheEngine cacheEngine = new CacheEngine();
        cacheEngine.setEngineType("redis");
        cacheEngine.setCacheEngineService("redis://cache-server:6379");
        configurations.setCacheEngine(cacheEngine);

        // 设置 PersonalEnvironment
        Map<String, String> personalEnv = new HashMap<>();
        personalEnv.put("username", "testuser");
        personalEnv.put("tenant", "default");
        configurations.setPersonalEnvironment(personalEnv);

        // 设置 RuntimeParameter
        Map<String, String> runtimeParams = new HashMap<>();
        runtimeParams.put("executionMode", "async");
        runtimeParams.put("retryCount", "3");
        configurations.setRuntimeParameter(runtimeParams);

        // 验证
        assertEquals(42, configurations.getWorkflowConfigurationId().intValue());

        assertNotNull(configurations.getEngine());
        assertEquals("spark", configurations.getEngine().getType());
        assertEquals("cluster", configurations.getEngine().getMode());
        assertEquals("8g", configurations.getEngine().getProperties().get("spark.executor.memory"));

        assertNotNull(configurations.getCacheEngine());
        assertEquals("redis", configurations.getCacheEngine().getEngineType());
        assertEquals("redis://cache-server:6379", configurations.getCacheEngine().getCacheEngineService());

        assertNotNull(configurations.getPersonalEnvironment());
        assertEquals("testuser", configurations.getPersonalEnvironment().get("username"));
        assertEquals("default", configurations.getPersonalEnvironment().get("tenant"));

        assertNotNull(configurations.getRuntimeParameter());
        assertEquals("async", configurations.getRuntimeParameter().get("executionMode"));
        assertEquals("3", configurations.getRuntimeParameter().get("retryCount"));
    }
}
