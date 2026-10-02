package com.zifang.util.office.word;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Word 邮件合并（mail-merge）：单模板 × N 数据记录 → N 份 DOCX。
 * <p>
 * 复用 {@link WordTemplate} 的占位符语义（{@code ${var}} / {@code ${list[*]}} / {@code ${table}}）
 * 只是把渲染批量化；模板以 {@code byte[]} 缓存，每条记录都从字节重新开流。
 * <p>
 * 目标场景：发票 / 通知书 / 报告 一次数据源出 N 份。
 *
 * @author zifang
 */
public class WordMailMerge {

    private WordMailMerge() {
    }

    /**
     * 用同一模板 + N 组数据，产出 N 份渲染后的 DOCX 字节。
     *
     * @param template .docx 模板字节
     * @param records  模型列表；每一项是一个 Map，键对应占位符
     * @return 渲染后的 DOCX 字节列表；records 为 null / 空 → 返回空列表
     * @throws IOException 模板读取或渲染失败
     */
    public static List<byte[]> render(byte[] template, List<Map<String, Object>> records) throws IOException {
        if (template == null) {
            throw new IllegalArgumentException("template must not be null");
        }
        List<byte[]> result = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return result;
        }
        for (Map<String, Object> model : records) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = new ByteArrayInputStream(template)) {
                WordTemplate.render(in, out, model);
            }
            result.add(out.toByteArray());
        }
        return result;
    }

    /**
     * 便捷入口：模板以流形式传入，方法内部读为字节。
     */
    public static List<byte[]> render(InputStream templateIn, List<Map<String, Object>> records) throws IOException {
        if (templateIn == null) {
            throw new IllegalArgumentException("templateIn must not be null");
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = templateIn.read(chunk)) > 0) {
            buf.write(chunk, 0, n);
        }
        return render(buf.toByteArray(), records);
    }

    /**
     * 合并产出直接写入 ZIP：entry 名 {@code <prefix>-0.docx, <prefix>-1.docx, ...}。
     *
     * @param template   模板字节
     * @param records    数据列表
     * @param zipOut     ZIP 输出流（方法内会关闭它）
     * @param namePrefix entry 文件名前缀；null 时按 "merge"
     * @throws IOException 渲染或压缩失败
     */
    public static void renderToZip(byte[] template, List<Map<String, Object>> records,
                                   java.io.OutputStream zipOut, String namePrefix) throws IOException {
        if (zipOut == null) {
            throw new IllegalArgumentException("zipOut must not be null");
        }
        String prefix = namePrefix == null ? "merge" : namePrefix;
        List<byte[]> docs = render(template, records);
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
            for (int i = 0; i < docs.size(); i++) {
                zos.putNextEntry(new ZipEntry(prefix + "-" + i + ".docx"));
                zos.write(docs.get(i));
                zos.closeEntry();
            }
        }
    }
}
