package com.zifang.util.core.io;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 图片压缩工具类
 * <p>
 * 在内存中按指定质量压缩图片字节，不产生磁盘文件，适合上传/传输前的体积控制。
 * 基于 ImageIO 标准编码器实现：
 * <ul>
 *   <li>透明通道会被压平为白色背景（避免透明区域输出为黑色）</li>
 *   <li>JPEG 等有损格式按质量参数压缩，质量越小体积越小</li>
 *   <li>PNG 等无损格式按默认压缩输出</li>
 * </ul>
 * <p>
 * 宽松语义：入参为空、非图片类型、字节无法解析为图片或无对应编码器时，
 * 原样返回输入字节，不抛异常。
 *
 * @author zifang
 * @see ImageIO
 * @see ImageWriteParam
 */
public class ImageCompressUtil {

    private static final float MIN_QUALITY = 0.01f;
    private static final float MAX_QUALITY = 1.0f;

    /**
     * 按质量压缩图片字节
     * <p>
     * 将图片字节按指定的 MIME 类型重新编码，有损格式（如 image/jpeg）受
     * quality 参数影响，取值范围 0.01~1.0（超出会自动收拢到边界）。
     *
     * @param imageBytes 原始图片字节（null 或空时原样返回）
     * @param contentType MIME 类型（null 或不以 image/ 开头时原样返回）
     * @param quality    压缩质量 0.01~1.0，值越小体积越小
     * @return 压缩后的图片字节；无法处理时返回原始字节
     * @throws RuntimeException 如果重新编码过程中发生 IO 错误
     */
    public static byte[] compress(byte[] imageBytes, String contentType, float quality) {
        if (imageBytes == null || imageBytes.length == 0) {
            return imageBytes;
        }
        if (contentType == null || !contentType.startsWith("image/")) {
            return imageBytes;
        }
        BufferedImage source;
        try {
            source = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            return imageBytes;
        }
        if (source == null) {
            return imageBytes;
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(contentType);
        if (!writers.hasNext()) {
            return imageBytes;
        }
        float clamped = Math.max(MIN_QUALITY, Math.min(MAX_QUALITY, quality));
        BufferedImage flattened = flattenToWhiteBackground(source);
        ImageWriter writer = writers.next();
        try {
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                try {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(clamped);
                } catch (UnsupportedOperationException | IllegalArgumentException unsupportedQuality) {
                    // 无损格式不支持质量参数时，按默认压缩输出
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writer.setOutput(ImageIO.createImageOutputStream(out));
            writer.write(null, new IIOImage(flattened, null, null), param);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("image compress error", e);
        } finally {
            writer.dispose();
        }
    }

    /**
     * 将图片压平到白色不透明背景
     *
     * @param source 原图片（可能带透明通道）
     * @return TYPE_INT_RGB 白底图片，宽高与原图一致
     */
    private static BufferedImage flattenToWhiteBackground(BufferedImage source) {
        BufferedImage flattened = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = flattened.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return flattened;
    }
}
