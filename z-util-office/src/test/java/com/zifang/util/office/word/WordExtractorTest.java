package com.zifang.util.office.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * WordExtractor 的往返测试：内存构建 docx → 提取 → 断言与写入一致。
 */
public class WordExtractorTest {

    @Test
    public void extractParagraphs_roundTrip() throws Exception {
        byte[] docx = docxWith("第一段", "第二段");
        List<String> paragraphs = WordExtractor.extractParagraphs(new ByteArrayInputStream(docx));
        assertEquals(2, paragraphs.size());
        assertEquals("第一段", paragraphs.get(0));
        assertEquals("第二段", paragraphs.get(1));
    }

    @Test
    public void extractText_includesTable() throws Exception {
        byte[] docx = docxWithTable();
        String text = WordExtractor.extractText(new ByteArrayInputStream(docx));
        assertTrue(text.contains("表头A"));
        assertTrue(text.contains("表头B"));
        assertTrue(text.contains("r1c1"));
        assertTrue(text.contains("r1c2"));
    }

    @Test
    public void readTable_returnsTwoByTwo() throws Exception {
        byte[] docx = docxWithTable();
        List<List<String>> table = WordExtractor.readTable(new ByteArrayInputStream(docx), 0);
        assertEquals(2, table.size());
        assertEquals(2, table.get(0).size());
        assertEquals("表头A", table.get(0).get(0));
        assertEquals("r1c2", table.get(1).get(1));
    }

    @Test
    public void extractMetadata_roundTrip() throws Exception {
        byte[] docx = docxWithMeta();
        Map<String, String> meta = WordExtractor.extractMetadata(new ByteArrayInputStream(docx));
        assertEquals("测试标题", meta.get("title"));
        assertEquals("作者甲", meta.get("creator"));
    }

    private static byte[] docxWith(String... paragraphTexts) throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            for (String t : paragraphTexts) {
                XWPFParagraph p = doc.createParagraph();
                p.createRun().setText(t);
            }
            return toBytes(doc);
        }
    }

    private static byte[] docxWithTable() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(2, 2);
            table.getRow(0).getCell(0).setText("表头A");
            table.getRow(0).getCell(1).setText("表头B");
            table.getRow(1).getCell(0).setText("r1c1");
            table.getRow(1).getCell(1).setText("r1c2");
            return toBytes(doc);
        }
    }

    private static byte[] docxWithMeta() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText("meta-body");
            doc.getProperties().getCoreProperties().setTitle("测试标题");
            doc.getProperties().getCoreProperties().setCreator("作者甲");
            return toBytes(doc);
        }
    }

    private static byte[] toBytes(XWPFDocument doc) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.write(out);
        return out.toByteArray();
    }
}
