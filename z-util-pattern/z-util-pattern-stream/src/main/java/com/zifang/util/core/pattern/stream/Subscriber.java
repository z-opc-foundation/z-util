package com.zifang.util.core.pattern.stream;

/**
 * 流消费者: 接 onSubscribe/onNext/onError/onComplete 四种信号.
 * <p>
 * 调用顺序契约 (与 Reactive Streams 对齐):
 *   onSubscribe 最多一次, 必发, 最先;
 *   onNext 零或多次;
 *   onError 或 onComplete 二选一且最后 (终止信号).
 * 实现违反契约将抛 IllegalStateException.
 */
public interface Subscriber<T> {

    void onSubscribe(Subscription s);

    void onNext(T item);

    void onError(Throwable t);

    void onComplete();
}