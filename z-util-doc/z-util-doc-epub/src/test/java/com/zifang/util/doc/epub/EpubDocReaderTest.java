package com.zifang.util.doc.epub;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocSpan;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpubDocReaderTest {

    private final EpubDocReader reader = new EpubDocReader();

    private byte[] epub() throws Exception {
        Map<String, Object> entries = new LinkedHashMap<>();
        byte[] png = {(byte) 0x89, 'P', 'N', 'G'};
        entries.put("mimetype", "application/epub+zip");
        entries.put("META-INF/container.xml", """
                <?xml version="1.0"?>
                <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                  <rootfiles><rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/></rootfiles>
                </container>""");
        entries.put("OEBPS/content.opf", """
                <?xml version="1.0"?>
                <package xmlns="http://www.idpf.org/2007/opf" version="2.0" unique-identifier="id">
                  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:title>测试电子书</dc:title>
                    <dc:creator>作者甲</dc:creator>
                    <dc:publisher>出版社乙</dc:publisher>
                  </metadata>
                  <manifest>
                    <item id="ch1" href="ch1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="ch2" href="ch2.xhtml" media-type="application/xhtml+xml"/>
                    <item id="pic" href="images/pic.png" media-type="image/png"/>
                  </manifest>
                  <spine><itemref idref="ch1"/><itemref idref="ch2"/></spine>
                </package>""");
        entries.put("OEBPS/ch1.xhtml", """
                <?xml version="1.0"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head><title>第一章 开端</title></head>
                  <body>
                    <h1>第一章 开端</h1>
                    <p>普通<strong>加粗</strong><em>斜体</em><code>代码</code>。</p>
                    <ul><li>无序一</li><li>无序二</li></ul>
                    <ol><li>有序一</li></ol>
                    <blockquote>引用内容</blockquote>
                    <pre class="language-java">int x = 1;</pre>
                  </body>
                </html>""");
        entries.put("OEBPS/ch2.xhtml", """
                <?xml version="1.0"?>
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head><title>第二章 图表</title></head>
                  <body>
                    <h2>表格</h2>
                    <table>
                      <tr><th>列A</th><th>列B</th></tr>
                      <tr><td>1</td><td colspan="2">跨列</td></tr>
                    </table>
                    <img src="images/pic.png" alt="图"/>
                  </body>
                </html>""");
        entries.put("OEBPS/images/pic.png", png);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (var e : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(e.getKey()));
                if (e.getValue() instanceof byte[] b) {
                    zip.write(b);
                } else {
                    zip.write(((String) e.getValue()).getBytes(StandardCharsets.UTF_8));
                }
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    @Test
    void formatsAndSupports() {
        assertEquals(DocFormat.EPUB, reader.format());
        assertFalse(reader.supports("plain text".getBytes(StandardCharsets.UTF_8)));
        assertFalse(reader.supports(new byte[]{0, 1, 2}));
    }

    @Test
    void spineOrderHeadingsAndParagraphs() throws Exception {
        DocDocument doc = reader.read(epub());
        List<DocBlock> blocks = doc.getBlocks();
        DocBlock.Heading h1 = (DocBlock.Heading) blocks.get(0);
        assertEquals(1, h1.level());
        assertEquals("第一章 开端", h1.spans().get(0).text());
        // ch1 自带 h1 不重复插章节标题；ch2 首块已是 Heading（h2）同样不插
        long chapterTwo = blocks.stream().filter(b -> b instanceof DocBlock.Heading hh
                && hh.spans().get(0).text().equals("第二章 图表")).count();
        assertEquals(0, chapterTwo);
        assertTrue(doc.toMarkdown().contains("第一章 开端"));
        assertTrue(doc.toMarkdown().contains("表格"));
    }

    @Test
    void inlineStylesAndListsWithinChapter() throws Exception {
        DocDocument doc = reader.read(epub());
        List<DocBlock> blocks = doc.getBlocks();
        DocBlock.Paragraph para = (DocBlock.Paragraph) blocks.get(1);
        assertEquals(new DocSpan("加粗", true, false, false, false, null), para.spans().get(1));
        assertEquals(new DocSpan("斜体", false, true, false, false, null), para.spans().get(2));
        assertEquals(new DocSpan("代码", false, false, true, false, null), para.spans().get(3));
        DocBlock.ListItem un = (DocBlock.ListItem) blocks.stream()
                .filter(b -> b instanceof DocBlock.ListItem li && !li.ordered() && li.level() == 0).findFirst().orElseThrow();
        assertEquals("无序一", un.spans().get(0).text());
        DocBlock.ListItem or = (DocBlock.ListItem) blocks.stream()
                .filter(b -> b instanceof DocBlock.ListItem li && li.ordered()).findFirst().orElseThrow();
        assertEquals("有序一", or.spans().get(0).text());
    }

    @Test
    void quoteAndCodeBlock() throws Exception {
        DocDocument doc = reader.read(epub());
        DocBlock.Quote quote = (DocBlock.Quote) doc.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.Quote).findFirst().orElseThrow();
        assertEquals("引用内容", quote.spans().get(0).text());
        DocBlock.CodeBlock code = (DocBlock.CodeBlock) doc.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.CodeBlock).findFirst().orElseThrow();
        assertEquals("java", code.language());
        assertEquals("int x = 1;", code.text().trim());
        assertTrue(doc.toMarkdown().contains("```java"));
        assertTrue(doc.toMarkdown().contains("> 引用内容"));
    }

    @Test
    void tableImageAndMetadata() throws Exception {
        DocDocument doc = reader.read(epub());
        DocBlock.Table table = (DocBlock.Table) doc.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.Table).findFirst().orElseThrow();
        assertTrue(table.rows().get(0).header());
        assertEquals(2, table.rows().get(1).cells().get(1).colspan());
        DocBlock.Image image = (DocBlock.Image) doc.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.Image).findFirst().orElseThrow();
        assertEquals("pic.png", image.name());
        assertEquals("image/png", image.mimeType());
        assertArrayEquals(new byte[]{(byte) 0x89, 'P', 'N', 'G'}, image.data());
        assertEquals("测试电子书", doc.getMeta().getTitle());
        assertEquals("作者甲", doc.getMeta().getAuthor());
        assertEquals("出版社乙", doc.getMeta().getExtra().get("publisher"));
    }
}
