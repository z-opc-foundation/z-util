package com.zifang.util.office.core;

import com.zifang.util.office.core.OfficeFormat.Format;
import com.zifang.util.office.excel.ExcelUtils;
import com.zifang.util.office.pdf.PdfExtractor;
import com.zifang.util.office.word.WordExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 深度提取测试：表格 / 内嵌图片走完整"内存构建 → 写 → 读回 → 断言"往返。
 */
public class DeepExtractionTest {

    @Test
    public void word_readAllTables() throws Exception {
        byte[] docx = buildDocxWithTables(new String[][][]{
                {{"a1", "b1"}, {"a2", "b2"}},
                {{"x"}}
        });
        try (InputStream in = new ByteArrayInputStream(docx)) {
            List<List<List<String>>> tables = WordExtractor.readAllTables(in);
            assertEquals(2, tables.size());
            assertEquals(2, tables.get(0).size());
            assertEquals("a1", tables.get(0).get(0).get(0));
            assertEquals("b2", tables.get(0).get(1).get(1));
            assertEquals(1, tables.get(1).size());
            assertEquals("x", tables.get(1).get(0).get(0));
        }
        try (InputStream in = new ByteArrayInputStream(docx)) {
            assertEquals(2, WordExtractor.tableCount(in));
        }
    }

    @Test
    public void word_extractImages() throws Exception {
        byte[] png = makePng(4, 4);
        byte[] docx = buildDocxWithPicture(png);
        try (InputStream in = new ByteArrayInputStream(docx)) {
            List<byte[]> imgs = WordExtractor.extractImages(in);
            assertEquals(1, imgs.size());
            assertTrue("image bytes should match", imgs.get(0).length > 0);
        }
    }

    @Test
    public void excel_extractPictures() throws Exception {
        byte[] png = makePng(8, 8);
        byte[] xlsx = buildXlsxWithPicture(png);
        try (InputStream in = new ByteArrayInputStream(xlsx)) {
            List<ExcelUtils.PictureRef> pics = ExcelUtils.extractPictures(in);
            assertEquals(1, pics.size());
            assertNotNull(pics.get(0).getFileName());
            assertTrue(pics.get(0).getMimeType(), pics.get(0).getMimeType().contains("png"));
            assertEquals(png.length, pics.get(0).getData().length);
        }
    }

    @Test
    public void pdf_extractImages() throws Exception {
        byte[] png = makePng(16, 16);
        File f = buildPdfWithImage(png);
        try {
            List<byte[]> imgs = PdfExtractor.extractImages(f);
            assertEquals(1, imgs.size());
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(imgs.get(0)));
            assertNotNull("extracted image must decode", decoded);
            assertEquals(16, decoded.getWidth());
            assertEquals(16, decoded.getHeight());
        } finally {
            f.delete();
        }
    }

    @Test
    public void pipeline_readWordTables() throws Exception {
        byte[] docx = buildDocxWithTables(new String[][][]{
                {{"c1", "c2"}, {"c3", "c4"}}
        });
        try (OfficePipeline p = OfficePipeline.bytes(docx, Format.DOCX, "doc.docx")) {
            List<List<List<String>>> tables = p.readWordTables();
            assertEquals(1, tables.size());
            assertEquals("c1", tables.get(0).get(0).get(0));
            assertEquals("c4", tables.get(0).get(1).get(1));
        }
    }

    @Test
    public void pipeline_extractWordImages() throws Exception {
        byte[] png = makePng(4, 4);
        byte[] docx = buildDocxWithPicture(png);
        try (OfficePipeline p = OfficePipeline.bytes(docx, Format.DOCX, "doc.docx")) {
            List<byte[]> imgs = p.extractWordImages();
            assertEquals(1, imgs.size());
        }
    }

    @Test
    public void pipeline_extractExcelPictures() throws Exception {
        byte[] png = makePng(6, 6);
        byte[] xlsx = buildXlsxWithPicture(png);
        try (OfficePipeline p = OfficePipeline.bytes(xlsx, Format.XLSX, "s.xlsx")) {
            List<ExcelUtils.PictureRef> pics = p.extractExcelPictures();
            assertEquals(1, pics.size());
            assertEquals(png.length, pics.get(0).getData().length);
        }
    }

    @Test
    public void pipeline_extractPdfImages() throws Exception {
        byte[] png = makePng(10, 10);
        File f = buildPdfWithImage(png);
        try {
            try (OfficePipeline p = OfficePipeline.open(f)) {
                List<byte[]> imgs = p.extractPdfImages();
                assertEquals(1, imgs.size());
                BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(imgs.get(0)));
                assertEquals(10, decoded.getWidth());
            }
        } finally {
            f.delete();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void pipeline_readWordTables_rejectsXlsx() throws Exception {
        byte[] xlsx = buildXlsxWithPicture(makePng(4, 4));
        try (OfficePipeline p = OfficePipeline.bytes(xlsx, Format.XLSX, "s.xlsx")) {
            p.readWordTables();
        }
    }

    // ====== fixtures ======

    private static byte[] makePng(int w, int h) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                img.setRGB(x, y, 0xFF0000FF);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    private static byte[] buildDocxWithTables(String[][][] tables) throws Exception {
        try (XWPFDocument doc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String[][] grid : tables) {
                XWPFTable tbl = doc.createTable(grid.length, grid[0].length);
                for (int r = 0; r < grid.length; r++) {
                    XWPFTableRow row = tbl.getRow(r);
                    for (int c = 0; c < grid[r].length; c++) {
                        XWPFTableCell cell = row.getCell(c);
                        if (cell == null) {
                            cell = row.createCell();
                        }
                        cell.setText(grid[r][c]);
                    }
                }
            }
            doc.write(out);
            return out.toByteArray();
        }
    }

    private static byte[] buildDocxWithPicture(byte[] png) throws Exception {
        try (XWPFDocument doc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.addPictureData(png, XWPFDocument.PICTURE_TYPE_PNG);
            XWPFParagraph p = doc.createParagraph();
            XWPFRun run = p.createRun();
            run.addPicture(new ByteArrayInputStream(png), XWPFDocument.PICTURE_TYPE_PNG,
                    "img.png", Units.toEMU(20), Units.toEMU(20));
            doc.write(out);
            return out.toByteArray();
        }
    }

    private static byte[] buildXlsxWithPicture(byte[] png) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            wb.createSheet("s1");
            int idx = wb.addPicture(png, XSSFWorkbook.PICTURE_TYPE_PNG);
            // 建一个 shape 引用它，确保 POI 的 media part 不被丢弃。
            org.apache.poi.xssf.usermodel.XSSFDrawing drawing =
                    wb.getSheetAt(0).createDrawingPatriarch();
            org.apache.poi.xssf.usermodel.XSSFClientAnchor anchor =
                    new org.apache.poi.xssf.usermodel.XSSFClientAnchor();
            anchor.setCol1(1);
            anchor.setRow1(1);
            drawing.createPicture(anchor, idx);
            wb.write(out);
            return out.toByteArray();
        }
    }

    private static File buildPdfWithImage(byte[] png) throws Exception {
        File f = File.createTempFile("deep-extract-pdf-", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDImageXObject xobj = PDImageXObject.createFromByteArray(doc, png, "test");
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(xobj, 100, 700, 20, 20);
            }
            doc.save(f);
        }
        Files.readAllBytes(f.toPath());
        return f;
    }
}
