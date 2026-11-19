package com.zifang.util.monitor.sampling;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 采样样本测试。
 */
class SampleTest {

    @Test
    /**
     * testBuilderStoresValues方法。
     */
    void testBuilderStoresValues() {
        Sample sample = Sample.builder(1000L)
                .put("a", 1.5)
                .put("b", 7)
                .attribute("host", "node-1")
                .build();

        assertEquals(1000L, sample.getTimestamp());
        assertEquals(1.5, sample.getDouble("a", -1));
        assertEquals(7L, sample.get("b").longValue());
        assertEquals("node-1", sample.getAttributes().get("host"));
        assertEquals(2, sample.metrics().size());
    }

    @Test
    /**
     * testNonFiniteValuesAreDropped方法。
     */
    void testNonFiniteValuesAreDropped() {
        Sample sample = Sample.builder(1000L)
                .put("nan", Double.NaN)
                .put("inf", Double.POSITIVE_INFINITY)
                .put("ok", 1)
                .build();

        assertEquals(1, sample.metrics().size());
        assertFalse(sample.metrics().contains("nan"));
        assertFalse(sample.metrics().contains("inf"));
    }

    @Test
    /**
     * testMissingMetricReturnsDefault方法。
     */
    void testMissingMetricReturnsDefault() {
        Sample sample = Sample.builder(1000L).put("a", 1).build();

        assertNull(sample.get("missing"));
        assertEquals(-1.0, sample.getDouble("missing", -1));
    }

    @Test
    /**
     * testEstimatedBytesGrowsWithMetrics方法。
     */
    void testEstimatedBytesGrowsWithMetrics() {
        Sample empty = Sample.builder(1000L).build();
        Sample loaded = Sample.builder(1000L).put("a", 1).put("b", 2).put("c", 3).build();

        assertTrue(loaded.estimatedBytes() > empty.estimatedBytes());
    }

    @Test
    /**
     * testSampleIsImmutable方法。
     */
    void testSampleIsImmutable() {
        Map<String, Number> values = new LinkedHashMap<>();
        values.put("a", 1);
        Sample sample = new Sample(1000L, values, null);

        values.put("b", 2);
        assertEquals(1, sample.metrics().size());

        try {
            sample.getValues().put("c", 3);
            org.junit.jupiter.api.Assertions.fail("样本的指标表应当是不可变的");
        } catch (UnsupportedOperationException expected) {
            // 预期行为
        }
    }
}
