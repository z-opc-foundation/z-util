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

    /**
     * 若干段文字，每段单独定位：前几段间距均匀，最后一段离得远。
     * 用来验证行内拼接只在大空档处补空格。
     */
    public static byte[] positionedRuns(String[] runs, double[] xs) {
        StringBuilder content = new StringBuilder();
        content.append("BT /F1 12 Tf ");
        for (int i = 0; i < runs.length; i++) {
            content.append("1 0 0 1 ").append(xs[i]).append(" 700 Tm (").append(esc(runs[i])).append(") Tj ");
        }
        content.append("ET\n");
        return build(content.toString(), false, true);
    }

    /** 带 q/Q 图形状态栈的内容（q 出现在第一次 Tf 之前）。 */
    public static byte[] withGraphicsState(String content) {
        return build(content, false, true);
    }

    /**
     * 带图片的 PDF：/Im0 是 DeviceRGB 位图（FlateDecode），可选 /SMask 灰度图当 alpha。
     *
     * @param width  宽（像素）
     * @param height 高（像素）
     * @param rgb    长度 width*height*3 的 RGB 采样
     * @param alpha  长度 width*height 的灰度 alpha，传 null 表示不带遮罩
     */
    public static byte[] withImage(int width, int height, byte[] rgb, byte[] alpha) {
        byte[] imageData = deflate(rgb);
        byte[] maskData = alpha == null ? null : deflate(alpha);

        StringBuilder content = new StringBuilder();
        content.append("q 200 0 0 100 50 600 cm /Im0 Do Q\n");
        byte[] contentBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);

        long[] offsets = new long[9];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(ascii("%PDF-1.4\n"));
        offsets[1] = out.size();
        out.writeBytes(ascii("1 0 obj\n<< /Type/Catalog /Pages 2 0 R >>\nendobj\n"));
        offsets[2] = out.size();
        out.writeBytes(ascii("2 0 obj\n<< /Type/Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"));
        offsets[3] = out.size();
        out.writeBytes(ascii("3 0 obj\n<< /Type/Page /Parent 2 0 R /MediaBox[0 0 612 792] "
                + "/Resources<< /XObject<< /Im0 6 0 R >> >> /Contents 5 0 R >>\nendobj\n"));
        offsets[4] = out.size();
        out.writeBytes(ascii("4 0 obj\n<< /Type/Font /Subtype/Type1 /BaseFont/Helvetica "
                + "/Encoding/WinAnsiEncoding >>\nendobj\n"));
        offsets[5] = out.size();
        out.writeBytes(ascii("5 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n"));
        out.writeBytes(contentBytes);
        out.writeBytes(ascii("\nendstream\nendobj\n"));
        offsets[6] = out.size();
        StringBuilder imageDict = new StringBuilder();
        imageDict.append("6 0 obj\n<< /Type/XObject /Subtype/Image /Width ").append(width)
                .append(" /Height ").append(height)
                .append(" /ColorSpace/DeviceRGB /BitsPerComponent 8 /Filter/FlateDecode");
        if (maskData != null) {
            imageDict.append(" /SMask 7 0 R");
        }
        imageDict.append(" /Length ").append(imageData.length).append(" >>\nstream\n");
        out.writeBytes(ascii(imageDict.toString()));
        out.writeBytes(imageData);
        out.writeBytes(ascii("\nendstream\nendobj\n"));
        int objCount = 7;
        if (maskData != null) {
            offsets[7] = out.size();
            out.writeBytes(ascii("7 0 obj\n<< /Type/XObject /Subtype/Image /Width " + width
                    + " /Height " + height + " /ColorSpace/DeviceGray /BitsPerComponent 8"
                    + " /Filter/FlateDecode /Length " + maskData.length + " >>\nstream\n"));
            out.writeBytes(maskData);
            out.writeBytes(ascii("\nendstream\nendobj\n"));
            objCount = 8;
        }
        long xrefPos = out.size();
        StringBuilder xref = new StringBuilder("xref\n0 " + (objCount + 1) + "\n");
        xref.append("0000000000 65535 f \n");
        for (int i = 1; i <= objCount; i++) {
            xref.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        xref.append("trailer\n<< /Size ").append(objCount + 1).append(" /Root 1 0 R >>\n");
        xref.append("startxref\n").append(xrefPos).append("\n%%EOF\n");
        out.writeBytes(ascii(xref.toString()));
        return out.toByteArray();
    }

    private static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (!deflater.finished()) {
            int n = deflater.deflate(buf);
            if (n <= 0) {
                break;
            }
            out.write(buf, 0, n);
        }
        deflater.end();
        return out.toByteArray();
    }

    private static byte[] ascii(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }
}
