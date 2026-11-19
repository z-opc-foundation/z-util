package com.zifang.util.doc.pdf;

import java.util.Objects;

/** PDF 名字对象（/Name）。 */
public final class PdfName {

    public final String name;

    public PdfName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PdfName n && n.name.equals(name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "/" + name;
    }
}
