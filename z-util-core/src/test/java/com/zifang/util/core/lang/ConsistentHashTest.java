package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    @Test
    public void testAffinityFirstNodeMatchesGet() {
        ConsistentHash<String> ch = new ConsistentHash<>(100, Arrays.asList("n1", "n2", "n3"));
        for (int i = 0; i < 50; i++) {
            String key = "key-" + i;
            assertEquals(ch.get(key), ch.affinityNodes(key, 1).get(0));
        }
    }

    @Test
    public void testAffinityReturnsDistinctNodes() {
        ConsistentHash<String> ch = new ConsistentHash<>(100, Arrays.asList("n1", "n2", "n3"));
        List<String> replicas = ch.affinityNodes("order-9527", 3);
        assertEquals(3, replicas.size());
        assertEquals(3, new HashSet<>(replicas).size());
    }

    @Test
    public void testAffinityCapsAtNodeCount() {
        ConsistentHash<String> ch = new ConsistentHash<>(100, Arrays.asList("n1", "n2"));
        assertEquals(2, ch.affinityNodes("k", 5).size());
        assertTrue(ch.affinityNodes("k", 0).isEmpty());
        assertTrue(new ConsistentHash<String>(10).affinityNodes("k", 3).isEmpty());
    }

    @Test
    public void testNodesSnapshotIsIndependent() {
        ConsistentHash<String> ch = new ConsistentHash<>(10, Arrays.asList("a", "b"));
        List<String> snapshot = ch.nodes();
        snapshot.add("c");
        assertEquals(2, ch.nodeCount());
    }

    @Test
    public void testFnv1HashIsStableAndInRange() {
        long first = ConsistentHash.fnv1_32("1001#VN0");
        assertEquals(first, ConsistentHash.fnv1_32("1001#VN0"));
        assertTrue(first >= 0 && first <= 0x7FFFFFFFL);
        assertTrue(ConsistentHash.fnv1_32("1001#VN0") != ConsistentHash.fnv1_32("1001#VN1"));
        long md5 = ConsistentHash.md5_32("1001#VN0");
        assertTrue(md5 >= 0 && md5 <= 0xFFFFFFFFL);
    }

    @Test
    public void testCustomHasherChangesPlacement() {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            keys.add("queue-" + i);
        }
        ConsistentHash<String> md5Ring = new ConsistentHash<>(160, Arrays.asList("c1", "c2", "c3"));
        ConsistentHash<String> fnvRing = new ConsistentHash<>(160, Arrays.asList("c1", "c2", "c3"),
                ConsistentHash::fnv1_32);
        int moved = 0;
        for (String key : keys) {
            if (!md5Ring.get(key).equals(fnvRing.get(key))) {
                moved++;
            }
        }
        assertTrue("两种哈希口径应产生不同落点", moved > 0);
        assertEquals(fnvRing.get("queue-7"), fnvRing.get("queue-7"));
    }

    @Test
    public void testRemoveNodeKeepsAffinityValid() {
        ConsistentHash<String> ch = new ConsistentHash<>(100, Arrays.asList("n1", "n2", "n3"));
        ch.remove("n2");
        List<String> replicas = ch.affinityNodes("any-key", 3);
        assertEquals(2, replicas.size());
        assertFalse(replicas.contains("n2"));
    }
}