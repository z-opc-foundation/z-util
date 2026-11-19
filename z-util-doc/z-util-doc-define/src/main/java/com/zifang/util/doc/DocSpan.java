package com.zifang.util.doc;

/** 行内片段：一段带样式的文本。 */
public record DocSpan(String text, boolean bold, boolean italic, boolean mono,
                      boolean strike, String link) {

    public static DocSpan plain(String text) {
        return new DocSpan(text, false, false, false, false, null);
    }

    public DocSpan withBold() {
        return new DocSpan(text, true, italic, mono, strike, link);
    }

    public DocSpan withItalic() {
        return new DocSpan(text, bold, true, mono, strike, link);
    }

    public DocSpan withMono() {
        return new DocSpan(text, bold, italic, true, strike, link);
    }

    public DocSpan withStrike() {
        return new DocSpan(text, bold, italic, mono, true, link);
    }

    public DocSpan withLink(String link) {
        return new DocSpan(text, bold, italic, mono, strike, link);
    }
}
