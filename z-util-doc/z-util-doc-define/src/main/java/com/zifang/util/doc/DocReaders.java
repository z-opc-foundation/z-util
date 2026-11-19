package com.zifang.util.doc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 读取器注册表：注册 → 嗅探 → 读取 → 导出，一条龙。
 * 典型组装（上层做 MinerU 形态时）：
 * <pre>{@code
 * DocReaders readers = new DocReaders();
 * readers.register(new PdfDocReader(), new DocxDocReader(), ...);
 * DocDocument doc = readers.read(bytes);
 * String md = readers.toMarkdown(doc);
 * }</pre>
 */
public final class DocReaders {

    private final List<DocReader> readers = new ArrayList<>();

    public DocReaders register(DocReader... reader) {
        for (DocReader r : reader) {
            if (r != null && readers.stream().noneMatch(x -> x.format() == r.format())) {
                readers.add(r);
            }
        }
        return this;
    }

    /** 通过 ServiceLoader 加载 classpath 上所有 DocReader 实现（META-INF/services）。 */
    public static DocReaders discover() {
        DocReaders result = new DocReaders();
        for (DocReader reader : ServiceLoader.load(DocReader.class)) {
            result.register(reader);
        }
        return result;
    }

    public List<DocReader> getReaders() {
        return readers;
    }

    public DocFormat detect(byte[] data) {
        return DocFormatDetector.detect(data, readers);
    }

    public boolean supports(byte[] data) {
        return detect(data) != null;
    }

    public DocDocument read(byte[] data) throws IOException {
        for (DocReader reader : readers) {
            if (reader.supports(data)) {
                return reader.read(data);
            }
        }
        throw new DocParseException("no reader supports this data (" + (data == null ? 0 : data.length) + " bytes)");
    }

    public String readToMarkdown(byte[] data) throws IOException {
        return DocConverter.toMarkdown(read(data));
    }

    public String readToPlainText(byte[] data) throws IOException {
        return DocConverter.toPlainText(read(data));
    }

    public String readToHtml(byte[] data) throws IOException {
        return DocConverter.toHtml(read(data));
    }
}
