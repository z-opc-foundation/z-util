package com.zifang.util.media.graph.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * 图片读取与写入接口。
 * 纯 Java 实现，使用 ImageIO + BufferedImage。
 * 如需切换实现（如 JavaCV），替换此实现类即可。
 */
public final class ImageReadWrite {

    private ImageReadWrite() {
    }

    // ==================== 读取 ====================

    /**
     * 从文件读取图片。
     *
     * @param path 文件路径
     * @return BufferedImage
     * @throws IOException 读取失败
     */
    public static BufferedImage read(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    /**
     * 从 File 对象读取图片。
     */
    public static BufferedImage read(File file) throws IOException {
        return ImageIO.read(file);
    }

    /**
     * 从 InputStream 读取图片。
     */
    public static BufferedImage read(InputStream in) throws IOException {
        return ImageIO.read(in);
    }

    /**
     * 从 byte[] 读取图片。
     */
    public static BufferedImage read(byte[] data) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(data));
    }

    // ==================== 写入 ====================

    /**
     * 将图片写入文件，自动根据扩展名推断格式。
     *
     * @param image      图片
     * @param formatName 格式名，如 "png", "jpg", "jpeg", "gif", "bmp"
     * @param path       目标路径
     * @throws IOException 写入失败
     */
    public static void write(BufferedImage image, String formatName, String path) throws IOException {
        File out = new File(path);
        // 确保父目录存在
        File parent = out.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!ImageIO.write(image, formatName, out)) {
            throw new IOException("No appropriate image writer for format: " + formatName);
        }
    }

    /**
     * 将图片写入文件。
     */
    public static void write(BufferedImage image, String formatName, File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!ImageIO.write(image, formatName, file)) {
            throw new IOException("No appropriate image writer for format: " + formatName);
        }
    }

    /**
     * 将图片写入 OutputStream。
     */
    public static void write(BufferedImage image, String formatName, OutputStream out) throws IOException {
        if (!ImageIO.write(image, formatName, out)) {
            throw new IOException("No appropriate image writer for format: " + formatName);
        }
    }

    /**
     * 将图片写入 byte[]。
     *
     * @return PNG 格式的 byte 数组
     */
    public static byte[] toBytes(BufferedImage image) throws IOException {
        return toBytes(image, "png");
    }

    /**
     * 将图片写入 byte[]。
     */
    public static byte[] toBytes(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(image, formatName, out);
        out.flush();
        return out.toByteArray();
    }

    // ==================== 工具 ====================

    /**
     * 根据文件扩展名推断格式名。
     */
    public static String inferFormat(String path) {
        String ext = "";
        int dot = path.lastIndexOf('.');
        if (dot >= 0) {
            ext = path.substring(dot + 1).toLowerCase();
        }
        // 统一 jpeg 扩展名
        if ("jpg".equals(ext)) ext = "jpeg";
        return ext;
    }

    /**
     * 判断文件是否是支持的图片格式。
     */
    public static boolean isSupported(String path) {
        String format = inferFormat(path);
        return ImageIO.getImageReadersByFormatName(format).hasNext();
    }
}
