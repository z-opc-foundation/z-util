package com.zifang.util.zex.bust.chapter4.case4;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
class Human5 {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Human5 human = new Human5();
        human.handle(true);
        human.handle((short) 1);
        human.handle((byte) 1);
        human.handle('c');
        human.handle((char) 99);
        human.handle(1);
        human.handle(1L);
        human.handle(1.1F);
        human.handle(1.1D);
        human.handle(Boolean.valueOf(true));
        human.handle(Short.valueOf((short) 1));
        human.handle(Byte.valueOf((byte) 1));
        human.handle(Character.valueOf('c'));
        human.handle(Character.valueOf((char) 99));
        human.handle(Integer.valueOf(1));
        human.handle(Long.valueOf(1L));
        human.handle(Float.valueOf(1.1F));
        human.handle(Double.valueOf(1.1D));
    }

    void handle(Object a) {
        System.out.println("#object:" + a);
    }
}
