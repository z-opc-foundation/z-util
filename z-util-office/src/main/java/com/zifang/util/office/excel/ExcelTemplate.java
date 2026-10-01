package com.zifang.util.office.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
 * Excel 模板引擎（v0）。在内存中按单元格扫描，把占位符替换成数据后写回工作簿。
 *
 * <p>支持三种占位符：
 * <ul>
 *   <li>{@code ${var}} —— 简单变量替换为 toString 结果</li>
 *   <li>{@code ${list[*]}} —— 占位符所在单元格被"垂直展开"为该 list 的逐项字符串</li>
 *   <li>{@code ${table}} —— 占位符所在行被"水平展开"为该 Map 一行：key/value 交替填入单元格</li>
 * </ul>
 *
 * <p>占位符按单元格粒度匹配；同一单元格出现多个占位符时只识别第一个。引擎只覆盖"创建 + 修改"两条路。
 */
public class ExcelTemplate {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^${}]+)\\}");

    /**
     * 把模板流渲染后写入输出流。**不关闭任何流**。
     *
     * @param templateIn 模板 .xlsx 输入流
     * @param out        渲染结果输出流
     * @param model      数据模型；占位符名 → 值；list[*] 期望 List；table 期望 Map
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
        try (Workbook wb = new XSSFWorkbook(templateIn)) {
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                renderSheet(wb.getSheetAt(s), model);
            }
            wb.write(out);
        }
    }

    /**
     * 在已有的工作簿上原地渲染占位符。
     *
     * @param workbook 工作簿
     * @param model    数据模型
     */
    public static void renderInPlace(Workbook workbook, Map<String, Object> model) {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
            renderSheet(workbook.getSheetAt(s), model);
        }
    }

    private static void renderSheet(Sheet sheet, Map<String, Object> model) {
        if (sheet == null) {
            return;
        }
        List<ListAction> listActions = new ArrayList<>();
        List<TableAction> tableActions = new ArrayList<>();
        // 从下到上扫描：list[*] / table 的副作用只往下扩展
        for (int r = sheet.getLastRowNum(); r >= 0; r--) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = row.getLastCellNum() - 1; c >= 0; c--) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    continue;
                }
                String text = readAsString(cell);
                if (text == null) {
                    continue;
                }
                Matcher m = PLACEHOLDER.matcher(text);
                if (!m.find()) {
                    continue;
                }
                String key = m.group(1).trim();
                String head = text.substring(0, m.start());
                String tail = text.substring(m.end());
                if (key.endsWith("[*]")) {
                    String base = key.substring(0, key.length() - 3);
                    listActions.add(new ListAction(r, c, base));
                    cell.setCellValue(head + tail);
                } else if ("table".equals(key)) {
                    tableActions.add(new TableAction(r, c));
                    cell.setCellValue(head + tail);
                } else {
                    Object v = model.get(key);
                    cell.setCellValue(head + (v == null ? "" : v.toString()) + tail);
                }
            }
        }
        // 应用 list[*]
        for (ListAction a : listActions) {
            Object v = model.get(a.base);
            if (!(v instanceof List)) {
                continue;
            }
            List<?> list = (List<?>) v;
            Row firstRow = sheet.getRow(a.row);
            if (firstRow != null) {
                Cell firstCell = firstRow.getCell(a.col);
                if (firstCell != null) {
                    firstCell.setBlank();
                }
            }
            for (int i = 0; i < list.size(); i++) {
                Row targetRow = sheet.getRow(a.row + i);
                if (targetRow == null) {
                    targetRow = sheet.createRow(a.row + i);
                }
                Cell targetCell = targetRow.getCell(a.col);
                if (targetCell == null) {
                    targetCell = targetRow.createCell(a.col);
                }
                Object item = list.get(i);
                targetCell.setCellValue(item == null ? "" : item.toString());
            }
        }
        // 应用 table
        for (TableAction a : tableActions) {
            Object v = model.get("table");
            if (!(v instanceof Map)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) v;
            Row row = sheet.getRow(a.row);
            if (row == null) {
                row = sheet.createRow(a.row);
            }
            int col = a.col + 1;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                writeString(row, col++, e.getKey() == null ? "" : e.getKey().toString());
                writeString(row, col++, e.getValue() == null ? "" : e.getValue().toString());
            }
        }
    }

    private static void writeString(Row row, int col, String text) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            cell = row.createCell(col);
        }
        cell.setCellValue(text);
    }

    private static String readAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    /**
     * 构造一个最小占位符模型，便于测试 / 调用方快速组装。
     *
     * @return 有序 Map
     */
    public static Map<String, Object> model() {
        return new LinkedHashMap<>();
    }

    private static final class ListAction {
        final int row;
        final int col;
        final String base;

        ListAction(int row, int col, String base) {
            this.row = row;
            this.col = col;
            this.base = base;
        }
    }

    private static final class TableAction {
        final int row;
        final int col;

        TableAction(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
