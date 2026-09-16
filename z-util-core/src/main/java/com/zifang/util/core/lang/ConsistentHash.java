package com.zifang.util.core.lang;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 一致性哈希（Consistent Hashing）工具。
 * <p>
 * 解决缓存节点增减时大量缓存失效的问题（对标 Ketama / DynamoDB 一致性哈希）。
 * <p>
 * 用法：
 * <pre>{@code
 *   ConsistentHash<String> ch = new ConsistentHash<>(100); // 每个节点 100 个虚拟节点
 *   ch.add("cache-node-1");
 *   ch.add("cache-node-2");
 *   String node = ch.get("user_123");
 * }</pre>
 *
 * @param <T> 节点类型
 * @author zifang
 */
public class ConsistentHash<T> {

    private final int virtualNodesPerNode;
    private final TreeMap<Long, T> circle = new TreeMap<>();
    private final List<T> nodes = new CopyOnWriteArrayList<>();

    /**
     * @param virtualNodesPerNode 每个真实节点映射的虚拟节点数量，越多越均匀（典型 100-200）
     */
    public ConsistentHash(int virtualNodesPerNode) {
        if (virtualNodesPerNode <= 0) {
            throw new IllegalArgumentException("virtualNodesPerNode must be > 0");
        }
        this.virtualNodesPerNode = virtualNodesPerNode;
    }

    public ConsistentHash(int virtualNodesPerNode, List<T> initialNodes) {
        this(virtualNodesPerNode);
        if (initialNodes != null) {
            for (T node : initialNodes) {
                add(node);
            }
        }
    }

    private static long hash(String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(key.getBytes(StandardCharsets.UTF_8));
            // 取前 4 字节拼成 long
            return ((long) (digest[3] & 0xFF) << 24)
                    | ((long) (digest[2] & 0xFF) << 16)
                    | ((long) (digest[1] & 0xFF) << 8)
                    | ((long) (digest[0] & 0xFF));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }

    public void add(T node) {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null");
        }
        nodes.add(node);
        for (int i = 0; i < virtualNodesPerNode; i++) {
            long h = hash(node.toString() + "#" + i);
            circle.put(h, node);
        }
    }

    public void remove(T node) {
        if (node == null) return;
        nodes.remove(node);
        for (int i = 0; i < virtualNodesPerNode; i++) {
            long h = hash(node.toString() + "#" + i);
            circle.remove(h);
        }
    }

    /**
     * 根据 key 找到顺时针最近的节点。
     */
    public T get(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        long h = hash(String.valueOf(key));
        Map.Entry<Long, T> e = circle.ceilingEntry(h);
        if (e == null) {
            // 环形回到第一个
            e = circle.firstEntry();
        }
        return e.getValue();
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int circleSize() {
        return circle.size();
    }
}