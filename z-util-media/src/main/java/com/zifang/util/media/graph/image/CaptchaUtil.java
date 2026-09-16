package com.zifang.util.media.graph.image;

import com.zifang.util.media.graph.image.GIF.GifEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 图形验证码生成工具。
 * 支持 PNG 静态验证码和 GIF 动态验证码（字符逐帧显现）。
 */
public final class CaptchaUtil {

    private static final Random RANDOM = new Random();
    private static Font DEFAULT_FONT = new Font("Verdana", Font.ITALIC | Font.BOLD, 28);

    private CaptchaUtil() {
    }

    /**
     * 设置验证码字体。
     */
    public static void setFont(Font font) {
        DEFAULT_FONT = font;
    }

    /**
     * 生成 PNG 验证码图片。
     *
     * @param randomStr 验证码字符
     * @param width     图片宽度
     * @param height    图片高度
     * @return 验证码图片
     */
    public static BufferedImage pngCaptcha(String randomStr, int width, int height) {
        char[] chars = randomStr.toCharArray();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        int len = chars.length;

        // 白底
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 绘制干扰圆点
        for (int i = 0; i < 15; i++) {
            g.setColor(color(150, 250));
            g.drawOval(num(width), num(height), 5 + num(10), 5 + num(10));
        }

        g.setFont(DEFAULT_FONT);
        int h = height - ((height - DEFAULT_FONT.getSize()) >> 1);
        int w = width / len;

        for (int i = 0; i < len; i++) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            g.setColor(new Color(20 + num(110), 30 + num(110), 30 + num(110)));
            // 逐字符错位绘制
            g.drawString(chars[i] + "", (width - (len - i) * w) + (w - DEFAULT_FONT.getSize()) + 1, h - 4);
        }
        g.dispose();
        return bi;
    }

    /**
     * 生成 PNG 验证码并写入文件。
     */
    public static boolean pngCaptcha(String randomStr, int width, int height, String filePath) {
        try {
            BufferedImage bi = pngCaptcha(randomStr, width, height);
            ImageIO.write(bi, "png", new File(filePath));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 生成 PNG 验证码并返回 Base64 字符串。
     */
    public static String pngCaptchaBase64(String randomStr, int width, int height) {
        try {
            BufferedImage bi = pngCaptcha(randomStr, width, height);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 生成 GIF 动态验证码（字符逐帧显现）。
     *
     * @param randomStr  验证码字符
     * @param width      宽度
     * @param height     高度
     * @param frameDelay 每帧延迟（毫秒）
     * @return GIF 图片
     */
    public static BufferedImage gifCaptcha(String randomStr, int width, int height, int frameDelay) {
        char[] chars = randomStr.toCharArray();
        int len = chars.length;

        GifEncoder gifEncoder = new GifEncoder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        gifEncoder.start(out);
        gifEncoder.setQuality(180);
        gifEncoder.setDelay(frameDelay);
        gifEncoder.setRepeat(0);

        Color[] fontColors = new Color[len];
        for (int i = 0; i < len; i++) {
            fontColors[i] = new Color(20 + num(110), 20 + num(110), 20 + num(110));
        }

        // 逐帧绘制：每帧多显示一个字符
        for (int frame = 0; frame < len; frame++) {
            BufferedImage frameImage = createGifFrame(chars, fontColors, frame, width, height, len);
            gifEncoder.addFrame(frameImage);
        }
        // 最后一帧停留稍久
        gifEncoder.setDelay(frameDelay * 5);
        gifEncoder.addFrame(createGifFrame(chars, fontColors, len - 1, width, height, len));
        gifEncoder.finish();

        try {
            return ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 生成 GIF 并写出到文件。
     */
    public static boolean gifCaptcha(String randomStr, int width, int height, int frameDelay, String filePath) {
        try {
            BufferedImage gif = gifCaptcha(randomStr, width, height, frameDelay);
            if (gif == null) return false;
            ImageIO.write(gif, "gif", new File(filePath));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 生成 GIF 并返回 Base64。
     */
    public static String gifCaptchaBase64(String randomStr, int width, int height, int frameDelay) {
        BufferedImage gif = gifCaptcha(randomStr, width, height, frameDelay);
        if (gif == null) return null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(gif, "gif", out);
            return "data:image/gif;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }

    // ==================== 内部工具 ====================

    private static int num(int bound) {
        return RANDOM.nextInt(bound);
    }

    private static Color color(int fc, int bc) {
        int r = fc + num(bc - fc);
        int g = fc + num(bc - fc);
        int b = fc + num(bc - fc);
        return new Color(
                r > 255 ? 255 : r,
                g > 255 ? 255 : g,
                b > 255 ? 255 : b
        );
    }

    /**
     * 生成 GIF 单帧。
     *
     * @param chars        全部字符
     * @param fontColors   每字符颜色
     * @param visibleCount 当前帧可见字符数量
     */
    private static BufferedImage createGifFrame(char[] chars, Color[] fontColors,
                                                int visibleCount, int width, int height, int len) {
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) frame.getGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setFont(DEFAULT_FONT);

        int h = height - ((height - DEFAULT_FONT.getSize()) >> 1);
        int w = width / len;

        // 绘制已显现的字符
        int count = Math.min(visibleCount + 1, len);
        for (int i = 0; i < count; i++) {
            float alpha = getAlpha(visibleCount, i, len);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(fontColors[i]);
            g2d.drawOval(num(width), num(height), 5 + num(10), 5 + num(10));
            g2d.drawString(chars[i] + "", (width - (len - i) * w) + (w - DEFAULT_FONT.getSize()) + 1, h - 4);
        }

        g2d.dispose();
        return frame;
    }

    /**
     * 计算透明度，从0到1随字符位置变化自动计算步长。
     *
     * @param visibleCount 当前帧序号
     * @param charIndex    字符索引
     * @param len          字符总数
     * @return 透明度 0~1
     */
    private static float getAlpha(int visibleCount, int charIndex, int len) {
        int num = visibleCount + charIndex;
        float r = (float) 1 / len;
        float s = (len + 1) * r;
        return num > len ? (num * r - s) : num * r;
    }
}
