package com.zifang.util.doc.pdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** PDF 字体：简单字体（1 字节码）与 Type0/CID（2 字节码），带 ToUnicode 与宽度表。 */
public final class PdfFont {

    private final String name;
    private final boolean cid;
    private final ToUnicodeCMap toUnicode;
    private final double[] widths;
    /** 字形宽度到文本空间的换算：普通字体 1/1000，Type3 用 /FontMatrix 的横向量。 */
    private final double glyphScale;
    private final int firstChar;
    private final double defaultWidth;
    private final Map<Integer, double[]> cidWidths;

    private PdfFont(String name, boolean cid, ToUnicodeCMap toUnicode,
                    double[] widths, int firstChar, double defaultWidth,
                    Map<Integer, double[]> cidWidths, double glyphScale) {
        this.name = name;
        this.cid = cid;
        this.toUnicode = toUnicode;
        this.widths = widths;
        this.firstChar = firstChar;
        this.defaultWidth = defaultWidth;
        this.cidWidths = cidWidths;
        this.glyphScale = glyphScale;
    }

    /** 字形宽度换算系数：普通字体 1/1000，Type3 是 /FontMatrix 的横向缩放。 */
    public double glyphScale() {
        return glyphScale;
    }

    public String getName() {
        return name;
    }

    public boolean isCid() {
        return cid;
    }

    public static PdfFont load(PdfParser parser, PdfDict fontDict) {
        String subtype = fontDict.getNameValue("Subtype");
        boolean cid = "Type0".equals(subtype);
        ToUnicodeCMap cmap = loadToUnicode(parser, fontDict);
        double[] widths = null;
        int firstChar = 0;
        Map<Integer, double[]> cidW = cid ? loadCidWidths(parser, fontDict) : new HashMap<>();
        if (!cid) {
            List<Object> w = fontDict.getArray("Widths");
            if (w != null) {
                widths = new double[w.size()];
                for (int i = 0; i < w.size(); i++) {
                    widths[i] = w.get(i) instanceof Number n ? n.doubleValue() : 500;
                }
                firstChar = (int) fontDict.getNumberOr("FirstChar", 0);
            }
        }
        double dft = cid ? 1000 : 500;
        double glyphScale = 0.001;
        if ("Type3".equals(subtype)) {
            // Type3 的字宽在字形空间里，靠 /FontMatrix 映射到文本空间
            List<Object> matrix = fontDict.getArray("FontMatrix");
            if (matrix != null && !matrix.isEmpty() && matrix.get(0) instanceof Number n) {
                glyphScale = n.doubleValue();
            }
        }
        return new PdfFont(nameOf(fontDict), cid, cmap, widths, firstChar, dft, cidW, glyphScale);
    }

    private static String nameOf(PdfDict fontDict) {
        String bf = fontDict.getNameValue("BaseFont");
        return bf != null ? bf : "";
    }

    private static ToUnicodeCMap loadToUnicode(PdfParser parser, PdfDict fontDict) {
        Object v = fontDict.getRaw("ToUnicode");
        if (v instanceof PdfRef r) {
            v = parser.getObject(r.obj);
        }
        if (v instanceof PdfStreamObj s) {
            return ToUnicodeCMap.parse(parser.streamData(s));
        }
        return new ToUnicodeCMap();
    }

    /** /W 数组：[c [w...] | c1 c2 w] 两种形态。 */
    private static Map<Integer, double[]> loadCidWidths(PdfParser parser, PdfDict fontDict) {
        Map<Integer, double[]> result = new HashMap<>();
        List<Object> w = fontDict.getArray("W");
        if (w == null) {
            // Type0 的宽度在 DescendantFonts[0] 上
            List<Object> dfs = fontDict.getArray("DescendantFonts");
            if (dfs != null && !dfs.isEmpty() && dfs.get(0) instanceof PdfRef r) {
                PdfDict df = parser.getDict(r.obj);
                if (df != null) {
                    w = df.getArray("W");
                }
            }
        }
        if (w == null) {
            return result;
        }
        int i = 0;
        while (i < w.size()) {
            if (!(w.get(i) instanceof Number n0)) {
                i++;
                continue;
            }
            int c = n0.intValue();
            if (i + 1 < w.size() && w.get(i + 1) instanceof List<?> range) {
                double[] arr = new double[range.size()];
                for (int k = 0; k < range.size(); k++) {
                    arr[k] = range.get(k) instanceof Number n ? n.doubleValue() : 1000;
                }
                result.put(c, arr);
                i += 2;
            } else if (i + 2 < w.size() && w.get(i + 1) instanceof Number n1 && w.get(i + 2) instanceof Number n2) {
                int last = n1.intValue();
                if (last >= c && last - c < 1000) {
                    for (int code = c; code <= last; code++) {
                        result.put(code, new double[]{n2.doubleValue()});
                    }
                }
                i += 3;
            } else {
                i++;
            }
        }
        return result;
    }

    /** 一次 show 的字节码 → 字形序列。 */
    public List<Glyph> decode(byte[] codes) {
        List<Glyph> out = new ArrayList<>();
        if (cid) {
            for (int i = 0; i + 1 < codes.length; i += 2) {
                int code = ((codes[i] & 0xFF) << 8) | (codes[i + 1] & 0xFF);
                out.add(new Glyph(textFor(code), widthFor(code)));
            }
        } else {
            for (byte b : codes) {
                int code = b & 0xFF;
                out.add(new Glyph(textFor(code), widthFor(code)));
            }
        }
        return out;
    }

    private String textFor(int code) {
        String s = toUnicode.lookup(code);
        if (s != null) {
            return s;
        }
        if (cid) {
            return "";
        }
        return String.valueOf(latin1Cp1252(code));
    }

    private static char latin1Cp1252(int code) {
        return switch (code) {
            case 0x80 -> '€';
            case 0x82 -> '‚';
            case 0x83 -> 'ƒ';
            case 0x84 -> '„';
            case 0x85 -> '…';
            case 0x86 -> '†';
            case 0x87 -> '‡';
            case 0x88 -> 'ˆ';
            case 0x89 -> '‰';
            case 0x8A -> 'Š';
            case 0x8C -> 'Œ';
            case 0x91 -> '\u2018';
            case 0x92 -> '\u2019';
            case 0x93 -> '\u201C';
            case 0x94 -> '\u201D';
            case 0x95 -> '•';
            case 0x96 -> '–';
            case 0x97 -> '—';
            case 0x99 -> '™';
            case 0x9A -> 'š';
            case 0x9C -> 'œ';
            case 0x9F -> 'Ÿ';
            default -> (char) code;
        };
    }

    private double widthFor(int code) {
        if (cid) {
            double[] arr = cidWidths.get(code);
            return arr != null ? arr[0] : defaultWidth;
        }
        if (widths != null && code >= firstChar && code - firstChar < widths.length) {
            return widths[code - firstChar];
        }
        return defaultWidth;
    }

    /** 字形：文本 + 宽度（1/1000 em）。 */
    public record Glyph(String text, double width) {
    }
}
