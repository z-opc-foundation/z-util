package com.zifang.util.doc.rtf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtfDocReaderTest {

    private final RtfDocReader reader = new RtfDocReader();

    private DocDocument read(String rtf) throws Exception {
        return reader.read(rtf.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String join(List<com.zifang.util.doc.DocSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (com.zifang.util.doc.DocSpan span : spans) {
            sb.append(span.text());
        }
        return sb.toString();
    }

    private static String text(DocDocument doc) {
        StringBuilder sb = new StringBuilder();
        for (DocBlock b : doc.getBlocks()) {
            if (b instanceof DocBlock.Paragraph p) {
                sb.append(join(p.spans()));
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    @Test
    void readsParagraphsWithCharacterFormatting() throws Exception {
        DocDocument doc = read("{\\rtf1\\ansi\\ansicpg1252\\deff0\n"
                + "{\\fonttbl{\\f0\\fnil Calibri;}}\n"
                + "{\\colortbl ;\\red0\\green0\\blue0;}\n"
                + "\\pard\\plain Hello \\b bold\\b0  and \\i italic\\i0  world.\\par\n"
                + "\\pard Second paragraph.\\par\n}");
        List<DocBlock> blocks = doc.getBlocks();
        assertEquals(2, blocks.size());
        List<com.zifang.util.doc.DocSpan> spans = ((DocBlock.Paragraph) blocks.get(0)).spans();
        assertEquals("Hello bold and italic world.", join(spans));
        assertTrue(spans.stream().anyMatch(s -> s.text().contains("bold") && s.bold()), spans.toString());
        assertTrue(spans.stream().anyMatch(s -> s.text().contains("italic") && s.italic()), spans.toString());
        assertEquals("Second paragraph.", join(((DocBlock.Paragraph) blocks.get(1)).spans()));
    }

    @Test
    void skipsDestinationTables() throws Exception {
        String out = text(read("{\\rtf1\\ansi{\\fonttbl{\\f0\\fnil Calibri;}}{\\colortbl ;\\red1\\green2\\blue3;}"
                + "\\pard visible text.\\par}"));
        assertTrue(out.contains("visible text."), out);
        assertFalse(out.contains("Calibri"), out);
        assertFalse(out.contains("colortbl"), out);
    }

    @Test
    void decodesUnicodeAndHexEscapes() throws Exception {
        String out = text(read("{\\rtf1\\ansi\\u20013?\\u25991? \\'e9\\'e8 caf\\'e9.\\par}"));
        assertTrue(out.contains("中文"), out);
        assertTrue(out.contains("é"), out);
        assertTrue(out.contains("café"), out);
    }

    @Test
    void keepsTabsAndRejectsNonRtf() throws Exception {
        String out = text(read("{\\rtf1\\ansi a\\tab b\\par}"));
        assertTrue(out.contains("a\tb"), out.replace("\t", "\\t"));
        assertFalse(reader.supports("plain text".getBytes(StandardCharsets.UTF_8)));
        assertEquals(DocFormat.RTF, reader.format());
    }
}
