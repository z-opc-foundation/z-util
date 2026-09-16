package com.zifang.util.zex.bust.chapter8;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.lang.reflect.Array;

/**
 * GenericArray类。
 */
public class GenericArray<T> {
    private T[] t;

    @SuppressWarnings({"unchecked", "hiding"})
    /**
     * init方法。
     *      * @param clazz ClassT类型参数
     * @param length int类型参数
     */
    public void init(Class<T> clazz, int length) {
        t = (T[]) Array.newInstance(clazz, length);
    }
}

