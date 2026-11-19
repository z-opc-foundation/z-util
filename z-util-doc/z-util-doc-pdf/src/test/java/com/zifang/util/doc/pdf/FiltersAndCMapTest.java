package com.zifang.util.doc.pdf;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiltersAndCMapTest {

    @Test
    void flateRoundtrip() {
        byte[] original = "hello flate world, hello flate world".getBytes(StandardCharsets.ISO_8859_1);
        Deflater deflater = new Deflater();
        deflater.setInput(original);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!deflater.finished()) {
            out.write(buf, 0, deflater.deflate(buf));
        }
        deflater.end();
        assertEquals(new String(original, StandardCharsets.ISO_8859_1),
                new String(PdfFilters.apply("FlateDecode", out.toByteArray(), null), StandardCharsets.ISO_8859_1));
    }

    @Test
    void asciiHex() {
        byte[] data = "48656C6C6F>".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals("Hello", new String(PdfFilters.apply("ASCIIHexDecode", data, null),
                StandardCharsets.ISO_8859_1));
    }

    @Test
    void ascii85() {
        // "Man " 的 ASCII85 编码是 "9jqo^"
        byte[] data = "9jqo^~>".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals("Man ", new String(PdfFilters.apply("ASCII85Decode", data, null),
                StandardCharsets.ISO_8859_1));
    }

    @Test
    void runLength() {
        // 2 followed by 'A' -> "AA"; 254 followed by 'B' -> "B" x3 (257-254)
        byte[] data = {0, (byte) 65, (byte) 254, (byte) 66, (byte) 128};
        assertEquals("ABBB", new String(PdfFilters.apply("RunLengthDecode", data, null),
                StandardCharsets.ISO_8859_1));
    }

    @Test
    void bfcharAndBfrange() {
        String cmap = """
                /CIDInit /ProcSet findresource begin
                beginbfchar
                <0041> <00E4>
                <0042> <4E2D6587>
                endbfchar
                beginbfrange
                <0050> <0052> <0061>
                endbfrange
                end
                """;
        ToUnicodeCMap map = ToUnicodeCMap.parse(cmap.getBytes(StandardCharsets.ISO_8859_1));
        assertEquals("ä", map.lookup(0x41));
        assertEquals("中文", map.lookup(0x42));
        assertEquals("a", map.lookup(0x50));
        assertEquals("b", map.lookup(0x51));
        assertEquals("c", map.lookup(0x52));
    }

    @Test
    void pngPredictor() {
        // 2 行，每行 2 字节 + filter byte；第二行 None filter 输出与 prev 无关的直通
        byte[] data = {1, 5, 5, 0, (byte) 3, (byte) 3};
        // 第一行 Up: [5,5]；第二行 None: [3,3]
        byte[] out = PdfFilters.undoPngPredictor(data, 1, 8, 2);
        assertEquals(4, out.length);
        assertEquals(5, out[0] & 0xFF);
        assertEquals(3, out[2] & 0xFF);
    }
}
