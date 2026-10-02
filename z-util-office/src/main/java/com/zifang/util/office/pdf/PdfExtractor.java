package com.zifang.util.office.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PDF 信息提取工具：从文件中抽出文本、页数、元数据。
 *
 * <p>所有方法对入参为 null / 文件不存在一律抛 {@link IllegalArgumentException}；
 * 文件层面的 IO 错误抛出 {@link IOException}。
 */
public class PdfExtractor {

    /**
     * 抽取整份 PDF 的纯文本，按页顺序拼接，行间用换行隔开。
     *
     * @param input 输入 PDF
     * @return 文本内容
     * @throws IOException 读 / 解析失败
     */
    public static String extractText(File input) throws IOException {
        return extractText(input, 0, Integer.MAX_VALUE);
    }

    /**
     * 抽取指定页范围的纯文本（含两端）。
     *
     * @param input   输入 PDF
     * @param fromIdx 起始页（1-based），小于 1 时按 1 处理
     * @param toIdx   结束页（1-based），超过总页数时按总页数处理
     * @return 文本内容
     * @throws IOException 读 / 解析失败
     */
    public static String extractText(File input, int fromIdx, int toIdx) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        try (PDDocument doc = Loader.loadPDF(input)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(Math.max(fromIdx, 1));
            stripper.setEndPage(Math.max(toIdx, 1));
            return stripper.getText(doc);
        }
    }

    /**
     * 抽取 PDF 文档信息（标题、作者、主题、关键字、创建者、生产者、创建日期、最近修改日期）。
     *
     * @param input 输入 PDF
     * @return 元数据键值对；不存在的字段不会出现在结果中
     * @throws IOException 读 / 解析失败
     */
    public static Map<String, String> extractMetadata(File input) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        Map<String, String> result = new LinkedHashMap<>();
        try (PDDocument doc = Loader.loadPDF(input)) {
            PDDocumentInformation info = doc.getDocumentInformation();
            if (info == null) {
                return result;
            }
            putIfPresent(result, "title", info.getTitle());
            putIfPresent(result, "author", info.getAuthor());
            putIfPresent(result, "subject", info.getSubject());
            putIfPresent(result, "keywords", info.getKeywords());
            putIfPresent(result, "creator", info.getCreator());
            putIfPresent(result, "producer", info.getProducer());
            if (info.getCreationDate() != null) {
                result.put("creationDate", info.getCreationDate().getTime().toString());
            }
            if (info.getModificationDate() != null) {
                result.put("modificationDate", info.getModificationDate().getTime().toString());
            }
        }
        return result;
    }

    /**
     * 取 PDF 页数。
     *
     * @param input 输入 PDF
     * @return 页数
     * @throws IOException 读 / 解析失败
     */
    public static int pageCount(File input) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        try (PDDocument doc = Loader.loadPDF(input)) {
            return doc.getNumberOfPages();
        }
    }

    /**
     * 判断 PDF 是否已加密。
     * 带非空 user 密码的文件在无密码打开时会抛 InvalidPasswordException——那本身就是"已加密"的证据。
     *
     * @param input 输入 PDF
     * @return true 表示带密码保护
     * @throws IOException 读 / 解析失败
     */
    public static boolean isEncrypted(File input) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        try (PDDocument doc = Loader.loadPDF(input)) {
            return doc.isEncrypted();
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
            return true;
        }
    }

    /**
     * 取指定页的旋转角度（0/90/180/270）。
     *
     * @param input     输入 PDF
     * @param pageIndex 页索引（0-based）
     * @return 旋转角度
     * @throws IOException 读 / 解析失败
     */
    public static int pageRotation(File input, int pageIndex) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        try (PDDocument doc = Loader.loadPDF(input)) {
            if (pageIndex < 0 || pageIndex >= doc.getNumberOfPages()) {
                throw new IllegalArgumentException("page index out of range: " + pageIndex);
            }
            return doc.getPage(pageIndex).getRotation();
        }
    }

    /**
     * 把指定页渲染为 PNG 图片写到目标文件（走 JDK ImageIO，300 像素/英寸下不额外缩放）。
     *
     * @param input     输入 PDF
     * @param pageIndex 页索引（0-based）
     * @param targetPng 输出 PNG 文件
     * @return 渲染出的图像宽度 × 高度（像素）
     * @throws IOException 渲染 / 写文件失败
     */
    public static int[] renderPageAsImage(File input, int pageIndex, File targetPng) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        if (targetPng == null) {
            throw new IllegalArgumentException("targetPng must not be null");
        }
        try (PDDocument doc = Loader.loadPDF(input)) {
            if (pageIndex < 0 || pageIndex >= doc.getNumberOfPages()) {
                throw new IllegalArgumentException("page index out of range: " + pageIndex);
            }
            org.apache.pdfbox.rendering.PDFRenderer renderer = new org.apache.pdfbox.rendering.PDFRenderer(doc);
            java.awt.image.BufferedImage image = renderer.renderImage(pageIndex);
            javax.imageio.ImageIO.write(image, "png", targetPng);
            return new int[]{image.getWidth(), image.getHeight()};
        }
    }

    /**
     * 抽取 PDF 每一页内嵌位图（PDImageXObject）为 PNG 字节。
     * 相同 XObject 出现在多页时按 (页, 位置) 重复计数——与页面渲染顺序一致。
     * 无图片返回空列表。
     *
     * @param input 输入 PDF
     * @return PNG 编码后的图片字节列表
     * @throws IOException 读 / 编码失败
     */
    public static List<byte[]> extractImages(File input) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        List<byte[]> result = new ArrayList<>();
        try (PDDocument doc = Loader.loadPDF(input)) {
            for (int p = 0; p < doc.getNumberOfPages(); p++) {
                org.apache.pdfbox.pdmodel.PDResources res = doc.getPage(p).getResources();
                if (res == null) {
                    continue;
                }
                for (org.apache.pdfbox.cos.COSName name : res.getXObjectNames()) {
                    org.apache.pdfbox.pdmodel.graphics.PDXObject xobj = res.getXObject(name);
                    if (xobj instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
                        org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject img =
                                (org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) xobj;
                        java.awt.image.BufferedImage bi = img.getImage();
                        if (bi == null) {
                            continue;
                        }
                        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                        javax.imageio.ImageIO.write(bi, "png", baos);
                        result.add(baos.toByteArray());
                    }
                }
            }
        }
        return result;
    }

    private static void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isEmpty()) {
            map.put(key, value);
        }
    }
}
