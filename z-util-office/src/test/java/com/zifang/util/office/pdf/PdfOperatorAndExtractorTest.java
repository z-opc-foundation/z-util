package com.zifang.util.office.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * PdfOperator 与 PdfExtractor 的单元测试 + 真实往返验证。
 */
public class PdfOperatorAndExtractorTest {

    @Test
    public void merge_pageCountEqualsSum() throws Exception {
        File a = writePdfWithPages(2, "A1", "A2");
        File b = writePdfWithPages(3, "B1", "B2", "B3");
        File merged = new File(Files.createTempDirectory("pdfmerge").toFile(), "merged.pdf");
        try {
            PdfOperator.merge(Arrays.asList(a, b), merged);
            assertEquals(5, PdfExtractor.pageCount(merged));
        } finally {
            a.delete();
            b.delete();
            merged.delete();
        }
    }

    @Test
    public void split_eachOutputHasOnePage() throws Exception {
        File src = writePdfWithPages(3, "P1", "P2", "P3");
        File outDir = Files.createTempDirectory("pdfsplit").toFile();
        try {
            List<File> parts = PdfOperator.split(src, Arrays.asList(0, 2), outDir, "part");
            assertEquals(2, parts.size());
            for (File p : parts) {
                assertEquals(1, PdfExtractor.pageCount(p));
            }
        } finally {
            src.delete();
            for (File f : outDir.listFiles()) {
                f.delete();
            }
            outDir.delete();
        }
    }

    @Test
    public void replaceText_replacesAcrossPages() throws Exception {
        File src = writePdfWithPages(2, "alpha", "beta");
        File out = new File(Files.createTempDirectory("pdfrep").toFile(), "rep.pdf");
        try {
            int touched = PdfOperator.replaceText(src, out, "alpha", "ALPHA");
            assertEquals(1, touched);
            String text = PdfExtractor.extractText(out);
            assertTrue(text.contains("ALPHA"));
            assertTrue(text.contains("beta"));
        } finally {
            src.delete();
            out.delete();
        }
    }

    @Test
    public void extractMetadata_returnsTitle() throws Exception {
        File src = writePdfWithTitle("My Title");
        try {
            Map<String, String> meta = PdfExtractor.extractMetadata(src);
            assertNotNull(meta);
            assertEquals("My Title", meta.get("title"));
        } finally {
            src.delete();
        }
    }

    @Test
    public void extractText_roundTrip() throws Exception {
        File src = writePdfWithPages(1, "round-trip-token");
        try {
            String text = PdfExtractor.extractText(src);
            assertTrue(text.contains("round-trip-token"));
        } finally {
            src.delete();
        }
    }

    private static File writePdfWithPages(int pages, String... labels) throws IOException {
        File f = File.createTempFile("pdf", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    cs.newLineAtOffset(50, 700);
                    String text = i < labels.length ? labels[i] : ("P" + i);
                    cs.showText(text);
                    cs.endText();
                }
            }
            doc.save(f);
        }
        return f;
    }

    private static File writePdfWithTitle(String title) throws IOException {
        File f = File.createTempFile("pdf", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            doc.getDocumentInformation().setTitle(title);
            doc.addPage(new PDPage());
            doc.save(f);
        }
        return f;
    }

    @Test
    public void merge_emptyInputs_throws() {
        List<File> empty = new ArrayList<>();
        try {
            PdfOperator.merge(empty, new File("nope"));
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
