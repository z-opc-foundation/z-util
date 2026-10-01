package com.zifang.util.core.pattern.pool.monitor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 池监控器
 * <p>
 * 可绑定到池上，收集统计数据并通知监听器
 *
 * @param <T> 对象类型
 */
public class PoolMonitor<T> {

    private final PoolStatsImpl stats = new PoolStatsImpl();
    private final List<PoolListener<T>> listeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<PoolStats>> statsConsumers = new CopyOnWriteArrayList<>();
    private volatile boolean enabled = true;

    /**
     * PoolMonitor方法。
     */
    public PoolMonitor() {
    }

    /**
     * PoolMonitor方法。
     * * @param listener PoolListenerT类型参数
     */
    public PoolMonitor(PoolListener<T> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 添加监听器
     */
    public void addListener(PoolListener<T> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 移除监听器
     */
    public void removeListener(PoolListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * 添加统计消费者，每次统计更新时调用
     */
    public void addStatsConsumer(Consumer<PoolStats> consumer) {
        if (consumer != null) {
            statsConsumers.add(consumer);
        }
    }

    /**
     * isEnabled方法。
     *
     * @return boolean类型返回值
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 启用/禁用监控
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取统计信息
     */
    public PoolStats getStats() {
        return stats;
    }

    /**
     * 重置统计
     */
    public void reset() {
        stats.reset();
    }

    // 内部方法，供池调用

    /**
     * recordBorrow方法。
     * * @param object T类型参数
     *
     * @param waitTime long类型参数
     */
    public void recordBorrow(T object, long waitTime) {
        if (!enabled) return;
        stats.recordBorrow(waitTime);
        notifyBorrow(object, waitTime);
        notifyStats();
    }

    /**
     * recordReturn方法。
     * * @param object T类型参数
     *
     * @param waitTime long类型参数
     */
    public void recordReturn(T object, long waitTime) {
        if (!enabled) return;
        stats.recordReturn(waitTime);
        notifyReturn(object, waitTime);
        notifyStats();
    }

    /**
     * recordCreate方法。
     * * @param object T类型参数
     */
    public void recordCreate(T object) {
        if (!enabled) return;
        stats.recordCreate();
        notifyCreate(object);
        notifyStats();
    }

    /**
     * recordDestroy方法。
     * * @param object T类型参数
     */
    public void recordDestroy(T object) {
        if (!enabled) return;
        stats.recordDestroy();
        notifyDestroy(object);
        notifyStats();
    }

    /**
     * recordValidate方法。
     * * @param object T类型参数
     *
     * @param valid boolean类型参数
     */
    public void recordValidate(T object, boolean valid) {
        if (!enabled) return;
        stats.recordValidate();
        notifyValidate(object, valid);
    }

    /**
     * recordEvict方法。
     * * @param object T类型参数
     */
    public void recordEvict(T object) {
        if (!enabled) return;
        notifyEvict(object);
    }

    /**
     * recordClose方法。
     */
    public void recordClose() {
        if (!enabled) return;
        notifyClose();
    }

    private void notifyBorrow(T object, long waitTime) {
        for (PoolListener<T> listener : listeners) {
            try {
                listener.onBorrow(object, waitTime);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    private void notifyReturn(T object, long waitTime) {
        for (PoolListener<T> listener : listeners) {
            try {
                listener.onReturn(object, waitTime);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    private void notifyCreate(T object) {
        for (PoolListener<T> listener : listeners) {
            try {
                listener.onCreate(object);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    private void notifyDestroy(T object) {
        for (PoolListener<T> listener : listeners) {
            try {
                listener.onDestroy(object);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    private void notifyValidate(T object, boolean valid) {
        for (PoolListener<T> listener : listeners) {
            try {
                listener.onValidate(object, valid);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    private void notifyEvict(T object) {
        for (PoolListener<T> listener : listeners) {
            try {
                listener.onEvict(object);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    private void notifyClose() {
        for (PoolListener<T> listener : listeners) {
            try {
                listener.onClose();
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    private void notifyStats() {
        for (Consumer<PoolStats> consumer : statsConsumers) {
            try {
                consumer.accept(stats);
            } catch (Exception e) {
                // 忽略消费者异常
            }
        }
    }
}