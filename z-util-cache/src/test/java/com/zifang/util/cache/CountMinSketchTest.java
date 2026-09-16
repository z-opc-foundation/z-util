package com.zifang.util.cache;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 自研 4-bit Count-Min Sketch 频次估计算法正确性测试。
 */
public class CountMinSketchTest {

    @Test
    public void testEstimate_monotonic() {
        // 高频 key 的估频次应该 ≥ 低频 key
        CountMinSketch cms = new CountMinSketch(64, 1024);
        cms.increment("hot");
        cms.increment("hot");
        cms.increment("hot");
        cms.increment("hot");
        cms.increment("hot");
        cms.increment("warm");
        cms.increment("warm");
        cms.increment("cold");

        int hot = cms.estimate("hot");
        int warm = cms.estimate("warm");
        int cold = cms.estimate("cold");
        assertTrue("hot=" + hot + " should > warm=" + warm, hot > warm);
        assertTrue("warm=" + warm + " should > cold=" + cold, warm > cold);
        assertTrue("estimate must saturate at 15, got " + hot, hot <= 15);
    }

    @Test
    public void testSaturateAt15() {
        CountMinSketch cms = new CountMinSketch(16, 100000);
        for (int i = 0; i < 1000; i++) cms.increment("x");
        assertEquals(15, cms.estimate("x"));
    }

    @Test
    public void testReset_halvesFrequency() {
        // sampleSize=10 → 第 10 次 increment 触发 reset
        // 行为：频次先 +1 到 10，然后 /2 = 5
        CountMinSketch cms = new CountMinSketch(16, 10);
        for (int i = 0; i < 9; i++) cms.increment("k");
        int before = cms.estimate("k");  // 9
        // 第 10 次：会触发 reset，estimate 变 5
        cms.increment("k");
        int after = cms.estimate("k");
        assertTrue("reset should halve: before=" + before + " after=" + after, after < before);
        assertTrue("after should be around before/2, got before=" + before + " after=" + after,
                Math.abs(after - before / 2) <= 1);
        assertTrue(after >= 1);
    }

    @Test
    public void testConstruction_tableSizeMustBePowerOf2() {
        try {
            new CountMinSketch(63, 10);
            fail("expected exception");
        } catch (IllegalArgumentException expected) { /* ok */ }
    }
}
