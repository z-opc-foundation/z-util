package com.zifang.util.doc.word;

import com.zifang.util.doc.DocDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordDocReaderTest {

    private final WordDocReader reader = new WordDocReader();

    /** WordDocument 流：FIB + 正文（压缩 cp1252 piece）。 */
    private byte[] wordDocument(String[] paragraphs, boolean use1Table, int lcbClx) {
        byte[] buf = new byte[8192];
        u16(buf, 0, 0xA5EC);
        u16(buf, 2, 0x00C1);
        u16(buf, 0x0A, use1Table ? 0x0200 : 0);
        int total = 0;
        for (String p : paragraphs) {
            total += p.length() + 1; // 段尾 \r
        }
        u32(buf, 76, total);
        // 正文压缩 cp1252 放在 1024，fc = 0x40000000 | 2048
        int p = 1024;
        for (String s : paragraphs) {
            byte[] b = (s + "\r").getBytes(StandardCharsets.ISO_8859_1);
            System.arraycopy(b, 0, buf, p, b.length);
            p += b.length;
        }
        u32(buf, 418, 0); // fcClx 在 table 流开头
        u32(buf, 422, lcbClx);
        return buf;
    }

    /** 1Table/0Table 里的 CLX：单个 Pcdt + 压缩 piece。 */
    private byte[] tableWithClx(String[] paragraphs) {
        int total = 0;
        for (String s : paragraphs) {
            total += s.length() + 1;
        }
        ByteArrayOutputStream clx = new ByteArrayOutputStream();
        clx.write(2); // clxt = Pcdt
        int plcLen = 4 + (1 + 1) * 4 + 8; // cps(n+1) + 1 pcd
        u32le(clx, plcLen);
        u32le(clx, 0);
        u32le(clx, total);
        u16le(clx, 0); // flags
        u32le(clx, 0x40000000 | (1024 * 2)); // fc：压缩，位于 WordDocument 偏移 1024
        u16le(clx, 0); // prm
        return clx.toByteArray();
    }

    private byte[] summaryInfo() {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        u32le(body, 4); // cProps
        // pairs：data 从 section 偏移 40 开始（8 + 4*8）
        u32le(body, 1);
        u32le(body, 40);
        u32le(body, 2);
        u32le(body, 44);
        u32le(body, 4);
        u32le(body, 60);
        u32le(body, 12);
        u32le(body, 72);
        u16le(body, 2); // VT_I2 codepage
        u16le(body, 1252);
        u16le(body, 30); // VT_LPSTR title：type(2)+count(4)+data
        u32le(body, 10);
        body.writeBytes("Test Title".getBytes(StandardCharsets.ISO_8859_1));
        u16le(body, 30); // VT_LPSTR author
        u32le(body, 6);
        body.writeBytes("zifang".getBytes(StandardCharsets.ISO_8859_1));
        u16le(body, 64); // VT_FILETIME：type(2)+8 字节
        long ft = (1731986400L + 11644473600L) * 10_000_000L;
        u32le(body, (int) ft);
        u32le(body, (int) (ft >> 32));
        byte[] bodyBytes = body.toByteArray();

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        u16le(s, 0xFFFE);
        u16le(s, 0);
        u32le(s, 0);
        for (int i = 0; i < 4; i++) {
            u32le(s, 0); // clsid
        }
        u32le(s, 1); // cSections
        for (int i = 0; i < 4; i++) {
            u32le(s, i + 1); // fmtid 随意
        }
        u32le(s, 48); // section offset
        u32le(s, 4 + bodyBytes.length); // cbSection
        s.writeBytes(bodyBytes);
        return s.toByteArray();
    }

    @Test
    void extractsCompressedPieceText() throws Exception {
        String[] paras = {"Hello Binary", "World 2003"};
        byte[] table = tableWithClx(paras);
        CfbTestBuilder cfb = new CfbTestBuilder()
                .add("WordDocument", wordDocument(paras, true, table.length))
                .add("1Table", table);
        byte[] file = cfb.build();
        assertTrue(reader.supports(file));
        DocDocument doc = reader.read(file);
        assertEquals(2, doc.getBlocks().size());
        assertTrue(doc.toPlainText().contains("Hello Binary"));
        assertTrue(doc.toPlainText().contains("World 2003"));
    }

    @Test
    void uses0TableWhenFlagClear() throws Exception {
        String[] paras = {"Legacy table"};
        byte[] table = tableWithClx(paras);
        CfbTestBuilder cfb = new CfbTestBuilder()
                .add("WordDocument", wordDocument(paras, false, table.length))
                .add("0Table", table);
        DocDocument doc = reader.read(cfb.build());
        assertTrue(doc.toPlainText().contains("Legacy table"));
    }

    @Test
    void utf16Piece() throws Exception {
        String text = "中文文档";
        ByteArrayOutputStream clx = new ByteArrayOutputStream();
        clx.write(2);
        u32le(clx, 4 + 8 + 8);
        u32le(clx, 0);
        u32le(clx, text.length());
        u16le(clx, 0);
        u32le(clx, 2048); // 未压缩 UTF-16
        u16le(clx, 0);

        byte[] buf = new byte[8192];
        u16(buf, 0, 0xA5EC);
        u16(buf, 2, 0x00C1);
        u16(buf, 0x0A, 0x0200);
        u32(buf, 76, text.length());
        byte[] utf16 = text.getBytes(StandardCharsets.UTF_16LE);
        System.arraycopy(utf16, 0, buf, 2048, utf16.length);
        u32(buf, 418, 0);
        u32(buf, 422, clx.size());

        CfbTestBuilder cfb = new CfbTestBuilder()
                .add("WordDocument", buf)
                .add("1Table", clx.toByteArray());
        DocDocument doc = reader.read(cfb.build());
        assertTrue(doc.toPlainText().contains("中文文档"));
    }

    @Test
    void readsSummaryInfoMetadata() throws Exception {
        String[] paras = {"Body text"};
        CfbTestBuilder cfb = new CfbTestBuilder()
                .add("WordDocument", wordDocument(paras, true, tableWithClx(paras).length))
                .add("1Table", tableWithClx(paras))
                .add("\u0005SummaryInformation", summaryInfo());
        DocDocument doc = reader.read(cfb.build());
        assertEquals("Test Title", doc.getMeta().getTitle());
        assertEquals("zifang", doc.getMeta().getAuthor());
        assertTrue(doc.getMeta().getCreationDate() != null);
        assertFalse(doc.getMeta().getCreationDate().getYear() < 2000);
    }

    @Test
    void rejectsNonWordOle() {
        CfbTestBuilder cfb = new CfbTestBuilder().add("Workbook", new byte[8192]);
        byte[] file = cfb.build();
        assertFalse(reader.supports(file));
    }

    @Test
    void chpxBoldItalicSpans() throws Exception {
        // 正文 "Hello World"（FC 4096 起，压缩 piece），CP 6..11 加粗+斜体。
        String text = "Hello World";
        byte[] buf = new byte[8192];
        u16(buf, 0, 0xA5EC);
        u16(buf, 2, 0x00C1);
        u16(buf, 0x0A, 0x0200);
        u32(buf, 76, text.length());
        System.arraycopy(text.getBytes(StandardCharsets.ISO_8859_1), 0, buf, 4096, text.length());
        u32(buf, 418, 0);
        // CLX：一个压缩 piece 覆盖全文
        ByteArrayOutputStream clx = new ByteArrayOutputStream();
        clx.write(2);
        u32le(clx, 4 + 8 + 8);
        u32le(clx, 0);
        u32le(clx, text.length());
        u16le(clx, 0);
        u32le(clx, 0x40000000 | (4096 * 2));
        u16le(clx, 0);
        u32(buf, 422, clx.size());

        // CHPX FKP：页 pn=1（WordDocument 偏移 512），crun=2，rgb 紧跟 rgfc。
        byte[] fkp = new byte[512];
        u32(fkp, 0, 4096);
        u32(fkp, 4, 4096 + 6);
        u32(fkp, 8, 4096 + text.length());
        fkp[511] = 2; // crun
        fkp[12] = 0;  // run0 无 chpx
        fkp[13] = (byte) 250; // run1 chpx 在页内偏移 500（250*2）
        fkp[500] = 6;  // cb：两个 sprm × 3 字节
        fkp[501] = 0x35; fkp[502] = (byte) 0x08; fkp[503] = 0x01; // sprmCFBold = 1
        fkp[504] = 0x36; fkp[505] = (byte) 0x08; fkp[506] = 0x01; // sprmCFItalic = 1
        System.arraycopy(fkp, 0, buf, 512, 512);
        // plcfbteChpx：FC 数组 [4096, 4096+len] + PM(pn=1)
        ByteArrayOutputStream table = new ByteArrayOutputStream();
        table.writeBytes(clx.toByteArray());
        int bteOff = table.size();
        u32le(table, 4096);
        u32le(table, 4096 + text.length());
        u32le(table, 1); // pn=1
        buf[250] = (byte) bteOff;
        buf[251] = (byte) (bteOff >> 8);
        u32(buf, 254, 12);

        CfbTestBuilder cfb = new CfbTestBuilder()
                .add("WordDocument", buf)
                .add("1Table", table.toByteArray());
        DocDocument doc = reader.read(cfb.build());
        assertEquals(1, doc.getBlocks().size());
        var spans = ((com.zifang.util.doc.DocBlock.Paragraph) doc.getBlocks().get(0)).spans();
        assertEquals(2, spans.size());
        assertEquals("Hello ", spans.get(0).text());
        assertFalse(spans.get(0).bold());
        assertEquals("World", spans.get(1).text());
        assertTrue(spans.get(1).bold());
        assertTrue(spans.get(1).italic());
    }

    @Test
    void headerStoryExtractedAsQuote() throws Exception {
        String body = "Body line";
        String header = "Header text\r";
        int total = body.length();
        int ccpHdd = header.length();
        byte[] buf = new byte[8192];
        u16(buf, 0, 0xA5EC);
        u16(buf, 2, 0x00C1);
        u16(buf, 0x0A, 0x0200);
        u32(buf, 76, total);
        u32(buf, 84, ccpHdd);
        // 正文与页眉都放压缩区 4096 起，piece 覆盖 total+ccpHdd
        System.arraycopy(body.getBytes(StandardCharsets.ISO_8859_1), 0, buf, 4096, body.length());
        System.arraycopy(header.getBytes(StandardCharsets.ISO_8859_1), 0, buf, 4096 + total, header.length());
        u32(buf, 418, 0);
        ByteArrayOutputStream clx = new ByteArrayOutputStream();
        clx.write(2);
        u32le(clx, 4 + 8 + 8); // 2 CP + 1 PCD
        u32le(clx, 0);
        u32le(clx, total + ccpHdd);
        u16le(clx, 0);
        u32le(clx, 0x40000000 | (4096 * 2));
        u16le(clx, 0);
        u32(buf, 422, clx.size());
        // plcfhdd：单个 CP 边界 = total（一个故事到 ccpHdd 尽头）
        ByteArrayOutputStream table = new ByteArrayOutputStream();
        table.writeBytes(clx.toByteArray());
        u32le(table, total);
        u32(buf, 242, clx.size());
        u32(buf, 246, 4);

        CfbTestBuilder cfb = new CfbTestBuilder()
                .add("WordDocument", buf)
                .add("1Table", table.toByteArray());
        DocDocument doc = reader.read(cfb.build());
        boolean hasHeader = doc.getBlocks().stream()
                .filter(b -> b instanceof com.zifang.util.doc.DocBlock.Quote)
                .map(b -> ((com.zifang.util.doc.DocBlock.Quote) b).spans().get(0).text())
                .anyMatch(t -> t.contains("[页眉] Header text"));
        assertTrue(hasHeader, "header story should be extracted as quote: " + doc.toPlainText());
    }

    @Test
    void fieldInstructionIsDroppedAndResultKept() throws Exception {
        String[] paras = {"See \u0013HYPERLINK \"http://example.com\"\u0014the link\u0015 end"};
        byte[] table = tableWithClx(paras);
        CfbTestBuilder cfb = new CfbTestBuilder()
                .add("WordDocument", wordDocument(paras, true, table.length))
                .add("1Table", table);
        String text = reader.read(cfb.build()).toPlainText();
        assertTrue(text.contains("See the link end"), text);
        assertFalse(text.contains("HYPERLINK"), text);
    }

    private static void u16le(ByteArrayOutputStream out, int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
    }

    private static void u32le(ByteArrayOutputStream out, int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
        out.write((v >> 16) & 0xFF);
        out.write((v >> 24) & 0xFF);
    }

    private static void u16(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
    }

    private static void u32(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
        b[off + 2] = (byte) (v >> 16);
        b[off + 3] = (byte) (v >> 24);
    }
}
