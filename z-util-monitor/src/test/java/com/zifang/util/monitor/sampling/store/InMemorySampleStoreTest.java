package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内存采样存储测试。
 */
class InMemorySampleStoreTest {

    private static Sample sampleAt(long timestamp, double value) {
        return Sample.builder(timestamp).put("m", value).build();
    }

    @Test
    /**
     * testAppendAndQuery方法。
     */
    void testAppendAndQuery() {
        InMemorySampleStore store = InMemorySampleStore.builder().build();
        store.append(sampleAt(1000, 1));
        store.append(sampleAt(2000, 2));
        store.append(sampleAt(3000, 3));

        assertEquals(3, store.size());
        List<Sample> all = store.query();
        assertEquals(3, all.size());
        assertEquals(1.0, all.get(0).getDouble("m", -1));
        assertEquals(3.0, all.get(2).getDouble("m", -1));

        List<Sample> ranged = store.query(1500, 2500);
        assertEquals(1, ranged.size());
        assertEquals(2000, ranged.get(0).getTimestamp());

        assertEquals(3000, store.latest().getTimestamp());
        assertEquals(3, store.stats().getTotalAppended());
    }

    @Test
    /**
     * testFifoEviction方法。
     */
    void testFifoEviction() {
        InMemorySampleStore store = InMemorySampleStore.builder().maxSamples(2).build();
        store.append(sampleAt(1000, 1));
        store.append(sampleAt(2000, 2));
        store.append(sampleAt(3000, 3));

        assertEquals(2, store.size());
        assertEquals(2000, store.query().get(0).getTimestamp());
        assertEquals(1, store.stats().getTotalEvicted());
    }

    @Test
    /**
     * testTtlEviction方法。
     */
    void testTtlEviction() {
        InMemorySampleStore store = InMemorySampleStore.builder()
                .maxAgeMillis(TimeUnit.MINUTES.toMillis(5))
                .build();
        long now = System.currentTimeMillis();
        store.append(sampleAt(now - TimeUnit.MINUTES.toMillis(10), 1));
        store.append(sampleAt(now, 2));

        assertEquals(1, store.size());
        assertEquals(2.0, store.latest().getDouble("m", -1));
    }

    @Test
    /**
     * testCapacityEviction方法。
     */
    void testCapacityEviction() {
        InMemorySampleStore store = InMemorySampleStore.builder().maxBytes(1).build();
        store.append(sampleAt(1000, 1));
        store.append(sampleAt(2000, 2));

        assertEquals(0, store.size());
        assertNull(store.latest());
    }

    @Test
    /**
     * testClear方法。
     */
    void testClear() {
        InMemorySampleStore store = InMemorySampleStore.builder().build();
        store.append(sampleAt(1000, 1));
        assertEquals(1, store.size());

        store.clear();
        assertEquals(0, store.size());
        assertNull(store.latest());
    }

    @Test
    /**
     * testConcurrentAccess方法。
     */
    void testConcurrentAccess() throws Exception {
        InMemorySampleStore store = InMemorySampleStore.builder().maxSamples(100).build();
        int threads = 4;
        int perThread = 250;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            pool.execute(() -> {
                try {
                    for (int j = 0; j < perThread; j++) {
                        store.append(sampleAt(System.currentTimeMillis(), j));
                        store.query();
                    }
                } finally {
                    done.countDown();
                }
            });
        }
        assertTrue(done.await(30, TimeUnit.SECONDS));
        pool.shutdown();

        assertTrue(store.size() <= 100);
        assertEquals(threads * perThread, store.stats().getTotalAppended());
        assertNotNull(store.stats());
    }
}
