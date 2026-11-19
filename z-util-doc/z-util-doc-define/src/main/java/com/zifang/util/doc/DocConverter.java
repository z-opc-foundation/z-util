package com.zifang.util.doc;

import com.zifang.util.doc.export.HtmlExporter;
import com.zifang.util.doc.export.MarkdownExporter;
import com.zifang.util.doc.export.PlainTextExporter;

/** 导出门面。 */
public final class DocConverter {

    private DocConverter() {
    }

    public static String toMarkdown(DocDocument doc) {
        return new MarkdownExporter().export(doc);
    }

    /** 图片内联为 base64 data URI 的 markdown。 */
    public static String toMarkdownInlineImages(DocDocument doc) {
        return new MarkdownExporter(true).export(doc);
    }

    public static String toPlainText(DocDocument doc) {
        return new PlainTextExporter().export(doc);
    }

    public static String toHtml(DocDocument doc) {
        return new HtmlExporter().export(doc);
    }
}
