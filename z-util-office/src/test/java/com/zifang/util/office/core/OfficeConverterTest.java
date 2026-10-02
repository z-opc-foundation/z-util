package com.zifang.util.office.core;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * OfficeConverter 往返测试：内存构建 → 转 PDF → 读回文本 → 断言内容。
 */
public class OfficeConverterTest {

    @Test
    public void wordToPdf_containsParagraphText() throws Exception {
        byte[] docx = buildDocx("hello from word", null);
        byte[] pdf = OfficeConverter.wordToPdf(new ByteArrayInputStream(docx));
        assertPdfContainsText(pdf, "hello from word");
    }

    @Test
    public void wordToPdf_containsTableCells() throws Exception {
        byte[] docx = buildDocx("table demo", new String[][]{{"a1", "b1"}, {"a2", "b2"}});
        byte[] pdf = OfficeConverter.wordToPdf(new ByteArrayInputStream(docx));
        String text = extractPdfText(pdf);
        assertTrue("expected a1 in:\n" + text, text.contains("a1"));
        assertTrue("expected b2 in:\n" + text, text.contains("b2"));
    }

    @Test
    public void excelToPdf_containsCellValues() throws Exception {
        byte[] xlsx = buildXlsx(new String[][]{{"Name", "Age"}, {"Alice", "30"}, {"Bob", "25"}});
        byte[] pdf = OfficeConverter.excelToPdf(new ByteArrayInputStream(xlsx));
        String text = extractPdfText(pdf);
        assertTrue("expected Alice in:\n" + text, text.contains("Alice"));
        assertTrue("expected Bob in:\n" + text, text.contains("Bob"));
    }

    @Test
    public void pptToPdf_onePagePerSlide() throws Exception {
        byte[] pptx = buildPptx(new String[]{"slide one", "slide two", "slide three"});
        byte[] pdf = OfficeConverter.pptToPdf(new ByteArrayInputStream(pptx));
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertEquals(3, doc.getNumberOfPages());
        }
        String text = extractPdfText(pdf);
        assertTrue("expected slide one in:\n" + text, text.contains("slide one"));
        assertTrue("expected slide three in:\n" + text, text.contains("slide three"));
    }

    @Test
    public void emptyWord_stillProducesValidPdf() throws Exception {
        byte[] docx = buildDocx("", null);
        byte[] pdf = OfficeConverter.wordToPdf(new ByteArrayInputStream(docx));
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertTrue("empty word must still yield at least one page",
                    doc.getNumberOfPages() >= 1);
        }
    }

    @Test
    public void nonAscii_fallsBackToQuestionMark() throws Exception {
        // 内建 Helvetica 不吃 CJK；转 PDF 时应以 '?' 占位而不是抛异常。
        byte[] docx = buildDocx("ascii 中文 tail", null);
        byte[] pdf = OfficeConverter.wordToPdf(new ByteArrayInputStream(docx));
        String text = extractPdfText(pdf);
        assertTrue("ascii prefix must survive", text.contains("ascii"));
        assertTrue("tail must survive", text.contains("tail"));
    }

    @Test
    public void nullInput_throwsIllegalArgument() throws Exception {
        try {
            OfficeConverter.wordToPdf(null, new ByteArrayOutputStream());
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("docxIn"));
        }
    }

    @Test
    public void toWinAnsi_mapsControlAndWideChars() {
        assertEquals("ab", OfficeConverter.toWinAnsi("a\u0000b"));
        assertEquals("a b", OfficeConverter.toWinAnsi("a\tb"));
        assertEquals("?", OfficeConverter.toWinAnsi("\u4e2d"));
        assertEquals("\u00e9", OfficeConverter.toWinAnsi("\u00e9"));
        assertEquals("", OfficeConverter.toWinAnsi(null));
    }

    // ====== helpers ======

    private static String extractPdfText(byte[] pdf) throws Exception {
        File tmp = File.createTempFile("converter-test-", ".pdf");
        try {
            java.nio.file.Files.write(tmp.toPath(), pdf);
            return com.zifang.util.office.pdf.PdfExtractor.extractText(tmp);
        } finally {
            tmp.delete();
        }
    }

    private static void assertPdfContainsText(byte[] pdf, String needle) throws Exception {
        String text = extractPdfText(pdf);
        assertTrue("expected [" + needle + "] in:\n" + text, text.contains(needle));
    }

    private static byte[] buildDocx(String paragraph, String[][] table) throws Exception {
        try (XWPFDocument doc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun run = p.createRun();
            run.setText(paragraph);
            if (table != null) {
                XWPFTable tbl = doc.createTable(table.length, table[0].length);
                for (int r = 0; r < table.length; r++) {
                    XWPFTableRow row = tbl.getRow(r);
                    for (int c = 0; c < table[r].length; c++) {
                        XWPFTableCell cell = row.getCell(c);
                        if (cell == null) {
                            cell = row.createCell();
                        }
                        cell.setText(table[r][c]);
                    }
                }
            }
            doc.write(out);
            return out.toByteArray();
        }
    }

    private static byte[] buildXlsx(String[][] grid) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("s1");
            for (int r = 0; r < grid.length; r++) {
                XSSFRow row = sheet.createRow(r);
                for (int c = 0; c < grid[r].length; c++) {
                    row.createCell(c).setCellValue(grid[r][c]);
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private static byte[] buildPptx(String[] slides) throws Exception {
        try (XMLSlideShow show = new XMLSlideShow();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String s : slides) {
                XSLFSlide slide = show.createSlide();
                com.zifang.util.office.ppt.PptUtils.addTextBox(slide, s);
            }
            show.write(out);
            return out.toByteArray();
        }
    }

    @SuppressWarnings("unused")
    private static InputStream ignore() {
        return new ByteArrayInputStream(new byte[0]);
    }
}
