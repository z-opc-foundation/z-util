package com.zifang.util.media.graph.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * 图形验证码生成器。
 * 提供简洁 API 生成 PNG / GIF 验证码图片。
 *
 * <p>示例：
 * <pre>
 * // 生成4位PNG验证码
 * String code = ImageCaptcha.create()
 *     .length(4)
 *     .pngToFile("captcha.png");
 *
 * // 生成GIF动态验证码
 * String gif = ImageCaptcha.create()
 *     .width(160).height(60)
 *     .pngToBase64(6);
 * </pre>
 */
public final class ImageCaptcha {

    private static final String DEFAULT_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random RANDOM = new Random();

    private int width = 200;
    private int height = 40;
    private int length = 4;
    private String chars = DEFAULT_CHARS;

    /**
     * create方法。
     *
     * @return static ImageCaptcha类型返回值
     */
    public static ImageCaptcha create() {
        return new ImageCaptcha();
    }

    /**
     * width方法。
     * * @param width int类型参数
     *
     * @return ImageCaptcha类型返回值
     */
    public ImageCaptcha width(int width) {
        this.width = width;
        return this;
    }

    /**
     * height方法。
     * * @param height int类型参数
     *
     * @return ImageCaptcha类型返回值
     */
    public ImageCaptcha height(int height) {
        this.height = height;
        return this;
    }

    /**
     * length方法。
     * * @param length int类型参数
     *
     * @return ImageCaptcha类型返回值
     */
    public ImageCaptcha length(int length) {
        this.length = length;
        return this;
    }

    /**
     * chars方法。
     * * @param chars String类型参数
     *
     * @return ImageCaptcha类型返回值
     */
    public ImageCaptcha chars(String chars) {
        this.chars = chars;
        return this;
    }

    /**
     * 生成随机验证码字符串。
     */
    public String randomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成 PNG 验证码 BufferedImage。
     */
    public BufferedImage pngImage(String code) {
        return CaptchaUtil.pngCaptcha(code, width, height);
    }

    /**
     * 生成 PNG 验证码并写出到文件，返回验证码字符串。
     */
    public String pngToFile(String filePath) throws IOException {
        String code = randomCode();
        BufferedImage bi = pngImage(code);
        if (!javax.imageio.ImageIO.write(bi, "png", new File(filePath))) {
            throw new IOException("Failed to write PNG captcha to: " + filePath);
        }
        return code;
    }

    /**
     * 生成 PNG 验证码并返回 Base64 DataURL。
     */
    public String pngToBase64(String code) {
        return CaptchaUtil.pngCaptchaBase64(code, width, height);
    }

    /**
     * 生成 PNG 验证码的 Base64，返回验证码字符串和 Base64 元组。
     */
    public CaptchaResult pngToBase64() throws IOException {
        String code = randomCode();
        String base64 = pngToBase64(code);
        return new CaptchaResult(code, base64);
    }

    /**
     * 生成 GIF 动态验证码并写出到文件。
     */
    public String gifToFile(String filePath) throws IOException {
        String code = randomCode();
        BufferedImage gif = CaptchaUtil.gifCaptcha(code, width, height, 100);
        if (gif == null) {
            throw new IOException("Failed to generate GIF captcha");
        }
        if (!javax.imageio.ImageIO.write(gif, "gif", new File(filePath))) {
            throw new IOException("Failed to write GIF captcha to: " + filePath);
        }
        return code;
    }

    /**
     * 生成 GIF 验证码并返回 Base64 DataURL。
     */
    public String gifToBase64(String code) {
        return CaptchaUtil.gifCaptchaBase64(code, width, height, 100);
    }

    /**
     * 生成 GIF 验证码的 Base64。
     */
    public CaptchaResult gifToBase64() throws IOException {
        String code = randomCode();
        String base64 = gifToBase64(code);
        return new CaptchaResult(code, base64);
    }

    /**
     * 验证码结果封装。
     */
    public static class CaptchaResult {
        public final String code;
        public final String base64;

        /**
         * CaptchaResult方法。
         * * @param code String类型参数
         *
         * @param base64 String类型参数
         */
        public CaptchaResult(String code, String base64) {
            this.code = code;
            this.base64 = base64;
        }
    }
}
