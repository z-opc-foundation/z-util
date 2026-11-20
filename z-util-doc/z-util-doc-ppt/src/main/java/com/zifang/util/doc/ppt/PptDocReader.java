package com.zifang.util.doc.ppt;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocParseException;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;
import com.zifang.util.doc.ole.CfbReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PowerPoint 97-2003（二进制 .ppt / HSLF）读取器。零第三方依赖：
 * OLE/CFB 容器（复用 z-util-doc-ole）+ HSLF 记录流自研解析。
 * 覆盖：播放顺序还原、标题占位符、形状文本与段落切分、备注页文本。
 */
public final class PptDocReader implements DocReader {

    private static final String PPT_STREAM = "PowerPoint Document";
    private static final String CURRENT_USER_STREAM = "Current User";
    private static final String PICTURES_STREAM = "Pictures";

    @Override
    public DocFormat format() {
        return DocFormat.PPT;
    }

    @Override
    public boolean supports(byte[] data) {
        if (!DocFormatDetector.isOle2(data)) {
            return false;
        }
        try {
            return new CfbReader(data).hasStream(PPT_STREAM);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        CfbReader cfb = new CfbReader(data);
        byte[] stream = cfb.readStream(PPT_STREAM);
        if (stream == null) {
            throw new DocParseException("不是 ppt：缺少 PowerPoint Document 流");
        }
        HslfDocument document = HslfDocument.parse(stream, cfb.readStream(CURRENT_USER_STREAM));
        List<DocBlock.Slides.Slide> slides = new ArrayList<>();
        for (HslfDocument.Slide slide : document.slides()) {
            slides.add(new DocBlock.Slides.Slide(slide.index(), slide.title(), blocksOf(slide)));
        }
        DocDocument doc = new DocDocument();
        doc.add(new DocBlock.Slides(slides));
        // 内嵌图片：Pictures 流（图片与幻灯片的对应关系在 HSLF 里靠 BSE 索引，这里只按流顺序给出）
        for (HslfPictures.Picture picture : HslfPictures.parse(cfb.readStream(PICTURES_STREAM))) {
            doc.add(new DocBlock.Image(picture.name(), picture.mimeType(), picture.data(), null, null));
        }
        return doc;
    }

    private List<DocBlock> blocksOf(HslfDocument.Slide slide) {
        List<DocBlock> blocks = new ArrayList<>();
        int titleIndex = -1;
        for (int i = 0; i < slide.shapes().size(); i++) {
            if (isTitle(slide.shapes().get(i))) {
                titleIndex = i;
                break;
            }
        }
        for (int i = 0; i < slide.shapes().size(); i++) {
            if (i == titleIndex) {
                continue;
            }
            for (String paragraph : slide.shapes().get(i).paragraphs()) {
                blocks.add(new DocBlock.Paragraph(List.of(DocSpan.plain(paragraph))));
            }
        }
        for (HslfDocument.Shape notes : slide.notesShapes()) {
            for (String paragraph : notes.paragraphs()) {
                blocks.add(new DocBlock.Paragraph(List.of(DocSpan.plain("[备注] " + paragraph))));
            }
        }
        return blocks;
    }

    private static boolean isTitle(HslfDocument.Shape shape) {
        return shape.placeholderType() == HslfRecord.PLACEHOLDER_TITLE
                || shape.placeholderType() == HslfRecord.PLACEHOLDER_CENTER_TITLE;
    }
}
