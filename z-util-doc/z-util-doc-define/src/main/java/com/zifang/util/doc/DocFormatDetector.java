package com.zifang.util.doc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** 魔数嗅探：不看扩展名，只看字节。 */
public final class DocFormatDetector {

    private static final byte[] ZIP_MAGIC = {'P', 'K', 3, 4};
    private static final byte[] OLE_MAGIC = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
            (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};

    private DocFormatDetector() {
    }

    public static boolean isPdf(byte[] data) {
        return startsWith(data, "%PDF");
    }

    public static boolean isOle2(byte[] data) {
        return startsWith(data, OLE_MAGIC);
    }

    public static boolean isZip(byte[] data) {
        return startsWith(data, ZIP_MAGIC);
    }

    public static boolean isRtf(byte[] data) {
        return startsWith(data, "{\\rtf");
    }

    public static boolean isHtml(byte[] data) {
        String head = headText(data, 512).toLowerCase();
        return head.contains("<!doctype html") || head.contains("<html");
    }

    /**
     * zip 容器的细分：docx 有 word/document.xml，xlsx 有 xl/workbook.xml，pptx 有 ppt/presentation.xml，
     * epub/odf 看 mimetype 条目。只顺序看前若干个本地文件头，不解压整个包。
     */
    public static DocFormat detectZipContainer(byte[] data) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            int scanned = 0;
            while ((entry = zip.getNextEntry()) != null && scanned < 32) {
                scanned++;
                String name = entry.getName();
                if (name.equals("mimetype")) {
                    byte[] content = zip.readNBytes(64);
                    String mime = new String(content, StandardCharsets.ISO_8859_1).trim();
                    DocFormat odf = detectOdfMime(mime);
                    if (odf != null) {
                        return odf;
                    }
                    if (mime.equals("application/epub+zip")) {
                        return DocFormat.EPUB;
                    }
                } else if (name.equals("xl/workbook.xml")) {
                    return DocFormat.XLSX;
                } else if (name.equals("word/document.xml")) {
                    return DocFormat.DOCX;
                } else if (name.equals("ppt/presentation.xml")) {
                    return DocFormat.PPTX;
                }
                zip.closeEntry();
            }
        } catch (IOException e) {
            // 容器损坏，交给上层按不支持处理
        }
        return null;
    }

    private static DocFormat detectOdfMime(String mime) {
        if ("application/vnd.oasis.opendocument.spreadsheet".equals(mime)) {
            return DocFormat.ODS;
        }
        if ("application/vnd.oasis.opendocument.text".equals(mime)) {
            return DocFormat.ODT;
        }
        if ("application/vnd.oasis.opendocument.presentation".equals(mime)) {
            return DocFormat.ODP;
        }
        return null;
    }

    public static boolean startsWith(byte[] data, String s) {
        return startsWith(data, s.getBytes(StandardCharsets.US_ASCII));
    }

    public static boolean startsWith(byte[] data, byte[] prefix) {
        if (data == null || data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static String headText(byte[] data, int limit) {
        int n = Math.min(limit, data == null ? 0 : data.length);
        return new String(data, 0, n, StandardCharsets.UTF_8);
    }

    /** 便捷：按注册的读取器嗅探格式。 */
    public static DocFormat detect(byte[] data, List<DocReader> readers) {
        List<DocFormat> hits = new ArrayList<>();
        for (DocReader reader : readers) {
            if (reader.supports(data)) {
                hits.add(reader.format());
            }
        }
        return hits.isEmpty() ? null : hits.get(0);
    }
}
