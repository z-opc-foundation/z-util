package com.zifang.util.office.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * ExcelUtils工具类的单元测试
 */
public class ExcelUtilsTest {

    /**
     * 构造一个 xlsx 工作簿字节数组：第一个工作表 3 行 2 列，第二个工作表（存在时）2 行 2 列
     */
    private byte[] buildXlsx(boolean withSecondSheet) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("first");
            writeHeaderAndData(sheet);
            if (withSecondSheet) {
                Sheet second = workbook.createSheet("second");
                Row r0 = second.createRow(0);
                r0.createCell(0).setCellValue("a");
                r0.createCell(1).setCellValue("b");
                Row r1 = second.createRow(1);
                r1.createCell(0).setCellValue("c");
                r1.createCell(1).setCellValue("d");
            }
            return toBytes(workbook);
        }
    }

    /**
     * 构造一个 xls 工作簿字节数组：1 个工作表 3 行 2 列
     */
    private byte[] buildXls() throws IOException {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("sheet0");
            writeHeaderAndData(sheet);
            return toBytes(workbook);
        }
    }

    /**
     * 写入表头与两行数据：name/age 表头，tom/20 与 jerry/21.5 数据行
     */
    private void writeHeaderAndData(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("age");
        Row r1 = sheet.createRow(1);
        r1.createCell(0).setCellValue("tom");
        r1.createCell(1).setCellValue(20);
        Row r2 = sheet.createRow(2);
        r2.createCell(0).setCellValue("jerry");
        r2.createCell(1).setCellValue(21.5);
    }

    /**
     * 工作簿序列化为字节数组
     */
    private byte[] toBytes(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    /**
     * testReadFirstSheetXlsx方法：xlsx 从指定行读取第一个工作表
     */
    @Test
    public void testReadFirstSheetXlsx() throws IOException {
        byte[] bytes = buildXlsx(true);

        List<List<String>> all = ExcelUtils.readFirstSheet(new ByteArrayInputStream(bytes), "xlsx", 0);
        assertEquals(3, all.size());
        assertEquals("name", all.get(0).get(0));
        assertEquals("tom", all.get(1).get(0));
        assertEquals("20", all.get(1).get(1));
        assertEquals("21.5", all.get(2).get(1));

        List<List<String>> skipHeader = ExcelUtils.readFirstSheet(new ByteArrayInputStream(bytes), "XLSX", 1);
        assertEquals(2, skipHeader.size());
        assertEquals("tom", skipHeader.get(0).get(0));
    }

    /**
     * testReadFirstSheetXls方法：xls 格式读取第一个工作表
     */
    @Test
    public void testReadFirstSheetXls() throws IOException {
        byte[] bytes = buildXls();

        List<List<String>> all = ExcelUtils.readFirstSheet(new ByteArrayInputStream(bytes), "xls", 0);
        assertEquals(3, all.size());
        assertEquals("name", all.get(0).get(0));
        assertEquals("20", all.get(1).get(1));
        assertEquals("21.5", all.get(2).get(1));
    }

    /**
     * testReadAllSheetsXlsx方法：合并读取全部工作表
     */
    @Test
    public void testReadAllSheetsXlsx() throws IOException {
        byte[] bytes = buildXlsx(true);

        List<List<String>> all = ExcelUtils.readAllSheets(new ByteArrayInputStream(bytes), "xlsx", 0);
        assertEquals(5, all.size());
        assertEquals("name", all.get(0).get(0));
        assertEquals("jerry", all.get(2).get(0));
        assertEquals("a", all.get(3).get(0));
        assertEquals("d", all.get(4).get(1));

        List<List<String>> skipHeader = ExcelUtils.readAllSheets(new ByteArrayInputStream(bytes), "xlsx", 1);
        assertEquals(3, skipHeader.size());
    }

    /**
     * testGetCellValueTypes方法：各类单元格的字符串转换规则
     */
    @Test
    public void testGetCellValueTypes() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("types");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("abc");
            row.createCell(1).setCellValue("");
            row.createCell(2).setCellValue(123);
            row.createCell(3).setCellValue(1.25);
            row.createCell(4).setCellValue(true);
            // 第 5 列不创建，保持为 null 单元格

            assertEquals("abc", ExcelUtils.getCellValue(row.getCell(0), null));
            assertNull(ExcelUtils.getCellValue(row.getCell(1), null));
            assertEquals("123", ExcelUtils.getCellValue(row.getCell(2), null));
            assertEquals("1.25", ExcelUtils.getCellValue(row.getCell(3), null));
            assertEquals("true", ExcelUtils.getCellValue(row.getCell(4), null));
            assertEquals("", ExcelUtils.getCellValue(row.getCell(5), null));
            assertEquals("", ExcelUtils.getCellValue(null, null));

            List<List<String>> data = ExcelUtils.readFirstSheet(
                    new ByteArrayInputStream(toBytes(workbook)), "xlsx", 0);
            assertEquals(1, data.size());
            // 行尾未创建的列不在 lastCellNum 范围内，结果只有前 5 列
            assertEquals(5, data.get(0).size());
            assertNull(data.get(0).get(1));
        }
    }

    /**
     * testFormulaCell方法：公式单元格返回计算后的结果值
     */
    @Test
    public void testFormulaCell() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("formula");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue(1);
            Cell formulaCell = row.createCell(1);
            formulaCell.setCellFormula("A1+2");

            List<List<String>> data = ExcelUtils.readFirstSheet(
                    new ByteArrayInputStream(toBytes(workbook)), "xlsx", 0);
            assertEquals(1, data.size());
            assertEquals("3", data.get(0).get(1));
        }
    }

    /**
     * testDateCell方法：日期单元格按内置日期格式输出 yyyy-MM-dd
     */
    @Test
    public void testDateCell() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("date");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            Date date = new GregorianCalendar(2026, Calendar.AUGUST, 22).getTime();
            cell.setCellValue(date);
            CellStyle style = workbook.createCellStyle();
            style.setDataFormat((short) 14);
            cell.setCellStyle(style);

            List<List<String>> data = ExcelUtils.readFirstSheet(
                    new ByteArrayInputStream(toBytes(workbook)), "xlsx", 0);
            assertEquals(1, data.size());
            assertEquals("2026-08-22", data.get(0).get(0));
        }
    }

    /**
     * testClassExists方法：验证类可实例化
     */
    @Test
    public void testClassExists() {
        ExcelUtils excelUtils = new ExcelUtils();
        assertNotNull(excelUtils);
        assertTrue(excelUtils instanceof ExcelUtils);
    }
}
