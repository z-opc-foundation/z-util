package com.zifang.util.office.word;

import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Word (.docx) 信息提取工具：段落、表格、全文文本、文档元数据。
 *
 * <p>与 pdf.PdfExtractor、ppt.PptUtils.collectText 对称——每个格式都有"读回来"的入口。
 */
public class WordExtractor {

    /**
     * 抽取全部段落的文本（不含表格内的段落）。
     *
     * @param in .docx 输入流
     * @return 段落文本列表
     * @throws IOException 读取失败
     */
    public static List<String> extractParagraphs(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            List<String> result = new ArrayList<>();
            for (XWPFParagraph p : doc.getParagraphs()) {
                result.add(p.getText());
            }
            return result;
        }
    }

    /**
     * 抽取全部段落文本 + 表格文本，按文档顺序拼接，段间换行。
     *
     * @param in .docx 输入流
     * @return 全文文本
     * @throws IOException 读取失败
     */
    public static String extractText(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append('\n');
            }
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    List<String> cells = new ArrayList<>();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        cells.add(cell.getText());
                    }
                    sb.append(String.join("\t", cells)).append('\n');
                }
            }
            return sb.toString();
        }
    }

    /**
     * 读取指定表格为二维文本（外层按行、内层按列）。
     *
     * @param in        .docx 输入流
     * @param tableIndex 表格索引（0-based）
     * @return 二维文本；索引越界抛 IndexOutOfBoundsException
     * @throws IOException 读取失败
     */
    public static List<List<String>> readTable(InputStream in, int tableIndex) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            XWPFTable table = doc.getTables().get(tableIndex);
            List<List<String>> result = new ArrayList<>();
            for (XWPFTableRow row : table.getRows()) {
                List<String> cells = new ArrayList<>();
                for (XWPFTableCell cell : row.getTableCells()) {
                    cells.add(cell.getText());
                }
                result.add(cells);
            }
            return result;
        }
    }

    /**
     * 抽取文档元数据（title / creator / subject / keywords / lastModifiedBy / created / modified）。
     *
     * @param in .docx 输入流
     * @return 键值对；不存在的字段不出现在结果中
     * @throws IOException 读取失败
     */
    public static Map<String, String> extractMetadata(InputStream in) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        try (XWPFDocument doc = new XWPFDocument(in)) {
            POIXMLProperties properties = doc.getProperties();
            if (properties == null) {
                return result;
            }
            POIXMLProperties.CoreProperties core = properties.getCoreProperties();
            if (core == null) {
                return result;
            }
            putIfPresent(result, "title", core.getTitle());
            putIfPresent(result, "creator", core.getCreator());
            putIfPresent(result, "subject", core.getSubject());
            putIfPresent(result, "keywords", core.getKeywords());
            putIfPresent(result, "lastModifiedBy", core.getLastModifiedByUser());
            if (core.getCreated() != null) {
                result.put("created", String.valueOf(core.getCreated().getTime()));
            }
            if (core.getModified() != null) {
                result.put("modified", String.valueOf(core.getModified().getTime()));
            }
        }
        return result;
    }

    /**
     * 便捷入口：从文件读全文。
     *
     * @param file .docx 文件
     * @return 全文文本
     * @throws IOException 读取失败
     */
    public static String extractText(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("not a file: " + file);
        }
        try (InputStream in = new java.io.FileInputStream(file)) {
            return extractText(in);
        }
    }

    /**
     * 抽取全部表格为三维文本：外层按表格索引、中层按行、内层按列。
     *
     * @param in .docx 输入流
     * @return 每个表格的二维文本列表；无表格时返回空列表
     * @throws IOException 读取失败
     */
    public static List<List<List<String>>> readAllTables(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            List<List<List<String>>> result = new ArrayList<>();
            for (XWPFTable table : doc.getTables()) {
                List<List<String>> grid = new ArrayList<>();
                for (XWPFTableRow row : table.getRows()) {
                    List<String> cells = new ArrayList<>();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        cells.add(cell.getText());
                    }
                    grid.add(cells);
                }
                result.add(grid);
            }
            return result;
        }
    }

    /**
     * 表格个数（不含内层嵌套）。
     *
     * @param in .docx 输入流
     * @return 顶层表格数
     * @throws IOException 读取失败
     */
    public static int tableCount(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            return doc.getTables().size();
        }
    }

    /**
     * 抽取文档内嵌图片（word/media/*）：返回原始字节列表，顺序与 POI 迭代顺序一致。
     * 调用方通过图片二进制头部魔数自行判定类型（PNG/JPEG/GIF/...）。
     *
     * @param in .docx 输入流
     * @return 每张图片的字节；无图片时返回空列表
     * @throws IOException 读取失败
     */
    public static List<byte[]> extractImages(InputStream in) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(in)) {
            List<byte[]> result = new ArrayList<>();
            for (org.apache.poi.xwpf.usermodel.XWPFPictureData pic : doc.getAllPictures()) {
                result.add(pic.getData());
            }
            return result;
        }
    }

    private static void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isEmpty()) {
            map.put(key, value);
        }
    }
}
