package com.zifang.util.devops.common;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * GavInfo 类测试
 */

/**
 * GavInfoTest类。
 */
public class GavInfoTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        GavInfo gavInfo = new GavInfo();
        assertNotNull(gavInfo);
    }
}
