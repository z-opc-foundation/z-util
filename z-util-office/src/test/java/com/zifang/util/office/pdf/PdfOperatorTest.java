package com.zifang.util.office.pdf;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * PdfOperator PDF操作类的单元测试
 */

/**
 * PdfOperatorTest类。
 */
public class PdfOperatorTest {

    @Test
    /**
     * testClassExists方法。
     */
    public void testClassExists() {
        PdfOperator pdfOperator = new PdfOperator();
        assertNotNull(pdfOperator);
    }
}