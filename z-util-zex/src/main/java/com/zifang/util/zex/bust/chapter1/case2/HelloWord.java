package com.zifang.util.zex.bust.chapter1.case2;

/* **/

/**
 * 文档注释同样不关心中间是否有换行
 */
public class HelloWord {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 单行注释
        System.out.println("hello word");

        /* 多行注释，并在一行，看上去就像是单行注释 */
        System.out.println("hello word");

        System.out/*多行注释甚至可以嵌在分隔符前后*/./**/println("hello word");
        System.out.println("hello word");
    }
}