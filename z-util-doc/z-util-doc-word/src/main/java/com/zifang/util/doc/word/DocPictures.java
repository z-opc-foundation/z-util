package com.zifang.util.doc.word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * .doc 内嵌图片提取。
 *
 * <p>两条路径：</p>
 * <ol>
 *   <li>绘图数据（FIB.fcDggInfo 指向的表流区间）：DggContainer → BStoreContainer → BSE。
 *       BSE 的 foDelay 非零时 BLIP 记录在主流（WordDocument）里，否则内嵌在 BSE 自身数据尾部。</li>
 *   <li>内联图片：正文里的特殊字符 0x01，其 CHPX 里的 sprmCPicLocation 指向 Data 流里的
 *       PICF 结构，PICF 之后同样是 OfficeArt 图片数据。</li>
 * </ol>
 *
 * <p>BLIP 记录类型：0xF01A EMF / 0xF01B WMF / 0xF01C PICT / 0xF01D JPEG /
 * 0xF01E PNG / 0xF01F DIB / 0xF029 TIFF。这里只还原原始字节，交给上层按类型使用。</p>
 */
final class DocPictures {

    /** 一张图片：原始字节 + MIME。 */
    record Picture(byte[] data, String mimeType, String name) {
    }

    private static final int TYPE_BSE = 0xF007;

    private static final int BLIP_EMF = 0xF01A;
    private static final int BLIP_WMF = 0xF01B;
    private static final int BLIP_PICT = 0xF01C;
    private static final int BLIP_JPEG = 0xF01D;
    private static final int BLIP_PNG = 0xF01E;
    private static final int BLIP_TIFF = 0xF029;

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] JPEG_END = {(byte) 0xFF, (byte) 0xD9};
    private static final byte[] GIF_SIGNATURE = {'G', 'I', 'F', '8'};

    private DocPictures() {
    }

    /** 绘图数据里的图片（浮动图形、水印等）。 */
    static List<Picture> fromDrawing(byte[] table, byte[] main, int fcDggInfo, int lcbDggInfo) {
        List<Picture> pictures = new ArrayList<>();
        if (table == null || fcDggInfo < 0 || lcbDggInfo <= 0 || fcDggInfo + lcbDggInfo > table.length) {
            return pictures;
        }
        collect(table, main, fcDggInfo, fcDggInfo + lcbDggInfo, pictures);
        return pictures;
    }

    /** 内联图片：Data 流里 picOffset 处的 PICF 结构之后的 OfficeArt 数据。 */
    static Picture fromInline(byte[] dataStream, int picOffset) {
        if (dataStream == null || picOffset < 0 || picOffset + 66 > dataStream.length) {
            return null;
        }
        int cbHeader = u16(dataStream, picOffset + 4);
        int headerSize = cbHeader >= 66 && cbHeader <= 1024 ? cbHeader : 66;
        int offset = picOffset + headerSize;
        if (offset + 8 > dataStream.length) {
            return null;
        }
        return blip(dataStream, offset, dataStream.length);
    }

    private static void collect(byte[] table, byte[] main, int p, int end, List<Picture> out) {
        while (p + 8 <= end) {
            int verInstance = u16(table, p);
            int ver = verInstance & 0x0F;
            int type = u16(table, p + 2);
            int len = u32(table, p + 4);
            if (len < 0 || p + 8L + len > end) {
                return;
            }
            if (ver == 0x0F) {
                collect(table, main, p + 8, p + 8 + len, out);
            } else if (type == TYPE_BSE) {
                Picture picture = bsePicture(table, main, p + 8, len);
                if (picture != null) {
                    out.add(picture);
                }
            }
            p += 8 + len;
        }
    }

    /** BSE：foDelay 指向主流里的 BLIP，否则 BLIP 内嵌在自身数据尾部。 */
    private static Picture bsePicture(byte[] table, byte[] main, int p, int len) {
        if (len < 32) {
            return null;
        }
        int foDelay = u32(table, p + 28);
        if (foDelay > 0 && main != null && foDelay + 8 <= main.length) {
            return blip(main, foDelay, main.length);
        }
        int cbName = table[p + 33] & 0xFF;
        int blipHeader = p + 36 + cbName;
        if (blipHeader + 8 <= p + len) {
            return blip(table, blipHeader, p + len);
        }
        return null;
    }

    /** BLIP 记录 → 原始图片字节。 */
    private static Picture blip(byte[] stream, int offset, int limit) {
        if (offset + 8 > limit) {
            return null;
        }
        int type = u16(stream, offset + 2);
        int len = u32(stream, offset + 4);
        int payload = offset + 8;
        int end = Math.min(limit, payload + Math.max(0, len));
        switch (type) {
            case BLIP_PNG -> {
                int start = indexOf(stream, PNG_SIGNATURE, payload, end);
                if (start < 0) {
                    return null;
                }
                int size = pngSize(stream, start, end);
                return new Picture(Arrays.copyOfRange(stream, start, start + size),
                        "image/png", "image.png");
            }
            case BLIP_JPEG -> {
                int start = indexOf(stream, JPEG_SIGNATURE, payload, end);
                if (start < 0) {
                    return null;
                }
                int tail = indexOf(stream, JPEG_END, start + 3, end);
                int size = tail < 0 ? end - start : tail + 2 - start;
                return new Picture(Arrays.copyOfRange(stream, start, start + size),
                        "image/jpeg", "image.jpg");
            }
            case BLIP_TIFF -> {
                return new Picture(Arrays.copyOfRange(stream, payload, end),
                        "image/tiff", "image.tif");
            }
            case BLIP_EMF -> {
                return new Picture(Arrays.copyOfRange(stream, payload, end),
                        "image/x-emf", "image.emf");
            }
            case BLIP_WMF -> {
                // WMF 的 BLIP 前置 22 字节 metafile 头，真正的图元数据在后面
                int start = payload + 22 <= end ? payload + 22 : payload;
                return new Picture(Arrays.copyOfRange(stream, start, end),
                        "image/x-wmf", "image.wmf");
            }
            case BLIP_PICT -> {
                return null;
            }
            default -> {
                return null;
            }
        }
    }

    /** PNG 长度：按 chunk 走到 IEND。 */
    private static int pngSize(byte[] data, int start, int limit) {
        int p = start + PNG_SIGNATURE.length;
        while (p + 8 <= limit) {
            int len = u32(data, p);
            String type = new String(data, p + 4, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
            long next = (long) p + 8 + len + 4;
            if (next > limit) {
                break;
            }
            p = (int) next;
            if ("IEND".equals(type)) {
                return p - start;
            }
        }
        return limit - start;
    }

    private static int indexOf(byte[] data, byte[] pattern, int from, int limit) {
        int end = Math.min(limit, data.length) - pattern.length;
        for (int i = Math.max(0, from); i <= end; i++) {
            boolean hit = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    hit = false;
                    break;
                }
            }
            if (hit) {
                return i;
            }
        }
        return -1;
    }

    private static int u16(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8);
    }

    private static int u32(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8)
                | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }
}
