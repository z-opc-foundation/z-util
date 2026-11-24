package com.zifang.util.doc.pdf;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CcittFaxTest {

    private static PdfDict parms(int k, int columns, int rows, boolean blackIs1, boolean byteAlign) {
        PdfDict p = new PdfDict();
        p.put(new PdfName("K"), k);
        p.put(new PdfName("Columns"), columns);
        if (rows > 0) {
            p.put(new PdfName("Rows"), rows);
        }
        if (blackIs1) {
            p.put(new PdfName("BlackIs1"), Boolean.TRUE);
        }
        if (byteAlign) {
            p.put(new PdfName("EncodedByteAlign"), Boolean.TRUE);
        }
        return p;
    }

    private static byte[] bits(String s) {
        byte[] out = new byte[(s.length() + 7) / 8];
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '1') {
                out[i / 8] |= (byte) (0x80 >> (i % 8));
            }
        }
        return out;
    }

    private static byte[] decode(String bits, PdfDict parms) {
        return CcittFax.decode(bits(bits), parms);
    }

    private static void assertPacked(byte[] actual, int[] expected) {
        byte[] want = new byte[expected.length];
        for (int i = 0; i < expected.length; i++) {
            want[i] = (byte) expected[i];
        }
        assertArrayEquals(want, actual, () -> "got " + hex(actual));
    }

    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte v : b) {
            sb.append(String.format("%02X ", v));
        }
        return sb.toString();
    }

    @Test
    void g4VerticalHorizontalPass() {
        // Columns=16, 五行：全白 / 黑[0,4) / 黑[1,4) / 黑[5,8)（Pass+H）/ 黑[2,5)（VL3）
        String stream = "1"
                + "001" + "00110101" + "011" + "1"
                + "011" + "1" + "1"
                + "0001" + "001" + "000111" + "10" + "1"
                + "0000010" + "0000010" + "1";
        byte[] out = decode(stream, parms(-1, 16, 0, false, false));
        assertPacked(out, new int[]{
                0xFF, 0xFF,
                0x0F, 0xFF,
                0x8F, 0xFF,
                0xF8, 0xFF,
                0xC7, 0xFF,
        });
    }

    @Test
    void g3OneDimensionalWithMakeup() {
        // Columns=72：白 72（makeup64+8）/ 白 66（makeup64+2）+ 黑 6
        String stream = "11011" + "10011"
                + "11011" + "0111" + "0010";
        byte[] out = decode(stream, parms(0, 72, 0, false, false));
        assertPacked(out, new int[]{
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
                0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xC0,
        });
    }

    @Test
    void g3ByteAlignedRows() {
        // Columns=8：白 8 / 黑 8，行间按字节对齐（行尾补齐到字节边界）
        String stream = "10011" + "000" + "00110101" + "000101" + "00";
        byte[] out = decode(stream, parms(0, 8, 0, false, true));
        assertPacked(out, new int[]{0xFF, 0x00});
    }

    @Test
    void blackIs1Inverts() {
        byte[] normal = decode("1", parms(-1, 8, 0, false, false));
        assertPacked(normal, new int[]{0xFF});
        byte[] inverted = decode("1", parms(-1, 8, 0, true, false));
        assertPacked(inverted, new int[]{0x00});
    }

    @Test
    void endOfBlockStopsAndPadsWhiteRows() {
        // Columns=8：白 2 + 黑 3，随后 EOFB；Rows=4 → 余下补白
        String stream = "001" + "0111" + "10"
                + "000000000001" + "000000000001";
        byte[] out = decode(stream, parms(-1, 8, 4, false, false));
        assertPacked(out, new int[]{0xC7, 0xFF, 0xFF, 0xFF});
    }

    @Test
    void g3TwoDimensionalWithEolFlags() {
        // K=2：EOL+1（一维全白）/ EOL+0（二维：黑[3,6)）/ EOL+1（一维全白）
        String stream = "000000000001" + "1" + "10011"
                + "000000000001" + "0" + "001" + "1000" + "10" + "1"
                + "000000000001" + "1" + "10011";
        byte[] out = decode(stream, parms(2, 8, 0, false, false));
        assertPacked(out, new int[]{0xFF, 0xE3, 0xFF});
    }

    @Test
    void truncatedStreamPadsWhite() {
        // G4 流在行中间断掉 → 已解部分按白补齐，不抛异常
        String stream = "1" + "001" + "0011010";
        byte[] out = decode(stream, parms(-1, 16, 0, false, false));
        assertSame(byte[].class, out.getClass());
    }

    @Test
    void invalidParmsPassThrough() {
        byte[] data = "junk".getBytes();
        assertSame(data, CcittFax.decode(data, null));
        assertSame(data, CcittFax.decode(data, new PdfDict()));
        assertNull(CcittFax.decode(null, parms(-1, 16, 0, false, false)));
        byte[] empty = new byte[0];
        assertSame(empty, CcittFax.decode(empty, parms(-1, 16, 0, false, false)));
    }

    @Test
    void codeTablesArePrefixFree() {
        check(CcittFax.WHITE_TERM, CcittFax.WHITE_MAKEUP);
        check(CcittFax.BLACK_TERM, CcittFax.BLACK_MAKEUP);
    }

    private static void check(String[] term, String[] makeup) {
        String[] all = new String[term.length + makeup.length];
        System.arraycopy(term, 0, all, 0, term.length);
        System.arraycopy(makeup, 0, all, term.length, makeup.length);
        Arrays.sort(all);
        for (int i = 1; i < all.length; i++) {
            if (all[i].startsWith(all[i - 1])) {
                throw new AssertionError("prefix collision: " + all[i - 1] + " -> " + all[i]);
            }
        }
    }
}
