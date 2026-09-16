package com.zifang.util.media.graph.image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;

/**
 * 图片处理核心实现类。
 * 纯 Java 实现，逐像素操作理解图片处理本质。
 * 所有操作均在内存 BufferedImage 上完成，不依赖第三方图片库。
 *
 * <p>核心概念：
 * <ul>
 *   <li>BufferedImage.TYPE_* 决定像素存储格式，转换时需匹配</li>
 *   <li>getRGB/setRGB 使用 (x,y) 坐标，像素以 int 存储 AARRGGBB</li>
 *   <li>Graphics2D 适合几何操作（裁剪/叠加/文字），逐像素适合颜色变换</li>
 *   <li>AffineTransformOp 用于旋转/翻转等几何变换，比 Graphics.drawImage 更精确</li>
 *   <li>ColorConvertOp 用于色彩空间转换（如灰度化）</li>
 *   <li>Kernel + ConvolveOp 用于卷积操作（模糊/锐化）</li>
 * </ul>
 */
public final class ImageProcessor {

    private BufferedImage image;
    private int width;
    private int height;

    /**
     * ImageProcessor方法。
     * * @param image BufferedImage类型参数
     */
    public ImageProcessor(BufferedImage image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    // ==================== 基础变换 ====================

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static float[] boxKernel(int radius) {
        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        float value = 1.0f / (size * size);
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
        }
        return data;
    }

    /**
     * 从文件加载。
     */
    public static ImageProcessor load(String path) throws java.io.IOException {
        return new ImageProcessor(ImageReadWrite.read(path));
    }

    /**
     * load方法。
     * * @param file File类型参数
     *
     * @return static ImageProcessor类型返回值
     */
    public static ImageProcessor load(File file) throws java.io.IOException {
        return new ImageProcessor(ImageReadWrite.read(file));
    }

    /**
     * load方法。
     * * @param data byte[]类型参数
     *
     * @return static ImageProcessor类型返回值
     */
    public static ImageProcessor load(byte[] data) throws java.io.IOException {
        return new ImageProcessor(ImageReadWrite.read(data));
    }

    /**
     * 调整尺寸。
     *
     * @param newWidth  目标宽度
     * @param newHeight 目标高度
     * @return this
     */
    public ImageProcessor resize(int newWidth, int newHeight) {
        if (newWidth <= 0) newWidth = 1;
        if (newHeight <= 0) newHeight = 1;
        BufferedImage resized = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g = resized.createGraphics();
        // 设置高质量插值
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        this.image = resized;
        this.width = newWidth;
        this.height = newHeight;
        return this;
    }

    // ==================== 颜色变换（像素级） ====================

    /**
     * 按比例缩放。
     *
     * @param scale 比例，0<scale<=1 缩小，>1 放大
     * @return this
     */
    public ImageProcessor scale(double scale) {
        if (scale <= 0) scale = 0.01;
        return resize((int) (width * scale), (int) (height * scale));
    }

    /**
     * 裁剪图片。
     *
     * @param x      起点 x
     * @param y      起点 y
     * @param width  裁剪宽度
     * @param height 裁剪高度
     * @return this
     */
    public ImageProcessor crop(int x, int y, int width, int height) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > this.width) width = this.width - x;
        if (y + height > this.height) height = this.height - y;
        if (width <= 0 || height <= 0) return this;
        this.image = image.getSubimage(x, y, width, height);
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * 旋转图片（绕中心点）。
     *
     * @param degrees 角度，正数为顺时针
     * @return this
     */
    public ImageProcessor rotate(double degrees) {
        AffineTransform tx = new AffineTransform();
        tx.rotate(Math.toRadians(degrees), width / 2.0, height / 2.0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        this.image = op.filter(image, null);
        this.width = image.getWidth();
        this.height = image.getHeight();
        return this;
    }

    /**
     * 水平翻转。
     */
    public ImageProcessor flipHorizontal() {
        AffineTransform tx = new AffineTransform(-1, 0, 0, 1, width, 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        this.image = op.filter(image, null);
        return this;
    }

    /**
     * 垂直翻转。
     */
    public ImageProcessor flipVertical() {
        AffineTransform tx = new AffineTransform(1, 0, 0, -1, 0, height);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        this.image = op.filter(image, null);
        return this;
    }

    // ==================== 卷积操作 ====================

    /**
     * 灰度化（逐像素实现）。
     * 灰度公式：Y = 0.299*R + 0.587*G + 0.114*B（人眼感知权重）
     */
    public ImageProcessor grayscale() {
        BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(image, gray);
        this.image = gray;
        return this;
    }

    /**
     * 调整亮度。
     *
     * @param delta 亮度调整值，-255~255
     * @return this
     */
    public ImageProcessor brightness(int delta) {
        int[] pixels = getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] >> 24) & 0xff;
            int r = (pixels[i] >> 16) & 0xff;
            int g = (pixels[i] >> 8) & 0xff;
            int b = pixels[i] & 0xff;
            r = clamp(r + delta);
            g = clamp(g + delta);
            b = clamp(b + delta);
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        setPixels(pixels);
        return this;
    }

    /**
     * 调整对比度。
     *
     * @param factor 对比度因子，>1 增强，<1 减弱，1 为不变
     * @return this
     */
    public ImageProcessor contrast(double factor) {
        int[] pixels = getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] >> 24) & 0xff;
            int r = (pixels[i] >> 16) & 0xff;
            int g = (pixels[i] >> 8) & 0xff;
            int b = pixels[i] & 0xff;
            r = clamp((int) ((r - 128) * factor + 128));
            g = clamp((int) ((g - 128) * factor + 128));
            b = clamp((int) ((b - 128) * factor + 128));
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        setPixels(pixels);
        return this;
    }

    // ==================== 叠加 / 水印 ====================

    /**
     * 反色。
     */
    public ImageProcessor invert() {
        int[] pixels = getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] >> 24) & 0xff;
            int r = (pixels[i] >> 16) & 0xff;
            int g = (pixels[i] >> 8) & 0xff;
            int b = pixels[i] & 0xff;
            r = 255 - r;
            g = 255 - g;
            b = 255 - b;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        setPixels(pixels);
        return this;
    }

    /**
     * 二值化（阈值）。
     *
     * @param threshold 阈值 0~255
     * @return this
     */
    public ImageProcessor threshold(int threshold) {
        int[] pixels = getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int a = (pixels[i] >> 24) & 0xff;
            int r = (pixels[i] >> 16) & 0xff;
            int g = (pixels[i] >> 8) & 0xff;
            int b = pixels[i] & 0xff;
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            int v = gray >= threshold ? 255 : 0;
            pixels[i] = (a << 24) | (v << 16) | (v << 8) | v;
        }
        setPixels(pixels);
        return this;
    }

    /**
     * 模糊（均值滤波）。
     *
     * @param radius 卷积核半径（3x3 / 5x5 / 7x7...）
     * @return this
     */
    public ImageProcessor blur(int radius) {
        float[] data = boxKernel(radius);
        convolve(data, radius);
        return this;
    }

    // ==================== 拼接 ====================

    /**
     * 锐化。
     */
    public ImageProcessor sharpen() {
        float[] data = {
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
        };
        convolve(data, 3);
        return this;
    }

    /**
     * 边缘检测（Sobel-like Laplacian）。
     */
    public ImageProcessor edgeDetect() {
        float[] data = {
                -1, -1, -1,
                -1, 8, -1,
                -1, -1, -1
        };
        convolve(data, 3);
        return this;
    }

    // ==================== 获取结果 ====================

    /**
     * 在图片上叠加另一个图片（图层混合）。
     *
     * @param overlay 叠加图层
     * @param x       叠加位置 x
     * @param y       叠加位置 y
     * @param alpha   透明度 0~1
     * @return this
     */
    public ImageProcessor overlay(BufferedImage overlay, int x, int y, float alpha) {
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(overlay, x, y, null);
        g.dispose();
        return this;
    }

    /**
     * 添加文字水印。
     *
     * @param text  文字
     * @param x     x 坐标
     * @param y     y 坐标
     * @param font  字体
     * @param color 颜色
     * @param alpha 透明度 0~1
     * @return this
     */
    public ImageProcessor watermarkText(String text, int x, int y, Font font, Color color, float alpha) {
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setFont(font != null ? font : new Font("SimHei", Font.BOLD, 30));
        g.setColor(color != null ? color : Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString(text, x, y);
        g.dispose();
        return this;
    }

    /**
     * 添加图片水印。
     *
     * @param watermark 水印图片
     * @param x         位置 x
     * @param y         位置 y
     * @param alpha     透明度 0~1
     * @return this
     */
    public ImageProcessor watermarkImage(BufferedImage watermark, int x, int y, float alpha) {
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(watermark, x, y, null);
        g.dispose();
        return this;
    }

    /**
     * 横向拼接另一张图片。
     *
     * @param other 右侧图片
     * @return 拼接后的新图片（当前图片在左，other 在右）
     */
    public ImageProcessor concatRight(BufferedImage other) {
        int newWidth = this.width + other.getWidth();
        int newHeight = Math.max(this.height, other.getHeight());
        BufferedImage combined = new BufferedImage(newWidth, newHeight, this.image.getType());
        Graphics2D g = combined.createGraphics();
        g.drawImage(this.image, 0, 0, null);
        g.drawImage(other, this.width, 0, null);
        g.dispose();
        this.image = combined;
        this.width = newWidth;
        this.height = newHeight;
        return this;
    }

    /**
     * 纵向拼接另一张图片。
     *
     * @param other 下方图片
     * @return 拼接后的新图片（当前图片在上，other 在下）
     */
    public ImageProcessor concatBottom(BufferedImage other) {
        int newWidth = Math.max(this.width, other.getWidth());
        int newHeight = this.height + other.getHeight();
        BufferedImage combined = new BufferedImage(newWidth, newHeight, this.image.getType());
        Graphics2D g = combined.createGraphics();
        g.drawImage(this.image, 0, 0, null);
        g.drawImage(other, 0, this.height, null);
        g.dispose();
        this.image = combined;
        this.width = newWidth;
        this.height = newHeight;
        return this;
    }

    /**
     * 获取处理后的图片。
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * 获取宽。
     */
    public int getWidth() {
        return width;
    }

    // ==================== 内部工具 ====================

    /**
     * 获取高。
     */
    public int getHeight() {
        return height;
    }

    /**
     * 导出为 File。
     */
    public void write(String format, String path) throws java.io.IOException {
        ImageReadWrite.write(image, format, path);
    }

    /**
     * 导出为 OutputStream。
     */
    public void write(String format, java.io.OutputStream out) throws java.io.IOException {
        ImageReadWrite.write(image, format, out);
    }

    /**
     * 导出为 byte[]。
     */
    public byte[] toBytes() throws java.io.IOException {
        return ImageReadWrite.toBytes(image);
    }

    /**
     * toBytes方法。
     * * @param format String类型参数
     *
     * @return byte[]类型返回值
     */
    public byte[] toBytes(String format) throws java.io.IOException {
        return ImageReadWrite.toBytes(image, format);
    }

    // ==================== 静态工厂 ====================

    private int[] getPixels() {
        return image.getRGB(0, 0, width, height, null, 0, width);
    }

    private void setPixels(int[] pixels) {
        image.setRGB(0, 0, width, height, pixels, 0, width);
    }

    private void convolve(float[] kernel, int size) {
        Kernel conv = new Kernel(size, size, kernel);
        ConvolveOp op = new ConvolveOp(conv, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage filtered = new BufferedImage(width, height, image.getType());
        op.filter(image, filtered);
        this.image = filtered;
    }
}
