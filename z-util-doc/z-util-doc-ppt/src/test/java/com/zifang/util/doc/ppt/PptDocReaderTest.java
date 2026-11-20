package com.zifang.util.doc.ppt;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocSpan;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PptDocReaderTest {

    private final PptDocReader reader = new PptDocReader();

    private byte[] corpus(String name) throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/corpus/" + name)) {
            assertNotNull(in, name);
            return in.readAllBytes();
        }
    }

    private DocBlock.Slides slides(byte[] data) throws Exception {
        DocDocument doc = reader.read(data);
        return (DocBlock.Slides) doc.getBlocks().get(0);
    }

    private static List<String> texts(DocBlock.Slides.Slide slide) {
        List<String> result = new ArrayList<>();
        for (DocBlock block : slide.blocks()) {
            if (block instanceof DocBlock.Paragraph paragraph) {
                StringBuilder sb = new StringBuilder();
                for (DocSpan span : paragraph.spans()) {
                    sb.append(span.text());
                }
                result.add(sb.toString());
            }
        }
        return result;
    }

    @Test
    void embeddedPictureExtracted() throws Exception {
        DocDocument doc = reader.read(corpus("picture.png.ppt"));
        List<DocBlock.Image> images = new ArrayList<>();
        for (DocBlock block : doc.getBlocks()) {
            if (block instanceof DocBlock.Image image) {
                images.add(image);
            }
        }
        assertEquals(1, images.size(), "应当抽到一张内嵌图片");
        DocBlock.Image image = images.get(0);
        assertEquals("image/png", image.mimeType());
        assertEquals(2313, image.data().length);
        assertEquals((byte) 0x89, image.data()[0]);
        assertEquals('P', image.data()[1]);
    }

    @Test
    void deckWithoutPictureHasNoImageBlock() throws Exception {
        DocDocument doc = reader.read(corpus("simple.ppt"));
        for (DocBlock block : doc.getBlocks()) {
            assertFalse(block instanceof DocBlock.Image, "simple.ppt 里没有图片");
        }
    }

    @Test
    void formatIsPpt() {
        assertEquals(DocFormat.PPT, reader.format());
    }

    @Test
    void detectsPptByOleStreamName() throws Exception {
        assertTrue(reader.supports(corpus("simple.ppt")));
        assertTrue(reader.supports(corpus("38256.ppt")));
        assertTrue(reader.supports(corpus("empty.ppt")));
        assertFalse(reader.supports("not a ppt".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void readsSlidesAndText() throws Exception {
        DocBlock.Slides slides = slides(corpus("simple.ppt"));
        assertEquals(2, slides.slides().size());
        DocBlock.Slides.Slide first = slides.slides().get(0);
        assertEquals(1, first.index());
        assertNull(first.title());
        assertEquals(List.of("第一张标题", "正文一", "正文二"), texts(first));
        assertEquals(List.of("第二页内容"), texts(slides.slides().get(1)));
    }

    @Test
    void readsTitlePlaceholder() throws Exception {
        DocBlock.Slides slides = slides(corpus("placeholder.ppt"));
        assertEquals(2, slides.slides().size());
        assertEquals("占位符标题", slides.slides().get(0).title());
        assertEquals("占位符正文", texts(slides.slides().get(0)).get(0));
        assertEquals("第二页标题", slides.slides().get(1).title());
    }

    @Test
    void keepsSlideOrderFromPersistTable() throws Exception {
        // 语料是「插入顺序与播放顺序故意不一致」的文件
        DocBlock.Slides slides = slides(corpus("incorrect_slide_order.ppt"));
        assertEquals(3, slides.slides().size());
        assertEquals("Slide 1", slides.slides().get(0).title());
        assertEquals("First slide I added", texts(slides.slides().get(0)).get(0));
        assertEquals("Slide 2", slides.slides().get(1).title());
        assertEquals("Third slide I added", texts(slides.slides().get(1)).get(0));
        assertEquals("Slide 3", slides.slides().get(2).title());
        assertEquals("Second slide I added", texts(slides.slides().get(2)).get(0));
    }

    @Test
    void splitsParagraphsOnCarriageReturn() throws Exception {
        DocBlock.Slides slides = slides(corpus("multiline.ppt"));
        assertEquals(List.of("第一章 总览", "第二章 细节", "第三章 结论"), texts(slides.slides().get(0)));
    }

    @Test
    void readsUnicodeText() throws Exception {
        DocBlock.Slides slides = slides(corpus("54880_chinese.ppt"));
        List<String> texts = texts(slides.slides().get(0));
        assertTrue(texts.contains("複数の文字"), texts.toString());
        assertTrue(texts.contains("表Mixパﾋﾟ𠮟"), texts.toString());
    }

    @Test
    void readsRealWorldFiles() throws Exception {
        DocBlock.Slides slides = slides(corpus("38256.ppt"));
        assertEquals(1, slides.slides().size());
        List<String> texts = texts(slides.slides().get(0));
        assertTrue(texts.contains("“HAPPY BIRTHDAY SCOTT”"), texts.toString());

        DocBlock.Slides masters = slides(corpus("slide_master.ppt"));
        assertEquals(3, masters.slides().size());
        assertEquals("Slide Masters", masters.slides().get(0).title());

        DocBlock.Slides bullets = slides(corpus("bullets.ppt"));
        assertEquals(2, bullets.slides().size());
        assertEquals("Title text", bullets.slides().get(0).title());
    }

    @Test
    void readsNotesAsSeparateParagraphs() throws Exception {
        DocBlock.Slides slides = slides(corpus("basic_test_ppt_file.ppt"));
        assertEquals(2, slides.slides().size());
        List<String> texts = texts(slides.slides().get(0));
        assertTrue(texts.stream().anyMatch(t -> t.startsWith("[备注]")), texts.toString());
    }

    @Test
    void emptyPresentationHasNoSlides() throws Exception {
        DocBlock.Slides slides = slides(corpus("empty.ppt"));
        assertTrue(slides.slides().isEmpty());
    }

    @Test
    void isDiscoverableThroughServiceLoader() {
        boolean found = java.util.ServiceLoader.load(com.zifang.util.doc.DocReader.class).stream()
                .anyMatch(provider -> provider.type() == PptDocReader.class);
        assertTrue(found, "META-INF/services 里没登记 PptDocReader");
    }
}
