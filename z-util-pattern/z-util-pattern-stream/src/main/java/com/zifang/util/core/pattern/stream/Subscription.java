package com.zifang.util.core.pattern.stream;

/**
 * 订阅句柄：控制背压与取消.
 * <p>
 * 设计要点: 与 Reactive Streams (Flow.Subscription) 同形, 但走 z-util 自家的接口
 * 以保持 z-util-pattern-stream 不依赖 java.util.concurrent.Flow 的可见性契约.
 */
public interface Subscription {

    /** 向源请求 n 个元素 (n<=0 时视为无界). */
    void request(long n);

    /** 取消订阅. 之后 onNext/onError/onComplete 都不应再被调用. */
    void cancel();

    /** 是否已被取消 —— 给 Subscriber 自我保护. */
    boolean isCancelled();
}