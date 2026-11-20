package com.zifang.util.doc.ppt;

import java.util.ArrayList;
import java.util.List;

/**
 * .ppt 内嵌图片：Pictures 流里的图片条目。
 *
 * <p>Pictures 流里每张图是一段 8 字节头（签名 u16、类型 u16、长度 i32）加图片数据；
 * 类型是 OfficeArt 原生类型 + 0xF018：0xF01A EMF、0xF01B WMF、0xF01C PICT、
 * 0xF01D JPEG、0xF01E PNG、0xF01F DIB。</p>
 *
 * <p>图片数据前面是 16 字节 UID（有两份 UID 时是 32 字节）加 1 字节 tag，
 * 真正的图片字节从签名处开始，所以这里按签名定位而不是按固定偏移切。</p>
 */
final class HslfPictures {

    /** 一张图片：原始字节 + MIME。 */
    record Picture(byte[] data, String mimeType, String name) {
    }

    private static final int TYPE_EMF = 0xF01A;
    private static final int TYPE_WMF = 0xF01B;
    private static final int TYPE_PICT = 0xF01C;
    private static final int TYPE_JPEG = 0xF01D;
    private static final int TYPE_PNG = 0xF01E;
    private static final int TYPE_DIB = 0xF01F;

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private HslfPictures() {
    }

    /** 解析 Pictures 流里的全部图片。 */
    static List<Picture> parse(byte[] pictures) {
        List<Picture> result = new ArrayList<>();
        if (pictures == null) {
            return result;
        }
        int pos = 0;
        while (pos + 8 <= pictures.length) {
            int type = u16(pictures, pos + 2);
            int size = u32(pictures, pos + 4);
            pos += 8;
            if (size < 0 || pos + size > pictures.length) {
                break;
            }
            Picture picture = toPicture(type, pictures, pos, size);
            if (picture != null) {
                result.add(picture);
            }
            pos += size;
        }
        return result;
    }

    private static Picture toPicture(int type, byte[] data, int offset, int size) {
        int end = offset + size;
        switch (type) {
            case TYPE_PNG -> {
                int start = indexOf(data, PNG_SIGNATURE, offset, end);
                if (start < 0) {
                    return null;
                }
                return new Picture(copy(data, start, end - start), "image/png", "image.png");
            }
            case TYPE_JPEG -> {
                int start = indexOf(data, JPEG_SIGNATURE, offset, end);
                if (start < 0) {
                    return null;
                }
                return new Picture(copy(data, start, end - start), "image/jpeg", "image.jpg");
            }
            case TYPE_DIB -> {
                int start = offset + 17;
                if (start + 4 > end) {
                    return null;
                }
                return new Picture(addBmpHeader(copy(data, start, end - start)), "image/bmp", "image.bmp");
            }
            case TYPE_EMF -> {
                int start = offset + 17;
                if (start >= end) {
                    return null;
                }
                return new Picture(copy(data, start, end - start), "image/x-emf", "image.emf");
            }
            case TYPE_WMF -> {
                int start = offset + 17;
                if (start >= end) {
                    return null;
                }
                return new Picture(copy(data, start, end - start), "image/x-wmf", "image.wmf");
            }
            case TYPE_PICT -> {
                int start = offset + 17;
                if (start >= end) {
                    return null;
                }
                return new Picture(copy(data, start, end - start), "image/pict", "image.pict");
            }
            default -> {
                return null;
            }
        }
    }

    /** DIB 没有文件头，补上 BMP 文件头才是可用的图片。 */
    private static byte[] addBmpHeader(byte[] dib) {
        if (dib.length < 40) {
            return dib;
        }
        byte[] header = new byte[14];
        header[0] = 'B';
        header[1] = 'M';
        int pixelOffset = u32(dib, 0x22 - 14);
        int fileSize = dib.length + 14;
        put32(header, 2, fileSize);
        put32(header, 10, pixelOffset);
        byte[] out = new byte[fileSize];
        System.arraycopy(header, 0, out, 0, 14);
        System.arraycopy(dib, 0, out, 14, dib.length);
        return out;
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

    private static byte[] copy(byte[] data, int offset, int length) {
        byte[] out = new byte[Math.max(0, length)];
        System.arraycopy(data, offset, out, 0, out.length);
        return out;
    }

    private static void put32(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
        b[off + 2] = (byte) (v >> 16);
        b[off + 3] = (byte) (v >> 24);
    }

    private static int u16(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8);
    }

    private static int u32(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8)
                | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }
}
