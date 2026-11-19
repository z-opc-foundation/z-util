package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 磁盘采样存储测试。
 */
class FileSampleStoreTest {

    @TempDir
    Path tempDir;

    private static Sample sampleAt(long timestamp, double value) {
        return Sample.builder(timestamp)
                .put("metric.a", value)
                .put("metric.b", value * 2)
                .attribute("host.name", "node-1")
                .build();
    }

    @Test
    /**
     * testAppendAndQuery方法。
     */
    void testAppendAndQuery() {
        FileSampleStore store = FileSampleStore.builder(tempDir).build();
        store.append(sampleAt(1000, 1));
        store.append(sampleAt(2000, 2));
        store.append(sampleAt(3000, 3));

        assertEquals(3, store.size());
        List<Sample> samples = store.query();
        assertEquals(3, samples.size());
        assertEquals(1000, samples.get(0).getTimestamp());
        assertEquals(1.0, samples.get(0).getDouble("metric.a", -1));
        assertEquals(2.0, samples.get(0).getDouble("metric.b", -1));
        assertEquals("node-1", samples.get(0).getAttributes().get("host.name"));

        assertEquals(1, store.query(1500, 2500).size());
        assertEquals(3000, store.latest().getTimestamp());
        assertEquals(3, store.stats().getTotalAppended());
        store.close();
    }

    @Test
    /**
     * testFilesAreCreated方法。
     */
    void testFilesAreCreated() {
        FileSampleStore store = FileSampleStore.builder(tempDir).build();
        store.append(sampleAt(1000, 1));
        store.close();

        assertTrue(store.listFiles().size() >= 1);
        assertTrue(store.estimatedBytes() > 0);
    }

    @Test
    /**
     * testRotation方法。
     */
    void testRotation() {
        FileSampleStore store = FileSampleStore.builder(tempDir)
                .maxFileBytes(200)
                .maxFiles(2)
                .build();
        for (int i = 0; i < 50; i++) {
            store.append(sampleAt(1000 + i, i));
        }
        store.close();

        assertTrue(store.listFiles().size() <= 2, "轮转后文件数应不超过上限");
        assertTrue(store.stats().getTotalEvicted() > 0);
    }

    @Test
    /**
     * testClear方法。
     */
    void testClear() {
        FileSampleStore store = FileSampleStore.builder(tempDir).build();
        store.append(sampleAt(1000, 1));
        assertEquals(1, store.size());

        store.clear();
        assertEquals(0, store.size());
        assertNull(store.latest());
        assertNotNull(store);
    }
}
