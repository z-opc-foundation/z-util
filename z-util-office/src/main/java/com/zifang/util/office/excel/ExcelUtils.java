package com.zifang.util.office.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
}
