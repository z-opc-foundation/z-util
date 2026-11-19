package com.zifang.util.doc;

import java.util.ArrayList;
import java.util.List;

/** 解析后的文档：元数据 + 有序块序列。 */
public final class DocDocument {

    private final DocMeta meta = new DocMeta();
    private final List<DocBlock> blocks = new ArrayList<>();

    public DocMeta getMeta() {
        return meta;
    }

    public List<DocBlock> getBlocks() {
        return blocks;
    }

    public DocDocument add(DocBlock block) {
        if (block != null) {
            blocks.add(block);
        }
        return this;
    }

    public String toMarkdown() {
        return DocConverter.toMarkdown(this);
    }

    public String toPlainText() {
        return DocConverter.toPlainText(this);
    }

    public String toHtml() {
        return DocConverter.toHtml(this);
    }
}
