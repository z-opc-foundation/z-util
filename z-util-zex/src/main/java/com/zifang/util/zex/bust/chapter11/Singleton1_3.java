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
public class Singleton1_3 {

    private static Singleton1_3 singleton = null;

    public int f1 = 1; // 触发部分初始化问题

    public int f2 = 2;

    private Singleton1_3() {
    }

    /**
     * getInstance方法。
     *
     * @return static Singleton1_3类型返回值
     */
    public static Singleton1_3 getInstance() {

        if (singleton == null) {
            synchronized (Singleton1_3.class) {
                // must be a complete instance
                if (singleton == null) {
                    singleton = new Singleton1_3();
                }
            }
        }
        return singleton;
    }
}