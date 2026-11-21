package com.zifang.util.doc.html;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocSpan;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlDocReaderTest {

    private final HtmlDocReader reader = new HtmlDocReader();

    private byte[] corpus(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/corpus/" + name)) {
            assertNotNull(in);
            return in.readAllBytes();
        }
    }

    private static void assertNotNull(InputStream in) {
        org.junit.jupiter.api.Assertions.assertNotNull(in, "corpus 缺失");
    }

    private static String text(DocBlock block) {
        List<DocSpan> spans = new java.util.ArrayList<>();
        if (block instanceof DocBlock.Paragraph p) {
            spans.addAll(p.spans());
        } else if (block instanceof DocBlock.Heading h) {
            spans.addAll(h.spans());
        } else if (block instanceof DocBlock.Quote q) {
            spans.addAll(q.spans());
        } else if (block instanceof DocBlock.ListItem li) {
            spans.addAll(li.spans());
        }
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            sb.append(span.text());
        }
        return sb.toString();
    }

    @Test
    void detectsAndRejects() {
        assertEquals(DocFormat.HTML, reader.format());
        assertTrue(reader.supports("<!DOCTYPE html><html>".getBytes(StandardCharsets.UTF_8)));
        assertTrue(reader.supports("<HTML><BODY>x</BODY></HTML>".getBytes(StandardCharsets.UTF_8)));
        assertFalse(reader.supports("just some text".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void readsHeadingsSpansLinksAndMeta() throws IOException {
        DocDocument doc = reader.read(corpus("article.html"));
        assertEquals("z-mill 研磨手册", doc.getMeta().getTitle());
        assertEquals("整理组", doc.getMeta().getAuthor());
        assertEquals("磨坊,资料,转换", doc.getMeta().getKeywords());
        assertEquals("研磨手册第一版", doc.getMeta().getSubject());
        assertEquals("手工书写", doc.getMeta().getProducer());

        List<DocBlock> blocks = doc.getBlocks();
        DocBlock.Heading h1 = (DocBlock.Heading) blocks.get(0);
        assertEquals(1, h1.level());
        assertEquals("研磨手册", h1.spans().get(0).text());

        // 第一段：加粗 / 斜体 / 删除线共处
        DocBlock.Paragraph first = (DocBlock.Paragraph) blocks.get(1);
        assertTrue(first.spans().stream().anyMatch(s -> s.bold() && s.text().equals("z-mill")));
        assertTrue(first.spans().stream().anyMatch(s -> s.italic() && s.text().contains("各种格式")));
        assertTrue(first.spans().stream().anyMatch(s -> s.strike() && s.text().contains("旧版流程")));

        // 第二段：链接 + 行内代码
        DocBlock.Paragraph second = (DocBlock.Paragraph) blocks.get(2);
        assertTrue(second.spans().stream()
                .anyMatch(s -> "https://github.com/z-opc-foundation/z-mill".equals(s.link())
                        && s.text().contains("GitHub")));
        assertTrue(second.spans().stream().anyMatch(s -> s.mono() && s.text().contains("Mill.grind()")));

        // 引用块两段合一
        DocBlock.Quote quote = blocks.stream()
                .filter(b -> b instanceof DocBlock.Quote)
                .map(b -> (DocBlock.Quote) b)
                .findFirst().orElseThrow();
        assertTrue(text(quote).contains("先嗅探格式"));
        assertTrue(text(quote).contains("最后按需渲染"));

        DocBlock.Paragraph hr = blocks.stream()
                .filter(b -> b instanceof DocBlock.Paragraph && "---".equals(text(b)))
                .map(b -> (DocBlock.Paragraph) b)
                .findFirst().orElseThrow();
        assertEquals("---", hr.spans().get(0).text());
    }

    @Test
    void readsTablesWithHeaderAndSpans() throws IOException {
        List<DocBlock> blocks = reader.read(corpus("tables.html")).getBlocks();
        // caption 按文档顺序落成表前段落
        assertEquals(2, blocks.size());
        assertEquals("季度库存", text(blocks.get(0)));
        DocBlock.Table table = (DocBlock.Table) blocks.get(1);
        assertEquals(4, table.rows().size());

        DocBlock.Table.Row header = table.rows().get(0);
        assertTrue(header.header());
        assertEquals(3, header.cells().size());
        assertEquals("品名", text(header.cells().get(0).blocks().get(0)));

        // rowspan=2 的“苹果/新鲜”在第一数据行（3 格），第二数据行只剩数量格
        assertEquals(3, table.rows().get(1).cells().size());
        assertEquals(2, table.rows().get(1).cells().get(0).rowspan());
        assertEquals(2, table.rows().get(1).cells().get(2).rowspan());
        assertEquals(1, table.rows().get(2).cells().size());
        // colspan=2 的合计格
        assertEquals(2, table.rows().get(3).cells().get(0).colspan());
    }

    @Test
    void readsNestedLists() throws IOException {
        List<DocBlock> blocks = reader.read(corpus("lists.html")).getBlocks();
        List<DocBlock> items = blocks.stream().filter(b -> b instanceof DocBlock.ListItem).toList();
        assertEquals(7, items.size());

        DocBlock.ListItem fruit = (DocBlock.ListItem) items.get(0);
        assertFalse(fruit.ordered());
        assertEquals(0, fruit.level());
        assertTrue(text(fruit).contains("水果"));

        DocBlock.ListItem apple = (DocBlock.ListItem) items.get(1);
        assertFalse(apple.ordered());
        assertEquals(1, apple.level());
        assertEquals("苹果", text(apple));

        DocBlock.ListItem staple = (DocBlock.ListItem) items.get(3);
        assertFalse(staple.ordered());
        assertEquals(0, staple.level());
        assertEquals("主食", text(staple));

        DocBlock.ListItem step1 = (DocBlock.ListItem) items.get(4);
        assertTrue(step1.ordered());
        assertEquals(0, step1.level());
        DocBlock.ListItem step2 = (DocBlock.ListItem) items.get(5);
        assertTrue(step2.ordered());
        DocBlock.ListItem prep = (DocBlock.ListItem) items.get(6);
        assertTrue(prep.ordered());
        assertEquals(1, prep.level());
    }

    @Test
    void decodesEntities() throws IOException {
        List<DocBlock> blocks = reader.read(corpus("entities.html")).getBlocks();
        String all = blocks.stream().map(HtmlDocReaderTest::text).reduce("", String::concat);
        assertTrue(all.contains("A & B < C > D \"E\""), all);
        assertTrue(all.contains("版权 © 2026，更多…第 1½ 章，温度 25°"), all);
        assertTrue(all.contains("十六进制 你好 与十进制 你好"), all);
    }

    @Test
    void readsPreCodeWithLanguage() throws IOException {
        List<DocBlock> blocks = reader.read(corpus("pre.html")).getBlocks();
        List<DocBlock.CodeBlock> code = blocks.stream()
                .filter(b -> b instanceof DocBlock.CodeBlock)
                .map(b -> (DocBlock.CodeBlock) b)
                .toList();
        assertEquals(2, code.size());
        assertEquals("java", code.get(0).language());
        assertTrue(code.get(0).text().contains("int stones = 2;"));
        assertFalse(code.get(0).text().contains("<code>"));
        assertNull(code.get(1).language());
        assertTrue(code.get(1).text().contains("  保留  缩进"));
    }

    @Test
    void toleratesMessyLegacyHtml() throws IOException {
        // script/style/注释整段跳过；未闭合 p/li 不断链；GBK 声明按 GBK 解
        DocDocument doc = reader.read(corpus("messy.html"));
        String all = doc.getBlocks().stream().map(HtmlDocReaderTest::text)
                .reduce("", String::concat);
        assertFalse(all.contains("alert"), all);
        assertFalse(all.contains("color: red"), all);
        assertFalse(all.contains("里全是假的"), all);
        assertTrue(doc.getMeta().getTitle().contains("老页面"), doc.getMeta().getTitle());

        List<DocBlock> items = doc.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.ListItem).toList();
        assertEquals(2, items.size());
        assertTrue(text(items.get(0)).contains("粗心的一"));
        assertTrue(text(items.get(1)).contains("粗心的二"));

        assertTrue(doc.getBlocks().stream().anyMatch(b -> b instanceof DocBlock.Heading));
        assertTrue(all.contains("属性不带引号"), all);
        assertTrue(all.contains("尾巴"), all);
    }
}
