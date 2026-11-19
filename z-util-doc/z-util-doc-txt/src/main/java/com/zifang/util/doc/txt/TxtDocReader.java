package com.zifang.util.doc.txt;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** 纯文本读取器：按空行分段，单换行视为软换行保留，charset 自动判别（UTF-8 严格解码失败回退 GBK）。 */
public class TxtDocReader implements DocReader {

    @Override
    public DocFormat format() {
        return DocFormat.TXT;
    }

    @Override
    public boolean supports(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        if (DocFormatDetector.isPdf(data) || DocFormatDetector.isOle2(data)
                || DocFormatDetector.isZip(data) || DocFormatDetector.isRtf(data)
                || DocFormatDetector.isHtml(data)) {
            return false;
        }
        return strictDecode(data, StandardCharsets.UTF_8) || strictDecode(data, Charset.forName("GBK"));
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        String text = decode(data);
        DocDocument doc = new DocDocument();
        for (String para : splitParagraphs(text)) {
            doc.add(new DocBlock.Paragraph(para));
        }
        return doc;
    }

    /** 先按 UTF-8 严格解码，失败回退 GBK。 */
    private String decode(byte[] data) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(data))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(data, Charset.forName("GBK"));
        }
    }

    /** 按空行（连续换行且中间行只有空白）切分段落；段内单换行视为软换行保留。 */
    private List<String> splitParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        for (String para : text.replace("\r\n", "\n").replace('\r', '\n')
                .split("\n[ \\t]*\\n+")) {
            if (!para.isBlank()) {
                paragraphs.add(para.strip());
            }
        }
        return paragraphs;
    }

    private boolean strictDecode(byte[] data, java.nio.charset.Charset charset) {
        try {
            CharsetDecoder decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            decoder.decode(ByteBuffer.wrap(data));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}
