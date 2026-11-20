package com.zifang.util.doc.word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 样式表（STSH）解析：istd → 样式名、样式类型与内置样式号，以及样式自带的段落属性。
 *
 * <p>布局：STSH = cbStshi(2) + STSHI + cstd 组 (cbStd(2) + STD)。
 * STD = STDFBase(10 字节，Word 2000 以后 18 字节) + 样式名（Xst：2 字节长度 + UTF-16）
 * + UPX 组。位域：sti = info1 & 0x0FFF；stk = info2 & 0x000F；istdBase = (info2 & 0xFFF0) >> 4；
 * cupx = info3 & 0x000F。</p>
 *
 * <p>UPX 组里第一个 UPX 是段落样式的 PAPX（istd(2) + grpprl），段落自己的属性会覆盖它，
 * 所以列表层级、列表号可以从样式继承（Word 里 "标题 1" 之类的样式就带着 ilfo）。</p>
 */
final class DocStyleSheet {

    /** 一个样式：istd 是样式索引，sti 是内置样式号（1..9 为标题 1..9），stk=1 段落样式。 */
    record Style(int istd, int sti, int stk, int istdBase, String name,
                 WordBinaryParser.Papx papx) {

        boolean paragraphStyle() {
            return stk == 1;
        }
    }

    private final List<Style> styles;

    private DocStyleSheet(List<Style> styles) {
        this.styles = styles;
    }

    static DocStyleSheet parse(byte[] table, int fcStshf, int lcbStshf, boolean word9) {
        List<Style> styles = new ArrayList<>();
        if (table == null || fcStshf < 0 || lcbStshf <= 0 || fcStshf + lcbStshf > table.length) {
            return new DocStyleSheet(styles);
        }
        int cbStshi = u16(table, fcStshf);
        int stshiStart = fcStshf + 2;
        if (stshiStart + 12 > table.length) {
            return new DocStyleSheet(styles);
        }
        int cstd = u16(table, stshiStart);
        int cbSTDBaseInFile = u16(table, stshiStart + 2);
        int offset = stshiStart + cbStshi;
        for (int istd = 0; istd < cstd && offset + 2 <= table.length; istd++) {
            int cbStd = u16(table, offset);
            offset += 2;
            if (cbStd > 0 && offset + cbStd <= table.length) {
                Style style = parseStd(table, offset, cbSTDBaseInFile, istd, word9);
                if (style != null) {
                    styles.add(style);
                }
            }
            offset += cbStd;
        }
        return new DocStyleSheet(styles);
    }

    private static Style parseStd(byte[] table, int offset, int cbSTDBaseInFile, int istd, boolean word9) {
        if (offset + 10 > table.length) {
            return null;
        }
        int info1 = u16(table, offset);
        int info2 = u16(table, offset + 2);
        int info3 = u16(table, offset + 4);
        int sti = info1 & 0x0FFF;
        int stk = info2 & 0x000F;
        int istdBase = (info2 & 0xFFF0) >> 4;
        int cupx = info3 & 0x000F;

        int nameStart = offset + cbSTDBaseInFile;
        if (nameStart >= table.length) {
            return new Style(istd, sti, stk, istdBase, "", null);
        }
        int nameLength;
        int multiplier;
        if (word9) {
            nameLength = u16(table, nameStart);
            multiplier = 2;
            nameStart += 2;
        } else {
            nameLength = table[nameStart] & 0xFF;
            multiplier = 1;
            nameStart += 1;
        }
        int charCount = nameLength * multiplier / 2;
        String name = "";
        if (charCount > 0 && nameStart + charCount * 2 <= table.length) {
            name = new String(table, nameStart, charCount * 2, java.nio.charset.StandardCharsets.UTF_16LE);
        }

        WordBinaryParser.Papx papx = null;
        if (stk == 1) {
            // UPX 组：名字（含结尾空字符）之后是 cupx 个 UPX，每段 2 字节长度 + 内容（按 2 字节对齐）
            int varOffset = nameStart + (nameLength + 1) * multiplier;
            if (cupx > 0 && varOffset + 2 <= table.length) {
                int upxSize = u16(table, varOffset);
                varOffset += 2;
                if (upxSize >= 2 && varOffset + upxSize <= table.length) {
                    byte[] upx = new byte[upxSize];
                    System.arraycopy(table, varOffset, upx, 0, upxSize);
                    papx = WordBinaryParser.parsePapx(upx);
                }
            }
        }
        return new Style(istd, sti, stk, istdBase, name, papx);
    }

    /** 全部样式。 */
    public List<Style> styles() {
        return styles;
    }

    Style byIstd(int istd) {
        for (Style style : styles) {
            if (style.istd() == istd) {
                return style;
            }
        }
        return null;
    }

    /** 样式名（找不到时返回空串）。 */
    String nameOf(int istd) {
        Style style = byIstd(istd);
        return style == null ? "" : style.name();
    }

    /**
     * 标题级别：内置 heading 1..9（sti 1..9）优先，其次样式名里的 "heading N" / "标题 N" / "标题N"。
     * 不是标题返回 0。
     */
    int headingLevel(int istd) {
        Style style = byIstd(istd);
        if (style == null || !style.paragraphStyle()) {
            return 0;
        }
        if (style.sti() >= 1 && style.sti() <= 9) {
            return style.sti();
        }
        return levelFromName(style.name());
    }

    /**
     * 样式链上解析出的段落属性（istdBase 逐级向上继承），没有段落样式时返回 null。
     */
    WordBinaryParser.Papx papxOf(int istd) {
        return resolvePapx(istd, new HashSet<>());
    }

    private WordBinaryParser.Papx resolvePapx(int istd, Set<Integer> visited) {
        Style style = byIstd(istd);
        if (style == null || style.papx() == null) {
            return null;
        }
        if (!visited.add(istd)) {
            return style.papx();
        }
        WordBinaryParser.Papx own = style.papx();
        int base = style.istdBase();
        if (base < 0 || base == istd) {
            return own;
        }
        WordBinaryParser.Papx basePapx = resolvePapx(base, visited);
        return basePapx == null ? own : own.mergeOver(basePapx);
    }

    static int levelFromName(String name) {
        if (name == null || name.isEmpty()) {
            return 0;
        }
        String lower = name.toLowerCase(java.util.Locale.ROOT).trim();
        int level = matchNumber(lower, "heading ");
        if (level > 0) {
            return level;
        }
        return matchNumber(lower, "标题 ");
    }

    private static int matchNumber(String lower, String prefix) {
        if (!lower.startsWith(prefix)) {
            return 0;
        }
        String rest = lower.substring(prefix.length()).trim();
        int end = 0;
        while (end < rest.length() && Character.isDigit(rest.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return 0;
        }
        try {
            int level = Integer.parseInt(rest.substring(0, end));
            return level >= 1 && level <= 9 ? level : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int u16(byte[] d, int off) {
        return (d[off] & 0xFF) | ((d[off + 1] & 0xFF) << 8);
    }
}
