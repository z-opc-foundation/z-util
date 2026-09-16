package com.zifang.util.core.feature;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 自研特性开关测试。
 */
public class FeatureManagerTest {

    @Test
    public void testEnableDisable() {
        InMemoryFeatureStore store = new InMemoryFeatureStore().define("a", true);
        FeatureManager fm = new FeatureManager(store);
        assertTrue(fm.isEnabled("a"));
        assertFalse(fm.isEnabled("b"));
        store.setEnabled("a", false);
        assertFalse(fm.isEnabled("a"));
    }

    @Test
    public void testAttributeMatching() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("region", "CN");
        InMemoryFeatureStore store = new InMemoryFeatureStore()
                .define("promo", true, attrs);
        FeatureManager fm = new FeatureManager(store);
        assertTrue(fm.isEnabled("promo", attrs));
        Map<String, String> us = new HashMap<>();
        us.put("region", "US");
        assertFalse(fm.isEnabled("promo", us));
    }

    @Test
    public void testRunIf() {
        InMemoryFeatureStore store = new InMemoryFeatureStore().define("x", true);
        FeatureManager fm = new FeatureManager(store);
        String r = fm.runIf("x", () -> "new", () -> "old");
        assertEquals("new", r);
        store.setEnabled("x", false);
        assertEquals("old", fm.runIf("x", () -> "new", () -> "old"));
    }

    @Test
    public void testRunIfRunnable() {
        InMemoryFeatureStore store = new InMemoryFeatureStore().define("x", true);
        FeatureManager fm = new FeatureManager(store);
        String[] box = new String[1];
        fm.runIf("x", () -> box[0] = "ran", () -> box[0] = "skipped");
        assertEquals("ran", box[0]);
    }

    @Test
    public void testFeatureKeys() {
        InMemoryFeatureStore store = new InMemoryFeatureStore()
                .define("a", true).define("b", false);
        assertEquals(2, store.featureKeys().size());
    }
}
