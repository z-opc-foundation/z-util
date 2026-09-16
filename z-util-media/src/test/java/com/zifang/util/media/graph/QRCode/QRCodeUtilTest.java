package com.zifang.util.media.graph.qrcode;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * QRCodeUtilTest类。
 */
public class QRCodeUtilTest {

    @Test
    /**
     * testQRCodeUtilExists方法。
     */
    public void testQRCodeUtilExists() {
        QRCodeUtil util = new QRCodeUtil();
        assertNotNull(util);
    }

    @Test
    /**
     * testMkdirs方法。
     */
    public void testMkdirs() {
        // Should not throw exception - mkdirs handles existing directories
        QRCodeUtil.mkdirs("/tmp/test_qr");
    }

    @Test
    /**
     * testEncodeWithOutputStream方法。
     */
    public void testEncodeWithOutputStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QRCodeUtil.encode("test content", out);
        // Should not throw exception
    }

    @Test
    /**
     * testEncodeWithoutLogo方法。
     */
    public void testEncodeWithoutLogo() throws Exception {
        String content = "Test QR Code";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QRCodeUtil.encode(content, out);
        assertTrue(out.size() > 0);
    }
}
