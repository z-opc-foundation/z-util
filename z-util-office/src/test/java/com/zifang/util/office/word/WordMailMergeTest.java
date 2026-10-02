package com.zifang.util.office.word;

import com.zifang.util.office.core.OfficeFormat.Format;
import com.zifang.util.office.core.OfficePipeline;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * WordMailMerge 往返：单模板 + N 数据 → N 份 DOCX；pipeline 门面同路径覆盖。
 */
public class WordMailMergeTest {

    @Test
    public void render_threeRecords_threeDocx() throws Exception {
        byte[] template = buildTemplate("Dear ${name}, welcome to ${dept}.");
        List<Map<String, Object>> records = new ArrayList<>();
        records.add(model("name", "Alice", "dept", "R&D"));
        records.add(model("name", "Bob", "dept", "Sales"));
        records.add(model("name", "Cindy", "dept", "HR"));
        List<byte[]> docs = WordMailMerge.render(template, records);
        assertEquals(3, docs.size());
        assertTextContains(docs.get(0), "Alice", "R&D");
        assertTextDoesNotContain(docs.get(0), "Bob");
        assertTextContains(docs.get(1), "Bob", "Sales");
        assertTextContains(docs.get(2), "Cindy", "HR");
    }

    @Test
    public void render_emptyRecords_returnsEmptyList() throws Exception {
        byte[] template = buildTemplate("hello ${name}");
        assertTrue(WordMailMerge.render(template, Collections.<Map<String, Object>>emptyList()).isEmpty());
        assertTrue(WordMailMerge.render(template, null).isEmpty());
    }

    @Test
    public void renderToZip_producesNDocxEntries() throws Exception {
        byte[] template = buildTemplate("hi ${name}");
        List<Map<String, Object>> records = Arrays.asList(
                model("name", "U1"),
                model("name", "U2"));
        ByteArrayOutputStream zipBuf = new ByteArrayOutputStream();
        WordMailMerge.renderToZip(template, records, zipBuf, "letter");
        File f = File.createTempFile("merge-zip-", ".zip");
        try {
            Files.write(f.toPath(), zipBuf.toByteArray());
            List<String> names = new ArrayList<>();
            try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBuf.toByteArray()))) {
                ZipEntry e;
                while ((e = zin.getNextEntry()) != null) {
                    names.add(e.getName());
                }
            }
            assertEquals(Arrays.asList("letter-0.docx", "letter-1.docx"), names);
        } finally {
            f.delete();
        }
    }

    @Test
    public void pipeline_mailMerge_returnsPipelines() throws Exception {
        byte[] template = buildTemplate("team ${name} / ${dept}");
        try (OfficePipeline p = OfficePipeline.bytes(template, Format.DOCX, "tpl.docx")) {
            List<OfficePipeline> merged = p.mailMerge(Arrays.asList(
                    model("name", "X", "dept", "Y"),
                    model("name", "Z", "dept", "W")));
            assertEquals(2, merged.size());
            assertEquals(Format.DOCX, merged.get(0).format());
            assertTrue(merged.get(0).name().startsWith("tpl-0"));
            assertTrue(merged.get(1).name().startsWith("tpl-1"));
            assertTrue(merged.get(0).extractWordText().contains("X"));
            assertTrue(merged.get(1).extractWordText().contains("W"));
        }
    }

    @Test
    public void pipeline_mailMerge_requiresDocx() throws Exception {
        File pdf = makeTinyPdf();
        try (OfficePipeline p = OfficePipeline.open(pdf)) {
            try {
                p.mailMerge(Collections.<Map<String, Object>>emptyList());
                fail("expected IllegalStateException");
            } catch (IllegalStateException expected) {
                assertTrue(expected.getMessage().contains("mailMerge"));
            }
        } finally {
            pdf.delete();
        }
    }

    @Test
    public void render_nullTemplate_throwsIllegalArgument() throws Exception {
        try {
            WordMailMerge.render((byte[]) null, Arrays.asList(model("k", "v")));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("template"));
        }
    }

    // ====== helpers ======

    private static File makeTinyPdf() throws Exception {
        File f = File.createTempFile("merge-test-pdf-", ".pdf");
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);
            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs =
                         new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(100, 700);
                cs.showText("x");
                cs.endText();
            }
            doc.save(f);
        }
        return f;
    }

    private static byte[] buildTemplate(String text) throws Exception {
        try (XWPFDocument doc = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun run = p.createRun();
            run.setText(text);
            doc.write(out);
            return out.toByteArray();
        }
    }

    private static Map<String, Object> model(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    private static void assertTextContains(byte[] docx, String... needles) throws Exception {
        String text;
        try (InputStream in = new ByteArrayInputStream(docx)) {
            text = WordExtractor.extractText(in);
        }
        for (String n : needles) {
            assertTrue("expected [" + n + "] in:\n" + text, text.contains(n));
        }
        assertNotNull(text);
    }

    private static void assertTextDoesNotContain(byte[] docx, String needle) throws Exception {
        String text;
        try (InputStream in = new ByteArrayInputStream(docx)) {
            text = WordExtractor.extractText(in);
        }
        assertTrue("did not expect [" + needle + "] in:\n" + text, !text.contains(needle));
    }
}
