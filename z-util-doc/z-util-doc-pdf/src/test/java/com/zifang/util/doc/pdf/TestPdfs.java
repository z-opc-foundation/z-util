package com.zifang.util.doc.pdf;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

/** 测试专用：手写最小 PDF 字节（经典 xref 表）。 */
public final class TestPdfs {

    private TestPdfs() {
    }

    /** 单页 PDF：每行一个 Tj，第一行 18pt 当标题，其余 12pt。 */
    public static byte[] simple(String title, String... bodyLines) {
        StringBuilder content = new StringBuilder();
        content.append("BT /F1 18 Tf 72 700 Td (").append(esc(title)).append(") Tj ET\n");
        int y = 660;
        for (String line : bodyLines) {
            content.append("BT /F1 12 Tf 72 ").append(y).append(" Td (")
                    .append(esc(line)).append(") Tj ET\n");
            y -= 20;
        }
        return build(content.toString(), false, true);
    }

    /** 内容流 Flate 压缩版。 */
    public static byte[] flated(String title, String... bodyLines) {
        StringBuilder content = new StringBuilder();
        content.append("BT /F1 18 Tf 72 700 Td (").append(esc(title)).append(") Tj ET\n");
        int y = 660;
        for (String line : bodyLines) {
            content.append("BT /F1 12 Tf 72 ").append(y).append(" Td (")
                    .append(esc(line)).append(") Tj ET\n");
            y -= 20;
        }
        return build(content.toString(), true, true);
    }

    public static byte[] withoutInfo() {
        return build("BT /F1 12 Tf 72 700 Td (plain) Tj ET\n", false, false);
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static byte[] build(String content, boolean flate, boolean withInfo) {
        byte[] contentBytes;
        String filterEntry;
        if (flate) {
            Deflater deflater = new Deflater();
            deflater.setInput(content.getBytes(StandardCharsets.ISO_8859_1));
            deflater.finish();
            ByteArrayOutputStream c = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            while (!deflater.finished()) {
                c.write(buf, 0, deflater.deflate(buf));
            }
            deflater.end();
            contentBytes = c.toByteArray();
            filterEntry = "/Filter/FlateDecode ";
        } else {
            contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);
            filterEntry = "";
        }

        StringBuilder widths = new StringBuilder("[");
        for (int i = 32; i <= 126; i++) {
            widths.append(i == 32 ? "" : " ").append(556);
        }
        widths.append("]");

        long[] offsets = new long[8];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(ascii("%PDF-1.4\n"));
        offsets[1] = out.size();
        out.writeBytes(ascii("1 0 obj\n<< /Type/Catalog /Pages 2 0 R >>\nendobj\n"));
        offsets[2] = out.size();
        out.writeBytes(ascii("2 0 obj\n<< /Type/Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"));
        offsets[3] = out.size();
        out.writeBytes(ascii("3 0 obj\n<< /Type/Page /Parent 2 0 R /MediaBox[0 0 612 792] "
                + "/Resources<< /Font<< /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n"));
        offsets[4] = out.size();
        out.writeBytes(ascii("4 0 obj\n<< /Type/Font /Subtype/Type1 /BaseFont/Helvetica "
                + "/Encoding/WinAnsiEncoding /FirstChar 32 /LastChar 126 /Widths " + widths + " >>\nendobj\n"));
        offsets[5] = out.size();
        out.writeBytes(ascii("5 0 obj\n<< /Length " + contentBytes.length + " " + filterEntry + ">>\nstream\n"));
        out.writeBytes(contentBytes);
        out.writeBytes(ascii("\nendstream\nendobj\n"));
        int infoObj = withInfo ? 6 : 0;
        if (withInfo) {
            offsets[6] = out.size();
            out.writeBytes(ascii("6 0 obj\n<< /Title (Test Doc) /Author (zifang) "
                    + "/CreationDate (D:20261119120000Z) >>\nendobj\n"));
        }
        long xrefPos = out.size();
        StringBuilder xref = new StringBuilder("xref\n0 " + (withInfo ? 7 : 6) + "\n");
        xref.append("0000000000 65535 f \n");
        for (int i = 1; i <= (withInfo ? 6 : 5); i++) {
            xref.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        xref.append("trailer\n<< /Size ").append(withInfo ? 7 : 6).append(" /Root 1 0 R");
        if (withInfo) {
            xref.append(" /Info ").append(infoObj).append(" 0 R");
        }
        xref.append(" >>\nstartxref\n").append(xrefPos).append("\n%%EOF\n");
        out.writeBytes(ascii(xref.toString()));
        return out.toByteArray();
    }

    private static byte[] ascii(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }
}
