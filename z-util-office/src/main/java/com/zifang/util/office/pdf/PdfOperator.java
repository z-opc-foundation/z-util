package com.zifang.util.office.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 操作工具：合并 / 拆分 / 文本替换 / 水印。
 *
 * <p>所有方法对入参为 null / 文件不存在一律抛 {@link IllegalArgumentException}；
 * 文件层面的 IO 错误抛出 {@link IOException}。
 */
public class PdfOperator {

    /**
     * 把多份 PDF 顺序合并为一份。
     *
     * @param inputs  输入 PDF 文件列表
     * @param output  输出 PDF 文件
     * @return 输出文件
     * @throws IOException 任意一份输入无法读取或输出无法写入
     */
    public static File merge(List<File> inputs, File output) throws IOException {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("inputs must not be empty");
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        try (PDDocument target = new PDDocument()) {
            for (File f : inputs) {
                if (f == null || !f.exists() || !f.isFile()) {
                    throw new IllegalArgumentException("input not a file: " + f);
                }
                try (PDDocument src = Loader.loadPDF(f)) {
                    for (PDPage page : src.getPages()) {
                        target.importPage(page);
                    }
                }
            }
            target.save(output);
        }
        return output;
    }

    /**
     * 按 0-based 页码列表把 PDF 拆成多份，每份为单页文件，输出顺序与 pages 对应。
     *
     * @param input    输入 PDF
     * @param pages    要导出的 0-based 页码
     * @param outDir   输出目录
     * @param baseName 输出文件名前缀
     * @return 拆分得到的 PDF 文件列表，命名规则：{@code baseName-<index>.pdf}
     * @throws IOException 读 / 写失败
     */
    public static List<File> split(File input, List<Integer> pages, File outDir, String baseName) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        if (pages == null || pages.isEmpty()) {
            throw new IllegalArgumentException("pages must not be empty");
        }
        if (outDir == null) {
            throw new IllegalArgumentException("outDir must not be null");
        }
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("cannot create outDir: " + outDir);
        }
        String prefix = baseName == null ? "split" : baseName;
        List<File> files = new ArrayList<>();
        try (PDDocument src = Loader.loadPDF(input)) {
            int total = src.getNumberOfPages();
            for (int i = 0; i < pages.size(); i++) {
                int idx = pages.get(i);
                if (idx < 0 || idx >= total) {
                    throw new IllegalArgumentException("page index out of range: " + idx);
                }
                try (PDDocument out = new PDDocument()) {
                    out.importPage(src.getPage(idx));
                    File f = new File(outDir, prefix + "-" + i + ".pdf");
                    out.save(f);
                    files.add(f);
                }
            }
        }
        return files;
    }

    /**
     * 在 PDF 的每一页文本层中，把 {@code oldText} 替换成 {@code newText}（仅替换文本内容，
     * 不重排版面）；按页面写出新文件。
     *
     * <p>文本替换走 PDFBox 的 {@code LegacyPDFStreamEngine}：命中 {@code oldText} 的文本块
     * 被整段覆写，其它内容保留。这是一种"够用"的实现，复杂版式（多栏、字距、换行）可能产生位置漂移。
     *
     * @param input   输入 PDF
     * @param output  输出 PDF
     * @param oldText 被替换文本
     * @param newText 替换文本
     * @return 实际发生替换的页数
     * @throws IOException 读 / 写失败
     */
    public static int replaceText(File input, File output, String oldText, String newText) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        if (oldText == null || oldText.isEmpty()) {
            throw new IllegalArgumentException("oldText must not be empty");
        }
        String replace = newText == null ? "" : newText;
        int touched = 0;
        try (PDDocument doc = Loader.loadPDF(input)) {
            for (int p = 0; p < doc.getNumberOfPages(); p++) {
                PDPage page = doc.getPage(p);
                InputStream contentStream = page.getContents();
                byte[] contentBytes = readAllBytes(contentStream);
                String contents = new String(contentBytes, StandardCharsets.ISO_8859_1);
                if (contents.contains(oldText)) {
                    contents = contents.replace(oldText, replace);
                    page.setContents(new PDStream(doc, new java.io.ByteArrayInputStream(
                            contents.getBytes(StandardCharsets.ISO_8859_1))));
                    touched++;
                }
            }
            doc.save(output);
        }
        return touched;
    }

    /**
     * 给 PDF 每一页加斜向水印（页中央 45° 文本层，半透明）。
     *
     * @param input  输入 PDF
     * @param output 输出 PDF
     * @param text   水印文本；null / 空视为 "WATERMARK"
     * @throws IOException 读 / 写失败
     */
    public static void addWatermark(File input, File output, String text) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        String label = (text == null || text.isEmpty()) ? "WATERMARK" : text;
        try (PDDocument doc = Loader.loadPDF(input)) {
            for (PDPage page : doc.getPages()) {
                PDRectangle box = page.getMediaBox();
                try (PDPageContentStream cs = new PDPageContentStream(doc, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                    gs.setNonStrokingAlphaConstant(0.3f);
                    cs.setGraphicsStateParameters(gs);
                    float fontSize = 60f;
                    float textWidth = label.length() * fontSize * 0.5f;
                    float cx = box.getWidth() / 2f;
                    float cy = box.getHeight() / 2f;
                    cs.saveGraphicsState();
                    cs.transform(org.apache.pdfbox.util.Matrix.getTranslateInstance(cx - textWidth / 2f, cy));
                    cs.transform(org.apache.pdfbox.util.Matrix.getRotateInstance(Math.toRadians(45), textWidth / 2f, 0));
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), fontSize);
                    cs.showText(label);
                    cs.endText();
                    cs.restoreGraphicsState();
                }
            }
            doc.save(output);
        }
    }

    /**
     * 旋转指定页（0-based），角度为 90 的倍数（90/180/270）。
     *
     * @param input     输入 PDF
     * @param output    输出 PDF
     * @param pageIndex 页索引（0-based）
     * @param degrees   旋转角度，取 0/90/180/270
     * @throws IOException 读 / 写失败
     */
    public static void rotatePage(File input, File output, int pageIndex, int degrees) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        if (degrees % 90 != 0) {
            throw new IllegalArgumentException("degrees must be a multiple of 90: " + degrees);
        }
        try (PDDocument doc = Loader.loadPDF(input)) {
            if (pageIndex < 0 || pageIndex >= doc.getNumberOfPages()) {
                throw new IllegalArgumentException("page index out of range: " + pageIndex);
            }
            doc.getPage(pageIndex).setRotation(degrees);
            doc.save(output);
        }
    }

    /**
     * 删除指定页（0-based），按传入集合一次性删除。
     *
     * @param input       输入 PDF
     * @param output      输出 PDF
     * @param pageIndexes 要删除的页索引集合
     * @return 删除的页数
     * @throws IOException 读 / 写失败
     */
    public static int removePages(File input, File output, List<Integer> pageIndexes) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        if (pageIndexes == null || pageIndexes.isEmpty()) {
            throw new IllegalArgumentException("pageIndexes must not be empty");
        }
        try (PDDocument doc = Loader.loadPDF(input)) {
            int total = doc.getNumberOfPages();
            List<PDPage> toRemove = new ArrayList<>();
            for (Integer idx : pageIndexes) {
                if (idx == null || idx < 0 || idx >= total) {
                    throw new IllegalArgumentException("page index out of range: " + idx);
                }
                toRemove.add(doc.getPage(idx));
            }
            for (PDPage page : toRemove) {
                doc.removePage(page);
            }
            doc.save(output);
            return toRemove.size();
        }
    }

    /**
     * 给 PDF 加密码保护（AES-128）。打开文件需要 userPwd；ownerPwd 拥有全部权限。
     *
     * @param input    输入 PDF
     * @param output   输出加密 PDF
     * @param userPwd  用户密码（打开口令）
     * @param ownerPwd 属主密码；传 null 时与 userPwd 相同
     * @throws IOException 读 / 写失败
     */
    public static void protect(File input, File output, String userPwd, String ownerPwd) throws IOException {
        if (input == null || !input.exists() || !input.isFile()) {
            throw new IllegalArgumentException("input not a file: " + input);
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        if (userPwd == null || userPwd.isEmpty()) {
            throw new IllegalArgumentException("userPwd must not be empty");
        }
        String owner = (ownerPwd == null || ownerPwd.isEmpty()) ? userPwd : ownerPwd;
        try (PDDocument doc = Loader.loadPDF(input)) {
            org.apache.pdfbox.pdmodel.encryption.AccessPermission ap = new org.apache.pdfbox.pdmodel.encryption.AccessPermission();
            org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy policy =
                    new org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy(owner, userPwd, ap);
            policy.setEncryptionKeyLength(128);
            doc.protect(policy);
            doc.save(output);
        }
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        try (InputStream stream = in) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = stream.read(chunk)) > 0) {
                buf.write(chunk, 0, n);
            }
            return buf.toByteArray();
        }
    }
}
