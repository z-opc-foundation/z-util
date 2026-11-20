package com.zifang.util.doc.word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * .doc 列表定义与计数：PlcfLst（cLst + LSTF 数组 + 各列表 LVL 数组）与 PlcfLfo（LFO + LFOData 覆盖）。
 *
 * <p>段落属性里的 ilfo 指向 PlcfLfo 里第 ilfo 个 LFO（1 起），LFO 的 lsid 对应 PlcfLst 里
 * 同 lsid 的 LSTF；简单列表（fSimpleList）只有 1 级，多级列表固定 9 级。每级 LVL =
 * LVLF(28 字节) + papx/chpx grpprl + Xst 编号文本，Xst 里 \u0000-\u0008 是对应层级计数器的
 * 占位符；LFOData 的 LFOLVL 可为某级整套换格式（fFormatting）或只换起始值（fStartAt）。
 * 按段落顺序推进每个 ilfo 的计数器：进入更浅层级时重置更深层级（该级 fNoRestart 除外），
 * 占位符按 nfc 格式化成十进制/罗马/字母/序数词。</p>
 */
final class DocLists {

    private static final int NFC_BULLET = 23;
    private static final int NFC_NONE = 255;

    private static final int LSTF_SIZE = 28;
    private static final int LVLF_SIZE = 28;
    /** LSTF 的 flags（末 2 字节）：bit0 fSimpleList。 */
    private static final int LSTF_SIMPLE = 0x0001;
    /** LFOLVL 的 flags：低 4 位 ilvl，bit4 fFormatting，bit5 fStartAt。 */
    private static final int LFOLVL_LVL = 0x0F;
    private static final int LFOLVL_FORMATTING = 0x10;
    private static final int LFOLVL_STARTAT = 0x20;
    /** LVLF.info：bit2 fLegal（编号转十进制显示），bit3 fNoRestart。 */
    private static final int INFO_LEGAL = 0x04;
    private static final int INFO_NORESTART = 0x08;

    /** 渲染一个列表段落后的标记：ordered = 是否编号列表，label = 注入的编号/符号文本。 */
    record Marker(boolean ordered, String label) {
    }

    /** 单层列表定义：编号格式、编号文本（\u0000-\u0008 占位）、起始值与重启标志。 */
    private record Level(int nfc, String text, int start, boolean noRestart, boolean legal) {
    }

    /** ilfo（1 起）→ 覆盖应用后的各级定义。 */
    private final Map<Integer, List<Level>> levelsByIlfo;
    /** ilfo → 各级计数器（0 表示还没写过，首段取 start）。 */
    private final Map<Integer, int[]> counters = new HashMap<>();

    private DocLists(Map<Integer, List<Level>> levelsByIlfo) {
        this.levelsByIlfo = levelsByIlfo;
    }

    static DocLists parse(byte[] table, int fcPlfLst, int lcbPlfLst, int fcPlfLfo, int lcbPlfLfo) {
        Map<Integer, List<Level>> levelsByIlfo = new HashMap<>();
        if (table == null) {
            return new DocLists(levelsByIlfo);
        }
        Map<Integer, List<Level>> levelsByLsid = parseLst(table, fcPlfLst, bound(table.length, fcPlfLfo));
        if (levelsByLsid.isEmpty()) {
            return new DocLists(levelsByIlfo);
        }
        applyOverrides(table, fcPlfLfo, levelsByLsid, levelsByIlfo);
        // 没有任何覆盖的 lsid 也按 ilfo 暴露（多个 ilfo 可共享同一 lsid）
        Map<Integer, Integer> lsidByIlfo = readLfoLsids(table, fcPlfLfo);
        for (Map.Entry<Integer, Integer> entry : lsidByIlfo.entrySet()) {
            levelsByIlfo.computeIfAbsent(entry.getKey(), k -> {
                List<Level> levels = levelsByLsid.get(entry.getValue());
                return levels == null ? List.of() : List.copyOf(levels);
            });
        }
        return new DocLists(levelsByIlfo);
    }

    /**
     * 推进计数并渲染当前段落（ilfo, ilvl）的编号文本。必须在段落遍历里按文档顺序调用，
     * 表格内段落与页眉页脚不计数。
     */
    Marker marker(int ilfo, int ilvl) {
        List<Level> levels = levelsByIlfo.get(ilfo);
        if (levels == null || levels.isEmpty()) {
            return new Marker(false, null);
        }
        int idx = Math.max(0, Math.min(ilvl, levels.size() - 1));
        Level level = levels.get(idx);
        if (level.nfc() == NFC_BULLET || level.nfc() == NFC_NONE) {
            String glyph = render(level.text(), idx, levels, null).strip();
            return new Marker(false, glyph.isEmpty() ? null : glyph);
        }
        int[] counter = counters.computeIfAbsent(ilfo, k -> new int[9]);
        for (int i = idx + 1; i < counter.length; i++) {
            // 更浅层级重置更深层级（fNoRestart 的级别不重置）
            if (i < levels.size() && levels.get(i).noRestart()) {
                continue;
            }
            counter[i] = 0;
        }
        if (counter[idx] == 0) {
            counter[idx] = Math.max(1, level.start());
        } else {
            counter[idx]++;
        }
        String text = render(level.text(), idx, levels, counter);
        return new Marker(true, text.isEmpty() ? null : text);
    }

    /** 编号文本：占位符换成对应层级的计数（未写过时用该级 start），无占位符照抄。 */
    private static String render(String text, int ilvl, List<Level> levels, int[] counter) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u0000' && c <= '\u0008') {
                int idx = c;
                Level ref = levels.get(Math.min(idx, levels.size() - 1));
                int nfc = ref.legal() ? 0 : ref.nfc();
                int value = counter != null && idx < counter.length && counter[idx] > 0
                        ? counter[idx]
                        : Math.max(1, ref.start());
                sb.append(formatNumber(value, nfc));
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /** nfc → 文本：0 十进制、1/2 罗马数字、3/4 字母、5 序数词；项目符号与未知格式兜底十进制。 */
    static String formatNumber(int value, int nfc) {
        if (value <= 0) {
            return Integer.toString(Math.max(0, value));
        }
        return switch (nfc) {
            case 0 -> Integer.toString(value);
            case 1 -> roman(value);
            case 2 -> roman(value).toLowerCase();
            case 3 -> letters(value);
            case 4 -> letters(value).toLowerCase();
            case 5 -> ordinal(value);
            case NFC_BULLET, NFC_NONE -> "";
            default -> Integer.toString(value);
        };
    }

    private static String roman(int value) {
        int[] numbers = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] glyphs = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        int rest = value;
        for (int i = 0; i < numbers.length; i++) {
            while (rest >= numbers[i]) {
                sb.append(glyphs[i]);
                rest -= numbers[i];
            }
        }
        return sb.toString();
    }

    /** 1=A … 26=Z，27=AA。 */
    private static String letters(int value) {
        StringBuilder sb = new StringBuilder();
        int rest = value;
        while (rest > 0) {
            rest--;
            sb.insert(0, (char) ('A' + rest % 26));
            rest /= 26;
        }
        return sb.toString();
    }

    private static String ordinal(int value) {
        int mod100 = value % 100;
        if (mod100 >= 11 && mod100 <= 13) {
            return value + "th";
        }
        return switch (value % 10) {
            case 1 -> value + "st";
            case 2 -> value + "nd";
            case 3 -> value + "rd";
            default -> value + "th";
        };
    }

    /** LVL 数据区边界：PlcfLfo 的起点（文档里紧挨着），没有就到表流末尾。 */
    private static int bound(int tableLength, int fcPlfLfo) {
        return fcPlfLfo > 0 && fcPlfLfo <= tableLength ? fcPlfLfo : tableLength;
    }

    /**
     * PlcfLst：cLst + LSTF 数组（各 28 字节）+ 所有列表的 LVL 依序拼接；
     * 每列表贡献 cLvl 级（简单列表 1 级，多级 9 级）。返回 lsid → 各级定义。
     */
    private static Map<Integer, List<Level>> parseLst(byte[] table, int fc, int bound) {
        Map<Integer, List<Level>> result = new HashMap<>();
        if (fc <= 0 || fc + 2 > bound) {
            return result;
        }
        int cLst = u16(table, fc);
        if (cLst <= 0 || cLst > 4096) {
            return result;
        }
        int p = fc + 2;
        int[] lsids = new int[cLst];
        boolean[] simple = new boolean[cLst];
        for (int i = 0; i < cLst; i++) {
            if (p + LSTF_SIZE > bound) {
                return result;
            }
            lsids[i] = u32(table, p);
            simple[i] = (u16(table, p + LSTF_SIZE - 2) & LSTF_SIMPLE) != 0;
            p += LSTF_SIZE;
        }
        for (int i = 0; i < cLst; i++) {
            int cLvl = simple[i] ? 1 : 9;
            List<Level> levels = new ArrayList<>();
            for (int j = 0; j < cLvl && p < bound; j++) {
                Map.Entry<Integer, Level> read = readLevel(table, p, bound);
                if (read == null) {
                    break;
                }
                levels.add(read.getValue());
                p = read.getKey();
            }
            result.put(lsids[i], levels);
        }
        return result;
    }

    /** LVL：LVLF(28) + papx grpprl + chpx grpprl + Xst，返回定义与结束偏移。 */
    private static Map.Entry<Integer, Level> readLevel(byte[] table, int p, int bound) {
        if (p + LVLF_SIZE > bound) {
            return null;
        }
        int iStartAt = u32(table, p);
        int nfc = table[p + 4] & 0xFF;
        int info = table[p + 5] & 0xFF;
        int cbGrpprlChpx = table[p + 24] & 0xFF;
        int cbGrpprlPapx = table[p + 25] & 0xFF;
        int q = p + LVLF_SIZE + cbGrpprlPapx + cbGrpprlChpx;
        if (q + 2 > bound) {
            return null;
        }
        int cch = u16(table, q);
        if (cch < 0 || cch > 1024 || q + 2 + cch * 2 > bound) {
            return null;
        }
        StringBuilder xst = new StringBuilder();
        for (int i = 0; i < cch; i++) {
            xst.append((char) u16(table, q + 2 + i * 2));
        }
        Level level = new Level(nfc, xst.toString(), iStartAt,
                (info & INFO_NORESTART) != 0, (info & INFO_LEGAL) != 0);
        return Map.entry(q + 2 + cch * 2, level);
    }

    /** PlcfLfo 头部：lfoMac + LFO 数组（各 16 字节），返回 ilfo（1 起）→ lsid。 */
    private static Map<Integer, Integer> readLfoLsids(byte[] table, int fc) {
        Map<Integer, Integer> result = new HashMap<>();
        int lfoMac = lfoMac(table, fc);
        if (lfoMac <= 0) {
            return result;
        }
        int rgLfo = fc + 4;
        for (int i = 0; i < lfoMac; i++) {
            int at = rgLfo + i * 16;
            if (at + 16 > table.length) {
                break;
            }
            result.put(i + 1, u32(table, at));
        }
        return result;
    }

    /** PlcfLfo 的 LFOData 数组：cp(4) + clfolvl 个 LFOLVL，把覆盖应用到各 ilfo。 */
    private static void applyOverrides(byte[] table, int fc, Map<Integer, List<Level>> byLsid,
                                       Map<Integer, List<Level>> byIlfo) {
        int lfoMac = lfoMac(table, fc);
        if (lfoMac <= 0 || fc <= 0) {
            return;
        }
        int q = fc + 4 + lfoMac * 16;
        for (int i = 0; i < lfoMac; i++) {
            if (q + 4 > table.length) {
                return;
            }
            q += 4; // cp
            int clfolvl = table[fc + 4 + i * 16 + 12] & 0xFF;
            List<Level> levels = new ArrayList<>();
            List<Level> base = byLsid.get(u32(table, fc + 4 + i * 16));
            if (base != null) {
                levels.addAll(base);
            }
            for (int k = 0; k < clfolvl && q + 8 <= table.length; k++) {
                int iStartAt = u32(table, q);
                int flags = u32(table, q + 4);
                int lvl = flags & LFOLVL_LVL;
                boolean formatting = (flags & LFOLVL_FORMATTING) != 0;
                boolean startAt = (flags & LFOLVL_STARTAT) != 0;
                q += 8;
                Level override = null;
                if (formatting) {
                    Map.Entry<Integer, Level> read = readLevel(table, q, table.length);
                    if (read == null) {
                        return;
                    }
                    override = read.getValue();
                    q = read.getKey();
                }
                if (lvl < levels.size()) {
                    Level current = levels.get(lvl);
                    Level applied = override != null
                            ? new Level(override.nfc(), override.text(),
                                    startAt ? iStartAt : override.start(),
                                    override.noRestart(), override.legal())
                            : new Level(current.nfc(), current.text(),
                                    startAt ? iStartAt : current.start(),
                                    current.noRestart(), current.legal());
                    levels.set(lvl, applied);
                }
            }
            if (!levels.isEmpty()) {
                byIlfo.put(i + 1, List.copyOf(levels));
            }
        }
    }

    private static int lfoMac(byte[] table, int fc) {
        if (fc <= 0 || fc + 4 > table.length) {
            return 0;
        }
        int lfoMac = u32(table, fc);
        return lfoMac > 0 && lfoMac <= 4096 ? lfoMac : 0;
    }

    private static int u16(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8);
    }

    private static int u32(byte[] b, int off) {
        return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8)
                | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
    }
}
