package com.zifang.util.office.ppt;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * PptUtils 的单元测试 + 真实往返验证。
 */
public class PptUtilsTest {

    @Test
    public void createAndCollectText_roundTrip() throws Exception {
        XMLSlideShow show = PptUtils.create();
        XSLFSlide slide = PptUtils.addBlankSlide(show);
        PptUtils.addTextBox(slide, "Hello PPT");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PptUtils.write(show, buf);

        try (XMLSlideShow back = PptUtils.read(new ByteArrayInputStream(buf.toByteArray()))) {
            assertEquals(1, PptUtils.pageCount(back));
            assertTrue(PptUtils.collectAllText(back).contains("Hello PPT"));
        }
    }

    @Test
    public void pageCount_zeroForFreshShow() {
        XMLSlideShow show = PptUtils.create();
        assertEquals(0, PptUtils.pageCount(show));
    }

    @Test
    public void render_simpleVar_replacesInTextBox() throws Exception {
        XMLSlideShow show = PptUtils.create();
        XSLFSlide slide = PptUtils.addBlankSlide(show);
        PptUtils.addTextBox(slide, "Hello ${name}!");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PptUtils.write(show, buf);

        Map<String, Object> model = new HashMap<>();
        model.put("name", "Bob");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PptTemplate.render(new ByteArrayInputStream(buf.toByteArray()), out, model);

        try (XMLSlideShow back = PptUtils.read(new ByteArrayInputStream(out.toByteArray()))) {
            String text = PptUtils.collectAllText(back);
            assertTrue("expected 'Hello Bob!', got <" + text + ">", text.contains("Hello Bob!"));
        }
    }

    @Test
    public void render_listVar_expandsTextBoxes() throws Exception {
        XMLSlideShow show = PptUtils.create();
        XSLFSlide slide = PptUtils.addBlankSlide(show);
        PptUtils.addTextBox(slide, "${xs[*]}");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PptUtils.write(show, buf);

        Map<String, Object> model = new HashMap<>();
        model.put("xs", Arrays.asList("一", "二", "三"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PptTemplate.render(new ByteArrayInputStream(buf.toByteArray()), out, model);

        try (XMLSlideShow back = PptUtils.read(new ByteArrayInputStream(out.toByteArray()))) {
            List<XSLFSlide> slides = back.getSlides();
            assertNotNull(slides);
            String all = PptUtils.collectAllText(back);
            assertTrue(all.contains("一"));
            assertTrue(all.contains("二"));
            assertTrue(all.contains("三"));
        }
    }
}
