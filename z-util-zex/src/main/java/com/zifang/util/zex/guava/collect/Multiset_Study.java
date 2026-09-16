/*
 * 文件名：Multiset_Study.java
 * 版权：Copyright 2007-2019 zxiaofan.com. Co. Ltd. All Rights Reserved.
 * 描述： Guava Multiset学习类
 * 修改人：zxiaofan
 * 修改时间：2019年12月19日
 * 修改内容：新增
 */
package com.zifang.util.zex.guava.collect;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * Guava Multiset学习类。
 * <p>
 * Multiset继承自JDK中的Collection接口，而不是Set接口，所以包含重复元素并没有违反原有的接口契约。
 * Multiset不是一个Map&lt;E,Integer&gt;，本质上是一个Set加一个元素计数器。
 * <p>
 * Multiset中的元素的重复个数只会是正数，且最大不会超过Integer.MAX_VALUE。
 * 设定计数为0的元素将不会出现multiset中，也不会出现elementSet()和entrySet()的返回结果中。
 * <p>
 * Map-Multiset对应关系-是否支持null：
 * <ul>
 *   <li>HashMap-HashMultiset-Yes</li>
 *   <li>TreeMap-TreeMultiset-Yes (if the comparator does)</li>
 *   <li>LinkedHashMap-LinkedHashMultiset-Yes</li>
 *   <li>ConcurrentHashMap-ConcurrentHashMultiset-No</li>
 *   <li>ImmutableMap-ImmutableMultiset-No</li>
 * </ul>
 *
 * @author zifang
 * @version 1.0
 */
public class Multiset_Study {
    /**
     * 允许重复，但是不保证顺序.
     * <p>
     * 底层结构HashMap<E, Count>()，Multiset遍历时可以遍历出Map.keySize * count个元素，而map却不可以，itertator和Entry<T,Count>的iterator实现。
     */
    @Test
    /**
     * HashMultisetTest方法。
     */
    public void HashMultisetTest() {
        Multiset<String> list = HashMultiset.create();
        list.add("a");
        list.add("c");
        list.add("c");
        Collection<? extends String> tempList = Arrays.asList("a", "b");
        list.addAll(tempList); // 添加一个集合
        System.out.println(list); // [a x 2, b, c x 2]
        System.out.println(list.count("a")); // 2（如果该元素不在该集中，那么返回的结果只会是0）
        list.remove("a"); // 移除一个a
        list.remove("c", 1); // 移除一个c
        System.out.println(list); // [a, b, c]
        list.setCount("a", 3); // 设定a的个数为3
        System.out.println(list); // [a x 3, b, c]
        list.setCount("b", 1, 2); // 若b的重复个数为1则将其重复个数置为2
        System.out.println(list); // [a x 3, b x 2, c]
        list.retainAll(Arrays.asList("a", "b")); // 保留出现在指定集合中的元素
        System.out.println(list); // [a x 3, b x 2]
        list.removeAll(Arrays.asList("a")); // 去除指定集合中的所有元素
        System.out.println(list); // [b x 2]
        list.add(null); // HashMultiset--HashMap 支持null
        System.out.println(list); // [null, b x 2]
    }

    /**
     * 底层结构为ConcurrentHashMap<E, AtomicInteger>().
     */
    @Test
    /**
     * ConcurrentHashMulisetTest方法。
     */
    public void ConcurrentHashMulisetTest() {
        Multiset<String> con = ConcurrentHashMultiset.create();
        con.add("a");
        con.add("b", 2); // ConcurrentMap.putIfAbsent(element, new AtomicInteger(occurrences))
        System.out.println(con);
    }

}
