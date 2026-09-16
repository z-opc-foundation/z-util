package com.zifang.util.distributes.sequence;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * 新增 ID 生成器测试。
 */
public class NewIdGeneratorTest {

    // ===== SegmentIdGenerator =====

    @Test
    public void testSegmentIdGenerator_inMemoryLoader() {
        // 内存 loader：每次返回 [base+1, base+step]
        long[] counter = {0L};
        SegmentIdGenerator.SegmentLoader loader = (bizTag, step) -> {
            long base = counter[0];
            counter[0] += step;
            return new long[]{base + 1, base + step};
        };
        SegmentIdGenerator g = new SegmentIdGenerator("order", 5, loader);
        // 第一段：1..5
        for (int i = 1; i <= 5; i++) assertEquals(i, g.nextId());
        // 第二段：6..10（触发重新加载）
        for (int i = 6; i <= 10; i++) assertEquals(i, g.nextId());
    }

    @Test
    public void testSegmentIdGenerator_concurrency() throws Exception {
        SegmentIdGenerator.SegmentLoader loader = (bizTag, step) -> {
            long base = System.nanoTime() & 0xFFFFFFFL;
            return new long[]{base + 1, base + step};
        };
        SegmentIdGenerator g = new SegmentIdGenerator("conc", 100, loader);
        int n = 50;
        Thread[] ts = new Thread[4];
        Set<Long> all = java.util.Collections.synchronizedSet(new HashSet<>());
        for (int i = 0; i < ts.length; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < n; j++) all.add(g.nextId());
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        // 期望至少 4 * 50 = 200 个不同 ID
        assertTrue("expected >= 200 unique, got " + all.size(), all.size() >= n);
    }

    // ===== UuidV7 =====

    @Test
    public void testUuidV7_format() {
        String id = UuidV7.next();
        assertEquals(36, id.length());
        // 验证 version 字段 = 7
        char ver = id.charAt(14);
        assertEquals("version digit should be 7", '7', ver);
        // 验证 variant 字段：第 19 字符（8-4-4-4-12 第 4 段首字符）的高位 = 8/9/a/b
        char var = id.charAt(19);
        assertTrue("variant nibble should be 8/9/a/b, got " + var,
                var == '8' || var == '9' || var == 'a' || var == 'b');
    }

    @Test
    public void testUuidV7_timeSortable() throws Exception {
        String a = UuidV7.next();
        Thread.sleep(10);
        String b = UuidV7.next();
        // 字符串字典序应反映时间序
        assertTrue("a should < b (time sortable): " + a + " vs " + b, a.compareTo(b) < 0);
    }

    @Test
    public void testUuidV7_fromMillis() {
        String id = UuidV7.fromMillis(1700000000000L);
        assertEquals(36, id.length());
        // 解析 timestamp：从第 0-17 字符（去掉 -）→ long
        String hex = id.substring(0, 8) + id.substring(9, 13) + id.substring(14, 18);
        long t = Long.parseLong(hex.substring(0, 12), 16);
        assertEquals(1700000000000L, t);
    }

    @Test
    public void testUuidV7_unique() {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 1000; i++) set.add(UuidV7.next());
        assertEquals(1000, set.size());
    }

    // ===== NanoId =====

    @Test
    public void testNanoId_length() {
        NanoId n = new NanoId(10);
        String id = n.next();
        assertEquals(10, id.length());
    }

    @Test
    public void testNanoId_charset() {
        NanoId n = new NanoId(50);
        String id = n.next();
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            assertTrue("unexpected char " + c, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".indexOf(c) >= 0);
            // 不能含易混字符
            assertFalse("confusing char " + c, "iIlL1oO0".indexOf(c) >= 0);
        }
    }

    @Test
    public void testNanoId_unique() {
        NanoId n = new NanoId(21);
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 1000; i++) set.add(n.next());
        assertEquals(1000, set.size());
    }

    @Test
    public void testNanoId_smallerThanUuid() {
        NanoId n = new NanoId(21);
        String nano = n.next();
        UUID uuid = UUID.randomUUID();
        assertTrue("NanoId(" + nano.length() + ") should be shorter than UUID(" + uuid.toString().length() + ")",
                nano.length() < uuid.toString().length());
    }
}
