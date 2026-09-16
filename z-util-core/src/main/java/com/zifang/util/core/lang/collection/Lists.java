package com.zifang.util.core.lang.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zifang
 */
public class Lists {

    /**
     * 将多个元素变为List
     */
    public static <T> List<T> of(T... t1) {
        return Arrays.asList(t1);
    }

    /**
     * 传入一个字符串，传入分割符，得到子序列的List
     */
    public static List<String> of(String content, String splitor) {
        return Arrays.asList(content.split(splitor));
    }

    /**
     * 将迭代器的数据转换为List
     */
    public static <E> List<E> of(Iterable<E> iterable) {
        List<E> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    /**
     * 对一个数组进行过滤操作
     */
    public static <E> List<E> filter(List<E> elements, Predicate<E> predicate) {
        return elements.stream().filter(predicate).collect(Collectors.toList());
    }
}
