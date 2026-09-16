package com.zifang.util.office.pdf;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * PdfEditor PDF编辑器类的单元测试
 */

/**
 * PdfEditorTest类。
 */
public class PdfEditorTest {

    @Test
    /**
     * testClassExists方法。
     */
    public void testClassExists() {
        PdfEditor pdfEditor = new PdfEditor();
        assertNotNull(pdfEditor);
    }
}