package com.zifang.util.db.context;

/**
 * 连接池参数。默认值取远程库友好的一档：小池起步、空闲保活、借还不做检测。
 *
 * @author zifang
 */
public final class PoolSpec {

    private int initialSize = 2;

    private int minIdle = 2;

    private int maxActive = 20;

    private long maxWaitMillis = 10_000L;

    private long idleCheckMillis = 30_000L;

    private long minEvictableMillis = 60_000L;

    private int validationTimeoutMillis = 3_000;

    private boolean keepAlive = true;

    public static PoolSpec defaults() {
        return new PoolSpec();
    }

    public PoolSpec initialSize(int initialSize) {
        this.initialSize = positive(initialSize, "initialSize");
        return this;
    }

    public PoolSpec minIdle(int minIdle) {
        this.minIdle = positive(minIdle, "minIdle");
        return this;
    }

    public PoolSpec maxActive(int maxActive) {
        this.maxActive = positive(maxActive, "maxActive");
        return this;
    }

    public PoolSpec maxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = positive(maxWaitMillis, "maxWaitMillis");
        return this;
    }

    public PoolSpec idleCheckMillis(long idleCheckMillis) {
        this.idleCheckMillis = positive(idleCheckMillis, "idleCheckMillis");
        return this;
    }

    public PoolSpec minEvictableMillis(long minEvictableMillis) {
        this.minEvictableMillis = positive(minEvictableMillis, "minEvictableMillis");
        return this;
    }

    public PoolSpec validationTimeoutMillis(int validationTimeoutMillis) {
        this.validationTimeoutMillis = positive(validationTimeoutMillis, "validationTimeoutMillis");
        return this;
    }

    public PoolSpec keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public long getIdleCheckMillis() {
        return idleCheckMillis;
    }

    public long getMinEvictableMillis() {
        return minEvictableMillis;
    }

    public int getValidationTimeoutMillis() {
        return validationTimeoutMillis;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    private static int positive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " 必须为正数: " + value);
        }
        return value;
    }

    private static long positive(long value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " 必须为正数: " + value);
        }
        return value;
    }

    @Override
    public String toString() {
        return "PoolSpec{initialSize=" + initialSize + ", minIdle=" + minIdle + ", maxActive=" + maxActive
                + ", maxWaitMillis=" + maxWaitMillis + ", idleCheckMillis=" + idleCheckMillis
                + ", minEvictableMillis=" + minEvictableMillis + ", keepAlive=" + keepAlive + "}";
    }
}
