package com.zifang.util.core.lang;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.ToLongFunction;

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
 * 需要多副本放置（K 个亲和节点）时用 {@link #affinityNodes(Object, int)}；
 * 需要与既有系统的哈希口径一致时，构造时传入 {@link #fnv1_32(String)} 等哈希函数。
 *
 * @param <T> 节点类型
 * @author zifang
 */
public class ConsistentHash<T> {

    private final int virtualNodesPerNode;
    private final TreeMap<Long, T> circle = new TreeMap<>();
    private final List<T> nodes = new CopyOnWriteArrayList<>();
    private final ToLongFunction<String> hasher;

    /**
     * @param virtualNodesPerNode 每个真实节点映射的虚拟节点数量，越多越均匀（典型 100-200）
     */
    public ConsistentHash(int virtualNodesPerNode) {
        this(virtualNodesPerNode, null, ConsistentHash::md5_32);
    }

    public ConsistentHash(int virtualNodesPerNode, List<T> initialNodes) {
        this(virtualNodesPerNode, initialNodes, ConsistentHash::md5_32);
    }

    /**
     * @param virtualNodesPerNode 每个真实节点映射的虚拟节点数量
     * @param initialNodes        初始节点集合，可为 null
     * @param hasher              哈希函数，为 null 时退回默认的 {@link #md5_32(String)}；
     *                            更换哈希函数会改变 key 到节点的落点，迁移时需灰度
     */
    public ConsistentHash(int virtualNodesPerNode, List<T> initialNodes, ToLongFunction<String> hasher) {
        if (virtualNodesPerNode <= 0) {
            throw new IllegalArgumentException("virtualNodesPerNode must be > 0");
        }
        this.virtualNodesPerNode = virtualNodesPerNode;
        this.hasher = hasher == null ? ConsistentHash::md5_32 : hasher;
        if (initialNodes != null) {
            for (T node : initialNodes) {
                add(node);
            }
        }
    }

    /**
     * 默认哈希：MD5 取前 4 字节拼成 32 位无符号值（Ketama 口径）。
     *
     * @param key 参与哈希的字符串
     * @return 环上的哈希值
     */
    public static long md5_32(String key) {
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

    /**
     * FNV1_32 哈希（xor-then-multiply + 移位混淆，掩到 31 位，与 Dubbo 口径一致）。
     * <p>
     * 落点与 {@link #md5_32(String)} 不兼容，仅用于与既有系统对齐（如 z-schedule 的调度路由环）。
     *
     * @param key 参与哈希的字符串
     * @return 环上的哈希值
     */
    public static long fnv1_32(String key) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < key.length(); i++) {
            hash = (hash ^ key.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash & 0x7FFFFFFF;
    }

    private long hash(String key) {
        return hasher.applyAsLong(key);
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

    /**
     * 从 key 的落点开始顺时针取最多 {@code count} 个互不相同的真实节点。
     * <p>
     * 用于多副本放置或主备切换；节点数不足 {@code count} 时返回全部节点。
     *
     * @param key   路由键
     * @param count 需要的节点个数，小于等于 0 时返回空列表
     * @return 顺时针顺序的节点列表，环为空时返回空列表
     */
    public List<T> affinityNodes(Object key, int count) {
        List<T> result = new ArrayList<>();
        if (circle.isEmpty() || count <= 0) {
            return result;
        }
        long h = hash(String.valueOf(key));
        Map.Entry<Long, T> start = circle.ceilingEntry(h);
        if (start == null) {
            start = circle.firstEntry();
        }
        NavigableSet<Long> keys = circle.navigableKeySet();
        Set<T> distinct = new LinkedHashSet<>();
        Long cursor = start.getKey();
        while (distinct.size() < nodes.size() && cursor != null) {
            T node = circle.get(cursor);
            if (node != null) {
                distinct.add(node);
            }
            Long next = keys.higher(cursor);
            if (next == null) {
                next = keys.first();
            }
            cursor = next.equals(cursor) ? null : next;
        }
        int taken = 0;
        for (T node : distinct) {
            if (taken++ >= count) {
                break;
            }
            result.add(node);
        }
        return result;
    }

    /**
     * 环上的真实节点列表（快照，修改不影响环）。
     *
     * @return 按加入顺序排列的节点列表
     */
    public List<T> nodes() {
        return new ArrayList<>(nodes);
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int circleSize() {
        return circle.size();
    }
}