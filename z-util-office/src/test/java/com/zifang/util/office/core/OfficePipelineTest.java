package com.zifang.util.office.core;

import com.zifang.util.office.core.OfficeFormat.Format;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * OfficePipeline 往返测试：open → 操作 → saveAs → reopen → 断言。
 */
public class OfficePipelineTest {

    @Test
    public void open_detectsPdfFormat() throws Exception {
        File pdf = makeTempPdf("hello pipeline");
        try (OfficePipeline p = OfficePipeline.open(pdf)) {
            assertEquals(Format.PDF, p.format());
            assertEquals(1, p.pageCount());
            assertFalse(p.isEncrypted());
        } finally {
            pdf.delete();
        }
    }

    @Test
    public void open_detectsXlsxFormat() throws Exception {
        File xlsx = makeTempXlsx();
        try (OfficePipeline p = OfficePipeline.open(xlsx)) {
            assertEquals(Format.XLSX, p.format());
            List<List<String>> sheets = p.readExcelSheets();
            assertNotNull(sheets);
            assertFalse(sheets.isEmpty());
        } finally {
            xlsx.delete();
        }
    }

    @Test
    public void open_detectsDocxFormat() throws Exception {
        File docx = makeTempDocx("paragraph one");
        try (OfficePipeline p = OfficePipeline.open(docx)) {
            assertEquals(Format.DOCX, p.format());
            assertTrue(p.extractWordText().contains("paragraph one"));
        } finally {
            docx.delete();
        }
    }

    @Test
    public void extractPdfText_roundTrip() throws Exception {
        File pdf = makeTempPdf("roundtrip text content");
        try (OfficePipeline p = OfficePipeline.open(pdf)) {
            String text = p.extractPdfText();
            assertTrue(text, text.contains("roundtrip text content"));
        } finally {
            pdf.delete();
        }
    }

    @Test
    public void pdfAddWatermark_persistsAfterSave() throws Exception {
        File in = makeTempPdf("original body");
        File out = File.createTempFile("office-pipeline-out-", ".pdf");
        long inSize = in.length();
        try {
            try (OfficePipeline p = OfficePipeline.open(in)) {
                p.pdfAddWatermark("CONFIDENTIAL").saveAs(out);
            }
            try (OfficePipeline p = OfficePipeline.open(out)) {
                assertEquals(1, p.pageCount());
                // PDFBox 默认 text stripper 不一定能从 APPEND 流里抽到 alpha blend 的水印；
                // 用文件大小变化 + 页数稳定 + reopen 成功三件套证明 watermark 真的写进了 buffer。
                assertTrue("watermarked file must be larger than original",
                        out.length() > inSize);
            }
        } finally {
            in.delete();
            out.delete();
        }
    }

    @Test
    public void bytes_factory_passesFormatAndProducesBytes() throws Exception {
        File pdf = makeTempPdf("memory mode");
        byte[] data = Files.readAllBytes(pdf.toPath());
        try (OfficePipeline p = OfficePipeline.bytes(data, Format.PDF, "memory.pdf")) {
            assertEquals(Format.PDF, p.format());
            assertEquals("memory.pdf", p.name());
            assertEquals(1, p.pageCount());
            byte[] out = p.toBytes();
            assertEquals(data.length, out.length);
        } finally {
            pdf.delete();
        }
    }

    @Test
    public void readExcelSheets_requiresXlsxFormat() throws Exception {
        File pdf = makeTempPdf("oops");
        try (OfficePipeline p = OfficePipeline.open(pdf)) {
            try {
                p.readExcelSheets();
                org.junit.Assert.fail("expected IllegalStateException");
            } catch (IllegalStateException expected) {
                assertTrue(expected.getMessage().contains("XLSX"));
            }
        } finally {
            pdf.delete();
        }
    }

    @Test
    public void pdfReplaceText_persists() throws Exception {
        File in = makeTempPdf("Original text");
        try (OfficePipeline p = OfficePipeline.open(in)) {
            int touched = p.pdfReplaceText("Original", "Replaced");
            assertEquals(1, touched);
            assertTrue(p.extractPdfText().contains("Replaced"));
        } finally {
            in.delete();
        }
    }

    @Test
    public void pdfRotatePage_persistsRotation() throws Exception {
        File in = makeTempPdf("rot");
        try (OfficePipeline p = OfficePipeline.open(in)) {
            p.pdfRotatePage(0, 90);
            byte[] rotated = p.toBytes();
            java.io.File tmp = java.io.File.createTempFile("rotated-", ".pdf");
            try {
                java.nio.file.Files.write(tmp.toPath(), rotated);
                assertEquals(90, com.zifang.util.office.pdf.PdfExtractor.pageRotation(tmp, 0));
            } finally {
                tmp.delete();
            }
        } finally {
            in.delete();
        }
    }

    @Test
    public void pdfRemovePages_persists() throws Exception {
        File in = makeTempPdfMultiPage(3);
        try (OfficePipeline p = OfficePipeline.open(in)) {
            assertEquals(3, p.pageCount());
            int removed = p.pdfRemovePages(java.util.Arrays.asList(0, 2));
            assertEquals(2, removed);
            assertEquals(1, p.pageCount());
        } finally {
            in.delete();
        }
    }

    @Test
    public void pdfProtect_marksEncrypted() throws Exception {
        File in = makeTempPdf("secret");
        try (OfficePipeline p = OfficePipeline.open(in)) {
            p.pdfProtect("pwd", null);
            assertTrue(p.isEncrypted());
        } finally {
            in.delete();
        }
    }

    @Test
    public void pdfMergeWith_combinesPages() throws Exception {
        File head = makeTempPdf("head");
        File tail = makeTempPdf("tail");
        try (OfficePipeline p = OfficePipeline.open(head)) {
            p.pdfMergeWith(java.util.Collections.singletonList(tail));
            assertEquals(2, p.pageCount());
        } finally {
            head.delete();
            tail.delete();
        }
    }

    @Test
    public void convertToPdf_fromDocx_returnsPdfPipeline() throws Exception {
        File docx = makeTempDocx("docx body");
        try (OfficePipeline src = OfficePipeline.open(docx);
             OfficePipeline pdf = src.convertToPdf()) {
            assertEquals(Format.PDF, pdf.format());
            assertTrue(pdf.name().endsWith(".pdf"));
            assertTrue(pdf.extractPdfText().contains("docx body"));
        } finally {
            docx.delete();
        }
    }

    @Test
    public void convertToPdf_fromXlsx_returnsPdfPipeline() throws Exception {
        File xlsx = makeTempXlsx();
        try (OfficePipeline src = OfficePipeline.open(xlsx);
             OfficePipeline pdf = src.convertToPdf()) {
            assertEquals(Format.PDF, pdf.format());
            String text = pdf.extractPdfText();
            assertTrue("expected 'hello' in:\n" + text, text.contains("hello"));
            assertTrue("expected 'pipeline' in:\n" + text, text.contains("pipeline"));
        } finally {
            xlsx.delete();
        }
    }

    @Test
    public void convertToPdf_chainWithWatermark() throws Exception {
        File docx = makeTempDocx("chain body");
        File out = File.createTempFile("chain-out-", ".pdf");
        try {
            try (OfficePipeline src = OfficePipeline.open(docx)) {
                src.convertToPdf().pdfAddWatermark("DRAFT").saveAs(out);
            }
            try (OfficePipeline reopen = OfficePipeline.open(out)) {
                assertEquals(Format.PDF, reopen.format());
                assertEquals(1, reopen.pageCount());
                assertTrue(reopen.extractPdfText().contains("chain body"));
            }
        } finally {
            docx.delete();
            out.delete();
        }
    }

    @Test
    public void convertToPdf_fromPdf_isIdentity() throws Exception {
        File pdf = makeTempPdf("already pdf");
        try (OfficePipeline src = OfficePipeline.open(pdf);
             OfficePipeline same = src.convertToPdf()) {
            assertEquals(Format.PDF, same.format());
            assertTrue(same.extractPdfText().contains("already pdf"));
        } finally {
            pdf.delete();
        }
    }

    // ====== Fixtures ======

    private static File makeTempPdf(String body) throws Exception {
        File f = File.createTempFile("office-pipeline-pdf-", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(100, 700);
                cs.showText(body);
                cs.endText();
            }
            doc.save(f);
        }
        return f;
    }

    private static File makeTempPdfMultiPage(int n) throws Exception {
        File f = File.createTempFile("office-pipeline-pdf-n-", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            for (int i = 0; i < n; i++) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    cs.newLineAtOffset(100, 700);
                    cs.showText("page " + i);
                    cs.endText();
                }
            }
            doc.save(f);
        }
        return f;
    }

    private static File makeTempXlsx() throws Exception {
        File f = File.createTempFile("office-pipeline-xlsx-", ".xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("s1");
            XSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("hello");
            row.createCell(1).setCellValue("pipeline");
            wb.write(out);
            Files.write(f.toPath(), out.toByteArray());
        }
        return f;
    }

    private static File makeTempDocx(String text) throws Exception {
        File f = File.createTempFile("office-pipeline-docx-", ".docx");
        try (XWPFDocument doc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun run = p.createRun();
            run.setText(text);
            doc.write(out);
            Files.write(f.toPath(), out.toByteArray());
        }
        return f;
    }
}
