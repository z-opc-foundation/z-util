package com.zifang.util.core.pattern.pool.monitor;

/**
 * 池事件监听器
 *
 * @param <T> 对象类型
 */
public interface PoolListener<T> {

    /**
     * 对象被借出时调用
     *
     * @param object   被借出的对象
     * @param waitTime 等待时间(毫秒)
     */
    void onBorrow(T object, long waitTime);

    /**
     * 对象被归还时调用
     *
     * @param object   被归还的对象
     * @param waitTime 归还处理时间(毫秒)
     */
    void onReturn(T object, long waitTime);

    /**
     * 对象被创建时调用
     *
     * @param object 创建的对象
     */
    void onCreate(T object);

    /**
     * 对象被销毁时调用
     *
     * @param object 被销毁的对象
     */
    void onDestroy(T object);

    /**
     * 对象验证时调用
     *
     * @param object 被验证的对象
     * @param valid  验证结果
     */
    void onValidate(T object, boolean valid);

    /**
     * 池关闭时调用
     */
    void onClose();

    /**
     * 对象从空闲队列移除进行 eviction 时调用
     *
     * @param object 被 evict 的对象
     */
    void onEvict(T object);
}