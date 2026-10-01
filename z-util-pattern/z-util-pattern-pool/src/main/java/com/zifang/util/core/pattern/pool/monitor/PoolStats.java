package com.zifang.util.core.pattern.pool.monitor;

/**
 * 池监控信息接口
 */
public interface PoolStats {

    /**
     * 获取活跃对象数
     */
    int getActiveCount();

    /**
     * 获取空闲对象数
     */
    int getIdleCount();

    /**
     * 获取总对象数
     */
    int getTotalCount();

    /**
     * 获取正在等待借出的线程数
     */
    int getWaitingThreads();

    /**
     * 获取累计借出次数
     */
    long getBorrowCount();

    /**
     * 获取累计归还次数
     */
    long getReturnCount();

    /**
     * 获取累计创建次数
     */
    long getCreateCount();

    /**
     * 获取累计销毁次数
     */
    long getDestroyCount();

    /**
     * 获取累计验证次数
     */
    long getValidateCount();

    /**
     * 获取最大借出等待时间(毫秒)
     */
    long getMaxBorrowWaitTime();

    /**
     * 获取平均借出等待时间(毫秒)
     */
    long getAvgBorrowWaitTime();

    /**
     * 获取最大归还等待时间(毫秒)
     */
    long getMaxReturnWaitTime();

    /**
     * 获取利用率 (active/total)
     */
    double getUtilization();

    /**
     * 获取每秒借出速率
     */
    double getBorrowRate();

    /**
     * 获取每秒归还速率
     */
    double getReturnRate();
}