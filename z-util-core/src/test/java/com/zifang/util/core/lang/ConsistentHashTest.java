package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ConsistentHashTest {

    @Test
    public void testSameKeyMapsToSameNode() {
        ConsistentHash<String> ch = new ConsistentHash<>(100);
        ch.add("node-A");
        ch.add("node-B");
        ch.add("node-C");
        String r1 = ch.get("user-1");
        String r2 = ch.get("user-1");
        assertEquals(r1, r2);
    }

    @Test
    public void testKeysDistributeAcrossNodes() {
        ConsistentHash<String> ch = new ConsistentHash<>(100);
        ch.add("node-A");
        ch.add("node-B");
        ch.add("node-C");
        Map<String, Integer> dist = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String n = ch.get("key-" + i);
            dist.merge(n, 1, Integer::sum);
        }
        // 至少分布到 2 个节点
        assertTrue("Should distribute to multiple nodes, got: " + dist,
                dist.size() >= 2);
    }

    @Test
    public void testRemoveNode() {
        ConsistentHash<String> ch = new ConsistentHash<>(100);
        ch.add("node-A");
        ch.add("node-B");
        ch.add("node-C");
        ch.remove("node-B");
        assertEquals(2, ch.nodeCount());
    }

    @Test
    public void testEmptyCircleReturnsNull() {
        ConsistentHash<String> ch = new ConsistentHash<>(10);
        assertNull(ch.get("anything"));
    }

    @Test
    public void testFromInitialNodes() {
        ConsistentHash<String> ch = new ConsistentHash<>(50, Arrays.asList("a", "b"));
        assertEquals(2, ch.nodeCount());
    }
}