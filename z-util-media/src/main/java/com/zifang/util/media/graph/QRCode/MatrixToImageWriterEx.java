package com.zifang.util.media.graph.qrcode;

import com.zifang.util.media.graph.qrcode.encoder.BitMatrix;
import com.zifang.util.media.graph.qrcode.encoder.ErrorCorrectionLevel;
import com.zifang.util.media.graph.qrcode.encoder.QRCodeEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 二维码图片写入扩展类。
 * 在 MatrixToImageWriter 的基础上增加了 Logo 水印功能，
 * 可以将 Logo 图片嵌入到二维码中间。
 */
public class MatrixToImageWriterEx {

    private static final MatrixToLogoImageConfig DEFAULT_CONFIG = new MatrixToLogoImageConfig();

    /**
     * 根据内容生成二维码位矩阵。
     *
     * @param content 二维码内容
     * @param width   二维码宽度
     * @param height  二维码高度
     * @return BitMatrix 位矩阵对象
     */
    public static BitMatrix createQRCode(String content, int width, int height) {
        return QRCodeEncoder.encode(content, width, height, ErrorCorrectionLevel.H, "UTF-8");
    }

    /**
     * 写入二维码到文件，并在二维码中间嵌入 Logo。
     *
     * @param matrix    二维码位矩阵
     * @param format    图片格式（如 "png", "jpg"）
     * @param imagePath 二维码图片保存路径
     * @param logoPath  Logo 图片路径
     * @throws IOException 如果文件写入失败
     */
    public static void writeToFile(BitMatrix matrix, String format, String imagePath, String logoPath) throws IOException {
        MatrixToImageWriter.writeToFile(matrix, format, new File(imagePath));

        //添加logo图片, 此处一定需要重新进行读取，而不能直接使用二维码的BufferedImage 对象
        BufferedImage img = ImageIO.read(new File(imagePath));
        overlapImage(img, format, imagePath, logoPath, DEFAULT_CONFIG);
    }

    /**
     * 写入二维码到文件，并嵌入自定义配置的 Logo。
     *
     * @param matrix     二维码位矩阵
     * @param format     图片格式
     * @param imagePath  二维码图片保存路径
     * @param logoPath   Logo 图片路径
     * @param logoConfig Logo 配置对象
     * @throws IOException 如果文件写入失败
     */
    public static void writeToFile(BitMatrix matrix, String format, String imagePath, String logoPath, MatrixToLogoImageConfig logoConfig) throws IOException {
        MatrixToImageWriter.writeToFile(matrix, format, new File(imagePath));

        //添加logo图片, 此处一定需要重新进行读取，而不能直接使用二维码的BufferedImage 对象
        BufferedImage img = ImageIO.read(new File(imagePath));
        overlapImage(img, format, imagePath, logoPath, logoConfig);
    }

    /**
     * 将 Logo 图片嵌入到二维码中间，并保存到文件。
     *
     * @param image      已生成的二维码图片
     * @param format     图片格式
     * @param imagePath  二维码图片保存路径
     * @param logoPath   Logo 图片路径
     * @param logoConfig Logo 配置对象
     */
    public static void overlapImage(BufferedImage image, String format, String imagePath, String logoPath, MatrixToLogoImageConfig logoConfig) {
        try {
            drawImage(logoPath, image, logoConfig);
            ImageIO.write(image, format, new File(imagePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将 Logo 添加到二维码中间，并输出到流。
     *
     * @param matrix     二维码位矩阵
     * @param format     图片格式
     * @param logoPath   Logo 图片路径
     * @param logoConfig Logo 配置对象，可为 null 使用默认配置
     * @param out        输出流
     * @throws IOException 如果写入失败
     */
    public static void overlapImage(BitMatrix matrix, String format, String logoPath, MatrixToLogoImageConfig logoConfig, OutputStream out) throws IOException {
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        drawImage(logoPath, image, logoConfig);
        ImageIO.write(image, format, out);
    }

    /**
     * 将 Logo 添加到二维码图片中间，并输出到流。
     *
     * @param image      已生成的二维码图片
     * @param format     图片格式
     * @param logoPath   Logo 图片路径
     * @param logoConfig Logo 配置对象，可为 null 使用默认配置
     * @param out        输出流
     * @throws IOException 如果写入失败
     */
    public static void overlapImage(BufferedImage image, String format, String logoPath, MatrixToLogoImageConfig logoConfig, OutputStream out) throws IOException {
        drawImage(logoPath, image, logoConfig);
        ImageIO.write(image, format, out);
    }

    /**
     * 将 Logo 绘制到二维码中间。
     *
     * @param logoPath   Logo 图片路径
     * @param image      目标二维码图片
     * @param logoConfig Logo 配置参数
     * @throws IOException 如果读取 Logo 文件失败
     */
    private static void drawImage(String logoPath, BufferedImage image, MatrixToLogoImageConfig logoConfig) throws IOException {
        if (logoConfig == null) {
            logoConfig = DEFAULT_CONFIG;
        }

        try {
            BufferedImage logo = ImageIO.read(new File(logoPath));
            Graphics2D g = image.createGraphics();

            //考虑到logo照片贴到二维码中，建议大小不要超过二维码的1/5;
            int width = image.getWidth() / logoConfig.getLogoPart();
            int height = image.getHeight() / logoConfig.getLogoPart();

            //logo起始位置，此目的是为logo居中显示
            int x = (image.getWidth() - width) / 2;
            int y = (image.getHeight() - height) / 2;

            //绘制图
            g.drawImage(logo, x, y, width, height, null);
            g.dispose();
        } catch (Exception e) {
            //捕捉异常后不做任何处理，防止图片路径错误而导致二维码生成失败
        }
    }
}
