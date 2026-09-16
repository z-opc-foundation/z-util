package com.zifang.util.media.graph.image;

import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

/**
 * CaptchaUtilTest类。
 */
public class CaptchaUtilTest {

    @Test
    /**
     * testPngCaptcha方法。
     */
    public void testPngCaptcha() {
        BufferedImage captcha = CaptchaUtil.pngCaptcha("ABCD", 200, 100);
        assertNotNull(captcha);
        assertEquals(200, captcha.getWidth());
        assertEquals(100, captcha.getHeight());
    }

    @Test
    /**
     * testPngCaptchaWithDifferentLength方法。
     */
    public void testPngCaptchaWithDifferentLength() {
        BufferedImage captcha = CaptchaUtil.pngCaptcha("123456", 200, 100);
        assertNotNull(captcha);
    }

    @Test
    /**
     * testPngCaptchaBase64方法。
     */
    public void testPngCaptchaBase64() {
        String base64 = CaptchaUtil.pngCaptchaBase64("ABCD", 200, 100);
        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/png;base64,"));
    }

    @Test
    /**
     * testGifCaptcha方法。
     */
    public void testGifCaptcha() {
        BufferedImage gif = CaptchaUtil.gifCaptcha("ABCD", 200, 100, 100);
        assertNotNull(gif);
    }

    @Test
    /**
     * testGifCaptchaBase64方法。
     */
    public void testGifCaptchaBase64() {
        String base64 = CaptchaUtil.gifCaptchaBase64("ABCD", 200, 100, 100);
        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/gif;base64,"));
    }

    @Test
    /**
     * testSetFont方法。
     */
    public void testSetFont() {
        Font font = new Font("Arial", Font.BOLD, 20);
        CaptchaUtil.setFont(font);
        // Should not throw exception
        BufferedImage captcha = CaptchaUtil.pngCaptcha("TEST", 200, 100);
        assertNotNull(captcha);
    }
}
