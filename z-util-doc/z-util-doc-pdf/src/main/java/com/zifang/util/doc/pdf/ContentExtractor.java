package com.zifang.util.doc.pdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容流解释器：BT/ET + Tj/TJ/Td/TD/Tm/Tstar/quote 文本算子 + Do 图片。
 * 输出按页收集的 TextPiece（带设备坐标与字号）和 ImagePiece。
 */
public final class ContentExtractor {

    /** 一段定位于设备坐标的文本；xEnd 是这段文字结束时的设备 x（按字宽推进算出来的真实位置）。 */
    public record TextPiece(String text, double x, double y, double fontSize, String fontName,
                            double xEnd) {
    }

    public record ImagePiece(String name, String mimeType, byte[] data, double x, double y, double scale) {
    }

    public static final class Page {
        public final List<TextPiece> text = new ArrayList<>();
        public final List<ImagePiece> images = new ArrayList<>();
    }

    private final PdfParser parser;
    private final Map<PdfDict, PdfFont> fontCache = new HashMap<>();

    public ContentExtractor(PdfParser parser) {
        this.parser = parser;
    }

    public Page extract(PdfDict pageDict) {
        Page page = new Page();
        PdfDict resources = parser.dict(pageDict.getRaw("Resources"));
        List<PdfStreamObj> contents = contentStreams(parser, pageDict);
        if (contents == null) {
            return page;
        }
        State st = new State();
        for (PdfStreamObj content : contents) {
            byte[] data = parser.streamData(content);
            interpret(new PdfParser.Reader(data, 0), resources, st, page);
        }
        return page;
    }

    private static List<PdfStreamObj> contentStreams(PdfParser parser, PdfDict pageDict) {
        Object v = parser.unref(pageDict.getRaw("Contents"));
        List<PdfStreamObj> out = new ArrayList<>();
        if (v instanceof PdfStreamObj s) {
            out.add(s);
        } else if (v instanceof List<?> arr) {
            for (Object o : arr) {
                Object r = parser.unref(o);
                if (r instanceof PdfStreamObj s) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    private PdfFont font(PdfDict resources, String fontKey) {
        if (resources == null) {
            return null;
        }
        PdfDict fonts = parser.dict(resources.getRaw("Font"));
        if (fonts == null) {
            return null;
        }
        PdfDict fontDict = parser.dict(fonts.getRaw(fontKey));
        if (fontDict == null) {
            return null;
        }
        PdfFont cached = fontCache.get(fontDict);
        if (cached != null) {
            return cached;
        }
        PdfFont f = PdfFont.load(parser, fontDict);
        fontCache.put(fontDict, f);
        return f;
    }

    private void handleImage(Page page, PdfDict resources, String name, double[] ctm) {
        PdfDict xobjects = resources == null ? null : parser.dict(resources.getRaw("XObject"));
        if (xobjects == null) {
            return;
        }
        Object v = parser.unref(xobjects.getRaw(name));
        if (!(v instanceof PdfStreamObj stream)) {
            return;
        }
        PdfDict img = stream.dict;
        if (!"Image".equals(img.getNameValue("Subtype"))) {
            return;
        }
        if (PdfImage.hasUnsupportedFilter(img, parser)) {
            return;
        }
        byte[] data;
        String mime;
        if (PdfImage.hasJpegFilter(img, parser)) {
            // DCTDecode 解出来就是 JPEG 字节
            data = parser.streamData(stream);
            mime = "image/jpeg";
        } else {
            PdfImage.Result result = PdfImage.build(img, parser.streamData(stream), parser);
            if (result == null) {
                return;
            }
            data = result.data();
            mime = result.mimeType();
        }
        page.images.add(new ImagePiece(name, mime, data, ctm[4], ctm[5],
                Math.max(Math.abs(ctm[0]), Math.abs(ctm[3]))));
    }

    private static final class State {
        double[] ctm = {1, 0, 0, 1, 0, 0};
        double[] tm = {1, 0, 0, 1, 0, 0};
        double[] tlm = {1, 0, 0, 1, 0, 0};
        double fontSize = 12;
        double leading = 0;
        double charSpacing = 0;
        double wordSpacing = 0;
        double hScale = 1;
        double rise = 0;
        String fontKey;
    }

    /** 逐 token 解释内容流。'/' 开头是名字操作数，裸 token 是算子。 */
    private void interpret(PdfParser.Reader r, PdfDict resources, State st, Page page) {
        List<Object> operands = new ArrayList<>();
        while (true) {
            r.skipWs();
            int c = r.peek();
            if (c < 0) {
                return;
            }
            // 内联图片（BI...ID...EI）跳过：定位到 EI
            if (c == 'B' && r.matchFrom("BI")) {
                skipInlineImage(r);
                operands.clear();
                continue;
            }
            boolean nameOperand = c == '/';
            Object v = r.readValue();
            if (v == null) {
                r.skip(1);
                continue;
            }
            if (v instanceof PdfName n && !nameOperand) {
                apply(n.name, operands, resources, st, page);
                operands.clear();
                continue;
            }
            operands.add(v);
        }
    }

    private void skipInlineImage(PdfParser.Reader r) {
        r.skip(2);
        while (true) {
            r.skipWs();
            if (r.matchFrom("EI")) {
                r.skip(2);
                return;
            }
            if (r.peek() < 0) {
                return;
            }
            r.skip(1);
        }
    }

    private void apply(String op, List<Object> operands, PdfDict resources, State st, Page page) {
        switch (op) {
            case "q" -> pushState(st);
            case "Q" -> popState(st);
            case "cm" -> {
                double[] m = matrix(operands);
                if (m != null) {
                    st.ctm = mul(m, st.ctm);
                }
            }
            case "BT" -> {
                st.tm = new double[]{1, 0, 0, 1, 0, 0};
                st.tlm = st.tm.clone();
            }
            case "Tf" -> {
                if (operands.size() >= 2 && operands.get(0) instanceof PdfName fn
                        && operands.get(1) instanceof Number size) {
                    st.fontKey = fn.name;
                    st.fontSize = size.doubleValue();
                }
            }
            case "Td" -> {
                if (operands.size() >= 2) {
                    double tx = num(operands.get(operands.size() - 2));
                    double ty = num(operands.get(operands.size() - 1));
                    double[] m = {1, 0, 0, 1, tx, ty};
                    st.tlm = mul(m, st.tlm);
                    st.tm = st.tlm.clone();
                }
            }
            case "TD" -> {
                if (operands.size() >= 2) {
                    st.leading = -num(operands.get(operands.size() - 2));
                }
                apply("Td", operands, resources, st, page);
            }
            case "Tm" -> {
                double[] m = matrix(operands);
                if (m != null) {
                    st.tm = m.clone();
                    st.tlm = m.clone();
                }
            }
            case "T*" -> {
                double[] m = {1, 0, 0, 1, 0, -st.leading};
                st.tlm = mul(m, st.tlm);
                st.tm = st.tlm.clone();
            }
            case "Tj", "'", "\"" -> show(operands, resources, st, page, true);
            case "TJ" -> show(operands, resources, st, page, false);
            case "Tc" -> {
                if (!operands.isEmpty()) {
                    st.charSpacing = num(operands.get(operands.size() - 1));
                }
            }
            case "Tw" -> {
                if (!operands.isEmpty()) {
                    st.wordSpacing = num(operands.get(operands.size() - 1));
                }
            }
            case "Tz" -> {
                if (!operands.isEmpty()) {
                    st.hScale = num(operands.get(operands.size() - 1)) / 100.0;
                }
            }
            case "Ts" -> {
                if (!operands.isEmpty()) {
                    st.rise = num(operands.get(operands.size() - 1));
                }
            }
            case "Do" -> {
                if (!operands.isEmpty() && operands.get(operands.size() - 1) instanceof PdfName n) {
                    PdfDict xobjects = resources == null ? null : parser.dict(resources.getRaw("XObject"));
                    Object xobj = xobjects == null ? null : parser.unref(xobjects.getRaw(n.name));
                    if (xobj instanceof PdfStreamObj stream) {
                        String subtype = stream.dict.getNameValue("Subtype");
                        if ("Form".equals(subtype)) {
                            // 表单 XObject：用自己的资源再解释一遍内容流
                            PdfDict formResources = parser.dict(stream.dict.getRaw("Resources"));
                            if (formResources == null) {
                                formResources = resources;
                            }
                            byte[] formData = parser.streamData(stream);
                            interpret(new PdfParser.Reader(formData, 0), formResources, st, page);
                        } else {
                            handleImage(page, resources, n.name, st.ctm);
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    private final java.util.ArrayDeque<double[]> gstack = new java.util.ArrayDeque<>();

    private void pushState(State st) {
        gstack.push(new double[]{st.ctm[0], st.ctm[1], st.ctm[2], st.ctm[3], st.ctm[4], st.ctm[5],
                st.fontSize, st.leading, st.charSpacing, st.wordSpacing, st.hScale, st.rise,
                st.fontKey != null ? 1 : 0});
        // fontKey 无法进 double 栈，简单按栈序配对（q/Q 总是成对）——用独立栈
        fontStack.push(st.fontKey == null ? "" : st.fontKey);
    }

    private final java.util.ArrayDeque<String> fontStack = new java.util.ArrayDeque<>();

    private void popState(State st) {
        if (!gstack.isEmpty()) {
            double[] s = gstack.pop();
            st.ctm = new double[]{s[0], s[1], s[2], s[3], s[4], s[5]};
            st.fontSize = s[6];
            st.leading = s[7];
            st.charSpacing = s[8];
            st.wordSpacing = s[9];
            st.hScale = s[10];
            st.rise = s[11];
            String fontKey = fontStack.isEmpty() ? "" : fontStack.pop();
            st.fontKey = fontKey.isEmpty() ? null : fontKey;
        }
    }

    private void show(List<Object> operands, PdfDict resources, State st, Page page, boolean single) {
        PdfFont font = st.fontKey == null ? null : font(resources, st.fontKey);
        if (font == null) {
            return;
        }
        List<Object> items = new ArrayList<>();
        if (single) {
            Object last = operands.get(operands.size() - 1);
            if (last instanceof byte[] b) {
                items.add(b);
            }
        } else {
            Object last = operands.get(operands.size() - 1);
            if (last instanceof List<?> arr) {
                for (Object o : arr) {
                    items.add(o);
                }
            }
        }
        double[] startDev = applyCtm(st.tm, st.ctm);
        StringBuilder sb = new StringBuilder();
        for (Object o : items) {
            if (o instanceof byte[] codes) {
                for (PdfFont.Glyph g : font.decode(codes)) {
                    sb.append(g.text());
                    advance(st, font, g.width(), isSpaceCode(g.text()));
                }
            } else if (o instanceof Number n) {
                double kern = -n.doubleValue() / 1000.0 * st.fontSize * st.hScale;
                st.tm[4] += kern;
                if (n.doubleValue() <= -100) {
                    sb.append(' ');
                }
            }
        }
        String text = sb.toString();
        // 不做 strip：空格字形本身要保留，词间距靠它还原
        if (!text.isEmpty()) {
            double[] dev = applyCtm(st.tm, st.ctm);
            page.text.add(new TextPiece(text, startDev[4], dev[5] + st.rise,
                    st.fontSize, st.fontKey, dev[4]));
        }
    }

    private boolean isSpaceCode(String t) {
        return t.equals(" ");
    }

    /** 文本矩阵按字宽推进（文本空间 → 经 hScale）。 */
    private void advance(State st, PdfFont font, double glyphWidth, boolean space) {
        double w = glyphWidth / 1000.0 * st.fontSize * st.hScale
                + st.charSpacing + (space ? st.wordSpacing : 0);
        st.tm[4] += w;
    }

    private static double num(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0;
    }

    private static double[] matrix(List<Object> operands) {
        if (operands.size() < 6) {
            return null;
        }
        double[] m = new double[6];
        for (int i = 0; i < 6; i++) {
            m[i] = num(operands.get(operands.size() - 6 + i));
        }
        return m;
    }

    private static double[] mul(double[] a, double[] b) {
        return new double[]{
                a[0] * b[0] + a[1] * b[2],
                a[0] * b[1] + a[1] * b[3],
                a[2] * b[0] + a[3] * b[2],
                a[2] * b[1] + a[3] * b[3],
                a[4] * b[0] + a[5] * b[2] + b[4],
                a[4] * b[1] + a[5] * b[3] + b[5]
        };
    }

    private static double[] applyCtm(double[] tm, double[] ctm) {
        return mul(tm, ctm);
    }
}
