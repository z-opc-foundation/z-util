package com.zifang.util.doc.doc;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocParseException;
import com.zifang.util.doc.DocSpan;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 真实 Word 97-2003 二进制语料回归。
 *
 * <p>语料来自 Apache POI 的 HWPF 测试集（Apache-2.0），解析结果与 POI 5.2.3 逐文件比对过。</p>
 */
class DocRealWorldTest {

    private final DocDocReader reader = new DocDocReader();

    private byte[] corpus(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/corpus/" + name)) {
            assertNotNull(in, "missing corpus file " + name);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    private DocDocument read(String name) throws IOException {
        byte[] file = corpus(name);
        assertTrue(reader.supports(file), name);
        return reader.read(file);
    }

    private static String text(List<DocSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            sb.append(span.text());
        }
        return sb.toString();
    }

    private static List<String> tableRows(DocDocument doc) {
        List<String> rows = new ArrayList<>();
        for (DocBlock block : doc.getBlocks()) {
            if (block instanceof DocBlock.Table table) {
                for (DocBlock.Table.Row row : table.rows()) {
                    StringBuilder sb = new StringBuilder();
                    for (DocBlock.Table.Cell cell : row.cells()) {
                        if (sb.length() > 0) {
                            sb.append(" | ");
                        }
                        for (DocBlock inner : cell.blocks()) {
                            if (inner instanceof DocBlock.Paragraph p) {
                                sb.append(text(p.spans()));
                            }
                        }
                    }
                    rows.add(sb.toString());
                }
            }
        }
        return rows;
    }

    @Test
    void simpleDocumentText() throws Exception {
        DocDocument doc = read("simple.doc");
        assertTrue(doc.toPlainText().contains("This is a simple file created with Word 97-SR2."),
                doc.toPlainText());
    }

    @Test
    void simpleTableRows() throws Exception {
        List<String> rows = tableRows(read("simple-table.doc"));
        assertEquals(2, rows.size(), rows.toString());
        assertEquals("Cell 1,1 | Cell 1,2 | Cell 1,3", rows.get(0));
        assertEquals("Cell 2,1 | Cell 2,2 | Cell 2,3", rows.get(1));
    }

    @Test
    void innerTableRows() throws Exception {
        List<String> rows = tableRows(read("innertable.doc"));
        assertEquals(3, rows.size(), rows.toString());
        assertEquals("A | B | C", rows.get(0));
        assertEquals("H | I | J", rows.get(2));
    }

    @Test
    void listItemsDetected() throws Exception {
        DocDocument doc = read("simple-list.doc");
        List<String> items = new ArrayList<>();
        for (DocBlock block : doc.getBlocks()) {
            if (block instanceof DocBlock.ListItem item) {
                items.add(text(item.spans()));
            }
        }
        assertEquals(3, items.size(), items.toString());
        assertEquals("First item in list", items.get(0));
        assertEquals("Third item in list", items.get(2));
    }

    @Test
    void listPropertiesInheritedFromStyle() throws Exception {
        // 该文件的第二段用的是带列表号的样式（ilfo 来自样式而不是段落本身）
        DocDocument doc = read("ListEntryNoListTable.doc");
        boolean hasListItem = doc.getBlocks().stream().anyMatch(b -> b instanceof DocBlock.ListItem);
        assertTrue(hasListItem, doc.toPlainText());
    }

    @Test
    void headingsDetectedByStyleSheet() throws Exception {
        DocDocument doc = read("watermark.doc");
        List<Integer> levels = new ArrayList<>();
        for (DocBlock block : doc.getBlocks()) {
            if (block instanceof DocBlock.Heading heading) {
                levels.add(heading.level());
            }
        }
        assertTrue(levels.contains(1), "expected a level 1 heading: " + levels);
        assertTrue(levels.contains(2), "expected a level 2 heading: " + levels);
    }

    @Test
    void fieldInstructionDroppedButResultKept() throws Exception {
        String text = read("hyperlink.doc").toPlainText();
        assertTrue(text.contains("Hyperlink text"), text);
        assertFalse(text.contains("HYPERLINK"), text);
    }

    @Test
    void tableOfContentsEntriesWithoutFieldCodes() throws Exception {
        String text = read("watermark.doc").toPlainText();
        assertTrue(text.contains("1. TERM"), text);
        assertFalse(text.contains("HYPERLINK"), text);
        assertFalse(text.contains(" TOC "), text);
    }

    @Test
    void pngPictureExtracted() throws Exception {
        DocDocument doc = read("PngPicture.doc");
        List<DocBlock.Image> images = new ArrayList<>();
        for (DocBlock block : doc.getBlocks()) {
            if (block instanceof DocBlock.Image image) {
                images.add(image);
            }
        }
        assertEquals(1, images.size(), "expected exactly one image");
        DocBlock.Image image = images.get(0);
        assertEquals("image/png", image.mimeType());
        assertTrue(image.data().length > 1000, "image bytes: " + image.data().length);
        assertEquals((byte) 0x89, image.data()[0]);
        assertEquals('P', image.data()[1]);
    }

    @Test
    void footnoteEndnoteAnnotationExtracted() throws Exception {
        List<String> quotes = new ArrayList<>();
        for (DocBlock block : read("footnote.doc").getBlocks()) {
            if (block instanceof DocBlock.Quote quote) {
                quotes.add(text(quote.spans()));
            }
        }
        assertTrue(quotes.contains("[脚注] TestFootnote"), quotes.toString());
        assertTrue(quotes.contains("[尾注] TestEndnote"), quotes.toString());
        assertTrue(quotes.contains("[批注] TestComment"), quotes.toString());
    }

    @Test
    void summaryInformationMetadata() throws Exception {
        DocDocument doc = read("documentProperties.doc");
        assertEquals("This is user field value", doc.getMeta().getTitle());
    }

    @Test
    void oldWordFormatRejected() throws Exception {
        byte[] file = corpus("Word6.doc");
        DocParseException error = assertThrows(DocParseException.class, () -> reader.read(file));
        assertTrue(error.getMessage().contains("Word 97-2003"), error.getMessage());
    }

    @Test
    void pageBreakKeepsBothLines() throws Exception {
        String text = read("page-break.doc").toPlainText();
        assertTrue(text.contains("Line 1"), text);
        assertTrue(text.contains("Line 2"), text);
    }
}
