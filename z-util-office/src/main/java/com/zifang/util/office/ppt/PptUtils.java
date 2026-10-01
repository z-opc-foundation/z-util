package com.zifang.util.office.ppt;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * PPT (.pptx) 基础工具：创建 / 读取 / 加 slide / 加文本框 / 文本查询。
 *
 * <p>本类不实现"美化版式"，所有 slide 都是空白页 + 一个文本框；用户可通过模板 + 模板引擎做更复杂的版式。
 */
public class PptUtils {

    /**
     * 创建一个全新的 .pptx。
     *
     * @return 新的 {@link XMLSlideShow}
     */
    public static XMLSlideShow create() {
        return new XMLSlideShow();
    }

    /**
     * 把 {@link XMLSlideShow} 写到输出流。**不关闭**输出流。
     *
     * @param show 演示文稿
     * @param out  输出流
     * @throws IOException 写入失败
     */
    public static void write(XMLSlideShow show, OutputStream out) throws IOException {
        if (show == null) {
            throw new IllegalArgumentException("show must not be null");
        }
        if (out == null) {
            throw new IllegalArgumentException("out must not be null");
        }
        show.write(out);
    }

    /**
     * 把 .pptx 写到指定文件。
     *
     * @param show 演示文稿
     * @param file 目标文件
     * @throws IOException 写入失败
     */
    public static void writeToFile(XMLSlideShow show, File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        try (OutputStream out = new java.io.FileOutputStream(file)) {
            write(show, out);
        }
    }

    /**
     * 从输入流读 .pptx。
     *
     * @param in 输入流
     * @return 演示文稿
     * @throws IOException 读取失败
     */
    public static XMLSlideShow read(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null");
        }
        return new XMLSlideShow(in);
    }

    /**
     * 添加一个空 slide（无占位符）。
     *
     * @param show 演示文稿
     * @return 新增的 slide
     */
    public static XSLFSlide addBlankSlide(XMLSlideShow show) {
        if (show == null) {
            throw new IllegalArgumentException("show must not be null");
        }
        return show.createSlide();
    }

    /**
     * 给 slide 添加一个文本框并写入文本。
     *
     * @param slide slide
     * @param text  文本内容
     * @return 文本框对象
     */
    public static XSLFTextBox addTextBox(XSLFSlide slide, String text) {
        if (slide == null) {
            throw new IllegalArgumentException("slide must not be null");
        }
        XSLFTextBox box = slide.createTextBox();
        if (text != null) {
            box.setText(text);
        }
        return box;
    }

    /**
     * 把 slide 的所有文本按段落拼接为一个字符串。
     *
     * @param slide slide
     * @return 文本内容
     */
    public static String collectText(XSLFSlide slide) {
        if (slide == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<XSLFTextParagraph> paragraphs = readParagraphs(slide);
        for (XSLFTextParagraph p : paragraphs) {
            List<XSLFTextRun> runs = p.getTextRuns();
            for (XSLFTextRun r : runs) {
                sb.append(r.getRawText());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * 把整份演示文稿的文本按 slide 拼接为多行字符串。
     *
     * @param show 演示文稿
     * @return 多行文本，每页一行
     */
    public static String collectAllText(XMLSlideShow show) {
        if (show == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (XSLFSlide slide : show.getSlides()) {
            sb.append(collectText(slide));
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * 取页数。
     *
     * @param show 演示文稿
     * @return 页数
     */
    public static int pageCount(XMLSlideShow show) {
        if (show == null) {
            throw new IllegalArgumentException("show must not be null");
        }
        return show.getSlides().size();
    }

    private static List<XSLFTextParagraph> readParagraphs(XSLFSlide slide) {
        java.util.List<XSLFTextParagraph> all = new java.util.ArrayList<>();
        java.util.List<org.apache.poi.xslf.usermodel.XSLFShape> shapes = slide.getShapes();
        for (org.apache.poi.xslf.usermodel.XSLFShape shape : shapes) {
            if (shape instanceof XSLFTextBox) {
                XSLFTextBox box = (XSLFTextBox) shape;
                all.addAll(box.getTextParagraphs());
            }
        }
        return all;
    }
}
