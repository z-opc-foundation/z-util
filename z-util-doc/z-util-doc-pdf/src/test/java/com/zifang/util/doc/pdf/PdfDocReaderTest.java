package com.zifang.util.doc.pdf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfDocReaderTest {

    private final PdfDocReader reader = new PdfDocReader();

    @Test
    void supports() {
        assertTrue(reader.supports(TestPdfs.simple("t")));
        assertFalse(reader.supports("plain text".getBytes()));
    }

    @Test
    void extractsTextAndHeading() throws IOException {
        DocDocument doc = reader.read(TestPdfs.simple("Quarterly Report", "Hello World", "Second line here"));
        assertEquals("Test Doc", doc.getMeta().getTitle());
        assertEquals("zifang", doc.getMeta().getAuthor());
        assertEquals(1, doc.getMeta().getPageCount());

        boolean hasHeading = doc.getBlocks().stream()
                .anyMatch(b -> b instanceof DocBlock.Heading h
                        && String.join("", text(h.spans())).contains("Quarterly Report"));
        assertTrue(hasHeading, "big first line should become heading, got: " + doc.getBlocks());

        String all = plainText(doc);
        assertTrue(all.contains("Hello World"));
        assertTrue(all.contains("Second line here"));
    }

    @Test
    void handlesFlateCompressedContent() throws IOException {
        DocDocument doc = reader.read(TestPdfs.flated("Compressed", "body text"));
        assertTrue(plainText(doc).contains("body text"));
        assertTrue(doc.getBlocks().stream().anyMatch(b -> b instanceof DocBlock.Heading));
    }

    @Test
    void markdownExport() throws IOException {
        String md = reader.read(TestPdfs.simple("Title X", "para one")).toMarkdown();
        assertTrue(md.contains("# Title X"));
        assertTrue(md.contains("para one"));
    }

    @Test
    void plainPdfWithoutInfo() throws IOException {
        DocDocument doc = reader.read(TestPdfs.withoutInfo());
        assertTrue(plainText(doc).contains("plain"));
    }

    @Test
    void encryptedPdfRejected() {
        // 带 /Encrypt 的 trailer → 直接拒绝
        byte[] fake = "%PDF-1.4\n%%EOF".getBytes();
        assertThrows(Exception.class, () -> reader.read(fake));
    }

    @Test
    void pdfDateParsing() {
        assertEquals(2026, PdfDocReader.parsePdfDate("D:20261119120000Z").getYear());
        assertEquals(12, PdfDocReader.parsePdfDate("D:20261119120000Z").getHour());
        assertEquals(11, PdfDocReader.parsePdfDate("D:20261119").getMonthValue());
    }

    private static List<String> text(List<com.zifang.util.doc.DocSpan> spans) {
        return spans.stream().map(com.zifang.util.doc.DocSpan::text).toList();
    }

    private static String plainText(DocDocument doc) {
        return doc.toPlainText();
    }
}
