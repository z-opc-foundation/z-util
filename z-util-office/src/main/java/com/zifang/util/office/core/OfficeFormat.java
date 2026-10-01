package com.zifang.util.office.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Office 文档格式检测：按文件魔数 + OOXML 包内部目录名判定。
 *
 * <ul>
 *   <li>{@code %PDF-} 开头 → PDF</li>
 *   <li>OLE2 头（D0 CF 11 E0）→ 老格式 DOC / XLS / PPT（魔数无法区分，返回 OLE2 泛型）</li>
 *   <li>ZIP 头（PK）→ OOXML 包：看首个 entry 前缀 word/ xl/ ppt/ 区分 DOCX / XLSX / PPTX</li>
 *   <li>其余 → UNKNOWN</li>
 * </ul>
 *
 * <p>检测只读输入流的前若干字节与首个 zip 目录项，不加载整份文档。
 */
public final class OfficeFormat {

    /** 可识别的文档格式。 */
    public enum Format {
        /** PDF 文档（%PDF- 魔数）。 */
        PDF,
        /** Word 2007+（OOXML，包内 word/ 目录）。 */
        DOCX,
        /** Excel 2007+（OOXML，包内 xl/ 目录）。 */
        XLSX,
        /** PowerPoint 2007+（OOXML，包内 ppt/ 目录）。 */
        PPTX,
        /** OLE2 容器（老 Word / Excel / PowerPoint 共用魔数，无法进一步区分）。 */
        OLE2,
        /** 无法识别。 */
        UNKNOWN
    }

    private OfficeFormat() {
    }

    /**
     * 检测输入流的格式。方法会读取流的一部分但**不关闭**流。
     *
     * @param in 文档输入流
     * @return 识别出的格式，永不返回 null
     * @throws IOException 读取失败
     */
    public static Format detect(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null");
        }
        in.mark(8);
        byte[] head = new byte[8];
        int read = 0;
        while (read < head.length) {
            int n = in.read(head, read, head.length - read);
            if (n < 0) {
                break;
            }
            read += n;
        }
        in.reset();
        if (read < 4) {
            return Format.UNKNOWN;
        }
        if (head[0] == '%' && head[1] == 'P' && head[2] == 'D' && head[3] == 'F') {
            return Format.PDF;
        }
        if ((head[0] & 0xFF) == 0xD0 && (head[1] & 0xFF) == 0xCF
                && (head[2] & 0xFF) == 0x11 && (head[3] & 0xFF) == 0xE0) {
            return Format.OLE2;
        }
        if (head[0] == 'P' && head[1] == 'K') {
            return detectOoxml(in);
        }
        return Format.UNKNOWN;
    }

    /**
     * 便捷入口：检测文件的格式。
     *
     * @param file 文档文件
     * @return 识别出的格式
     * @throws IOException 读取失败
     */
    public static Format detect(java.io.File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("not a file: " + file);
        }
        try (InputStream in = new java.io.FileInputStream(file)) {
            return detect(in);
        }
    }

    private static Format detectOoxml(InputStream in) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("word/")) {
                    return Format.DOCX;
                }
                if (name.startsWith("xl/")) {
                    return Format.XLSX;
                }
                if (name.startsWith("ppt/")) {
                    return Format.PPTX;
                }
                zip.closeEntry();
            }
        }
        return Format.UNKNOWN;
    }
}
