package com.zifang.util.office.excel;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ExcelTemplate 的单元测试 + 真实往返验证：
 * 构造模板 → 渲染 → 重读 → 单元格内容等于期望串。
 */
public class ExcelTemplateTest {

    @Test
    public void render_simpleVar_replacesInCell() throws Exception {
        byte[] template = templateBytesWith("${name}", "${age}");
        Map<String, Object> model = new HashMap<>();
        model.put("name", "Alice");
        model.put("age", 30);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelTemplate.render(new ByteArrayInputStream(template), out, model);

        List<List<String>> data = ExcelUtils.readFirstSheet(
                new ByteArrayInputStream(out.toByteArray()), "xlsx", 0);
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("Alice", data.get(0).get(0));
        assertEquals("30", data.get(0).get(1));
    }

    @Test
    public void render_listVar_expandsVertically() throws Exception {
        byte[] template = templateBytesWith("header", "${items[*]}");
        Map<String, Object> model = new HashMap<>();
        model.put("items", Arrays.asList("a", "b", "c"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelTemplate.render(new ByteArrayInputStream(template), out, model);

        List<List<String>> data = ExcelUtils.readFirstSheet(
                new ByteArrayInputStream(out.toByteArray()), "xlsx", 0);
        // 第一行 header 在第 0 行；${items[*]} 从第 0 行第 1 列展开为 a/b/c，
        // 由于 list[*] 把 suffix 写成 "" 后再逐项覆盖，header 行在第 0 行，
        // 列表 a 在 (0,1), b 在 (1,1), c 在 (2,1)
        // 取所有非空单元格：
        assertEquals("header", data.get(0).get(0));
        assertEquals("a", data.get(0).get(1));
        assertEquals("b", data.get(1).get(1));
        assertEquals("c", data.get(2).get(1));
    }

    @Test
    public void render_table_expandsHorizontally() throws Exception {
        byte[] template = templateBytesWith("row ${table}");
        Map<String, Object> model = new HashMap<>();
        Map<String, Object> table = new HashMap<>();
        table.put("k1", "v1");
        table.put("k2", "v2");
        model.put("table", table);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelTemplate.render(new ByteArrayInputStream(template), out, model);

        List<List<String>> data = ExcelUtils.readFirstSheet(
                new ByteArrayInputStream(out.toByteArray()), "xlsx", 0);
        assertEquals("row ", data.get(0).get(0));
        assertEquals("k1", data.get(0).get(1));
        assertEquals("v1", data.get(0).get(2));
        assertEquals("k2", data.get(0).get(3));
        assertEquals("v2", data.get(0).get(4));
    }

    @Test
    public void renderInPlace_modifiesWorkbook() {
        Workbook wb = ExcelUtils.createWorkbook("xlsx");
        Sheet sheet = ExcelUtils.writeSheet(wb, "s",
                Arrays.asList(Arrays.asList("${a}", "${b}")));
        Map<String, Object> model = new HashMap<>();
        model.put("a", "X");
        model.put("b", "Y");
        ExcelTemplate.renderInPlace(wb, model);
        assertEquals("X", ExcelUtils.getCellValue(sheet.getRow(0).getCell(0), null));
        assertEquals("Y", ExcelUtils.getCellValue(sheet.getRow(0).getCell(1), null));
    }

    private static byte[] templateBytesWith(String... cellValues) throws Exception {
        try (Workbook wb = ExcelUtils.createWorkbook("xlsx")) {
            Sheet sheet = wb.createSheet("s");
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
            for (int i = 0; i < cellValues.length; i++) {
                row.createCell(i).setCellValue(cellValues[i]);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}
