package com.zifang.util.core.lock;

/**
 * 分布式锁接口（自研，对标 Redisson Lock）。
 */
public interface DistributedLock {

    /**
     * 尝试获取锁，立即返回。
     *
     * @return 锁令牌（持有标识），用于 {@link #unlock}；返回 null 表示获取失败
     */
    String tryLock();

    /**
     * 最多等 {@code waitNanos} 纳秒。返回 null 表示超时。
     */
    String tryLock(long waitNanos);

    /**
     * 释放锁。
     *
     * @return true 释放成功；false 不是持有者或锁已过期
     */
    boolean unlock(String token);
}
