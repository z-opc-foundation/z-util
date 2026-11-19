package com.zifang.util.doc.pdf;

import com.zifang.util.doc.DocParseException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/** PDF 流过滤器：Flate / LZW / ASCIIHex / ASCII85 / RunLength + PNG 预测器还原。 */
public final class PdfFilters {

    private PdfFilters() {
    }

    /** 按字典 /Filter 顺序解一遍（支持 /DecodeParms 的 Predictor）。 */
    public static byte[] decode(PdfDict dict, byte[] data, PdfDict.RefResolver resolver) {
        List<Object> filters = filterNames(dict, resolver);
        if (filters == null || filters.isEmpty()) {
            return data;
        }
        List<Object> parms = filterParms(dict, resolver);
        for (int i = 0; i < filters.size(); i++) {
            String f = ((PdfName) filters.get(i)).name;
            PdfDict dp = parms != null && i < parms.size() && parms.get(i) instanceof PdfDict d ? d : null;
            data = apply(f, data, dp);
        }
        return data;
    }

    private static List<Object> filterNames(PdfDict dict, PdfDict.RefResolver resolver) {
        Object v = dict.getRaw("Filter");
        if (v instanceof PdfRef r) {
            v = resolver.resolve(r);
        }
        if (v instanceof PdfName n) {
            return List.of(n);
        }
        return v instanceof List<?> ? PdfDict.resolveList(v, resolver) : null;
    }

    private static List<Object> filterParms(PdfDict dict, PdfDict.RefResolver resolver) {
        Object v = dict.getRaw("DecodeParms");
        if (v instanceof PdfRef r) {
            v = resolver.resolve(r);
        }
        if (v instanceof PdfDict d) {
            return List.of(d);
        }
        return v instanceof List<?> ? PdfDict.resolveList(v, resolver) : null;
    }

    public static byte[] apply(String filter, byte[] data, PdfDict parms) {
        byte[] result = switch (filter) {
            case "FlateDecode" -> flate(data);
            case "LZWDecode" -> lzw(data);
            case "ASCIIHexDecode" -> asciiHex(data);
            case "ASCII85Decode" -> ascii85(data);
            case "RunLengthDecode" -> runLength(data);
            default -> data;
        };
        if (parms != null) {
            int predictor = (int) parms.getNumberOr("Predictor", 1);
            if (predictor >= 10) {
                int colors = (int) parms.getNumberOr("Colors", 1);
                int bpc = (int) parms.getNumberOr("BitsPerComponent", 8);
                int columns = (int) parms.getNumberOr("Columns", 1);
                result = undoPngPredictor(result, colors, bpc, columns);
            }
        }
        return result;
    }

    private static byte[] flate(byte[] data) {
        for (boolean raw : new boolean[]{false, true}) {
            Inflater inflater = new Inflater(raw);
            try {
                inflater.setInput(data);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                while (!inflater.finished()) {
                    int n = inflater.inflate(buf);
                    if (n == 0) {
                        if (inflater.needsInput() || inflater.needsDictionary()) {
                            break;
                        }
                    } else {
                        out.write(buf, 0, n);
                    }
                }
                return out.toByteArray();
            } catch (DataFormatException e) {
                // 试 raw 模式
            } finally {
                inflater.end();
            }
        }
        throw new DocParseException("FlateDecode failed");
    }

    private static byte[] lzw(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[][] table = new byte[4096][];
        for (int i = 0; i < 256; i++) {
            table[i] = new byte[]{(byte) i};
        }
        int next = 258;
        int codeLen = 9;
        int prev = -1;
        int bits = 0;
        int cur = 0;
        int pos = 0;
        while (true) {
            while (bits < codeLen && pos < data.length) {
                cur = (cur << 8) | (data[pos++] & 0xFF);
                bits += 8;
            }
            if (bits < codeLen) {
                break;
            }
            int code = (cur >> (bits - codeLen)) & ((1 << codeLen) - 1);
            bits -= codeLen;
            if (code == 256) {
                next = 258;
                codeLen = 9;
                prev = -1;
                continue;
            }
            if (code == 257) {
                break;
            }
            byte[] entry;
            if (code < next && table[code] != null) {
                entry = table[code];
            } else if (prev >= 0) {
                byte[] p = table[prev];
                entry = new byte[p.length + 1];
                System.arraycopy(p, 0, entry, 0, p.length);
                entry[p.length] = p[0];
            } else {
                break;
            }
            out.write(entry, 0, entry.length);
            if (prev >= 0 && next < 4096) {
                byte[] p = table[prev];
                byte[] ne = new byte[p.length + 1];
                System.arraycopy(p, 0, ne, 0, p.length);
                ne[p.length] = entry[0];
                table[next++] = ne;
                if (next == 511 || next == 1023 || next == 2047) {
                    codeLen++;
                }
            }
            prev = code;
        }
        return out.toByteArray();
    }

    private static byte[] asciiHex(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int hi = -1;
        for (byte b : data) {
            int v = hexVal((char) b);
            if (v < 0) {
                if (b == '>') {
                    break;
                }
                continue;
            }
            if (hi < 0) {
                hi = v;
            } else {
                out.write((hi << 4) | v);
                hi = -1;
            }
        }
        if (hi >= 0) {
            out.write(hi << 4);
        }
        return out.toByteArray();
    }

    private static int hexVal(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return -1;
    }

    private static byte[] ascii85(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int[] group = new int[5];
        int count = 0;
        boolean end = false;
        for (int i = 0; i < data.length && !end; i++) {
            char c = (char) (data[i] & 0xFF);
            if (c == 'z' && count == 0) {
                for (int k = 0; k < 4; k++) {
                    out.write(0);
                }
                continue;
            }
            if (c == '~') {
                end = true;
                break;
            }
            if (c < '!' || c > 'u') {
                continue;
            }
            group[count++] = c - '!';
            if (count == 5) {
                writeGroup(out, group, 5);
                count = 0;
            }
        }
        if (count > 0) {
            for (int k = count; k < 5; k++) {
                group[k] = 84;
            }
            writeGroup(out, group, count);
        }
        return out.toByteArray();
    }

    private static void writeGroup(ByteArrayOutputStream out, int[] group, int count) {
        long val = 0;
        for (int g : group) {
            val = val * 85 + g;
        }
        byte[] four = {(byte) (val >> 24), (byte) (val >> 16), (byte) (val >> 8), (byte) val};
        out.write(four, 0, count - 1);
    }

    private static byte[] runLength(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        while (i < data.length) {
            int len = data[i++] & 0xFF;
            if (len == 128) {
                break;
            }
            if (len < 128) {
                int n = len + 1;
                out.write(data, i, Math.min(n, data.length - i));
                i += n;
            } else {
                byte b = i < data.length ? data[i++] : 0;
                for (int k = 0; k < 257 - len; k++) {
                    out.write(b);
                }
            }
        }
        return out.toByteArray();
    }

    /** PNG 预测器（Predictor >= 10）还原，返回解完行的字节。 */
    public static byte[] undoPngPredictor(byte[] data, int colors, int bpc, int columns) {
        int bpp = Math.max(1, (colors * bpc + 7) / 8);
        int rowLen = (columns * colors * bpc + 7) / 8;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] prev = new byte[rowLen];
        int pos = 0;
        while (pos + 1 <= data.length) {
            int filter = data[pos++] & 0xFF;
            if (pos + rowLen > data.length) {
                break;
            }
            byte[] row = new byte[rowLen];
            System.arraycopy(data, pos, row, 0, rowLen);
            pos += rowLen;
            for (int i = 0; i < rowLen; i++) {
                int a = i >= bpp ? row[i - bpp] & 0xFF : 0;
                int b = prev[i] & 0xFF;
                int c = i >= bpp ? prev[i - bpp] & 0xFF : 0;
                switch (filter) {
                    case 1 -> row[i] = (byte) ((row[i] & 0xFF) + a);
                    case 2 -> row[i] = (byte) ((row[i] & 0xFF) + b);
                    case 3 -> row[i] = (byte) ((row[i] & 0xFF) + (a + b) / 2);
                    case 4 -> {
                        int p = a + b - c;
                        int pa = Math.abs(p - a);
                        int pb = Math.abs(p - b);
                        int pc = Math.abs(p - c);
                        int pred = (pa <= pb && pa <= pc) ? a : (pb <= pc ? b : c);
                        row[i] = (byte) ((row[i] & 0xFF) + pred);
                    }
                    default -> {
                    }
                }
            }
            out.write(row, 0, rowLen);
            prev = row;
        }
        return out.toByteArray();
    }

    /** PDF 文本字符串解码：FE FF 开头按 UTF-16BE，否则 Cp1252。 */
    public static String decodeString(byte[] bytes) {
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }
}
