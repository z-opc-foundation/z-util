package com.zifang.util.zex.bust.chapter6;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LogTest类。
 */
public class LogTest {
    private static final Logger log = LoggerFactory.getLogger(LogTest.class);

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        log.info("打印info日志");
        log.debug("打印debug日志");
        log.warn("打印warn日志");
        log.error("打印error日志");
    }
}
