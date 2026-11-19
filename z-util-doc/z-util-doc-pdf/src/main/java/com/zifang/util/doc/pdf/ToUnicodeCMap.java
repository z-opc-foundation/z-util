package com.zifang.util.doc.pdf;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** ToUnicode CMap：bfchar / bfrange 两段式解析，code → unicode 字符串。 */
public final class ToUnicodeCMap {

    private final Map<Integer, String> map = new HashMap<>();

    public static ToUnicodeCMap parse(byte[] cmapData) {
        ToUnicodeCMap result = new ToUnicodeCMap();
        if (cmapData == null) {
            return result;
        }
        String text = new String(cmapData, StandardCharsets.ISO_8859_1);
        parseBfchar(result, text);
        parseBfrange(result, text);
        return result;
    }

    private static void parseBfchar(ToUnicodeCMap result, String text) {
        int section = text.indexOf("beginbfchar");
        while (section >= 0) {
            int end = text.indexOf("endbfchar", section);
            if (end < 0) {
                end = text.length();
            }
            String body = text.substring(section + 11, end);
            int p = 0;
            while (true) {
                int src = body.indexOf('<', p);
                if (src < 0) {
                    break;
                }
                int srcEnd = body.indexOf('>', src);
                int dst = body.indexOf('<', srcEnd + 1);
                if (srcEnd < 0 || dst < 0) {
                    break;
                }
                int dstEnd = body.indexOf('>', dst);
                if (dstEnd < 0) {
                    break;
                }
                int code = (int) parseHex(body.substring(src + 1, srcEnd));
                result.map.put(code, hexToUtf16(body.substring(dst + 1, dstEnd)));
                p = dstEnd + 1;
            }
            section = text.indexOf("beginbfchar", end);
        }
    }

    private static void parseBfrange(ToUnicodeCMap result, String text) {
        int section = text.indexOf("beginbfrange");
        while (section >= 0) {
            int end = text.indexOf("endbfrange", section);
            if (end < 0) {
                end = text.length();
            }
            String body = text.substring(section + 12, end);
            int p = 0;
            while (true) {
                int lo = body.indexOf('<', p);
                if (lo < 0) {
                    break;
                }
                int loEnd = body.indexOf('>', lo);
                int hi = body.indexOf('<', loEnd + 1);
                int hiEnd = body.indexOf('>', hi + 1);
                if (loEnd < 0 || hi < 0 || hiEnd < 0) {
                    break;
                }
                long loVal = parseHex(body.substring(lo + 1, loEnd));
                long hiVal = parseHex(body.substring(hi + 1, hiEnd));
                int next = body.indexOf('<', hiEnd + 1);
                if (next < 0) {
                    break;
                }
                // 目标可能是单个 <hex> 也可能是 [ <h1> <h2> ... ]
                boolean arrayForm = body.indexOf('[', hiEnd + 1) >= 0
                        && body.indexOf('[', hiEnd + 1) < next;
                if (arrayForm) {
                    int arrStart = body.indexOf('[', hiEnd + 1);
                    int arrEnd = body.indexOf(']', arrStart);
                    if (arrEnd < 0) {
                        break;
                    }
                    String arr = body.substring(arrStart + 1, arrEnd);
                    int q = 0;
                    long code = loVal;
                    while (code <= hiVal) {
                        int s = arr.indexOf('<', q);
                        int sEnd = arr.indexOf('>', s);
                        if (s < 0 || sEnd < 0) {
                            break;
                        }
                        result.map.put((int) code, hexToUtf16(arr.substring(s + 1, sEnd)));
                        q = sEnd + 1;
                        code++;
                    }
                    p = arrEnd + 1;
                } else {
                    int dstEnd = body.indexOf('>', next);
                    if (dstEnd < 0) {
                        break;
                    }
                    long dstBase = parseHex(body.substring(next + 1, dstEnd));
                    int dstLen = body.substring(next + 1, dstEnd).length() / 2;
                    for (long code = loVal; code <= hiVal && code - loVal < 65536; code++) {
                        result.map.put((int) code, utf16For((int) (dstBase + (code - loVal)), dstLen));
                    }
                    p = dstEnd + 1;
                }
            }
            section = text.indexOf("beginbfrange", end);
        }
    }

    private static long parseHex(String hex) {
        if (hex.isEmpty()) {
            return 0;
        }
        return Long.parseLong(hex, 16);
    }

    private static String hexToUtf16(String hex) {
        if (hex.length() % 2 != 0) {
            hex = hex + "0";
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return new String(bytes, StandardCharsets.UTF_16BE);
    }

    private static String utf16For(int codePoint, int byteLen) {
        if (byteLen == 2 && codePoint >= 0 && codePoint <= 0xFFFF) {
            return String.valueOf((char) codePoint);
        }
        StringBuilder sb = new StringBuilder();
        if (byteLen >= 4) {
            // 高低各两字节
            int hi = (codePoint >> 16) & 0xFFFF;
            int lo = codePoint & 0xFFFF;
            if (hi > 0) {
                sb.append((char) hi);
            }
            sb.append((char) lo);
            return sb.toString();
        }
        if (codePoint >= 0) {
            sb.append((char) codePoint);
        }
        return sb.toString();
    }

    public String lookup(int code) {
        return map.get(code);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
