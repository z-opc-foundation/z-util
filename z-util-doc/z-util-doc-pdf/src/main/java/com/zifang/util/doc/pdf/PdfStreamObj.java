package com.zifang.util.doc.pdf;

/** 带字典的流对象（内容流 / xref 流 / ToUnicode / 图片等）。 */
public final class PdfStreamObj {

    public final PdfDict dict;
    public final byte[] data;

    public PdfStreamObj(PdfDict dict, byte[] data) {
        this.dict = dict;
        this.data = data;
    }
}
