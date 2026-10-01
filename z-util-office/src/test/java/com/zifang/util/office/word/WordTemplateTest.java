package com.zifang.util.office.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * WordTemplate 的单元测试 + 真实往返验证。
 */
public class WordTemplateTest {

    @Test
    public void render_simpleVar_replacesInParagraph() throws Exception {
        byte[] template = docxBytes(p -> p.createRun().setText("Hello ${name}!"));
        Map<String, Object> model = new HashMap<>();
        model.put("name", "Alice");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WordTemplate.render(new ByteArrayInputStream(template), out, model);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(out.toByteArray()))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append('\n');
            }
            assertEquals("Hello Alice!\n", sb.toString());
        }
    }

    @Test
    public void render_listVar_expandsParagraphs() throws Exception {
        byte[] template = docxBytes(p -> p.createRun().setText("${names[*]}"));
        Map<String, Object> model = new HashMap<>();
        model.put("names", Arrays.asList("甲", "乙", "丙"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WordTemplate.render(new ByteArrayInputStream(template), out, model);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(out.toByteArray()))) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            assertNotNull(paragraphs);
            // 第一个段是 list[0]，后面 createParagraph 的 2 个是 list[1]、list[2]
            String joined = "";
            for (XWPFParagraph p : paragraphs) {
                joined += p.getText();
            }
            assertEquals("甲乙丙", joined);
        }
    }

    private static byte[] docxBytes(java.util.function.Consumer<XWPFParagraph> body) throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();
            body.accept(p);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            return out.toByteArray();
        }
    }
}
