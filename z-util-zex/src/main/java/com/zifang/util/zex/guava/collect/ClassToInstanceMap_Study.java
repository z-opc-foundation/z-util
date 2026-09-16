/*
 * 文件名：ClassToInstanceMap_Study.java
 * 版权：Copyright 2007-2019 zxiaofan.com. Co. Ltd. All Rights Reserved.
 * 描述： Guava ClassToInstanceMap学习类
 * 修改人：zxiaofan
 * 修改时间：2019年12月27日
 * 修改内容：新增
 */
package com.zifang.util.zex.guava.collect;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import org.junit.Test;


/**
 * Guava ClassToInstanceMap学习类。
 * <p>
 * ClassToInstanceMap是一种特殊的Map，其key是类型（Class对象），value是相应类型的实例。
 * 可通过类型直接获取对应类型的实例，类似依赖注入容器。
 * <p>
 * 保证放入的value和key的类型对应，不一致将报转换异常。
 * <p>
 * 实现类：MutableClassToInstanceMap、ImmutableClassToInstanceMap
 *
 * @author zifang
 * @version 1.0
 */
public class ClassToInstanceMap_Study {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    /**
     * basicTest方法。
     */
    public void basicTest() {
        // MutableClassToInstanceMap
        ClassToInstanceMap map = MutableClassToInstanceMap.create();
        UserBo user = new UserBo();
        user.setName("n1");
        map.put(UserBo.class, user);
        map.put(String.class, "str");
        map.put(String.class, "str2");
        map.putInstance(Integer.class, 1);
        UserBo bo = (UserBo) map.getInstance(UserBo.class);
        System.out.println(bo.getName()); // n1
        System.out.println(map.getInstance(String.class)); // str2
        // ImmutableClassToInstanceMap
        ClassToInstanceMap immuMap = new ImmutableClassToInstanceMap.Builder<Number>().put(Integer.class, 1).put(Double.class, 2.2).build();
        System.out.println(immuMap.toString()); // {class java.lang.Integer=1, class java.lang.Double=2.2}
    }
}
