package com.zifang.util.zex.bust.charpter12;

/**
 * 字段可见性示例类。
 * <p>
 * 此类展示了Java中字段可见性的概念。
 * 用于测试多线程环境下字段的可见性问题。
 *
 * @author zifang
 * @version 1.0
 */
public class FieldVisibility {

    int a = 1;
    int b = 2;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        while (true) {
            FieldVisibility test = new FieldVisibility();
            new Thread(() -> {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //给 a b 重新赋值
                test.change();
            }).start();

            new Thread(() -> {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //给 a b 打印出来
                test.print();
            }).start();
        }
    }

    //给a 赋值, 并把值给b
    private void change() {
        a = 3;
        b = a;
    }

    /**
     * 打印出a b
     */
    private void print() {
        System.out.println("b=" + b + ";a=" + a);
    }
}