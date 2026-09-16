package com.zifang.util.zex.bust.chapter11;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.io.Serializable;

/**
 * Person类。
 */
public class Person implements Serializable {
    private int id;
    private String name;
    private int age;

    /**
     * Person方法。
     */
    public Person() {
    }

    /**
     * Person方法。
     * * @param id int类型参数
     *
     * @param name String类型参数
     * @param age  int类型参数
     */
    public Person(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    /**
     * getId方法。
     *
     * @return int类型返回值
     */
    public int getId() {
        return id;
    }

    /**
     * setId方法。
     * * @param id int类型参数
     */
    public void setId(int id) {
        this.id = id;
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
    public int getAge() {
        return age;
    }

    /**
     * setAge方法。
     * * @param age int类型参数
     */
    public void setAge(int age) {
        this.age = age;
    }
}