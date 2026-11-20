package com.zifang.util.doc;

/** 本解析家族覆盖的文档格式。每种格式对应一个 z-util-doc-{format} 子模块的读取器。 */
public enum DocFormat {

    PDF("pdf"),
    DOCX("docx"),
    DOC("doc"),
    EPUB("epub"),
    TXT("txt"),
    RTF("rtf"),
    HTML("html"),
    XLSX("xlsx"),
    XLS("xls"),
    PPTX("pptx"),
    PPT("ppt"),
    ODS("ods"),
    ODT("odt"),
    ODP("odp");

    private final String extension;

    DocFormat(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }
}
