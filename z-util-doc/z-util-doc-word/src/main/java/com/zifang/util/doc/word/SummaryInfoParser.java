package com.zifang.util.doc.doc;

import com.zifang.util.doc.DocMeta;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** \u0005SummaryInformation 属性集解析：title/author/subject/keywords/时间。 */
public final class SummaryInfoParser {

    private SummaryInfoParser() {
    }

    public static void parse(byte[] s, DocMeta meta) {
        if (s == null || s.length < 48) {
            return;
        }
        int sectionOffset = u32(s, 44);
        if (sectionOffset <= 0 || sectionOffset + 8 > s.length) {
            return;
        }
        int cProps = u32(s, sectionOffset + 4);
        int codepage = 1252;
        for (int i = 0; i < cProps; i++) {
            int pair = sectionOffset + 8 + i * 8;
            if (pair + 8 > s.length) {
                return;
            }
            int id = u32(s, pair);
            int valueOff = sectionOffset + u32(s, pair + 4);
            if (id == 1 && valueOff + 4 <= s.length && u16(s, valueOff) == 2) {
                codepage = u16(s, valueOff + 2);
            }
        }
        for (int i = 0; i < cProps; i++) {
            int pair = sectionOffset + 8 + i * 8;
            int id = u32(s, pair);
            int valueOff = sectionOffset + u32(s, pair + 4);
            if (id == 1 || valueOff + 2 > s.length) {
                continue;
            }
            int type = u16(s, valueOff);
            switch (id) {
                case 2 -> meta.setTitle(lpString(s, valueOff, type, codepage));
                case 3 -> meta.setSubject(lpString(s, valueOff, type, codepage));
                case 4 -> meta.setAuthor(lpString(s, valueOff, type, codepage));
                case 5 -> meta.setKeywords(lpString(s, valueOff, type, codepage));
                case 6 -> meta.putExtra("comments", lpString(s, valueOff, type, codepage));
                case 12 -> meta.setCreationDate(filetime(s, valueOff));
                case 13 -> meta.setModDate(filetime(s, valueOff));
                default -> {
                }
            }
        }
    }

    /** 序列化 PROPVARIANT：type(2) 后紧跟值，无 padding。 */
    private static String lpString(byte[] s, int off, int type, int codepage) {
        if (type == 31) { // VT_LPWSTR
            if (off + 6 > s.length) {
                return null;
            }
            int chars = u32(s, off + 2);
            int end = Math.min(off + 6 + chars * 2, s.length);
            return new String(s, off + 6, end - off - 6, StandardCharsets.UTF_16LE).replace("\u0000", "");
        }
        if (type == 30) { // VT_LPSTR
            if (off + 6 > s.length) {
                return null;
            }
            int len = u32(s, off + 2);
            int end = Math.min(off + 6 + len, s.length);
            return new String(s, off + 6, end - off - 6,
                    codepage == 1200 ? StandardCharsets.UTF_16LE : StandardCharsets.ISO_8859_1).replace("\u0000", "");
        }
        return null;
    }

    private static OffsetDateTime filetime(byte[] s, int off) {
        if (off + 10 > s.length || u16(s, off) != 64) {
            return null;
        }
        long ft = (u32(s, off + 2) & 0xFFFFFFFFL) | ((long) u32(s, off + 6) << 32);
        long millis = ft / 10_000 - 11644473600000L;
        return OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), ZoneOffset.UTC);
    }

    private static int u16(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8);
    }

    private static int u32(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8)
                | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }
}
