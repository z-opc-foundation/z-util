package com.zifang.util.media.graph.image;

import com.zifang.util.media.graph.image.GIF.GifEncoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态 GIF 图片处理器。
 * 基于 {@link GifEncoder} 实现。
 *
 * <p>使用示例：
 * <pre>
 * new GifBuilder()
 *     .size(400, 300)
 *     .delay(100)          // 100ms 每帧
 *     .repeat(0)          // 0=无限循环
 *     .addFrame(frame1)
 *     .addFrame(frame2)
 *     .build()
 *     .write("output.gif");
 * </pre>
 */
public final class GifBuilder {

    private final List<BufferedImage> frames = new ArrayList<>();
    private int width = 200;
    private int height = 200;
    private int delay = 100;      // 帧间隔，毫秒
    private int repeat = 0;      // 循环次数，0=无限
    private int quality = 10;    // 1=最好（慢），20=最快
    private boolean started = false;

    /**
     * GifBuilder方法。
     */
    public GifBuilder() {
    }

    /**
     * 设置 GIF 尺寸。
     */
    public GifBuilder size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * 设置每帧间隔。
     *
     * @param delayMs 毫秒
     */
    public GifBuilder delay(int delayMs) {
        this.delay = delayMs;
        return this;
    }

    /**
     * 设置循环次数。
     *
     * @param count 0=无限循环
     */
    public GifBuilder repeat(int count) {
        this.repeat = count;
        return this;
    }

    /**
     * 设置编码质量。
     *
     * @param quality 1（最好）~20（最快）
     */
    public GifBuilder quality(int quality) {
        this.quality = quality;
        return this;
    }

    /**
     * 添加一帧。
     * 帧会被自动缩放到指定尺寸。
     */
    public GifBuilder addFrame(BufferedImage frame) {
        // 缩放至统一尺寸
        if (frame.getWidth() != width || frame.getHeight() != height) {
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(frame, 0, 0, width, height, null);
            g.dispose();
            frames.add(scaled);
        } else {
            frames.add(frame);
        }
        return this;
    }

    /**
     * 添加一帧（从文件）。
     */
    public GifBuilder addFrame(String path) throws IOException {
        return addFrame(ImageReadWrite.read(path));
    }

    /**
     * 构建 GIF。
     *
     * @throws IllegalStateException 未添加帧或未调用 build
     */
    public GifBuilder build() throws IOException {
        if (frames.isEmpty()) {
            throw new IllegalStateException("No frames added");
        }
        this.started = true;
        return this;
    }

    /**
     * 写入到文件。
     */
    public void write(String path) throws IOException {
        write(new File(path));
    }

    /**
     * 写入到 File。
     */
    public void write(File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            write(out);
        } finally {
            out.close();
        }
    }

    /**
     * 写入到 OutputStream（调用方负责关闭流）。
     */
    public void write(OutputStream out) throws IOException {
        if (frames.isEmpty()) {
            throw new IllegalStateException("No frames added. Call addFrame() before write().");
        }
        GifEncoder encoder = new GifEncoder();
        encoder.start(out);
        encoder.setDelay(delay);
        encoder.setRepeat(repeat);
        encoder.setQuality(quality);

        for (BufferedImage frame : frames) {
            encoder.addFrame(frame);
        }
        encoder.finish();
    }

    /**
     * 导出为 byte[]。
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out);
        return out.toByteArray();
    }
}
