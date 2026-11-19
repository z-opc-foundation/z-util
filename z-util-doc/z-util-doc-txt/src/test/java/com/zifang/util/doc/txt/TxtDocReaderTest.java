package com.zifang.util.doc.txt;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TxtDocReaderTest {

    private final TxtDocReader reader = new TxtDocReader();

    @Test
    void splitsOnBlankLinesAndKeepsSoftBreaks() throws Exception {
        DocDocument doc = reader.read("第一段行一\n第一段行二\n\n第二段\n\n\n第三段".getBytes(StandardCharsets.UTF_8));
        List<DocBlock> blocks = doc.getBlocks();
        assertEquals(3, blocks.size());
        assertEquals("第一段行一\n第一段行二", ((DocBlock.Paragraph) blocks.get(0)).spans().get(0).text());
        assertEquals("第二段", ((DocBlock.Paragraph) blocks.get(1)).spans().get(0).text());
        assertTrue(doc.toMarkdown().contains("第三段"));
    }

    @Test
    void fallsBackToGbk() throws Exception {
        DocDocument doc = reader.read("中文段落A\n\n中文段落B".getBytes(Charset.forName("GBK")));
        assertEquals(2, doc.getBlocks().size());
        assertTrue(doc.toMarkdown().contains("中文段落A"));
    }

    @Test
    void rejectsBinaryAndHtml() {
        assertFalse(reader.supports("<html><body>x</body></html>".getBytes(StandardCharsets.UTF_8)));
        assertFalse(reader.supports("{\\rtf1 x}".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(reader.supports(new byte[]{0, 1, 2, 3, (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0}));
        assertFalse(reader.supports("ok\uFFFD\uFFFEx".getBytes(StandardCharsets.UTF_16LE)));
    }

    @Test
    void supportsPlainUtf8AndCrlf() throws Exception {
        byte[] data = "hello\r\nworld\r\n\r\nagain".getBytes(StandardCharsets.UTF_8);
        assertTrue(reader.supports(data));
        DocDocument doc = reader.read(data);
        assertEquals(2, doc.getBlocks().size());
        assertEquals("hello\nworld", ((DocBlock.Paragraph) doc.getBlocks().get(0)).spans().get(0).text());
    }
}
