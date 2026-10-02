package com.zifang.util.office.core;

import com.zifang.util.office.excel.ExcelUtils;
import com.zifang.util.office.ppt.PptUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Office → PDF 单向文本渲染转换器。
 * <p>
 * 定位：把 DOCX / XLSX / PPTX 抽出文本层，按 A4 逐行排版为可搜索的 PDF。
 * 不是版式还原工具——不嵌入 TTF、不渲染图片、不画表格线、不保留字体字号样式。
 * 目标场景：产物归档预览、纯文本比对、批量转 PDF 供下游 PDF 工具链。
 * <p>
 * <b>字符覆盖</b>：PDFBox 内建 Helvetica（WinAnsiEncoding）只覆盖拉丁-1；
 * 非 ASCII 字符按 '?' 占位。CJK 场景需自行嵌入 TTF（不在本 util 范围）。
 * <p>
 * <b>输入</b>：调用方负责传入对应扩展名的流；null 或空内容一律生成合法空 PDF（0 页时补 1 空白页），
 * 不抛异常。IO/解析失败抛 {@link IOException}。
 *
 * @author zifang
 */
public class OfficeConverter {

    private OfficeConverter() {
    }

    /**
     * DOCX → PDF。段落 + 表格文本按文档顺序渲染；表格行以 "  |  " 分隔单元格。
     */
    public static void wordToPdf(InputStream docxIn, OutputStream pdfOut) throws IOException {
        if (docxIn == null) {
            throw new IllegalArgumentException("docxIn must not be null");
        }
        if (pdfOut == null) {
            throw new IllegalArgumentException("pdfOut must not be null");
        }
        try (XWPFDocument doc = new XWPFDocument(docxIn);
             PDDocument pdf = new PDDocument()) {
            try (LineWriter w = new LineWriter(pdf, 11f)) {
                for (XWPFParagraph p : doc.getParagraphs()) {
                    String t = p.getText();
                    if (t != null) {
                        w.writeLine(t);
                    }
                }
                for (XWPFTable tbl : doc.getTables()) {
                    w.writeLine("");
                    for (XWPFTableRow row : tbl.getRows()) {
                        StringBuilder sb = new StringBuilder();
                        boolean first = true;
                        for (XWPFTableCell c : row.getTableCells()) {
                            if (!first) {
                                sb.append("  |  ");
                            }
                            sb.append(c.getText());
                            first = false;
                        }
                        w.writeLine(sb.toString());
                    }
                }
            }
            if (pdf.getNumberOfPages() == 0) {
                pdf.addPage(new PDPage(PDRectangle.A4));
            }
            pdf.save(pdfOut);
        }
    }

    /**
     * XLSX → PDF。全部 sheet 的行按顺序拼接；每行以 "  |  " 分隔单元格；空行跳过。
     */
    public static void excelToPdf(InputStream xlsxIn, OutputStream pdfOut) throws IOException {
        if (xlsxIn == null) {
            throw new IllegalArgumentException("xlsxIn must not be null");
        }
        if (pdfOut == null) {
            throw new IllegalArgumentException("pdfOut must not be null");
        }
        List<List<String>> rows = ExcelUtils.readAllSheets(xlsxIn, "xlsx", 0);
        try (PDDocument pdf = new PDDocument()) {
            try (LineWriter w = new LineWriter(pdf, 10f)) {
                for (List<String> row : rows) {
                    w.writeLine(String.join("  |  ", row));
                }
            }
            if (pdf.getNumberOfPages() == 0) {
                pdf.addPage(new PDPage(PDRectangle.A4));
            }
            pdf.save(pdfOut);
        }
    }

    /**
     * PPTX → PDF。每个 slide 一页；slide 内文本按段落换行。
     */
    public static void pptToPdf(InputStream pptxIn, OutputStream pdfOut) throws IOException {
        if (pptxIn == null) {
            throw new IllegalArgumentException("pptxIn must not be null");
        }
        if (pdfOut == null) {
            throw new IllegalArgumentException("pdfOut must not be null");
        }
        try (XMLSlideShow show = PptUtils.read(pptxIn);
             PDDocument pdf = new PDDocument()) {
            List<XSLFSlide> slides = show.getSlides();
            if (slides.isEmpty()) {
                pdf.addPage(new PDPage(PDRectangle.A4));
            } else {
                for (XSLFSlide slide : slides) {
                    try (LineWriter w = new LineWriter(pdf, 12f)) {
                        String text = PptUtils.collectText(slide);
                        if (text != null) {
                            for (String line : text.split("\n", -1)) {
                                w.writeLine(line);
                            }
                        }
                    }
                }
            }
            pdf.save(pdfOut);
        }
    }

    /**
     * 便捷：DOCX → PDF 字节。
     */
    public static byte[] wordToPdf(InputStream docxIn) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wordToPdf(docxIn, out);
        return out.toByteArray();
    }

    /**
     * 便捷：XLSX → PDF 字节。
     */
    public static byte[] excelToPdf(InputStream xlsxIn) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        excelToPdf(xlsxIn, out);
        return out.toByteArray();
    }

    /**
     * 便捷：PPTX → PDF 字节。
     */
    public static byte[] pptToPdf(InputStream pptxIn) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pptToPdf(pptxIn, out);
        return out.toByteArray();
    }

    /**
     * 简易逐行排版写入器：A4 尺寸、上下 40pt 边距、字号 1.4 倍行距；每行自动折行、
     * 每满页自动开新页。
     */
    private static final class LineWriter implements AutoCloseable {
        private final PDDocument pdf;
        private final PDType1Font font;
        private final float fontSize;
        private final float margin = 40f;
        private final float lineHeight;
        private final float maxLineWidth;
        private final float charAdvanceApprox;
        private PDPage page;
        private PDPageContentStream cs;
        private float y;

        LineWriter(PDDocument pdf, float fontSize) throws IOException {
            this.pdf = pdf;
            this.fontSize = fontSize;
            this.font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            this.lineHeight = fontSize * 1.4f;
            this.maxLineWidth = PDRectangle.A4.getWidth() - 2 * margin;
            this.charAdvanceApprox = fontSize * 0.55f;
            newPage();
        }

        void writeLine(String s) throws IOException {
            if (s == null || s.isEmpty()) {
                y -= lineHeight;
                if (y < margin) {
                    newPage();
                }
                return;
            }
            int chunk = Math.max(1, (int) (maxLineWidth / charAdvanceApprox));
            for (int i = 0; i < s.length(); i += chunk) {
                int end = Math.min(s.length(), i + chunk);
                writeRaw(s.substring(i, end));
            }
        }

        private void writeRaw(String s) throws IOException {
            if (y < margin) {
                newPage();
            }
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.newLineAtOffset(margin, y);
            cs.showText(toWinAnsi(s));
            cs.endText();
            y -= lineHeight;
        }

        private void newPage() throws IOException {
            if (cs != null) {
                cs.close();
            }
            page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);
            cs = new PDPageContentStream(pdf, page);
            y = PDRectangle.A4.getHeight() - margin;
        }

        @Override
        public void close() throws IOException {
            if (cs != null) {
                cs.close();
                cs = null;
            }
        }
    }

    /**
     * 把任意字符串安全映射到 PDFBox 内建 Helvetica 支持的 WinAnsi 范围：
     * TAB → 空格；控制字符（含 DEL）丢弃；&gt;=0x80 一律替换为 '?'（不做 CP1252 精确映射，
     * 因为下游只保证可搜索）。
     */
    static String toWinAnsi(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 0x09) {
                sb.append(' ');
            } else if (c < 0x20 || c == 0x7F) {
                // skip control
            } else if (c < 0x80) {
                sb.append(c);
            } else if (c >= 0xA0 && c <= 0xFF) {
                sb.append(c);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
