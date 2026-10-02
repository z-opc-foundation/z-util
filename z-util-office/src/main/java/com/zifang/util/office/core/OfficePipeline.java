package com.zifang.util.office.core;

import com.zifang.util.office.core.OfficeFormat.Format;
import com.zifang.util.office.excel.ExcelUtils;
import com.zifang.util.office.pdf.PdfExtractor;
import com.zifang.util.office.pdf.PdfOperator;
import com.zifang.util.office.word.WordExtractor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Office 文档流式门面
 * <p>
 * 用一行链式调用打开 → 读 → 改 → 输出四格式文档（PDF / DOCX / XLSX / PPTX）。
 * 自动按 {@link OfficeFormat} 检测格式；buffer 持有当前字节，PDF 变更走临时文件
 * （PdfOperator 静态门面基于 File），buffer 在每次变更后原地更新。
 * <p>
 * 典型用法：
 * <pre>{@code
 * // 读
 * OfficePipeline.open(new File("in.xlsx")).readExcelSheets();
 * OfficePipeline.open(new File("in.docx")).extractWordText();
 * OfficePipeline.open(new File("in.pdf")).extractPdfText();
 *
 * // 写 + 往返
 * OfficePipeline.open(in)
 *     .pdfAddWatermark("CONFIDENTIAL")
 *     .saveAs(new File("out.pdf"));
 *
 * // 内存入 / 内存出
 * byte[] out = OfficePipeline.bytes(data, Format.PDF, "doc.pdf")
 *     .pdfAddWatermark("DRAFT")
 *     .toBytes();
 * }</pre>
 *
 * @author zifang
 */
public class OfficePipeline implements AutoCloseable {

    private byte[] buffer;
    private final Format format;
    private final String name;

    private OfficePipeline(byte[] buffer, Format format, String name) {
        this.buffer = buffer;
        this.format = format;
        this.name = name;
    }

    // ====== Factories ======

    /**
     * 打开文件并检测格式。
     */
    public static OfficePipeline open(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("not a file: " + file);
        }
        byte[] data = Files.readAllBytes(file.toPath());
        // 用 ByteArrayInputStream 走 detect(InputStream) 而非 detect(File)：
        // detect(File) 内部走 FileInputStream，而 FileInputStream 不支持 mark/reset
        // （OfficeFormat 检测需要 mark(8) 后 reset），会抛 IOException。
        Format fmt;
        try (InputStream in = new ByteArrayInputStream(data)) {
            fmt = OfficeFormat.detect(in);
        }
        return new OfficePipeline(data, fmt, file.getName());
    }

    /**
     * 打开路径文件并检测格式。
     */
    public static OfficePipeline open(Path path) throws IOException {
        return open(path.toFile());
    }

    /**
     * 从字节数组构造，调用方负责指定格式与文件名。
     */
    public static OfficePipeline bytes(byte[] data, Format fmt, String name) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        return new OfficePipeline(data.clone(), fmt, name == null ? "doc" : name);
    }

    // ====== Inspection ======

    public Format format() {
        return format;
    }

    public String name() {
        return name;
    }

    /**
     * PDF 页数；非 PDF 格式返回 -1。
     */
    public int pageCount() throws IOException {
        requireFormat(Format.PDF, "pageCount");
        File tmp = writeTemp();
        try {
            return PdfExtractor.pageCount(tmp);
        } finally {
            tmp.delete();
        }
    }

    /**
     * PDF 是否加密；非 PDF 格式返回 false。
     */
    public boolean isEncrypted() throws IOException {
        requireFormat(Format.PDF, "isEncrypted");
        File tmp = writeTemp();
        try {
            return PdfExtractor.isEncrypted(tmp);
        } finally {
            tmp.delete();
        }
    }

    // ====== Read shortcuts (non-mutating) ======

    /**
     * 读取 XLSX 所有 sheet 内容（每个 sheet 一组行）。要求 XLSX 格式。
     */
    public List<List<String>> readExcelSheets() throws IOException {
        requireFormat(Format.XLSX, "readExcelSheets");
        try (InputStream in = new ByteArrayInputStream(buffer)) {
            // ExcelUtils 的扩展名参数不带点：EXT_XLSX = "xlsx"
            return ExcelUtils.readAllSheets(in, "xlsx", 0);
        }
    }

    /**
     * 读取 DOCX 全文（段落拼接）。要求 DOCX 格式。
     */
    public String extractWordText() throws IOException {
        requireFormat(Format.DOCX, "extractWordText");
        try (InputStream in = new ByteArrayInputStream(buffer)) {
            return WordExtractor.extractText(in);
        }
    }

    /**
     * 读取 PDF 全文。要求 PDF 格式。
     */
    public String extractPdfText() throws IOException {
        requireFormat(Format.PDF, "extractPdfText");
        File tmp = writeTemp();
        try {
            return PdfExtractor.extractText(tmp);
        } finally {
            tmp.delete();
        }
    }

    /**
     * 读取 PDF 元信息（标题/作者/...）。要求 PDF 格式。
     */
    public Map<String, String> extractPdfMetadata() throws IOException {
        requireFormat(Format.PDF, "extractPdfMetadata");
        File tmp = writeTemp();
        try {
            return PdfExtractor.extractMetadata(tmp);
        } finally {
            tmp.delete();
        }
    }

    // ====== PDF mutating ops (return this, buffer replaced) ======

    /**
     * 给 PDF 加水印；buffer 替换为加完水印的字节。要求当前 buffer 为 PDF。
     */
    public OfficePipeline pdfAddWatermark(String text) throws IOException {
        requireFormat(Format.PDF, "pdfAddWatermark");
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        File in = writeTemp();
        File out = File.createTempFile("office-pipeline-", ".pdf");
        try {
            PdfOperator.addWatermark(in, out, text);
            this.buffer = Files.readAllBytes(out.toPath());
        } finally {
            in.delete();
            out.delete();
        }
        return this;
    }

    /**
     * 在 PDF 文本层把 {@code oldText} 替换为 {@code newText}（走 PdfOperator 的内容流字符串替换）。
     *
     * @return 实际发生替换的页数
     */
    public int pdfReplaceText(String oldText, String newText) throws IOException {
        requireFormat(Format.PDF, "pdfReplaceText");
        File in = writeTemp();
        File out = File.createTempFile("office-pipeline-", ".pdf");
        try {
            int touched = PdfOperator.replaceText(in, out, oldText, newText);
            this.buffer = Files.readAllBytes(out.toPath());
            return touched;
        } finally {
            in.delete();
            out.delete();
        }
    }

    /**
     * 旋转指定页（0-based），角度为 90 的倍数。
     */
    public OfficePipeline pdfRotatePage(int pageIndex, int degrees) throws IOException {
        requireFormat(Format.PDF, "pdfRotatePage");
        File in = writeTemp();
        File out = File.createTempFile("office-pipeline-", ".pdf");
        try {
            PdfOperator.rotatePage(in, out, pageIndex, degrees);
            this.buffer = Files.readAllBytes(out.toPath());
        } finally {
            in.delete();
            out.delete();
        }
        return this;
    }

    /**
     * 删除指定页集合（0-based）。
     *
     * @return 删除的页数
     */
    public int pdfRemovePages(List<Integer> pageIndexes) throws IOException {
        requireFormat(Format.PDF, "pdfRemovePages");
        File in = writeTemp();
        File out = File.createTempFile("office-pipeline-", ".pdf");
        try {
            int removed = PdfOperator.removePages(in, out, pageIndexes);
            this.buffer = Files.readAllBytes(out.toPath());
            return removed;
        } finally {
            in.delete();
            out.delete();
        }
    }

    /**
     * 给 PDF 加密保护（AES-128）。加密后 buffer 不能再走其它 PDF mutate（无密码加载会抛）。
     */
    public OfficePipeline pdfProtect(String userPwd, String ownerPwd) throws IOException {
        requireFormat(Format.PDF, "pdfProtect");
        File in = writeTemp();
        File out = File.createTempFile("office-pipeline-", ".pdf");
        try {
            PdfOperator.protect(in, out, userPwd, ownerPwd);
            this.buffer = Files.readAllBytes(out.toPath());
        } finally {
            in.delete();
            out.delete();
        }
        return this;
    }

    /**
     * 把当前 PDF 与外部 PDF 文件列表按顺序合并；当前管线 buffer 被替换为合并结果。
     */
    public OfficePipeline pdfMergeWith(List<File> others) throws IOException {
        requireFormat(Format.PDF, "pdfMergeWith");
        if (others == null || others.isEmpty()) {
            throw new IllegalArgumentException("others must not be empty");
        }
        File head = writeTemp();
        List<File> inputs = new ArrayList<>();
        inputs.add(head);
        inputs.addAll(others);
        File out = File.createTempFile("office-pipeline-", ".pdf");
        try {
            PdfOperator.merge(inputs, out);
            this.buffer = Files.readAllBytes(out.toPath());
        } finally {
            head.delete();
            out.delete();
        }
        return this;
    }

    // ====== Cross-format conversion ======

    /**
     * 把 DOCX / XLSX / PPTX 单向渲染为 PDF，返回一条<b>新</b>的 PDF 管线；当前管线不动。
     * 已是 PDF 则返回 clone。
     */
    public OfficePipeline convertToPdf() throws IOException {
        byte[] pdf;
        switch (format) {
            case PDF:
                return OfficePipeline.bytes(buffer, Format.PDF, name);
            case DOCX:
                try (InputStream in = new ByteArrayInputStream(buffer)) {
                    pdf = OfficeConverter.wordToPdf(in);
                }
                break;
            case XLSX:
                try (InputStream in = new ByteArrayInputStream(buffer)) {
                    pdf = OfficeConverter.excelToPdf(in);
                }
                break;
            case PPTX:
                try (InputStream in = new ByteArrayInputStream(buffer)) {
                    pdf = OfficeConverter.pptToPdf(in);
                }
                break;
            default:
                throw new IllegalStateException("convertToPdf does not support format " + format);
        }
        String pdfName = name == null ? "doc.pdf"
                : (name.endsWith(".pdf") ? name : stripExtension(name) + ".pdf");
        return OfficePipeline.bytes(pdf, Format.PDF, pdfName);
    }

    // ====== Output ======

    /**
     * 把当前 buffer 写到目标文件（保持原格式扩展名）。
     */
    public OfficePipeline saveAs(File dest) throws IOException {
        if (dest == null) {
            throw new IllegalArgumentException("dest must not be null");
        }
        Files.write(dest.toPath(), buffer);
        return this;
    }

    /**
     * 把当前 buffer 写到目标路径。
     */
    public OfficePipeline saveAs(Path dest) throws IOException {
        return saveAs(dest.toFile());
    }

    /**
     * 返回当前 buffer 的拷贝。
     */
    public byte[] toBytes() {
        return buffer.clone();
    }

    /**
     * 关闭管线（当前 buffer 是内存字节，关闭是 no-op；保留接口供未来持有临时文件时钩入）。
     */
    @Override
    public void close() {
        // no-op
    }

    // ====== Internal ======

    private void requireFormat(Format want, String op) {
        if (format != want) {
            throw new IllegalStateException(op + " requires format " + want + " but pipeline is " + format);
        }
    }

    private File writeTemp() throws IOException {
        File tmp = File.createTempFile("office-pipeline-", "-" + safeSuffix(name));
        Files.write(tmp.toPath(), buffer);
        return tmp;
    }

    private static String safeSuffix(String name) {
        int dot = name == null ? -1 : name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "bin";
        }
        return name.substring(dot + 1);
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }
}
