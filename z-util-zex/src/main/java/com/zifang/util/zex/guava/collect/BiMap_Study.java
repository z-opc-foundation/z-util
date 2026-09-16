/*
 * 文件名：BiMap_Study.java
 * 版权：Copyright 2007-2019 zxiaofan.com. Co. Ltd. All Rights Reserved.
 * 描述： Guava BiMap学习类
 * 修改人：zxiaofan
 * 修改时间：2019年12月26日
 * 修改内容：新增
 */
package com.zifang.util.zex.guava.collect;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumBiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Guava BiMap学习类。
 * <p>
 * BiMap是一种双向映射的Map，key-value唯一，value也唯一。
 * inverse()方法可以反转BiMap的键值映射，反转后的map是一个视图，任何增删改操作都会影响原map。
 * <p>
 * key/value均唯一，因此values()返回的是Set而不是普通的Collection。
 * 把键映射到已经存在的值，会抛出IllegalArgumentException异常。
 * 想要强制替换特定值的键，使用BiMap.inverse().forcePut(key, value)。
 * <p>
 * BiMap实现与底层数据结构对应关系：
 * <ul>
 *   <li>HashMap + HashMap = HashBiMap</li>
 *   <li>ImmutableMap + ImmutableMap = ImmutableBiMap</li>
 *   <li>EnumMap + EnumMap = EnumBiMap</li>
 *   <li>EnumMap + HashMap = EnumHashBiMap</li>
 * </ul>
 *
 * @author zifang
 * @version 1.0
 */
public class BiMap_Study {
    /**
     * BiMap基础方法，其他实现类亦有如下方法.
     */
    @Test
    /**
     * basicTest方法。
     */
    public void basicTest() {
        BiMap<String, Integer> nameId = HashBiMap.create();
        initMap(nameId);
        System.err.println(nameId.get("a")); // 1
        System.err.println(nameId.inverse().get(2)); // b, value-->key
        nameId.putIfAbsent("c", 33); // 不存在则插入
        System.err.println(nameId.inverse().get(3)); // c
        nameId.put("a", 11);
        try {
            nameId.inverse().put(111, "a"); // key已存在
        } catch (Exception e) {
            e.printStackTrace(); // IllegalArgumentException:value already present: a
        }
        nameId.inverse().forcePut(444, "c"); // 强制更新
        System.err.println(nameId); // {a=11, b=2, c=444}
        System.out.println(nameId.values()); // values返回Set<T>而不是Collection
    }

    @Test
    /**
     * otherImpl方法。
     */
    public void otherImpl() {
        // ImmutableBiMap
        Map<String, Integer> temp = new HashMap<>();
        temp.put("cc", 33);
        BiMap<String, Integer> map = new ImmutableBiMap.Builder<String, Integer>().put("a", 1).put("b", 2).putAll(temp).build(); // 不可变
        System.out.println(map); // {a=1, b=2, cc=33} ImmutableBiMap不支持任何修改操作
        // EnumBiMap
        BiMap<keys, values> enumMap = EnumBiMap.create(keys.class, values.class);
        enumMap.put(keys.K1, values.V2);
        System.out.println(enumMap); // {K1=V2}
    }

    void initMap(BiMap<String, Integer> map) {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
    }

    private enum keys {
        K1, K2, K3
    }

    private enum values {
        V1, V2, V3
    }

}
