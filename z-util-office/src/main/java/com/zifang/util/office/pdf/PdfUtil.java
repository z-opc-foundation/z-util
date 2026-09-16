package com.zifang.util.office.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF文档操作工具类
 * 提供PDF文档的创建、编辑等常用操作
 */
public class PdfUtil {

    /**
     * 将图片列表填充到PDF文档中
     * 每张图片生成一页，页面大小与图片尺寸一致
     *
     * @param pdfTargetFilePath 目标PDF文件路径
     * @param imageFiles        图片文件列表
     * @param errorFlag         是否返回错误文件列表，true返回错误列表，false返回成功列表
     * @return 根据errorFlag返回成功或失败的文件列表
     * @throws RuntimeException 如果保存PDF失败则抛出运行时异常
     */
    public static List<File> fillImages(String pdfTargetFilePath, List<File> imageFiles, boolean errorFlag) {

        List<File> su = new ArrayList<>();
        List<File> error = new ArrayList<>();

        PDDocument document = new PDDocument();

        for (File image : imageFiles) {

            // 创建图片
            PDImageXObject pdImage = null;
            try {
                pdImage = PDImageXObject.createFromFile(image.getAbsolutePath(), document);
                int weight = pdImage.getWidth();
                int height = pdImage.getHeight();

                // 设置页大小
                PDRectangle rectangle = new PDRectangle(weight, height);
                PDPage page = new PDPage(rectangle);

                // 填充
                PDPageContentStream contents = new PDPageContentStream(document, page);
                contents.drawImage(pdImage, 0, 0, weight, height);

                // 添加到文档
                document.addPage(page);
                contents.close();
            } catch (Exception e) {
                error.add(image);
                continue;
            }
            su.add(image);
        }

        try {
            document.save(pdfTargetFilePath);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (errorFlag) {
            return error;
        } else {
            return su;
        }
    }
}
