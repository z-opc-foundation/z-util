package com.zifang.util.core.concurrency.packages;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Example3类。
 */
public class Example3 extends Thread {

    private TestDo2 testDo2;
    private String key;
    private String value;

    /**
     * Example3方法。
     * * @param key String类型参数
     *
     * @param key2  String类型参数
     * @param value String类型参数
     */
    public Example3(String key, String key2, String value) {
        this.testDo2 = TestDo2.getIntance();
        this.key = key;
        //这两个key不一样，因为 key+key2 都是变量，编译器不会优化
//		this.key = key+key2;
		/*
		a==b  常量相加，编译器编译时，知道 a==b ，代码编译时，就知道结果
		a = "1"+"";
		b = "1"+"";
		*/
        this.value = value;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Example3 a = new Example3("1", "", "1");
        Example3 b = new Example3("1", "", "2");
        Example3 c = new Example3("3", "", "3");
        Example3 d = new Example3("4", "", "4");
        a.start();
        b.start();
        c.start();
        d.start();
    }

    /**
     * run方法。
     */
    public void run() {
        testDo2.doSame(key, value);
    }
}

class TestDo2 {
    private static TestDo2 _instance = new TestDo2();
    private CopyOnWriteArrayList<String> keys = new CopyOnWriteArrayList<String>();

    private TestDo2() {
    }

    /**
     * getIntance方法。
     *
     * @return static TestDo2类型返回值
     */
    public static TestDo2 getIntance() {
        return _instance;
    }

    /**
     * doSame方法。
     * * @param key String类型参数
     *
     * @param value String类型参数
     */
    public void doSame(String key, String value) {
        String o = key;
        if (!keys.contains(o)) {
            keys.add(o);
        } else {
            for (String s : keys) {
                if (o.equals(s)) {
                    o = s;
                }
            }
        }
        synchronized (o) {
            try {
                Thread.sleep(1000);
                System.out.println(key + ":" + value + ":" + (System.currentTimeMillis() / 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}