package com.zifang.util.proxy.ct.generater;

/**
 * 静态分析源码包
 */

/**
 * AnalysisApplication类。
 */
public class AnalysisApplication {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        String packageName = "com.zifang.util.jvm.management";

        AnalysisContext analysisContext = new AnalysisContext(packageName);
        //analysisContext.setFilterChain();过滤链条
        analysisContext.analysis();// 对当前的包做所有的分析

    }
}
