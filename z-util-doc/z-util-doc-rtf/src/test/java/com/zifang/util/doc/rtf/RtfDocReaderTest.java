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

    private static String cellText(DocBlock.Table.Cell cell) {
        DocBlock block = cell.blocks().get(0);
        return block instanceof DocBlock.Paragraph p ? join(p.spans()) : "";
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
    void tableRowsAndCells() throws Exception {
        String rtf = "{\\rtf1\\ansi "
                + "\\trowd\\trgaph108\\cellx2000\\cellx4000 "
                + "\\intbl A1\\cell B1\\cell "
                + "\\row "
                + "\\trowd\\trgaph108\\cellx2000\\cellx4000 "
                + "\\intbl A2\\cell B2\\cell "
                + "\\row }";
        DocDocument doc = read(rtf);
        List<DocBlock.Table> tables = doc.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.Table)
                .map(b -> (DocBlock.Table) b)
                .toList();
        assertEquals(1, tables.size(), "应当解析出一张表格");
        DocBlock.Table table = tables.get(0);
        assertEquals(2, table.rows().size());
        assertEquals(2, table.rows().get(0).cells().size());
        assertEquals("A1", cellText(table.rows().get(0).cells().get(0)));
        assertEquals("B2", cellText(table.rows().get(1).cells().get(1)));
    }

    @Test
    void listLabelBecomesListItem() throws Exception {
        String rtf = "{\\rtf1\\ansi "
                + "{\\listtext\\pard\\plain\\f0 {\\*\\pn\\pnlvlblt0\\pnf3\\pnindent0{\\pntxtb\\'b7}}\\'b7\\tab}"
                + "\\pard\\plain\\li360\\fi-180\\ls1\\ilvl0 Bullet item\\par "
                + "{\\listtext\\pard\\plain\\f0 {\\*\\pn\\pnlvlblt0\\pnf3\\pnindent0{\\pntxtb\\'b7}}\\'b7\\tab}"
                + "\\pard\\plain\\li360\\fi-180\\ls1\\ilvl0 Second item\\par }";
        DocDocument doc = read(rtf);
        List<DocBlock.ListItem> items = doc.getBlocks().stream()
                .filter(b -> b instanceof DocBlock.ListItem)
                .map(b -> (DocBlock.ListItem) b)
                .toList();
        assertEquals(2, items.size(), "应当解析出两个列表项");
        assertFalse(items.get(0).ordered(), "项目符号列表");
        assertTrue(join(items.get(0).spans()).startsWith("\u00b7"), join(items.get(0).spans()));
        assertTrue(join(items.get(1).spans()).contains("Second item"));
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
