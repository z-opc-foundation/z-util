package com.zifang.util.office.pdf;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * PdfConverter PDF转换器类的单元测试
 */

/**
 * PdfConverterTest类。
 */
public class PdfConverterTest {

    @Test
    /**
     * testClassExists方法。
     */
    public void testClassExists() {
        PdfConverter pdfConverter = new PdfConverter();
        assertNotNull(pdfConverter);
    }
}