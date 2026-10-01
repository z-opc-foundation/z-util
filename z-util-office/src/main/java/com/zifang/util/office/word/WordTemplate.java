package com.zifang.util.office.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word 模板引擎（v0）。遍历段落与表格，把占位符替换为数据后写回文档。
 *
 * <p>支持占位符：
 * <ul>
 *   <li>{@code ${var}} —— 段落文本或单元格文本中的简单变量替换</li>
 *   <li>{@code ${list[*]}} —— 占位符段落被复制为 N 段，每段填入列表的逐项</li>
 *   <li>{@code ${table}} —— 占位符所在表格行被复制 M 次，每行填入 Map 的 key/value 对</li>
 * </ul>
 */
public class WordTemplate {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^${}]+)\\}");

    /**
     * 渲染模板流到输出流。**不关闭任何流**。
     *
     * @param templateIn 模板 .docx 输入流
     * @param out        渲染结果输出流
     * @param model      数据模型
     * @throws IOException 读写失败
     */
    public static void render(InputStream templateIn, OutputStream out, Map<String, Object> model) throws IOException {
        if (templateIn == null) {
            throw new IllegalArgumentException("templateIn must not be null");
        }
        if (out == null) {
            throw new IllegalArgumentException("out must not be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
        try (XWPFDocument doc = new XWPFDocument(templateIn)) {
            renderInPlace(doc, model);
            doc.write(out);
        }
    }

    /**
     * 原地渲染已加载的 Word 文档。
     *
     * @param doc   文档
     * @param model 数据模型
     */
    public static void renderInPlace(XWPFDocument doc, Map<String, Object> model) {
        if (doc == null) {
            throw new IllegalArgumentException("doc must not be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
        // 1) 段落
        List<ListParagraphAction> listActions = new ArrayList<>();
        for (XWPFParagraph p : doc.getParagraphs()) {
            String text = p.getText();
            if (text == null || text.isEmpty()) {
                continue;
            }
            Matcher m = PLACEHOLDER.matcher(text);
            if (!m.find()) {
                continue;
            }
            String key = m.group(1).trim();
            if (key.endsWith("[*]")) {
                listActions.add(new ListParagraphAction(p, key.substring(0, key.length() - 3)));
                replaceAllRuns(p, m.replaceAll(""));
            } else {
                Object v = model.get(key);
                String head = text.substring(0, m.start());
                String tail = text.substring(m.end());
                replaceAllRuns(p, head + (v == null ? "" : v.toString()) + tail);
            }
        }
        // 应用 list[*] 段落展开
        for (ListParagraphAction a : listActions) {
            Object v = model.get(a.base);
            if (!(v instanceof List)) {
                continue;
            }
            List<?> list = (List<?>) v;
            // 模板段落清空
            replaceAllRuns(a.paragraph, "");
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    a.paragraph.createRun().setText(toText(list.get(i)));
                } else {
                    // v0 简化：新段落 append 到文档末尾，不做 XmlCursor 重排
                    XWPFParagraph copy = doc.createParagraph();
                    copy.setStyle(a.paragraph.getStyle());
                    copy.createRun().setText(toText(list.get(i)));
                }
            }
        }
        // 2) 表格
        List<TableRowAction> tableActions = new ArrayList<>();
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                boolean hit = false;
                for (XWPFTableCell cell : row.getTableCells()) {
                    String text = cell.getText();
                    if (text == null) {
                        continue;
                    }
                    Matcher m = PLACEHOLDER.matcher(text);
                    if (m.find()) {
                        hit = true;
                        break;
                    }
                }
                if (hit) {
                    tableActions.add(new TableRowAction(table, row));
                }
            }
        }
        for (TableRowAction a : tableActions) {
            Object v = model.get("table");
            if (!(v instanceof List)) {
                continue;
            }
            List<?> rows = (List<?>) v;
            // 模板行清空
            for (XWPFTableCell cell : a.row.getTableCells()) {
                cell.setText("");
            }
            for (int i = 0; i < rows.size(); i++) {
                Object rowData = rows.get(i);
                if (!(rowData instanceof Map)) {
                    continue;
                }
                Map<?, ?> map = (Map<?, ?>) rowData;
                XWPFTableRow targetRow;
                if (i == 0) {
                    targetRow = a.row;
                } else {
                    XWPFTableRow clone = a.table.createRow();
                    // 拷贝模板行的样式 / 单元格数（仅当原行有表头时需要）
                    int templateCells = a.row.getTableCells().size();
                    while (clone.getTableCells().size() < templateCells) {
                        clone.addNewTableCell();
                    }
                    targetRow = clone;
                }
                int col = 0;
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    XWPFTableCell cell = targetRow.getCell(col);
                    if (cell == null) {
                        cell = targetRow.addNewTableCell();
                    }
                    cell.setText(toText(e.getValue()));
                    col++;
                }
            }
        }
    }

    private static String toText(Object v) {
        return v == null ? "" : v.toString();
    }

    private static void replaceAllRuns(XWPFParagraph p, String text) {
        // 清除所有 run，写一个新 run 进去；保样式由第一个 run 继承
        List<XWPFRun> runs = p.getRuns();
        if (runs.isEmpty()) {
            p.createRun().setText(text);
            return;
        }
        XWPFRun first = runs.get(0);
        for (int i = 1; i < runs.size(); i++) {
            p.removeRun(i);
        }
        first.setText(text, 0);
    }

    /**
     * 构造一个最小占位符模型。
     *
     * @return 有序 Map
     */
    public static Map<String, Object> model() {
        return new LinkedHashMap<>();
    }

    private static final class ListParagraphAction {
        final XWPFParagraph paragraph;
        final String base;

        ListParagraphAction(XWPFParagraph paragraph, String base) {
            this.paragraph = paragraph;
            this.base = base;
        }
    }

    private static final class TableRowAction {
        final XWPFTable table;
        final XWPFTableRow row;

        TableRowAction(XWPFTable table, XWPFTableRow row) {
            this.table = table;
            this.row = row;
        }
    }
}
