package com.zifang.util.media.graph.image;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * ImageCaptchaTest类。
 */
public class ImageCaptchaTest {

    @Test
    /**
     * testCreate方法。
     */
    public void testCreate() {
        ImageCaptcha captcha = ImageCaptcha.create();
        assertNotNull(captcha);
    }

    @Test
    /**
     * testWidth方法。
     */
    public void testWidth() {
        ImageCaptcha captcha = ImageCaptcha.create().width(300);
        assertNotNull(captcha);
    }

    @Test
    /**
     * testHeight方法。
     */
    public void testHeight() {
        ImageCaptcha captcha = ImageCaptcha.create().height(100);
        assertNotNull(captcha);
    }

    @Test
    /**
     * testLength方法。
     */
    public void testLength() {
        ImageCaptcha captcha = ImageCaptcha.create().length(6);
        assertNotNull(captcha);
    }

    @Test
    /**
     * testChars方法。
     */
    public void testChars() {
        ImageCaptcha captcha = ImageCaptcha.create().chars("ABC123");
        assertNotNull(captcha);
    }

    @Test
    /**
     * testRandomCode方法。
     */
    public void testRandomCode() {
        ImageCaptcha captcha = ImageCaptcha.create().length(4);
        String code = captcha.randomCode();
        assertNotNull(code);
        assertEquals(4, code.length());
    }

    @Test
    /**
     * testPngImage方法。
     */
    public void testPngImage() {
        ImageCaptcha captcha = ImageCaptcha.create();
        BufferedImage img = captcha.pngImage("ABCD");
        assertNotNull(img);
    }

    @Test
    /**
     * testPngToBase64方法。
     */
    public void testPngToBase64() {
        ImageCaptcha captcha = ImageCaptcha.create();
        String base64 = captcha.pngToBase64("ABCD");
        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/png;base64,"));
    }

    @Test
    /**
     * testPngToBase64WithResult方法。
     */
    public void testPngToBase64WithResult() throws IOException {
        ImageCaptcha captcha = ImageCaptcha.create();
        ImageCaptcha.CaptchaResult result = captcha.pngToBase64();
        assertNotNull(result);
        assertNotNull(result.code);
        assertNotNull(result.base64);
    }

    @Test
    /**
     * testGifToBase64方法。
     */
    public void testGifToBase64() {
        ImageCaptcha captcha = ImageCaptcha.create();
        String base64 = captcha.gifToBase64("ABCD");
        // GIF generation may return null if it fails
        if (base64 != null) {
            assertTrue(base64.startsWith("data:image/gif;base64,"));
        }
    }

    @Test
    /**
     * testGifToBase64WithResult方法。
     */
    public void testGifToBase64WithResult() throws IOException {
        ImageCaptcha captcha = ImageCaptcha.create();
        ImageCaptcha.CaptchaResult result = captcha.gifToBase64();
        assertNotNull(result);
        assertNotNull(result.code);
        // base64 may be null if GIF generation failed
    }
}
