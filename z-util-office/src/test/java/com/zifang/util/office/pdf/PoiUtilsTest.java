package com.zifang.util.office.pdf;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * PoiUtils POI工具类的单元测试
 */

/**
 * PoiUtilsTest类。
 */
public class PoiUtilsTest {

    @Test
    /**
     * testClassExists方法。
     */
    public void testClassExists() {
        PoiUtils poiUtils = new PoiUtils();
        assertNotNull(poiUtils);
    }
}