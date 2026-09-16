package com.zifang.util.proxy;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LogTest类。
 */
public class LogTest {
    private static final Logger log = LoggerFactory.getLogger(LogTest.class);

    @Test
    /**
     * aaa方法。
     */
    public void aaa() {
        log.info("INFOXXXXX");
        log.debug("debug XXXXX");

    }
}
