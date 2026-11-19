package com.zifang.util.monitor.sampling.store;

/**
 * 淘汰口径。
 * <p>
 * 三个维度互相独立、同时生效：任意一个上限被突破，就从最旧的一端开始淘汰，
 * 直到全部回到上限之内。取值小于等于 0 表示该维度不设限。
 * </p>
 *
 * @author zifang
 */
public final class EvictionPolicy {

    private final int maxSamples;
    private final long maxAgeMillis;
    private final long maxBytes;

    /**
     * EvictionPolicy方法。
     *
     * @param maxSamples    int类型参数
     * @param maxAgeMillis  long类型参数
     * @param maxBytes      long类型参数
     */
    public EvictionPolicy(int maxSamples, long maxAgeMillis, long maxBytes) {
        this.maxSamples = maxSamples;
        this.maxAgeMillis = maxAgeMillis;
        this.maxBytes = maxBytes;
    }

    /**
     * 不淘汰任何样本。
     *
     * @return EvictionPolicy类型返回值
     */
    public static EvictionPolicy unlimited() {
        return new EvictionPolicy(0, 0, 0);
    }

    /**
     * getMaxSamples方法。
     *
     * @return int类型返回值
     */
    public int getMaxSamples() {
        return maxSamples;
    }

    /**
     * getMaxAgeMillis方法。
     *
     * @return long类型返回值
     */
    public long getMaxAgeMillis() {
        return maxAgeMillis;
    }

    /**
     * getMaxBytes方法。
     *
     * @return long类型返回值
     */
    public long getMaxBytes() {
        return maxBytes;
    }

    @Override
    public String toString() {
        return "EvictionPolicy{maxSamples=" + maxSamples + ", maxAgeMillis=" + maxAgeMillis
                + ", maxBytes=" + maxBytes + '}';
    }

    /**
     * 淘汰口径构建器。
     */
    public static final class Builder {

        private int maxSamples = 0;
        private long maxAgeMillis = 0;
        private long maxBytes = 0;

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
         * build方法。
         *
         * @return EvictionPolicy类型返回值
         */
        public EvictionPolicy build() {
            return new EvictionPolicy(maxSamples, maxAgeMillis, maxBytes);
        }
    }
}
