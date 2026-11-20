package com.zifang.util.doc.odf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** OpenDocument 文本（.odt）读取器。零第三方依赖：JDK Zip + DOM。 */
public final class OdtDocReader implements DocReader {

    @Override
    public DocFormat format() {
        return DocFormat.ODT;
    }

    @Override
    public boolean supports(byte[] data) {
        try {
            return OdfSupport.MIME_TEXT.equals(OdfSupport.mimetype(OdfSupport.entries(data)));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        Map<String, byte[]> entries = OdfSupport.entries(data);
        Element contentRoot = OdfSupport.contentRoot(entries);
        Map<String, OdfSupport.Style> styles = OdfSupport.parseStyles(entries, contentRoot);
        Element body = OdfSupport.bodyPart(contentRoot, "text");
        DocDocument doc = new DocDocument();
        if (body != null) {
            List<DocBlock> blocks = new ArrayList<>();
            OdfSupport.appendTextBlocks(body, blocks, styles, entries, 0);
            blocks.forEach(doc::add);
        }
        // master-page 页眉页脚：[页眉]/[页脚] Quote 块挂在正文后（与 doc 模块口径一致）
        for (OdfSupport.MasterHeaderFooter master : OdfSupport.masterHeaderFooters(entries, styles)) {
            for (String line : master.header()) {
                doc.add(new DocBlock.Quote(List.of(DocSpan.plain("[页眉] " + line))));
            }
            for (String line : master.footer()) {
                doc.add(new DocBlock.Quote(List.of(DocSpan.plain("[页脚] " + line))));
            }
        }
        return doc;
    }
}
