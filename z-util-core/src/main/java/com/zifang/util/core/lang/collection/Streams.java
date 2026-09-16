package com.zifang.util.core.lang.collection;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 将Iterator类型的数据转化为流操作
 *
 * @author zifang
 */
public class Streams {

    /**
     * 将 Iterator 转换为 Stream（顺序流）
     *
     * @param iterator 迭代器
     * @param <T>      元素类型
     * @return 对应元素的 Stream
     */
    public static <T> Stream<T> streamOf(Iterator<T> iterator) {
        return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), false);
    }

    /**
     * 将 Iterable 转换为 Stream（顺序流）
     *
     * @param iterable 可迭代对象
     * @param <T>      元素类型
     * @return 对应元素的 Stream
     */
    public static <T> Stream<T> streamOf(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * 将 Iterator 转换为 Stream（并行流）
     *
     * @param iterator 迭代器
     * @param <T>      元素类型
     * @return 对应元素的并行 Stream
     */
    public static <T> Stream<T> parallelStreamOf(Iterator<T> iterator) {
        return StreamSupport.stream(((Iterable<T>) () -> iterator).spliterator(), true);
    }

    /**
     * 将 Iterable 转换为 Stream（并行流）
     *
     * @param iterable 可迭代对象
     * @param <T>      元素类型
     * @return 对应元素的并行 Stream
     */
    public static <T> Stream<T> parallelStreamOf(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), true);
    }
}
