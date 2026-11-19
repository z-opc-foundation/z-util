package com.zifang.util.doc.doc;

/** Word 97-2003 二进制（FIB + CLX piece table）文本提取。 */
public final class WordBinaryParser {

    private WordBinaryParser() {
    }

    /** fWhichTblStm：true → "1Table"，false → "0Table"。 */
    public static boolean usesTable1(byte[] wordDoc) {
        return (u16(wordDoc, 0x0A) & 0x0200) != 0;
    }

    /** 提取正文（CP 0..ccpText），保留 \r 段落分隔。 */
    public static String extractText(byte[] wordDoc, byte[] table) {
        int ccpText = u32(wordDoc, 72);
        byte[] clx = readClx(wordDoc, table);
        if (clx == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Piece piece : pieces(clx)) {
            int len = Math.min(piece.cpEnd - piece.cpStart, ccpText - piece.cpStart);
            if (len <= 0) {
                continue;
            }
            if (piece.compressed) {
                int off = piece.fc / 2;
                for (int i = 0; i < len && off + i < wordDoc.length; i++) {
                    sb.append(cp1252(wordDoc[off + i] & 0xFF));
                }
            } else {
                int off = piece.fc;
                for (int i = 0; i < len && off + i * 2 + 1 < wordDoc.length; i++) {
                    sb.append((char) (wordDoc[off + i * 2] & 0xFF | (wordDoc[off + i * 2 + 1] & 0xFF) << 8));
                }
            }
        }
        return sb.toString();
    }

    private static final class Piece {
        int cpStart;
        int cpEnd;
        int fc;
        boolean compressed;
    }

    /** CLX：跳过 Prc（clxt=1），取 Pcdt（clxt=2）里的 PlcPcd。 */
    private static byte[] readClx(byte[] wordDoc, byte[] table) {
        if (table == null) {
            return null;
        }
        int fcClx = u32(wordDoc, 418);
        int lcbClx = u32(wordDoc, 422);
        if (lcbClx <= 0 || fcClx + lcbClx > table.length) {
            return null;
        }
        byte[] clx = new byte[lcbClx];
        System.arraycopy(table, fcClx, clx, 0, lcbClx);
        return clx;
    }

    private static java.util.List<Piece> pieces(byte[] clx) {
        java.util.List<Piece> result = new java.util.ArrayList<>();
        int p = 0;
        while (p < clx.length) {
            int clxt = clx[p] & 0xFF;
            if (clxt == 1) {
                int cb = u16(clx, p + 1);
                p += 3 + cb;
            } else if (clxt == 2) {
                int lcb = u32(clx, p + 1);
                parsePlcPcd(clx, p + 5, lcb, result);
                break;
            } else {
                break;
            }
        }
        return result;
    }

    private static void parsePlcPcd(byte[] clx, int p, int lcb, java.util.List<Piece> out) {
        int n = (lcb - 4) / 12;
        if (n <= 0) {
            return;
        }
        int[] cps = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            cps[i] = u32(clx, p + i * 4);
        }
        int pcdBase = p + (n + 1) * 4;
        for (int i = 0; i < n; i++) {
            Piece piece = new Piece();
            piece.cpStart = cps[i];
            piece.cpEnd = cps[i + 1];
            int pcd = pcdBase + i * 8;
            int fcField = u32(clx, pcd + 2);
            piece.compressed = (fcField & 0x40000000) != 0;
            piece.fc = fcField & 0x3FFFFFFF;
            out.add(piece);
        }
    }

    private static char cp1252(int b) {
        return switch (b) {
            case 0x80 -> '€';
            case 0x91 -> '\u2018';
            case 0x92 -> '\u2019';
            case 0x93 -> '\u201C';
            case 0x94 -> '\u201D';
            case 0x95 -> '•';
            case 0x96 -> '–';
            case 0x97 -> '—';
            case 0x85 -> '…';
            default -> (char) b;
        };
    }

    private static int u16(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8);
    }

    private static int u32(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8)
                | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }
}
