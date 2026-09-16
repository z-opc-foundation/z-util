/*
 * 文件名：UserBo.java
 * 版权：Copyright 2007-2019 zxiaofan.com. Co. Ltd. All Rights Reserved.
 * 描述： 用户业务对象类
 * 修改人：zxiaofan
 * 修改时间：2019年12月27日
 * 修改内容：新增
 */
package com.zifang.util.zex.guava.collect;

/**
 * 用户业务对象类。
 * <p>
 * 此类是用户数据的传输对象，包含用户的名称和年龄信息。
 *
 * @author zifang
 * @version 1.0
 */
public class UserBo {
    private String name;

    private Integer age;

    /**
     * UserBo方法。
     */
    public UserBo() {
        super();
    }

    /**
     * UserBo方法。
     * * @param name String类型参数
     *
     * @param age int类型参数
     */
    public UserBo(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * setName方法。
     * * @param name String类型参数
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getAge方法。
     *
     * @return int类型返回值
     */
    public Integer getAge() {
        return age;
    }

    /**
     * setAge方法。
     * * @param age int类型参数
     */
    public void setAge(Integer age) {
        this.age = age;
    }

}
