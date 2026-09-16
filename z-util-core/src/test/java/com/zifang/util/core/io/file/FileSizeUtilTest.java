package com.zifang.util.core.io.file;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * FileSizeUtilTest类。
 */
public class FileSizeUtilTest {

    @Test
    /**
     * testFormatSize_Bytes方法。
     */
    public void testFormatSize_Bytes() {
        assertEquals("0B", FileSizeUtil.formatSize(0));
        assertEquals("512B", FileSizeUtil.formatSize(512));
        assertEquals("1023B", FileSizeUtil.formatSize(1023));
        // 负数按0处理
        assertEquals("0B", FileSizeUtil.formatSize(-1));
    }

    @Test
    /**
     * testFormatSize_Kilobytes方法。
     */
    public void testFormatSize_Kilobytes() {
        assertEquals("1.00KB", FileSizeUtil.formatSize(1024));
        assertEquals("1.50KB", FileSizeUtil.formatSize(1536));
        // 1023.999KB四舍五入进位为1024.00KB
        assertEquals("1024.00KB", FileSizeUtil.formatSize(1024 * 1024 - 1));
    }

    @Test
    /**
     * testFormatSize_LargerUnits方法。
     */
    public void testFormatSize_LargerUnits() {
        long mb = 1024L * 1024;
        long gb = mb * 1024;
        long tb = gb * 1024;
        assertEquals("1.00MB", FileSizeUtil.formatSize(mb));
        assertEquals("10.00MB", FileSizeUtil.formatSize(10 * mb));
        assertEquals("1.00GB", FileSizeUtil.formatSize(gb));
        assertEquals("1.00TB", FileSizeUtil.formatSize(tb));
        // PB级
        long pb = tb * 1024;
        assertEquals("1.00PB", FileSizeUtil.formatSize(pb));
        // 超出最大单位时停留在PB
        assertEquals("2.00PB", FileSizeUtil.formatSize(2 * pb));
    }

    @Test
    /**
     * testFormatSize_CustomScale方法。
     */
    public void testFormatSize_CustomScale() {
        // 1536字节 = 1.5KB
        assertEquals("1.5KB", FileSizeUtil.formatSize(1536, 1));
        assertEquals("2KB", FileSizeUtil.formatSize(1536, 0));
        // 负scale按0处理
        assertEquals("2KB", FileSizeUtil.formatSize(1536, -1));
        // 小于1KB时始终整数表示
        assertEquals("512B", FileSizeUtil.formatSize(512, 3));
    }
}
