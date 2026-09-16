package com.zifang.util.zex.bust.ex;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
class CloneField implements Cloneable {

    private Long l = System.currentTimeMillis();

}

/**
 * CloneableTest类。
 */
public class CloneableTest implements Cloneable {

    public String name;

    /**
     * CloneField方法。
     *
     * @return CloneField field = new类型返回值
     */
    public CloneField field = new CloneField();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws CloneNotSupportedException {
        CloneableTest cloneableTest = new CloneableTest();
        cloneableTest.name = "吃饭";

        CloneableTest cloneableTest1 = (CloneableTest) cloneableTest.clone();
        System.out.println(cloneableTest1.name);
        System.out.println(cloneableTest.field == cloneableTest1.field);

    }

    @Override
    /**
     * clone方法。
     * @return Object类型返回值
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

