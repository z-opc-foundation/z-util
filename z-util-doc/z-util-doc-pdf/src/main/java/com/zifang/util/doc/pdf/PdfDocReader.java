package com.zifang.util.doc.pdf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 纯 Java PDF 读取器（零 Apache 依赖）：xref/对象流解析 + 内容流 + 版面启发式。 */
public class PdfDocReader implements DocReader {

    private static final Pattern PDF_DATE = Pattern.compile(
            "D:(\\d{4})(\\d{2})?(\\d{2})?(\\d{2})?(\\d{2})?(\\d{2})?([+\\-Z]\\d{2}'?\\d{2}?)?");

    @Override
    public DocFormat format() {
        return DocFormat.PDF;
    }

    @Override
    public boolean supports(byte[] data) {
        return DocFormatDetector.isPdf(data);
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        PdfParser parser = new PdfParser(data);
        PdfDict trailer = parser.parse();
        DocDocument doc = new DocDocument();
        fillMeta(doc, parser, trailer);

        PdfDict root = parser.dict(trailer.getRaw("Root"));
        if (root == null) {
            throw new IOException("PDF catalog not found");
        }
        List<PdfDict> pages = new ArrayList<>();
        collectPages(parser, parser.dict(root.getRaw("Pages")), pages, 0);
        doc.getMeta().setPageCount(pages.size());

        ContentExtractor extractor = new ContentExtractor(parser);
        LayoutAssembler assembler = new LayoutAssembler();
        for (PdfDict page : pages) {
            ContentExtractor.Page extracted = extractor.extract(page);
            doc.getBlocks().addAll(assembler.assemblePage(extracted));
        }
        return doc;
    }

    private void collectPages(PdfParser parser, PdfDict node, List<PdfDict> pages, int depth) {
        if (node == null || depth > 32) {
            return;
        }
        String type = node.getNameValue("Type");
        if ("Page".equals(type)) {
            pages.add(node);
            return;
        }
        // /Kids 本身可能是间接对象（常见于增量更新/特定写出器），必须先解引用
        List<Object> kids = PdfDict.resolveList(parser.unref(node.getRaw("Kids")), parser::unref);
        if (kids != null) {
            for (Object k : kids) {
                PdfDict kid = parser.dict(k);
                if (kid != null) {
                    collectPages(parser, kid, pages, depth + 1);
                }
            }
        }
    }

    private void fillMeta(DocDocument doc, PdfParser parser, PdfDict trailer) {
        PdfDict info = parser.dict(trailer.getRaw("Info"));
        if (info == null) {
            return;
        }
        doc.getMeta().setTitle(text(parser, info.getRaw("Title")));
        doc.getMeta().setAuthor(text(parser, info.getRaw("Author")));
        doc.getMeta().setSubject(text(parser, info.getRaw("Subject")));
        doc.getMeta().setKeywords(text(parser, info.getRaw("Keywords")));
        doc.getMeta().setCreator(text(parser, info.getRaw("Creator")));
        doc.getMeta().setProducer(text(parser, info.getRaw("Producer")));
        OffsetDateTime created = parsePdfDate(text(parser, info.getRaw("CreationDate")));
        if (created != null) {
            doc.getMeta().setCreationDate(created);
        }
        OffsetDateTime mod = parsePdfDate(text(parser, info.getRaw("ModDate")));
        if (mod != null) {
            doc.getMeta().setModDate(mod);
        }
    }

    private static String text(PdfParser parser, Object v) {
        Object o = parser.unref(v);
        if (o instanceof byte[] b) {
            return PdfFilters.decodeString(b).strip();
        }
        return null;
    }

    /** D:20261119120000+08'00' → OffsetDateTime。 */
    static OffsetDateTime parsePdfDate(String s) {
        if (s == null) {
            return null;
        }
        Matcher m = PDF_DATE.matcher(s);
        if (!m.find()) {
            return null;
        }
        try {
            int year = Integer.parseInt(m.group(1));
            int month = opt(m.group(2), 1);
            int day = opt(m.group(3), 1);
            int hour = opt(m.group(4), 0);
            int minute = opt(m.group(5), 0);
            int second = opt(m.group(6), 0);
            String zone = m.group(7);
            if (zone == null || zone.startsWith("Z")) {
                return OffsetDateTime.of(year, month, day, hour, minute, second, 0,
                        java.time.ZoneOffset.UTC);
            }
            String z = zone.replace("'", "");
            if (z.length() == 3) {
                z += "00";
            }
            return OffsetDateTime.of(year, month, day, hour, minute, second, 0,
                    java.time.ZoneOffset.ofHoursMinutes(
                            Integer.parseInt(z.substring(0, 3)),
                            Integer.parseInt(z.substring(0, 1) + z.substring(3))));
        } catch (Exception e) {
            return null;
        }
    }

    private static int opt(String g, int dft) {
        return g == null ? dft : Integer.parseInt(g);
    }
}
