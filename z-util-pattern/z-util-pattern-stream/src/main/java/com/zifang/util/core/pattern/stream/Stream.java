package com.zifang.util.core.pattern.stream;

/**
 * 推送式流: 异步地把 T 序列给订阅者. 同步串请用 {@link com.zifang.util.core.pattern.chain.Chain}.
 */
public interface Stream<T> {

    /** 拉一个 Subscriber, 由 Stream 内部 push 已就绪的元素. */
    void subscribe(Subscriber<T> subscriber);
}