package com.zifang.util.office.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excel工具类
 * 提供Excel文档的读取、编辑、格式化等常用操作
 */
public class ExcelUtils {

    /**
     * 扩展名：xls（Excel 97-2003 工作簿）
     */
    private static final String EXT_XLS = "xls";

    /**
     * 扩展名：xlsx（Excel 2007 及以上工作簿）
     */
    private static final String EXT_XLSX = "xlsx";

    /**
     * 读取第一个工作表的全部数据，返回二维字符串列表。
     * 数值单元格按整数/小数原样转为字符串，日期单元格按内置格式转为日期字符串，
     * 公式单元格返回计算后的结果值。
     *
     * @param in           工作簿输入流（方法内不负责关闭，由调用方管理）
     * @param expandedName 扩展名，传入 xls 或 xlsx（不区分大小写）
     * @param startRowNum  起始行号（0-based），通常传 0 读全部或 1 跳过表头
     * @return 二维字符串列表，外层按行、内层按列
     * @throws IOException 读取工作簿失败时抛出
     */
    public static List<List<String>> readFirstSheet(InputStream in, String expandedName, int startRowNum)
            throws IOException {
        try (Workbook workbook = createWorkbook(in, expandedName)) {
            List<List<String>> result = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return result;
            }
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int rowNum = Math.max(startRowNum, 0); rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }
                result.add(readRow(row, evaluator));
            }
            return result;
        }
    }

    /**
     * 读取全部工作表的数据并合并，返回二维字符串列表。
     * 多个工作表的行按顺序拼接，单元格取值规则与 {@link #readFirstSheet} 一致。
     *
     * @param in           工作簿输入流（方法内不负责关闭，由调用方管理）
     * @param expandedName 扩展名，传入 xls 或 xlsx（不区分大小写）
     * @param startRowNum  起始行号（0-based），每个工作表均从该行开始读取
     * @return 二维字符串列表，外层按行、内层按列
     * @throws IOException 读取工作簿失败时抛出
     */
    public static List<List<String>> readAllSheets(InputStream in, String expandedName, int startRowNum)
            throws IOException {
        try (Workbook workbook = createWorkbook(in, expandedName)) {
            List<List<String>> result = new ArrayList<>();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet == null) {
                    continue;
                }
                for (int rowNum = Math.max(startRowNum, 0); rowNum <= sheet.getLastRowNum(); rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        continue;
                    }
                    result.add(readRow(row, evaluator));
                }
            }
            return result;
        }
    }

    /**
     * 将单元格的值转为字符串。
     * 字符串单元格内容为空串时返回 null（便于区分空单元格与空内容）；
     * 数值单元格为整数值时省略小数点，日期单元格按内置格式转为日期字符串；
     * 布尔单元格返回 true/false；公式单元格返回计算后的结果值；
     * 空白与错误单元格返回空串。
     *
     * @param cell            单元格，允许为 null
     * @param formulaEvaluator 公式求值器，为 null 时公式单元格返回空串
     * @return 单元格内容的字符串表示
     */
    public static String getCellValue(Cell cell, FormulaEvaluator formulaEvaluator) {
        if (cell == null) {
            return "";
        }
        CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA && formulaEvaluator != null) {
            CellValue value = formulaEvaluator.evaluate(cell);
            if (value == null) {
                return "";
            }
            switch (value.getCellType()) {
                case STRING:
                    return value.getStringValue();
                case BOOLEAN:
                    return String.valueOf(value.getBooleanValue());
                case NUMERIC:
                    return numberToString(value.getNumberValue());
                default:
                    return "";
            }
        }
        switch (cellType) {
            case STRING:
                String text = cell.getStringCellValue();
                return "".equals(text) ? null : text;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return formatDateCell(cell);
                }
                return numberToString(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    /**
     * 读取一行的全部单元格内容。
     *
     * @param row       行
     * @param evaluator 公式求值器
     * @return 该行各列的字符串列表
     */
    private static List<String> readRow(Row row, FormulaEvaluator evaluator) {
        List<String> cells = new ArrayList<>();
        for (int columnNum = 0; columnNum < row.getLastCellNum(); columnNum++) {
            cells.add(getCellValue(row.getCell(columnNum), evaluator));
        }
        return cells;
    }

    /**
     * 列出工作簿的全部工作表名，按工作簿内顺序。
     *
     * @param in           工作簿输入流（方法内关闭）
     * @param expandedName 扩展名 xls / xlsx（不区分大小写）
     * @return 工作表名列表
     * @throws IOException 读取工作簿失败时抛出
     */
    public static List<String> readSheetNames(InputStream in, String expandedName) throws IOException {
        try (Workbook workbook = createWorkbook(in, expandedName)) {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                names.add(workbook.getSheetName(i));
            }
            return names;
        }
    }

    /**
     * 按扩展名创建工作簿对象。
     *
     * @param in           工作簿输入流
     * @param expandedName 扩展名，xlsx 创建 XSSFWorkbook，其余（含 xls）创建 HSSFWorkbook
     * @return 工作簿对象
     * @throws IOException 读取失败时抛出
     */
    private static Workbook createWorkbook(InputStream in, String expandedName) throws IOException {
        String name = expandedName == null ? "" : expandedName.trim();
        if (EXT_XLSX.equalsIgnoreCase(name)) {
            return new XSSFWorkbook(in);
        }
        return new HSSFWorkbook(in);
    }

    /**
     * 日期单元格按内置格式转为字符串。
     * 格式 14/31/57/58 为日期样式（yyyy-MM-dd），20/32 为时间样式（HH:mm），
     * 其余日期样式按 yyyy-MM-dd HH:mm:ss 输出。
     *
     * @param cell 日期单元格
     * @return 日期字符串
     */
    private static String formatDateCell(Cell cell) {
        short format = cell.getCellStyle().getDataFormat();
        SimpleDateFormat sdf;
        if (format == 14 || format == 31 || format == 57 || format == 58) {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        } else if (format == 20 || format == 32) {
            sdf = new SimpleDateFormat("HH:mm");
        } else {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        return sdf.format(cell.getDateCellValue());
    }

    /**
     * 数值转字符串，整数值省略小数点。
     *
     * @param value 数值
     * @return 数值的字符串表示
     */
    private static String numberToString(double value) {
        long longValue = Math.round(value);
        if (value == longValue && !Double.isInfinite(value)) {
            return String.valueOf(longValue);
        }
        return String.valueOf(value);
    }

    /**
     * 按扩展名创建一个全新的工作簿（xlsx → XSSFWorkbook；其它 → HSSFWorkbook）。
     *
     * @param expandedName 扩展名 xls / xlsx（不区分大小写），传 null 时按 xlsx 处理
     * @return 新建的工作簿对象
     */
    public static Workbook createWorkbook(String expandedName) {
        String name = expandedName == null ? "" : expandedName.trim();
        return EXT_XLSX.equalsIgnoreCase(name) ? new XSSFWorkbook() : new HSSFWorkbook();
    }

    /**
     * 将工作簿内容写入输出流，**不关闭**输出流，由调用方管理。
     *
     * @param workbook 工作簿
     * @param out      输出流
     * @throws IOException 写入失败时抛出
     */
    public static void write(Workbook workbook, OutputStream out) throws IOException {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null");
        }
        if (out == null) {
            throw new IllegalArgumentException("out must not be null");
        }
        workbook.write(out);
    }

    /**
     * 在指定工作表中创建或覆盖一个单元格，并按值类型写入。
     *
     * @param sheet      工作表
     * @param rowIndex   行索引（0-based）
     * @param columnIndex 列索引（0-based）
     * @param value      写入的值，支持 String/Number/Boolean/Date/null；null 视为空白
     */
    public static void writeCell(Sheet sheet, int rowIndex, int columnIndex, Object value) {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null");
        }
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        setCellValue(cell, value);
    }

    /**
     * 在指定工作表中写入一行，按顺序填入 values。
     *
     * @param sheet    工作表
     * @param rowIndex 行索引（0-based）
     * @param values   单元格值列表，传 null 时该单元格写空白
     */
    public static void writeRow(Sheet sheet, int rowIndex, List<?> values) {
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null");
        }
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        int column = 0;
        if (values != null) {
            for (Object v : values) {
                Cell cell = row.getCell(column);
                if (cell == null) {
                    cell = row.createCell(column);
                }
                setCellValue(cell, v);
                column++;
            }
        }
    }

    /**
     * 在工作簿中创建一个新工作表并填入二维数据。第一行视为表头，作为示例保留。
     *
     * @param workbook   工作簿
     * @param sheetName  工作表名，传 null 时使用默认名 SheetN
     * @param rows       二维行数据，外层按行、内层按列；任一单元支持 String/Number/Boolean/Date/null
     * @return 创建的工作表
     */
    public static Sheet writeSheet(Workbook workbook, String sheetName, List<List<?>> rows) {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null");
        }
        Sheet sheet = sheetName == null ? workbook.createSheet() : workbook.createSheet(sheetName);
        int rowIdx = 0;
        if (rows != null) {
            for (List<?> row : rows) {
                writeRow(sheet, rowIdx, row);
                rowIdx++;
            }
        }
        return sheet;
    }

    /**
     * 按 Java 值类型给单元格赋值。null 视为空白；其它类型按对应 POI 类型写入。
     *
     * @param cell  单元格
     * @param value 值
     */
    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            return;
        }
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
            return;
        }
        if (value instanceof Date) {
            cell.setCellValue((Date) value);
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }
}
