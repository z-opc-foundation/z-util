package com.zifang.util.core.pattern.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Least Frequently Used（最近最少使用）淘汰策略实现类
 * <p>
 * 当缓存容量达到上限时，会优先淘汰访问频率最低的元素。如果存在访问频率相同的元素，
 * 则淘汰最早访问的那个。
 *
 * @param <k> 键类型
 * @param <v> 值类型
 */
public class LFU<k, v> {

    private final int capcity;

    private Map<k, v> cache = new HashMap<>();

    private Map<k, HitRate> count = new HashMap<>();

    /**
     * 构造一个LFU缓存
     *
     * @param capcity 缓存容量，必须大于0
     */
    public LFU(int capcity) {
        this.capcity = capcity;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        LFU<Integer, Integer> cache = new LFU<>(3);
        cache.put(2, 2);
        cache.put(1, 1);

        System.out.println(cache.get(2));
        System.out.println(cache.get(1));
        System.out.println(cache.get(2));

        cache.put(3, 3);
        cache.put(4, 4);

        //1、2元素都有访问次数，放入3后缓存满，加入4时淘汰3
        System.out.println(cache.get(3));
        System.out.println(cache.get(2));
        //System.out.println(cache.get(1));
        System.out.println(cache.get(4));

        cache.put(5, 5);
        //目前2访问2次，1访问一次，4访问一次，由于4的时间比较新，放入5的时候移除1元素。
        System.out.println("-=-=-=-");
        cache.cache.entrySet().forEach(entry -> {
            System.out.println(entry.getValue());
        });

    }

    /**
     * 向缓存中放入键值对
     * <p>
     * 如果键已存在，则更新其访问频率；如果键不存在，则添加新元素。
     * 当缓存满时，会自动淘汰访问频率最低的元素。
     *
     * @param key   键
     * @param value 值
     */
    public void put(k key, v value) {
        v v = cache.get(key);
        if (v == null) {
            if (cache.size() == capcity) {
                removeElement();
            }
            count.put(key, new HitRate(key, 1, System.nanoTime()));
        } else {
            addHitCount(key);
        }
        cache.put(key, value);
    }

    /**
     * 从缓存中获取值
     * <p>
     * 如果键存在，会增加该键的访问频率
     *
     * @param key 键
     * @return 缓存的值，如果键不存在则返回null
     */
    public v get(k key) {
        v value = cache.get(key);
        if (value != null) {
            addHitCount(key);
            return value;
        }
        return null;
    }

    /**
     * 移除访问频率最低的元素
     * <p>
     * 当缓存满且需要添加新元素时调用
     */
    private void removeElement() {
        HitRate hr = Collections.min(count.values());
        cache.remove(hr.key);
        count.remove(hr.key);
    }

    /**
     * 增加指定键的访问次数
     *
     * @param key 键
     */
    private void addHitCount(k key) {
        HitRate hitRate = count.get(key);
        hitRate.hitCount = hitRate.hitCount + 1;
        hitRate.lastTime = System.nanoTime();
    }

    /**
     * 访问频率记录内部类
     * <p>
     * 记录每个键的访问次数和最后访问时间，用于淘汰决策
     */
    class HitRate implements Comparable<HitRate> {
        private k key;
        private int hitCount;
        private long lastTime;

        /**
         * 构造访问频率记录
         *
         * @param key      键
         * @param hitCount 初始访问次数
         * @param lastTime 最后访问时间（纳秒）
         */
        private HitRate(k key, int hitCount, long lastTime) {
            this.key = key;
            this.hitCount = hitCount;
            this.lastTime = lastTime;
        }

        /**
         * 比较两个访问频率记录
         * <p>
         * 优先比较访问次数，访问次数相同时比较最后访问时间
         *
         * @param o 另一个访问频率记录
         * @return 比较结果
         */
        @Override
        /**
         * compareTo方法。
         *      * @param o HitRate类型参数
         * @return int类型返回值
         */
        public int compareTo(HitRate o) {
            int compare = Integer.compare(this.hitCount, o.hitCount);
            return compare == 0 ? Long.compare(this.lastTime, o.lastTime) : compare;
        }
    }
}