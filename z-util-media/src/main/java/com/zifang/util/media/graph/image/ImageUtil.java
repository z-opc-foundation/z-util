package com.zifang.util.media.graph.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;

/**
 * 图片处理流式工具 API。
 * 静态工厂方法创建 ImageProcessor，链式调用完成复杂操作。
 *
 * <p>示例：
 * <pre>
 * ImageUtil.from("input.png")
 *     .resize(800, 600)
 *     .grayscale()
 *     .watermarkText("hello", 10, 30, null, Color.RED, 0.5f)
 *     .toFile("output.png");
 *
 * ImageUtil.from(file)
 *     .scale(0.5)
 *     .blur(2)
 *     .toBytes("jpg");
 * </pre>
 */
public final class ImageUtil {

    private ImageUtil() {
    }

    // ==================== 工厂方法 ====================

    /**
     * 从文件路径加载图片。
     */
    public static ImageProcessor from(String path) throws IOException {
        return ImageProcessor.load(path);
    }

    /**
     * 从 File 对象加载图片。
     */
    public static ImageProcessor from(File file) throws IOException {
        return ImageProcessor.load(file);
    }

    /**
     * 从 byte[] 加载图片。
     */
    public static ImageProcessor from(byte[] data) throws IOException {
        return ImageProcessor.load(data);
    }

    /**
     * 从 InputStream 加载图片。
     */
    public static ImageProcessor from(InputStream in) throws IOException {
        return new ImageProcessor(ImageReadWrite.read(in));
    }

    /**
     * 从 Path 加载图片。
     */
    public static ImageProcessor from(Path path) throws IOException {
        return ImageProcessor.load(path.toFile());
    }

    // ==================== 快捷静态方法 ====================

    /**
     * 调整图片尺寸。
     */
    public static BufferedImage resize(BufferedImage src, int width, int height) {
        return new ImageProcessor(src).resize(width, height).getImage();
    }

    /**
     * 裁剪图片。
     */
    public static BufferedImage crop(BufferedImage src, int x, int y, int width, int height) {
        return new ImageProcessor(src).crop(x, y, width, height).getImage();
    }

    /**
     * 灰度化。
     */
    public static BufferedImage grayscale(BufferedImage src) {
        return new ImageProcessor(src).grayscale().getImage();
    }

    /**
     * 调整亮度。
     */
    public static BufferedImage brightness(BufferedImage src, int delta) {
        return new ImageProcessor(src).brightness(delta).getImage();
    }

    /**
     * 调整对比度。
     */
    public static BufferedImage contrast(BufferedImage src, double factor) {
        return new ImageProcessor(src).contrast(factor).getImage();
    }

    /**
     * 模糊。
     */
    public static BufferedImage blur(BufferedImage src, int radius) {
        return new ImageProcessor(src).blur(radius).getImage();
    }

    /**
     * 锐化。
     */
    public static BufferedImage sharpen(BufferedImage src) {
        return new ImageProcessor(src).sharpen().getImage();
    }

    /**
     * 反色。
     */
    public static BufferedImage invert(BufferedImage src) {
        return new ImageProcessor(src).invert().getImage();
    }

    /**
     * 二值化。
     */
    public static BufferedImage threshold(BufferedImage src, int threshold) {
        return new ImageProcessor(src).threshold(threshold).getImage();
    }

    /**
     * 旋转。
     */
    public static BufferedImage rotate(BufferedImage src, double degrees) {
        return new ImageProcessor(src).rotate(degrees).getImage();
    }

    /**
     * 水平翻转。
     */
    public static BufferedImage flipH(BufferedImage src) {
        return new ImageProcessor(src).flipHorizontal().getImage();
    }

    /**
     * 垂直翻转。
     */
    public static BufferedImage flipV(BufferedImage src) {
        return new ImageProcessor(src).flipVertical().getImage();
    }

    /**
     * 添加文字水印。
     */
    public static BufferedImage watermark(BufferedImage src, String text, int x, int y, Font font, Color color, float alpha) {
        return new ImageProcessor(src).watermarkText(text, x, y, font, color, alpha).getImage();
    }

    /**
     * 横向拼接两张图片。
     */
    public static BufferedImage concatH(BufferedImage left, BufferedImage right) {
        return new ImageProcessor(left).concatRight(right).getImage();
    }

    /**
     * 纵向拼接两张图片。
     */
    public static BufferedImage concatV(BufferedImage top, BufferedImage bottom) {
        return new ImageProcessor(top).concatBottom(bottom).getImage();
    }

    // ==================== 写出 ====================

    /**
     * 将 BufferedImage 写出到文件（根据扩展名推断格式）。
     */
    public static void write(BufferedImage image, String path) throws IOException {
        String format = ImageReadWrite.inferFormat(path);
        ImageReadWrite.write(image, format, path);
    }

    /**
     * 将 BufferedImage 写出到文件。
     */
    public static void write(BufferedImage image, String formatName, String path) throws IOException {
        ImageReadWrite.write(image, formatName, path);
    }

    /**
     * 将 BufferedImage 写出到 OutputStream。
     */
    public static void write(BufferedImage image, String formatName, OutputStream out) throws IOException {
        ImageReadWrite.write(image, formatName, out);
    }

    /**
     * 将 BufferedImage 转为 byte[]。
     */
    public static byte[] toBytes(BufferedImage image) throws IOException {
        return ImageReadWrite.toBytes(image);
    }

    /**
     * 将 BufferedImage 转为指定格式的 byte[]。
     */
    public static byte[] toBytes(BufferedImage image, String formatName) throws IOException {
        return ImageReadWrite.toBytes(image, formatName);
    }

    /**
     * 将 BufferedImage 转为 base64 字符串（DataURL 形式）。
     */
    public static String toBase64(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageReadWrite.write(image, format, out);
        return "data:image/" + format + ";base64,"
                + java.util.Base64.getEncoder().encodeToString(out.toByteArray());
    }

    // ==================== 格式转换 ====================

    /**
     * 将任意图片转换为 PNG。
     */
    public static BufferedImage toPng(BufferedImage src) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageReadWrite.write(src, "png", out);
            return ImageReadWrite.read(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 将任意图片转换为 JPG（自动处理透明背景为白色）。
     */
    public static BufferedImage toJpg(BufferedImage src) {
        BufferedImage jpg = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = jpg.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return jpg;
    }
}
