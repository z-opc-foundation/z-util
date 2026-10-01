package com.zifang.util.core.pattern.pool;

/**
 * 对象池配置
 */
public class PoolConfig {

    /**
     * 最大池对象数量
     */
    private int maxTotal = 8;

    /**
     * 最大空闲对象数量
     */
    private int maxIdle = 8;

    /**
     * 最小空闲对象数量
     */
    private int minIdle = 0;

    /**
     * 借出前是否验证
     */
    private boolean testOnBorrow = false;

    /**
     * 归还后是否验证
     */
    private boolean testOnReturn = false;

    /**
     * 空闲时是否验证
     */
    private boolean testWhileIdle = false;

    /**
     * 空闲对象验证器调用的时间间隔（毫秒）
     */
    private long durationBetweenEvictionRuns = -1;

    /**
     * 空闲对象最大数量
     */
    private int maxWaitMillis = -1;

    /**
     * 最小空闲对象数量检查
     */
    private boolean blockWhenExhausted = true;

    /**
     * 软引用缓冲类型
     */
    private boolean softMinIdle = false;

    /**
     * PoolConfig方法。
     */
    public PoolConfig() {
    }

    /**
     * PoolConfig方法。
     * * @param maxTotal int类型参数
     *
     * @param maxIdle int类型参数
     * @param minIdle int类型参数
     */
    public PoolConfig(int maxTotal, int maxIdle, int minIdle) {
        this.maxTotal = maxTotal;
        this.maxIdle = maxIdle;
        this.minIdle = minIdle;
    }

    // Getters and Setters

    /**
     * 创建默认配置
     */
    public static PoolConfig createDefault() {
        return new PoolConfig();
    }

    /**
     * 创建配置Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * getMaxTotal方法。
     *
     * @return int类型返回值
     */
    public int getMaxTotal() {
        return maxTotal;
    }

    /**
     * setMaxTotal方法。
     * * @param maxTotal int类型参数
     */
    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    /**
     * getMaxIdle方法。
     *
     * @return int类型返回值
     */
    public int getMaxIdle() {
        return maxIdle;
    }

    /**
     * setMaxIdle方法。
     * * @param maxIdle int类型参数
     */
    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    /**
     * getMinIdle方法。
     *
     * @return int类型返回值
     */
    public int getMinIdle() {
        return minIdle;
    }

    /**
     * setMinIdle方法。
     * * @param minIdle int类型参数
     */
    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    /**
     * isTestOnBorrow方法。
     *
     * @return boolean类型返回值
     */
    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    /**
     * setTestOnBorrow方法。
     * * @param testOnBorrow boolean类型参数
     */
    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    /**
     * isTestOnReturn方法。
     *
     * @return boolean类型返回值
     */
    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    /**
     * setTestOnReturn方法。
     * * @param testOnReturn boolean类型参数
     */
    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    /**
     * isTestWhileIdle方法。
     *
     * @return boolean类型返回值
     */
    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    /**
     * setTestWhileIdle方法。
     * * @param testWhileIdle boolean类型参数
     */
    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    /**
     * getDurationBetweenEvictionRuns方法。
     *
     * @return long类型返回值
     */
    public long getDurationBetweenEvictionRuns() {
        return durationBetweenEvictionRuns;
    }

    /**
     * setDurationBetweenEvictionRuns方法。
     * * @param durationBetweenEvictionRuns long类型参数
     */
    public void setDurationBetweenEvictionRuns(long durationBetweenEvictionRuns) {
        this.durationBetweenEvictionRuns = durationBetweenEvictionRuns;
    }

    /**
     * getMaxWaitMillis方法。
     *
     * @return int类型返回值
     */
    public int getMaxWaitMillis() {
        return maxWaitMillis;
    }

    /**
     * setMaxWaitMillis方法。
     * * @param maxWaitMillis int类型参数
     */
    public void setMaxWaitMillis(int maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    /**
     * isBlockWhenExhausted方法。
     *
     * @return boolean类型返回值
     */
    public boolean isBlockWhenExhausted() {
        return blockWhenExhausted;
    }

    /**
     * setBlockWhenExhausted方法。
     * * @param blockWhenExhausted boolean类型参数
     */
    public void setBlockWhenExhausted(boolean blockWhenExhausted) {
        this.blockWhenExhausted = blockWhenExhausted;
    }

    /**
     * isSoftMinIdle方法。
     *
     * @return boolean类型返回值
     */
    public boolean isSoftMinIdle() {
        return softMinIdle;
    }

    /**
     * setSoftMinIdle方法。
     * * @param softMinIdle boolean类型参数
     */
    public void setSoftMinIdle(boolean softMinIdle) {
        this.softMinIdle = softMinIdle;
    }

    public static class Builder {
        private final PoolConfig config = new PoolConfig();

        /**
         * maxTotal方法。
         * * @param maxTotal int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxTotal(int maxTotal) {
            config.setMaxTotal(maxTotal);
            return this;
        }

        /**
         * maxIdle方法。
         * * @param maxIdle int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxIdle(int maxIdle) {
            config.setMaxIdle(maxIdle);
            return this;
        }

        /**
         * minIdle方法。
         * * @param minIdle int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder minIdle(int minIdle) {
            config.setMinIdle(minIdle);
            return this;
        }

        /**
         * testOnBorrow方法。
         * * @param testOnBorrow boolean类型参数
         *
         * @return Builder类型返回值
         */
        public Builder testOnBorrow(boolean testOnBorrow) {
            config.setTestOnBorrow(testOnBorrow);
            return this;
        }

        /**
         * testOnReturn方法。
         * * @param testOnReturn boolean类型参数
         *
         * @return Builder类型返回值
         */
        public Builder testOnReturn(boolean testOnReturn) {
            config.setTestOnReturn(testOnReturn);
            return this;
        }

        /**
         * testWhileIdle方法。
         * * @param testWhileIdle boolean类型参数
         *
         * @return Builder类型返回值
         */
        public Builder testWhileIdle(boolean testWhileIdle) {
            config.setTestWhileIdle(testWhileIdle);
            return this;
        }

        /**
         * durationBetweenEvictionRuns方法。
         * * @param durationBetweenEvictionRuns long类型参数
         *
         * @return Builder类型返回值
         */
        public Builder durationBetweenEvictionRuns(long durationBetweenEvictionRuns) {
            config.setDurationBetweenEvictionRuns(durationBetweenEvictionRuns);
            return this;
        }

        /**
         * maxWaitMillis方法。
         * * @param maxWaitMillis int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxWaitMillis(int maxWaitMillis) {
            config.setMaxWaitMillis(maxWaitMillis);
            return this;
        }

        /**
         * blockWhenExhausted方法。
         * * @param blockWhenExhausted boolean类型参数
         *
         * @return Builder类型返回值
         */
        public Builder blockWhenExhausted(boolean blockWhenExhausted) {
            config.setBlockWhenExhausted(blockWhenExhausted);
            return this;
        }

        /**
         * build方法。
         *
         * @return PoolConfig类型返回值
         */
        public PoolConfig build() {
            return config;
        }
    }
}
