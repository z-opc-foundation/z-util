package com.zifang.util.doc.pptx;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PptxDocReaderTest {

    private final PptxDocReader reader = new PptxDocReader();

    private byte[] corpus(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/corpus/" + name)) {
            assertNotNull(in, "corpus 缺失: " + name);
            return in.readAllBytes();
        }
    }

    private DocBlock.Slides slides(DocDocument doc) {
        assertEquals(1, doc.getBlocks().size());
        return (DocBlock.Slides) doc.getBlocks().get(0);
    }

    @Test
    void readsTitleAndBodyFromRealDeck() throws Exception {
        byte[] data = corpus("pptx-test-deck.pptx");
        assertTrue(reader.supports(data));
        DocBlock.Slides slides = slides(reader.read(data));
        assertEquals(1, slides.slides().size());
        DocBlock.Slides.Slide slide = slides.slides().get(0);
        assertEquals("Presentation Title Text", slide.title());
        assertTrue(slide.blocks().stream().anyMatch(b -> b instanceof DocBlock.Paragraph p
                && p.spans().get(0).text().contains("Subtitle Text")));
    }

    @Test
    void readsShapesGroupsAndTables() throws Exception {
        DocBlock.Slides slides = slides(reader.read(corpus("pptx-test-slides.pptx")));
        DocBlock.Slides.Slide slide = slides.slides().get(0);
        List<String> texts = slide.blocks().stream()
                .filter(b -> b instanceof DocBlock.Paragraph)
                .map(b -> ((DocBlock.Paragraph) b).spans().get(0).text())
                .toList();
        assertTrue(texts.contains("Test text"), texts.toString());
        assertTrue(texts.contains("Group test text"), texts.toString());
        assertTrue(slide.blocks().stream().anyMatch(b -> b instanceof DocBlock.Table), "应有表格");
        assertTrue(slide.blocks().stream().anyMatch(b -> b instanceof DocBlock.Image), "应有图片");
    }

    @Test
    void readsTablesFromEverySlide() throws Exception {
        DocBlock.Slides slides = slides(reader.read(corpus("pptx-table.pptx")));
        assertEquals(3, slides.slides().size());
        for (DocBlock.Slides.Slide slide : slides.slides()) {
            assertTrue(slide.blocks().stream().anyMatch(b -> b instanceof DocBlock.Table),
                    "第 " + slide.index() + " 页应有表格");
        }
    }

    @Test
    void extractsImagesWithBytes() throws Exception {
        DocBlock.Slides slides = slides(reader.read(corpus("pptx-picture.pptx")));
        List<DocBlock.Image> images = slides.slides().stream()
                .flatMap(s -> s.blocks().stream())
                .filter(b -> b instanceof DocBlock.Image)
                .map(b -> (DocBlock.Image) b)
                .toList();
        assertFalse(images.isEmpty());
        assertEquals("image1.png", images.get(0).name());
        assertEquals("image/png", images.get(0).mimeType());
        assertTrue(images.get(0).data().length > 0);
    }

    @Test
    void extractsSpeakerNotes() throws Exception {
        DocBlock.Slides slides = slides(reader.read(corpus("pptx-notes.pptx")));
        boolean hasNotes = slides.slides().stream()
                .flatMap(s -> s.blocks().stream())
                .filter(b -> b instanceof DocBlock.Paragraph)
                .map(b -> ((DocBlock.Paragraph) b).spans().get(0).text())
                .anyMatch(t -> t.startsWith("[备注]") && t.contains("Notes"));
        assertTrue(hasNotes, "应提取到备注页文字");
    }

    @Test
    void deckWithoutSlidesIsHandled() throws Exception {
        DocBlock.Slides slides = slides(reader.read(corpus("pptx-no-slides.pptx")));
        assertTrue(slides.slides().isEmpty());
    }

    @Test
    void placeholderTitleAndBody() throws Exception {
        DocBlock.Slides slides = slides(reader.read(corpus("pptx-placeholders.pptx")));
        List<String> titles = slides.slides().stream().map(DocBlock.Slides.Slide::title).toList();
        assertTrue(titles.contains("Title Text"), titles.toString());
        List<String> texts = slides.slides().stream()
                .flatMap(s -> s.blocks().stream())
                .filter(b -> b instanceof DocBlock.Paragraph)
                .map(b -> ((DocBlock.Paragraph) b).spans().get(0).text())
                .toList();
        assertTrue(texts.contains("Content"), texts.toString());
        assertTrue(texts.contains("Body text"), texts.toString());
    }

    @Test
    void exportsSlidesToMarkdown() throws Exception {
        String md = reader.read(corpus("pptx-test-deck.pptx")).toMarkdown();
        assertTrue(md.contains("## Presentation Title Text"), md);
        assertTrue(md.contains("Subtitle Text"), md);
    }

    @Test
    void formatIsPptx() {
        assertEquals(DocFormat.PPTX, reader.format());
    }
}
