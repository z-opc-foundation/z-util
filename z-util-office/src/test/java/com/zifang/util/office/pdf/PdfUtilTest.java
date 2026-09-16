package com.zifang.util.office.pdf;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * PdfUtil的单元测试
 */

/**
 * PdfUtilTest类。
 */
public class PdfUtilTest {

    @Test
    /**
     * testFillImagesWithEmptyList方法。
     */
    public void testFillImagesWithEmptyList() {
        // Test with empty list
        File tempFile = new File(System.getProperty("java.io.tmpdir"), "empty_test_" + System.currentTimeMillis() + ".pdf");

        try {
            List<File> result = PdfUtil.fillImages(tempFile.getAbsolutePath(), Collections.emptyList(), false);
            assertTrue("Empty list should return empty result", result.isEmpty());
            assertTrue("PDF file should be created even with empty list", tempFile.exists());
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test
    /**
     * testFillImagesWithNonExistentFile方法。
     */
    public void testFillImagesWithNonExistentFile() {
        // Test with non-existent file - should handle gracefully
        File tempFile = new File(System.getProperty("java.io.tmpdir"), "nonexistent_test_" + System.currentTimeMillis() + ".pdf");
        File nonExistentImage = new File("/nonexistent/path/image.png");

        try {
            List<File> errors = PdfUtil.fillImages(tempFile.getAbsolutePath(), Arrays.asList(nonExistentImage), true);
            List<File> success = PdfUtil.fillImages(tempFile.getAbsolutePath(), Arrays.asList(nonExistentImage), false);

            assertFalse("Error list should not be empty for non-existent file", errors.isEmpty());
            assertTrue("Success list should be empty for non-existent file", success.isEmpty());
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test
    /**
     * testClassExists方法。
     */
    public void testClassExists() {
        // Verify the class can be instantiated
        PdfUtil pdfUtil = new PdfUtil();
        assertNotNull(pdfUtil);
    }
}