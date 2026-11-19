package com.zifang.util.doc.pdf;

/** PDF 间接引用（N G R），保持未解析，由 PdfParser 按需解。 */
public final class PdfRef {

    public final int obj;
    public final int gen;

    public PdfRef(int obj, int gen) {
        this.obj = obj;
        this.gen = gen;
    }

    @Override
    public String toString() {
        return obj + " " + gen + " R";
    }
}
