package com.zifang.util.office.pdf;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * PDF 页级操作与加密的往返测试：
 * 旋转后 rotation 值相等、删页后页数相等、渲染图尺寸 &gt; 0、加密后带密码可开。
 */
public class PdfPageOpsTest {

    @Test
    public void rotatePage_roundTrip() throws Exception {
        File src = writePdf(1);
        File out = temp("rot.pdf");
        try {
            PdfOperator.rotatePage(src, out, 0, 90);
            assertEquals(90, PdfExtractor.pageRotation(out, 0));
        } finally {
            cleanup(src, out);
        }
    }

    @Test
    public void removePages_countRoundTrip() throws Exception {
        File src = writePdf(4);
        File out = temp("rem.pdf");
        try {
            int removed = PdfOperator.removePages(src, out, Arrays.asList(0, 2));
            assertEquals(2, removed);
            assertEquals(2, PdfExtractor.pageCount(out));
        } finally {
            cleanup(src, out);
        }
    }

    @Test
    public void renderPageAsImage_sizePositive() throws Exception {
        File src = writePdf(1);
        File png = temp("page.png");
        try {
            int[] size = PdfExtractor.renderPageAsImage(src, 0, png);
            assertTrue("width should be > 0", size[0] > 0);
            assertTrue("height should be > 0", size[1] > 0);
            assertTrue("png should exist", png.isFile());
            assertTrue("png should not be empty", png.length() > 0);
        } finally {
            cleanup(src, png);
        }
    }

    @Test
    public void protect_thenOpenWithPassword() throws Exception {
        File src = writePdf(1);
        File enc = temp("enc.pdf");
        try {
            PdfOperator.protect(src, enc, "secret", null);
            assertTrue(PdfExtractor.isEncrypted(enc));
            try (org.apache.pdfbox.pdmodel.PDDocument doc =
                         org.apache.pdfbox.Loader.loadPDF(enc, "secret")) {
                assertEquals(1, doc.getNumberOfPages());
            }
        } finally {
            cleanup(src, enc);
        }
    }

    private static File writePdf(int pages) throws Exception {
        File f = File.createTempFile("pdfops", ".pdf");
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            for (int i = 0; i < pages; i++) {
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
                doc.addPage(page);
                try (org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                             new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                            org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
                    cs.newLineAtOffset(50, 700);
                    cs.showText("page-" + i);
                    cs.endText();
                }
            }
            doc.save(f);
        }
        return f;
    }

    private static File temp(String name) throws Exception {
        return new File(Files.createTempDirectory("pdfops").toFile(), name);
    }

    private static void cleanup(File... files) {
        for (File f : files) {
            if (f != null && f.exists()) {
                f.delete();
                File parent = f.getParentFile();
                if (parent != null && parent.getName().startsWith("pdfops")) {
                    parent.delete();
                }
            }
        }
    }
}
