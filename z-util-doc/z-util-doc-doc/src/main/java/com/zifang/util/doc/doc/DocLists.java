package com.zifang.util.doc.doc;

import java.util.HashMap;
import java.util.Map;

/**
 * .doc 列表定义：PlfLst（LSTF + LVL 数组）与 PlfLfo（LFO → lsid）。
 *
 * <p>段落的 ilfo 指向 PlfLfo 里的第 ilfo 个 LFO（1 起），LFO 里的 lsid 再指向 PlfLst 里
 * 同 lsid 的列表定义；该列表的 LVL 数组给出每一级的数字格式 nfc：
 * 0 十进制、1 大写罗马、2 小写罗马、3 大写字母、4 小写字母、23 项目符号、255 无。</p>
 */
final class DocLists {

    private static final int NFC_BULLET = 23;
    private static final int NFC_NONE = 255;

    /** ilfo（1 起）→ 各级别是否有序。 */
    private final Map<Integer, boolean[]> orderedByIlfo;

    private DocLists(Map<Integer, boolean[]> orderedByIlfo) {
        this.orderedByIlfo = orderedByIlfo;
    }

    static DocLists parse(byte[] table, int fcPlfLst, int lcbPlfLst, int fcPlfLfo, int lcbPlfLfo) {
        Map<Integer, boolean[]> orderedByIlfo = new HashMap<>();
        if (table == null) {
            return new DocLists(orderedByIlfo);
        }
        Map<Integer, int[]> nfcByLsid = parseLst(table, fcPlfLst, lcbPlfLst);
        Map<Integer, Integer> lsidByIlfo = parseLfo(table, fcPlfLfo, lcbPlfLfo);
        for (Map.Entry<Integer, Integer> entry : lsidByIlfo.entrySet()) {
            int[] nfc = nfcByLsid.get(entry.getValue());
            if (nfc == null) {
                continue;
            }
            boolean[] ordered = new boolean[Math.max(9, nfc.length)];
            for (int i = 0; i < nfc.length; i++) {
                ordered[i] = nfc[i] != NFC_BULLET && nfc[i] != NFC_NONE;
            }
            orderedByIlfo.put(entry.getKey(), ordered);
        }
        return new DocLists(orderedByIlfo);
    }

    /** 该列表级别是否是有序（编号）列表。 */
    boolean ordered(int ilfo, int ilvl) {
        boolean[] ordered = orderedByIlfo.get(ilfo);
        if (ordered == null) {
            return false;
        }
        int index = Math.max(0, Math.min(ilvl, ordered.length - 1));
        return ordered[index];
    }

    /** PlfLst：cLst + LSTF 数组 + 每列表的 LVL 数组，返回 lsid → 各级别 nfc。 */
    private static Map<Integer, int[]> parseLst(byte[] table, int fc, int lcb) {
        Map<Integer, int[]> result = new HashMap<>();
        if (fc < 0 || lcb <= 0 || fc + lcb > table.length) {
            return result;
        }
        int cLst = u16(table, fc);
        int lsfOffset = fc + 2;
        if (lsfOffset + cLst * 28 > table.length) {
            return result;
        }
        int p = lsfOffset + cLst * 28;
        for (int i = 0; i < cLst && p < table.length; i++) {
            int lsid = u32(table, lsfOffset + i * 28);
            int cLvl = table[p] & 0xFF;
            int[] nfc = new int[Math.max(9, cLvl)];
            int q = p + 1;
            for (int j = 0; j < cLvl && q + 16 <= table.length; j++) {
                nfc[j] = table[q + 5] & 0xFF;
                q = skipLvl(table, q);
            }
            result.put(lsid, nfc);
            p = q;
        }
        return result;
    }

    /** PlfLfo：lfoMac + LFO 数组 + LFOData 偏移数组，返回 ilfo（1 起）→ lsid。 */
    private static Map<Integer, Integer> parseLfo(byte[] table, int fc, int lcb) {
        Map<Integer, Integer> result = new HashMap<>();
        if (fc < 0 || lcb <= 0 || fc + lcb > table.length) {
            return result;
        }
        int lfoMac = u32(table, fc);
        int rgLfo = fc + 4;
        int rgLfoData = rgLfo + lfoMac * 16;
        if (lfoMac <= 0 || lfoMac > 4096 || rgLfoData + lfoMac * 4 > table.length) {
            return result;
        }
        for (int i = 0; i < lfoMac; i++) {
            int lsid = u32(table, rgLfo + i * 16);
            result.put(i + 1, lsid);
        }
        return result;
    }

    /** LVL：LVLF(16) + Xst(cch*2 字节 + 结尾空字符)。 */
    private static int skipLvl(byte[] table, int p) {
        int cch = p + 16 + 2 <= table.length ? u16(table, p + 16) : 0;
        return p + 16 + 2 + cch * 2 + 2;
    }

    private static int u16(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8);
    }

    private static int u32(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8)
                | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }
}
