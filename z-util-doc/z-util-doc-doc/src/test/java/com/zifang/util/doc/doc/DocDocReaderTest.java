package com.zifang.util.doc.doc;

import com.zifang.util.doc.DocDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocDocReaderTest {

    private final DocDocReader reader = new DocDocReader();

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
        u32(buf, 72, total);
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
        u32(buf, 72, text.length());
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
