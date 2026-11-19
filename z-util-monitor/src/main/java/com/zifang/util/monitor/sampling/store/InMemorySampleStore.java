package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;

import java.util.ArrayList;
import java.util.List;

/**
 * 内存采样存储。
 * <p>
 * 内部是一条按时间递增的列表：新样本追加到尾部，超限时从头部淘汰最旧的。读写
 * 全程加锁，因此可以在采样线程与查询线程之间安全共享。
 * </p>
 *
 * @author zifang
 */
public final class InMemorySampleStore implements SampleStore {

    private final List<Sample> samples = new ArrayList<>();
    private final EvictionPolicy policy;
    private final EvictionStrategy strategy;

    private long totalAppended;
    private long totalEvicted;
    private long totalQueried;

    /**
     * InMemorySampleStore方法。
     *
     * @param policy   EvictionPolicy类型参数
     * @param strategy EvictionStrategy类型参数
     */
    public InMemorySampleStore(EvictionPolicy policy, EvictionStrategy strategy) {
        this.policy = policy;
        this.strategy = strategy;
    }

    /**
     * 创建构建器。
     *
     * @return Builder类型返回值
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * getPolicy方法。
     *
     * @return EvictionPolicy类型返回值
     */
    public EvictionPolicy getPolicy() {
        return policy;
    }

    /**
     * getStrategy方法。
     *
     * @return EvictionStrategy类型返回值
     */
    public EvictionStrategy getStrategy() {
        return strategy;
    }

    @Override
    public void append(Sample sample) {
        if (sample == null) {
            return;
        }
        synchronized (samples) {
            samples.add(sample);
            totalAppended++;
            evictIfNeeded(System.currentTimeMillis());
        }
    }

    @Override
    public List<Sample> query(long from, long to) {
        synchronized (samples) {
            evictIfNeeded(System.currentTimeMillis());
            List<Sample> result = new ArrayList<>();
            for (Sample sample : samples) {
                long ts = sample.getTimestamp();
                if (ts >= from && ts <= to) {
                    result.add(sample);
                }
            }
            totalQueried += result.size();
            return result;
        }
    }

    @Override
    public List<Sample> query() {
        synchronized (samples) {
            evictIfNeeded(System.currentTimeMillis());
            List<Sample> result = new ArrayList<>(samples);
            totalQueried += result.size();
            return result;
        }
    }

    @Override
    public Sample latest() {
        synchronized (samples) {
            evictIfNeeded(System.currentTimeMillis());
            return samples.isEmpty() ? null : samples.get(samples.size() - 1);
        }
    }

    @Override
    public void clear() {
        synchronized (samples) {
            samples.clear();
        }
    }

    @Override
    public int size() {
        synchronized (samples) {
            return samples.size();
        }
    }

    @Override
    public long estimatedBytes() {
        synchronized (samples) {
            long total = 0;
            for (Sample sample : samples) {
                total += sample.estimatedBytes();
            }
            return total;
        }
    }

    @Override
    public StoreStats stats() {
        synchronized (samples) {
            return new StoreStats(totalAppended, totalEvicted, totalQueried,
                    samples.size(), estimatedBytes());
        }
    }

    @Override
    public void close() {
        clear();
    }

    private void evictIfNeeded(long now) {
        int count = strategy.selectEvictionCount(samples, policy, now);
        if (count > 0) {
            samples.subList(0, count).clear();
            totalEvicted += count;
        }
    }

    /**
     * 内存存储构建器。
     */
    public static final class Builder {

        private int maxSamples = 0;
        private long maxAgeMillis = 0;
        private long maxBytes = 0;
        private EvictionStrategy strategy = CompositeEvictionStrategy.defaultStrategy();

        /**
         * maxSamples方法。
         *
         * @param maxSamples int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxSamples(int maxSamples) {
            this.maxSamples = maxSamples;
            return this;
        }

        /**
         * maxAgeMillis方法。
         *
         * @param maxAgeMillis long类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxAgeMillis(long maxAgeMillis) {
            this.maxAgeMillis = maxAgeMillis;
            return this;
        }

        /**
         * maxBytes方法。
         *
         * @param maxBytes long类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxBytes(long maxBytes) {
            this.maxBytes = maxBytes;
            return this;
        }

        /**
         * strategy方法。
         *
         * @param strategy EvictionStrategy类型参数
         *
         * @return Builder类型返回值
         */
        public Builder strategy(EvictionStrategy strategy) {
            if (strategy != null) {
                this.strategy = strategy;
            }
            return this;
        }

        /**
         * build方法。
         *
         * @return InMemorySampleStore类型返回值
         */
        public InMemorySampleStore build() {
            EvictionPolicy policy = new EvictionPolicy(maxSamples, maxAgeMillis, maxBytes);
            return new InMemorySampleStore(policy, strategy);
        }
    }
}
