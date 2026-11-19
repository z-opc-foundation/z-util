package com.zifang.util.doc;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocFormatDetectorTest {

    private byte[] zipWith(String... entries) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (int i = 0; i < entries.length; i += 2) {
                zip.putNextEntry(new ZipEntry(entries[i]));
                zip.write(entries[i + 1].getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    @Test
    void detectsPdfOleRtfHtml() {
        assertTrue(DocFormatDetector.isPdf("%PDF-1.7 ...".getBytes(StandardCharsets.US_ASCII)));
        byte[] ole = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1, 0, 0};
        assertTrue(DocFormatDetector.isOle2(ole));
        assertTrue(DocFormatDetector.isRtf("{\\rtf1\\ansi}".getBytes(StandardCharsets.US_ASCII)));
        assertTrue(DocFormatDetector.isHtml("<!DOCTYPE html><html>".getBytes(StandardCharsets.UTF_8)));
        assertTrue(DocFormatDetector.isHtml("<HTML><BODY>x</BODY></HTML>".getBytes(StandardCharsets.UTF_8)));
        assertFalse(DocFormatDetector.isPdf("plain text".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void distinguishesDocxAndEpub() throws Exception {
        byte[] docx = zipWith("[Content_Types].xml", "<Types/>", "word/document.xml", "<w/>");
        byte[] epub = zipWith("mimetype", "application/epub+zip", "META-INF/container.xml", "<container/>");
        assertEquals(DocFormat.DOCX, DocFormatDetector.detectZipContainer(docx));
        assertEquals(DocFormat.EPUB, DocFormatDetector.detectZipContainer(epub));
        assertNull(DocFormatDetector.detectZipContainer(zipWith("random.txt", "hi")));
    }

    @Test
    void detectViaReaders() {
        DocReaders readers = new DocReaders();
        readers.register(new StubReader(DocFormat.PDF) {
            @Override
            public boolean supports(byte[] data) {
                return DocFormatDetector.isPdf(data);
            }
        });
        assertEquals(DocFormat.PDF, readers.detect("%PDF-1.4".getBytes(StandardCharsets.US_ASCII)));
        assertNull(readers.detect("nothing".getBytes(StandardCharsets.US_ASCII)));
    }

    private static class StubReader implements DocReader {
        private final DocFormat format;

        StubReader(DocFormat format) {
            this.format = format;
        }

        @Override
        public DocFormat format() {
            return format;
        }

        @Override
        public boolean supports(byte[] data) {
            return false;
        }

        @Override
        public DocDocument read(byte[] data) {
            throw new UnsupportedOperationException();
        }
    }
}
