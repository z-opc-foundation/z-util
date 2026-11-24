package com.zifang.util.doc.pdf;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.Deflater;

/**
 * PDF 图像 XObject → 原始图片字节。
 *
 * <p>DCTDecode 直接就是 JPEG，原样输出；其余滤镜（Flate / LZW / RunLength / ASCII*）解出来的是
 * 裸采样，按位深与色彩空间重新封装成 PNG（PNG 容器由本类拼，zlib 用 JDK 的 Deflater）。</p>
 *
 * <p>色彩空间：DeviceGray/CalGray → 灰度，DeviceRGB/CalRGB/ICCBased(N=3) → 真彩，
 * DeviceCMYK/ICCBased(N=4) → 转成 RGB，Indexed → 调色板，Separation/DeviceN → 按灰度近似。
 * /SMask 存在时补一条 alpha 通道。</p>
 */
final class PdfImage {

    /** 解析结果：图片字节 + MIME。 */
    record Result(byte[] data, String mimeType) {
    }

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    private PdfImage() {
    }

    /**
     * @param img     图像字典（/Width /Height /BitsPerComponent /ColorSpace /Filter /SMask …）
     * @param decoded 已解滤镜的采样数据
     * @param resolver 解引用回调
     */
    static Result build(PdfDict img, byte[] decoded, PdfDict.RefResolver resolver) {
        if (img == null || decoded == null) {
            return null;
        }
        int width = (int) img.getNumberOr("Width", 0);
        int height = (int) img.getNumberOr("Height", 0);
        if (width <= 0 || height <= 0) {
            return null;
        }
        int bits = (int) img.getNumberOr("BitsPerComponent", 8);
        if (bits != 1 && bits != 2 && bits != 4 && bits != 8 && bits != 16) {
            return null;
        }
        Space space = space(img, resolver);
        if (space == null) {
            return null;
        }
        int components = space.components();
        int rowBytes = (width * components * bits + 7) / 8;
        if (decoded.length < rowBytes * height) {
            return null;
        }
        byte[] alpha = mask(img, resolver, width, height);

        byte[] png = writePng(width, height, bits, space, decoded, rowBytes, alpha);
        if (png == null) {
            return null;
        }
        return new Result(png, "image/png");
    }

    // ---------------- 色彩空间 ----------------

    /** 色彩空间：分量数 + 是否索引（索引时带调色板）。 */
    private record Space(int components, boolean indexed, byte[] palette, boolean cmyk) {

        static Space rgb() {
            return new Space(3, false, null, false);
        }

        static Space gray() {
            return new Space(1, false, null, false);
        }
    }

    private static Space space(PdfDict img, PdfDict.RefResolver resolver) {
        Object cs = PdfDict.resolve(img.getRaw("ColorSpace"), resolver, 8);
        String name = cs instanceof PdfName n ? n.name : null;
        if (name == null && cs instanceof List<?> list && !list.isEmpty()) {
            Object head = PdfDict.resolve(list.get(0), resolver, 8);
            name = head instanceof PdfName n ? n.name : null;
            if ("ICCBased".equals(name) && list.size() > 1) {
                PdfDict icc = asDict(PdfDict.resolve(list.get(1), resolver, 8));
                int n = icc == null ? 3 : (int) icc.getNumberOr("N", 3);
                return switch (n) {
                    case 1 -> Space.gray();
                    case 4 -> new Space(4, false, null, true);
                    default -> Space.rgb();
                };
            }
            if ("Indexed".equals(name) && list.size() >= 4) {
                byte[] palette = lookupBytes(PdfDict.resolve(list.get(3), resolver, 8));
                if (palette == null) {
                    return null;
                }
                return new Space(1, true, palette, false);
            }
            if ("CalRGB".equals(name) || ("DeviceN".equals(name))) {
                return Space.rgb();
            }
            if ("CalGray".equals(name) || "Separation".equals(name)) {
                return Space.gray();
            }
        }
        if (name == null) {
            return Space.gray();
        }
        return switch (name) {
            case "DeviceGray", "CalGray", "Separation", "DeviceN" -> Space.gray();
            case "DeviceRGB", "CalRGB", "Lab" -> Space.rgb();
            case "DeviceCMYK" -> new Space(4, false, null, true);
            default -> null;
        };
    }

    /** Indexed 调色板：字符串或流对象里的字节。 */
    private static byte[] lookupBytes(Object v) {
        if (v instanceof byte[] b) {
            return b;
        }
        if (v instanceof PdfStreamObj s) {
            return s.data;
        }
        return null;
    }

    private static PdfDict asDict(Object v) {
        return v instanceof PdfDict d ? d : null;
    }

    /** /SMask 的灰度图作为 alpha 通道。 */
    private static byte[] mask(PdfDict img, PdfDict.RefResolver resolver, int width, int height) {
        Object smask = PdfDict.resolve(img.getRaw("SMask"), resolver, 8);
        if (!(smask instanceof PdfStreamObj maskStream)) {
            return null;
        }
        PdfDict maskDict = maskStream.dict;
        int mw = (int) maskDict.getNumberOr("Width", 0);
        int mh = (int) maskDict.getNumberOr("Height", 0);
        if (mw != width || mh != height) {
            return null;
        }
        byte[] decoded = PdfFilters.decode(maskDict, maskStream.data, resolver);
        int bits = (int) maskDict.getNumberOr("BitsPerComponent", 8);
        if (bits != 8) {
            return null;
        }
        int rowBytes = (width * bits + 7) / 8;
        if (decoded.length < rowBytes * height) {
            return null;
        }
        byte[] alpha = new byte[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                alpha[y * width + x] = decoded[y * rowBytes + x];
            }
        }
        return alpha;
    }

    // ---------------- PNG 封装 ----------------

    private static byte[] writePng(int width, int height, int bits, Space space,
                                   byte[] samples, int rowBytes, byte[] alpha) {
        int colorType;
        byte[] palette = null;
        byte[] raw;
        if (space.indexed()) {
            colorType = 3;
            palette = space.palette();
            raw = samples;
        } else if (space.cmyk()) {
            colorType = 2;
            raw = cmykToRgb(samples, rowBytes, width, height);
            rowBytes = width * 3;
        } else if (space.components() == 1) {
            colorType = alpha == null ? 0 : 4;
            raw = samples;
            if (alpha != null) {
                raw = withAlpha(samples, rowBytes, width, height, alpha, 1);
                rowBytes = width * 2;
            }
        } else {
            colorType = alpha == null ? 2 : 6;
            raw = samples;
            if (alpha != null) {
                raw = withAlpha(samples, rowBytes, width, height, alpha, 3);
                rowBytes = width * 4;
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(PNG_SIGNATURE);
        ByteArrayOutputStream ihdr = new ByteArrayOutputStream();
        writeInt(ihdr, width);
        writeInt(ihdr, height);
        ihdr.write(bits);
        ihdr.write(colorType);
        ihdr.write(0);
        ihdr.write(0);
        ihdr.write(0);
        chunk(out, "IHDR", ihdr.toByteArray());
        if (palette != null) {
            chunk(out, "PLTE", palette);
        }
        chunk(out, "IDAT", deflate(scanlines(raw, rowBytes, height)));
        chunk(out, "IEND", new byte[0]);
        return out.toByteArray();
    }

    /** 补 PNG 的 filter 字节（0 = None）。 */
    private static byte[] scanlines(byte[] raw, int rowBytes, int height) {
        byte[] out = new byte[(rowBytes + 1) * height];
        for (int y = 0; y < height; y++) {
            out[y * (rowBytes + 1)] = 0;
            System.arraycopy(raw, y * rowBytes, out, y * (rowBytes + 1) + 1, rowBytes);
        }
        return out;
    }

    private static byte[] cmykToRgb(byte[] src, int rowBytes, int width, int height) {
        byte[] out = new byte[width * 3 * height];
        for (int y = 0; y < height; y++) {
            int so = y * rowBytes;
            int to = y * width * 3;
            for (int x = 0; x < width; x++) {
                int c = src[so++] & 0xFF;
                int m = src[so++] & 0xFF;
                int yy = src[so++] & 0xFF;
                int k = src[so++] & 0xFF;
                out[to++] = (byte) clamp255(255 - Math.min(255, c + k));
                out[to++] = (byte) clamp255(255 - Math.min(255, m + k));
                out[to++] = (byte) clamp255(255 - Math.min(255, yy + k));
            }
        }
        return out;
    }

    private static byte[] withAlpha(byte[] src, int rowBytes, int width, int height,
                                    byte[] alpha, int components) {
        byte[] out = new byte[width * (components + 1) * height];
        int dst = 0;
        for (int y = 0; y < height; y++) {
            int so = y * rowBytes;
            for (int x = 0; x < width; x++) {
                for (int c = 0; c < components; c++) {
                    out[dst++] = src[so++];
                }
                out[dst++] = alpha[y * width + x];
            }
        }
        return out;
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : Math.min(255, v);
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

    private static void chunk(ByteArrayOutputStream out, String type, byte[] data) {
        writeInt(out, data.length);
        byte[] typeBytes = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        out.writeBytes(typeBytes);
        out.writeBytes(data);
        writeInt(out, crc(typeBytes, data));
    }

    private static int crc(byte[] type, byte[] data) {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(type);
        crc.update(data);
        return (int) crc.getValue();
    }

    private static void writeInt(ByteArrayOutputStream out, int v) {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }

    /** 判断滤镜链里是否含 DCTDecode（输出就是 JPEG）。 */
    static boolean hasJpegFilter(PdfDict img, PdfDict.RefResolver resolver) {
        Object filter = PdfDict.resolve(img.getRaw("Filter"), resolver, 8);
        if (filter instanceof PdfName n) {
            return "DCTDecode".equals(n.name);
        }
        if (filter instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof PdfName n && "DCTDecode".equals(n.name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 判断滤镜链里是否含当前无法解码的编码（JPX = JPEG2000、JBIG2）。 */
    static boolean hasUnsupportedFilter(PdfDict img, PdfDict.RefResolver resolver) {
        Object filter = PdfDict.resolve(img.getRaw("Filter"), resolver, 8);
        List<?> list;
        if (filter instanceof PdfName n) {
            list = List.of(n);
        } else if (filter instanceof List<?> l) {
            list = l;
        } else {
            return false;
        }
        for (Object o : list) {
            if (o instanceof PdfName n && ("JPXDecode".equals(n.name) || "JBIG2Decode".equals(n.name))) {
                return true;
            }
        }
        return false;
    }
}
