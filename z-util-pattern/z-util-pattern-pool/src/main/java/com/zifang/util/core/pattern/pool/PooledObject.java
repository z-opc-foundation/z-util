package com.zifang.util.core.pattern.pool;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 池化对象包装器
 * <p>
 * 封装被池化的对象及其元数据
 *
 * @param <T> 对象类型
 */
public class PooledObject<T> {

    private final T object;
    private final AtomicInteger state;
    private final long createTime;
    private final AtomicLong lastBorrowTime;
    private final AtomicLong lastReturnTime;
    private final AtomicLong borrowCount;
    private final AtomicLong useCount;

    /**
     * PooledObject方法。
     * * @param object T类型参数
     */
    public PooledObject(T object) {
        this.object = object;
        this.state = new AtomicInteger(PooledObjectState.IDLE.ordinal());
        this.createTime = System.currentTimeMillis();
        this.lastBorrowTime = new AtomicLong(createTime);
        this.lastReturnTime = new AtomicLong(createTime);
        this.borrowCount = new AtomicLong(0);
        this.useCount = new AtomicLong(0);
    }

    /**
     * 获取原始对象
     */
    public T getObject() {
        return object;
    }

    /**
     * 获取创建时间
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * 获取最后借用时间
     */
    public long getLastBorrowTime() {
        return lastBorrowTime.get();
    }

    /**
     * 获取最后归还时间
     */
    public long getLastReturnTime() {
        return lastReturnTime.get();
    }

    /**
     * 获取借用次数
     */
    public long getBorrowCount() {
        return borrowCount.get();
    }

    /**
     * 获取使用次数
     */
    public long getUseCount() {
        return useCount.get();
    }

    /**
     * 获取空闲时间（毫秒）
     */
    public long getIdleTime() {
        return System.currentTimeMillis() - lastReturnTime.get();
    }

    /**
     * 获取使用时间（毫秒）
     */
    public long getActiveTime() {
        return System.currentTimeMillis() - lastBorrowTime.get();
    }

    /**
     * 是否空闲
     */
    public boolean isIdle() {
        return state.get() == PooledObjectState.IDLE.ordinal();
    }

    /**
     * 分配（借用）
     */
    public boolean allocate() {
        return state.compareAndSet(PooledObjectState.IDLE.ordinal(), PooledObjectState.ALLOCATED.ordinal());
    }

    /**
     * 归还
     */
    public void returnObject() {
        if (state.compareAndSet(PooledObjectState.ALLOCATED.ordinal(), PooledObjectState.IDLE.ordinal())) {
            lastReturnTime.set(System.currentTimeMillis());
            useCount.incrementAndGet();
        }
    }

    /**
     * 标记为废弃
     */
    public void invalidate() {
        state.set(PooledObjectState.INVALID.ordinal());
    }

    /**
     * 标记为正在验证
     */
    public boolean startEvictionTest() {
        return state.compareAndSet(PooledObjectState.IDLE.ordinal(), PooledObjectState.EVICTION.ordinal());
    }

    /**
     * 结束验证
     */
    public void endEvictionTest(boolean evictable) {
        if (evictable) {
            state.set(PooledObjectState.IDLE.ordinal());
        } else {
            state.set(PooledObjectState.INVALID.ordinal());
        }
    }

    /**
     * 获取状态
     */
    public PooledObjectState getState() {
        return PooledObjectState.values()[state.get()];
    }

    /**
     * 记录借用
     */
    public void recordBorrow() {
        lastBorrowTime.set(System.currentTimeMillis());
        borrowCount.incrementAndGet();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "PooledObject{" +
                "state=" + getState() +
                ", createTime=" + createTime +
                ", borrowCount=" + borrowCount +
                ", useCount=" + useCount +
                '}';
    }
}
