package com.zifang.util.doc.doc;

import com.zifang.util.doc.DocParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Word 97-2003 二进制（.doc）核心解析：FIB + CLX piece table + CHPX 字符格式 + 页眉页脚。
 * FIB 偏移已用真实 Word 文件校验（fibRgLw97 基址 64：ccpText@76, ccpFtn@80, ccpHdd@84,
 * ccpAtn@92, ccpEdn@96；fcPlcfhdd@242, fcPlcfbteChpx@250, fcClx@418）。
 */
public final class WordBinaryParser {

    private WordBinaryParser() {
    }

    /** 版本校验：只支持 Word 97-2003（nFib >= 193）；更老的 Word 6/95 是另一套 FIB 布局。 */
    public static void checkVersion(byte[] wordDoc) {
        int nFib = u16(wordDoc, 2);
        if (nFib < 193) {
            throw new DocParseException("Word 二进制版本过旧（nFib=" + nFib + "），仅支持 Word 97-2003 及以后");
        }
    }

    /** fWhichTblStm：true → "1Table"，false → "0Table"。 */
    public static boolean usesTable1(byte[] wordDoc) {
        return (u16(wordDoc, 0x0A) & 0x0200) != 0;
    }

    /** 列表定义：PlfLst@738, PlfLfo@746。 */
    public static DocLists lists(byte[] wordDoc, byte[] table) {
        return DocLists.parse(table, u32(wordDoc, 738), u32(wordDoc, 742),
                u32(wordDoc, 746), u32(wordDoc, 750));
    }

    /** 绘图数据（OfficeArt）在表流里的位置：fcDggInfo@554, lcbDggInfo@558。 */
    public static int fcDggInfo(byte[] wordDoc) {
        return u32(wordDoc, 554);
    }

    public static int lcbDggInfo(byte[] wordDoc) {
        return u32(wordDoc, 558);
    }

    /** fibRgLw97 各子文档字数：main@76, footnote@80, header@84, macro@88, annotation@92, endnote@96。 */
    public static int ccpText(byte[] wordDoc) {
        return u32(wordDoc, 76);
    }

    public static int ccpFtn(byte[] wordDoc) {
        return u32(wordDoc, 80);
    }

    public static int ccpHdd(byte[] wordDoc) {
        return u32(wordDoc, 84);
    }

    public static int ccpAtn(byte[] wordDoc) {
        return u32(wordDoc, 92);
    }

    public static int ccpEdn(byte[] wordDoc) {
        return u32(wordDoc, 96);
    }

    // ---------- piece table ----------

    static final class Piece {
        int cpStart;
        int cpEnd;
        int fc;
        boolean compressed;
    }

    /** CLX → PlcPcd → pieces；无 CLX 时返回空表。 */
    static List<Piece> pieces(byte[] wordDoc, byte[] table) {
        List<Piece> result = new ArrayList<>();
        if (table == null) {
            return result;
        }
        int fcClx = u32(wordDoc, 418);
        int lcbClx = u32(wordDoc, 422);
        if (lcbClx <= 0 || fcClx < 0 || fcClx + lcbClx > table.length) {
            return result;
        }
        int p = fcClx;
        int end = fcClx + lcbClx;
        while (p < end) {
            int clxt = table[p] & 0xFF;
            if (clxt == 1) {
                p += 3 + u16(table, p + 1);
            } else if (clxt == 2) {
                int lcb = u32(table, p + 1);
                parsePlcPcd(table, p + 5, lcb, result);
                break;
            } else {
                break;
            }
        }
        return result;
    }

    private static void parsePlcPcd(byte[] table, int p, int lcb, List<Piece> out) {
        int n = (lcb - 4) / 12;
        if (n <= 0) {
            return;
        }
        int[] cps = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            cps[i] = u32(table, p + i * 4);
        }
        int pcdBase = p + (n + 1) * 4;
        for (int i = 0; i < n; i++) {
            Piece piece = new Piece();
            piece.cpStart = cps[i];
            piece.cpEnd = cps[i + 1];
            int fcField = u32(table, pcdBase + i * 8 + 2);
            piece.compressed = (fcField & 0x40000000) != 0;
            piece.fc = fcField & 0x3FFFFFFF;
            out.add(piece);
        }
    }

    /** CP → WordDocument 流内字节偏移。 */
    private static int cpToFc(List<Piece> pieces, int cp) {
        for (Piece piece : pieces) {
            if (cp >= piece.cpStart && cp < piece.cpEnd) {
                return piece.compressed ? piece.fc / 2 + (cp - piece.cpStart) : piece.fc + (cp - piece.cpStart) * 2;
            }
        }
        return -1;
    }

    /** 提取 CP 区间文本（[cpStart, cpEnd)），保留 \r 段落分隔；越界自动钳制。 */
    public static String extractRange(byte[] wordDoc, byte[] table, int cpStart, int cpEnd) {
        StringBuilder sb = new StringBuilder();
        for (Piece piece : pieces(wordDoc, table)) {
            int from = Math.max(piece.cpStart, cpStart);
            int to = Math.min(piece.cpEnd, cpEnd);
            if (from >= to) {
                continue;
            }
            if (piece.compressed) {
                int off = piece.fc / 2 + (from - piece.cpStart);
                for (int i = 0; i < to - from && off + i < wordDoc.length; i++) {
                    sb.append(cp1252(wordDoc[off + i] & 0xFF));
                }
            } else {
                int off = piece.fc + (from - piece.cpStart) * 2;
                for (int i = 0; i < to - from && off + i * 2 + 1 < wordDoc.length; i++) {
                    sb.append((char) (wordDoc[off + i * 2] & 0xFF | (wordDoc[off + i * 2 + 1] & 0xFF) << 8));
                }
            }
        }
        return sb.toString();
    }

    /** 提取正文（CP 0..ccpText），保留 \r 段落分隔。 */
    public static String extractText(byte[] wordDoc, byte[] table) {
        return extractRange(wordDoc, table, 0, ccpText(wordDoc));
    }

    // ---------- CHPX 字符格式 ----------

    /** 字符 run：CP 区间 + 加粗/斜体/删除线；picOffset >= 0 表示该 run 是图片（指向 Data 流）。 */
    public record CharRun(int cpStart, int cpEnd, boolean bold, boolean italic, boolean strike,
                          int picOffset) {
    }

    /**
     * 正文（CP 0..cpEnd）的字符 run 列表：plcfbteChpx → FKP 页 → CHPX grpprl sprm。
     * 无格式信息时返回覆盖全文的单一无格式 run。
     */
    public static List<CharRun> charRuns(byte[] wordDoc, byte[] table, int cpEnd) {
        List<CharRun> out = new ArrayList<>();
        if (table == null || cpEnd <= 0) {
            return out;
        }
        List<Piece> pieceList = pieces(wordDoc, table);
        int fcBte = u32(wordDoc, 250);
        int lcbBte = u32(wordDoc, 254);
        if (lcbBte < 8 || fcBte + lcbBte > table.length) {
            return out;
        }
        int n = (lcbBte - 4) / 8;
        for (int page = 0; page < n; page++) {
            int pn = u32(table, fcBte + (n + 1) * 4 + page * 4) & 0x3FFFFF;
            byte[] fkp = fkpPage(wordDoc, pn);
            if (fkp == null) {
                continue;
            }
            int crun = fkp[511] & 0xFF;
            if (crun <= 0 || crun > 127) {
                continue;
            }
            for (int i = 0; i < crun; i++) {
                int fcStart = u32(fkp, i * 4);
                int fcEnd = u32(fkp, (i + 1) * 4);
                int rgb = fkp[4 * (crun + 1) + i] & 0xFF;
                boolean bold = false;
                boolean italic = false;
                boolean strike = false;
                int picOffset = -1;
                if (rgb != 0) {
                    int chpx = rgb * 2;
                    int cb = fkp[chpx] & 0xFF;
                    int grpprlEnd = Math.min(chpx + 1 + cb, 511);
                    for (int q = chpx + 1; q + 2 <= grpprlEnd; ) {
                        int sprm = u16(fkp, q);
                        int spra = (sprm >> 13) & 0x7;
                        int opSize = switch (spra) {
                            case 0, 1 -> 1;
                            case 2, 4, 5 -> 2;
                            case 3 -> 4;
                            case 7 -> 3;
                            default -> -1;
                        };
                        if (opSize < 0) {
                            opSize = 1 + (fkp[q + 2] & 0xFF);
                        }
                        if (opSize == 1 && (sprm == 0x0835 || sprm == 0x0836 || sprm == 0x0837)) {
                            boolean on = (fkp[q + 2] & 1) != 0;
                            if (sprm == 0x0835) {
                                bold = on;
                            } else if (sprm == 0x0836) {
                                italic = on;
                            } else {
                                strike = on;
                            }
                        }
                        if (sprm == 0x6A03 && opSize == 4 && q + 2 + 4 <= 512) {
                            // sprmCPicLocation：图片在 Data 流里的偏移
                            picOffset = u32(fkp, q + 2);
                        }
                        q += 2 + opSize;
                    }
                }
                int cpStart = fcToCp(pieceList, fcStart);
                int cpEndRun = fcToCp(pieceList, fcEnd);
                if (cpStart < 0 || cpEndRun < 0) {
                    continue;
                }
                cpStart = Math.max(0, Math.min(cpStart, cpEnd));
                cpEndRun = Math.max(0, Math.min(cpEndRun, cpEnd));
                if (cpEndRun > cpStart) {
                    out.add(new CharRun(cpStart, cpEndRun, bold, italic, strike, picOffset));
                }
            }
        }
        out.sort((a, b) -> Integer.compare(a.cpStart(), b.cpStart()));
        return out;
    }

    /** FKP 页：pn 为 1 基 512 字节页号。 */
    private static byte[] fkpPage(byte[] wordDoc, int pn) {
        int off = pn * 512;
        if (pn <= 0 || off + 512 > wordDoc.length) {
            return null;
        }
        byte[] page = new byte[512];
        System.arraycopy(wordDoc, off, page, 0, 512);
        return page;
    }

    /** FC → CP（按 piece 映射，区间按闭上界容差处理末 run，找不到返回 -1）。 */
    private static int fcToCp(List<Piece> pieces, int fc) {
        for (Piece piece : pieces) {
            int base = piece.compressed ? piece.fc / 2 : piece.fc;
            int span = piece.cpEnd - piece.cpStart;
            if (piece.compressed) {
                if (fc >= base && fc <= base + span) {
                    return piece.cpStart + (fc - base);
                }
            } else {
                if (fc >= base && fc <= base + span * 2) {
                    return piece.cpStart + (fc - base) / 2;
                }
            }
        }
        return -1;
    }

    // ---------- PAPX 段落属性 ----------

    /** 段落属性：样式索引、列表层级与列表号、是否在表内、是否行尾标记、该行单元格数。 */
    public record Papx(int istd, int ilvl, int ilfo, boolean inTable, boolean rowEnd, int cellCount,
                        boolean ilvlSet, boolean ilfoSet) {

        /** 段落属性覆盖样式属性：段落显式给出的值优先，其余继承样式。 */
        public Papx mergeOver(Papx base) {
            if (base == null) {
                return this;
            }
            return new Papx(istd,
                    ilvlSet ? ilvl : base.ilvl(),
                    ilfoSet ? ilfo : base.ilfo(),
                    inTable || base.inTable(),
                    rowEnd || base.rowEnd(),
                    cellCount > 0 ? cellCount : base.cellCount(),
                    ilvlSet || base.ilvlSet(),
                    ilfoSet || base.ilfoSet());
        }
    }

    /** PAPX 条目：覆盖 FC 区间 [fcStart, fcEnd)。 */
    public record PapxEntry(int fcStart, int fcEnd, Papx papx) {
    }

    private static final int FC_STSHF = 162;
    private static final int LCB_STSHF = 166;
    private static final int FC_PLCFBTE_PAPX = 258;
    private static final int LCB_PLCFBTE_PAPX = 262;

    /** 样式表（istd → 样式名/类型）。 */
    public static DocStyleSheet styleSheet(byte[] wordDoc, byte[] table) {
        return DocStyleSheet.parse(table, u32(wordDoc, FC_STSHF), u32(wordDoc, LCB_STSHF),
                u16(wordDoc, 2) >= 0x00C1);
    }

    /**
     * PAPX 索引：plcfbtePapx → PAPX FKP 页 → 每段的 grpprl sprm。
     * 返回按 FC 升序的条目列表。
     */
    public static List<PapxEntry> papxIndex(byte[] wordDoc, byte[] table) {
        List<PapxEntry> out = new ArrayList<>();
        if (table == null) {
            return out;
        }
        int fc = u32(wordDoc, FC_PLCFBTE_PAPX);
        int lcb = u32(wordDoc, LCB_PLCFBTE_PAPX);
        if (lcb < 8 || fc < 0 || fc + lcb > table.length) {
            return out;
        }
        int n = (lcb - 4) / 8;
        for (int i = 0; i < n; i++) {
            int pn = u32(table, fc + (n + 1) * 4 + i * 4) & 0x3FFFFF;
            byte[] fkp = fkpPage(wordDoc, pn);
            if (fkp == null) {
                continue;
            }
            int crun = fkp[511] & 0xFF;
            if (crun <= 0 || crun > 127) {
                continue;
            }
            for (int j = 0; j < crun; j++) {
                int fcStart = u32(fkp, j * 4);
                int fcEnd = u32(fkp, (j + 1) * 4);
                int bOffset = fkp[4 * (crun + 1) + j * 13] & 0xFF;
                if (bOffset == 0) {
                    continue;
                }
                int papxOffset = bOffset * 2;
                int size = 2 * (fkp[papxOffset] & 0xFF);
                if (size == 0) {
                    papxOffset++;
                    size = 2 * (fkp[papxOffset] & 0xFF);
                } else {
                    size--;
                }
                if (size < 2 || papxOffset + 1 + size > 512) {
                    continue;
                }
                byte[] papx = new byte[size];
                System.arraycopy(fkp, papxOffset + 1, papx, 0, size);
                out.add(new PapxEntry(fcStart, fcEnd, parsePapx(papx)));
            }
        }
        out.sort((a, b) -> Integer.compare(a.fcStart(), b.fcStart()));
        return out;
    }

    /** 按 FC 找段落属性；找不到返回 null。 */
    public static Papx papxAt(List<PapxEntry> index, int fc) {
        for (PapxEntry entry : index) {
            if (fc >= entry.fcStart() && fc < entry.fcEnd()) {
                return entry.papx();
            }
        }
        return null;
    }

    /** CP → 字符位置 FC（与 PAPX FKP 里的 rgfc 同一坐标系）。 */
    public static int cpToFc(byte[] wordDoc, byte[] table, int cp) {
        return cpToFc(pieces(wordDoc, table), cp);
    }

    /** PAPX：istd(2) + grpprl；sprm 里取列表层级与表格标记。 */
    static Papx parsePapx(byte[] papx) {
        int istd = u16(papx, 0);
        int ilvl = 0;
        int ilfo = 0;
        boolean ilvlSet = false;
        boolean ilfoSet = false;
        boolean inTable = false;
        boolean rowEnd = false;
        int cellCount = 0;
        int p = 2;
        while (p + 2 <= papx.length) {
            int sprm = u16(papx, p);
            int spra = (sprm >> 13) & 0x7;
            p += 2;
            int[] operand = readOperand(papx, p, sprm, spra);
            if (operand == null) {
                break;
            }
            int operandStart = operand[0];
            int operandLength = operand[1];
            if (operandLength < 0 || operandStart + operandLength > papx.length) {
                break;
            }
            switch (sprm) {
                case 0x4600 -> istd = u16(papx, operandStart);         // sprmPIstd
                case 0x260A -> {
                    ilvl = papx[operandStart] & 0xFF;
                    ilvlSet = true;
                }
                case 0x460B -> {
                    ilfo = u16(papx, operandStart);
                    ilfoSet = true;
                }
                case 0x2416 -> inTable = (papx[operandStart] & 0xFF) != 0;   // sprmPFInTable
                case 0x2417 -> rowEnd = (papx[operandStart] & 0xFF) != 0;     // sprmPFTtp
                case 0xD608 -> cellCount = operandLength > 0 ? (papx[operandStart] & 0xFF) : 0;
                default -> {
                }
            }
            p = operandStart + operandLength;
        }
        return new Papx(istd, ilvl, ilfo, inTable, rowEnd, cellCount, ilvlSet, ilfoSet);
    }

    /** sprm 操作数：返回 {起点, 长度}；解析失败返回 null。 */
    private static int[] readOperand(byte[] buf, int p, int sprm, int spra) {
        return switch (spra) {
            case 0, 1 -> new int[]{p, 1};
            case 2, 4, 5 -> new int[]{p, 2};
            case 3 -> new int[]{p, 4};
            case 7 -> new int[]{p, 3};
            // 变长：0xD608（表定义）与 0xC615 用 2 字节长度，其余 1 字节
            default -> {
                if (sprm == 0xD608 || sprm == 0xC615) {
                    if (p + 2 > buf.length) {
                        yield null;
                    }
                    yield new int[]{p + 2, u16(buf, p)};
                }
                if (p >= buf.length) {
                    yield null;
                }
                yield new int[]{p + 1, buf[p] & 0xFF};
            }
        };
    }

    // ---------- 页眉页脚 ----------

    /**
     * 页眉/页脚子文档 CP 边界（plcfhdd，纯 CP 数组）。
     * 返回各故事 CP 区间列表，裁剪到 [textEnd, textEnd + ccpHdd)。
     */
    public static List<int[]> headerStoryRanges(byte[] wordDoc, byte[] table, int textEnd, int ccpHdd) {
        List<int[]> out = new ArrayList<>();
        if (table == null || ccpHdd <= 0) {
            return out;
        }
        int fc = u32(wordDoc, 242);
        int lcb = u32(wordDoc, 246);
        if (lcb < 4 || fc + lcb > table.length) {
            return out;
        }
        int count = lcb / 4;
        int storyTop = textEnd + ccpHdd;
        for (int i = 0; i < count; i++) {
            int cp = u32(table, fc + i * 4);
            int next = i + 1 < count ? u32(table, fc + (i + 1) * 4) : storyTop;
            int from = Math.max(cp, textEnd);
            int to = Math.min(next, storyTop);
            if (to > from) {
                out.add(new int[]{from, to});
            }
        }
        return out;
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
