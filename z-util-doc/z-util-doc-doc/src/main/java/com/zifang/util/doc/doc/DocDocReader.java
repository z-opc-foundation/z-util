package com.zifang.util.doc.doc;

import com.zifang.util.doc.DocParseException;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 二进制 Word（.doc, Word 97-2003）读取器：CFB 容器 + FIB/piece table，纯 JDK。 */
public class DocDocReader implements DocReader {

    private static final String WORD_STREAM = "WordDocument";

    @Override
    public DocFormat format() {
        return DocFormat.DOC;
    }

    @Override
    public boolean supports(byte[] data) {
        if (!DocFormatDetector.isOle2(data)) {
            return false;
        }
        try {
            return new CfbReader(data).hasStream(WORD_STREAM);
        } catch (DocParseException e) {
            return false;
        }
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        CfbReader cfb = new CfbReader(data);
        byte[] wordDoc = cfb.readStream(WORD_STREAM);
        if (wordDoc == null) {
            throw new IOException("not a Word binary document (no WordDocument stream)");
        }
        DocDocument doc = new DocDocument();
        fillMeta(doc, cfb);

        boolean use1 = WordBinaryParser.usesTable1(wordDoc);
        byte[] table = cfb.readStream(use1 ? "1Table" : "0Table");
        if (table == null) {
            table = cfb.readStream(use1 ? "0Table" : "1Table");
        }
        String text = WordBinaryParser.extractText(wordDoc, table);
        for (String paragraph : paragraphs(text)) {
            if (!paragraph.isBlank()) {
                doc.add(new DocBlock.Paragraph(List.of(DocSpan.plain(paragraph))));
            }
        }
        return doc;
    }

    /** \r 段落 / 0x07 单元格 / 0x0B 软换行 / 字段与对象控制符清理。 */
    static List<String> paragraphs(String raw) {
        StringBuilder cleaned = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '\r', '\u0007', '\u000C' -> cleaned.append('\n');
                case '\u000B' -> cleaned.append('\n');
                case '\u001E' -> cleaned.append('-');
                case '\u0001', '\u0002', '\u0005', '\u0008', '\u0013', '\u0014', '\u0015', '\u001F' -> {
                }
                default -> cleaned.append(c);
            }
        }
        List<String> out = new ArrayList<>();
        for (String part : cleaned.toString().split("\n+")) {
            String p = part.strip();
            if (!p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    private void fillMeta(DocDocument doc, CfbReader cfb) {
        for (CfbReader.Entry e : cfb.getEntries()) {
            if (e.isStream() && e.name.startsWith("\u0005")) {
                byte[] s = cfb.readStream(e.name);
                if (s != null) {
                    SummaryInfoParser.parse(s, doc.getMeta());
                }
            }
        }
    }
}
