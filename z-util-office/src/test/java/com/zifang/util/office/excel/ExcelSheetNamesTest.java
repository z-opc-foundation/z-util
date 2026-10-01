package com.zifang.util.office.excel;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * ExcelUtils.readSheetNames 的往返测试。
 */
public class ExcelSheetNamesTest {

    @Test
    public void readSheetNames_roundTrip() throws Exception {
        byte[] xlsx = workbookWith("alpha", "beta", "gamma");
        List<String> names = ExcelUtils.readSheetNames(new ByteArrayInputStream(xlsx), "xlsx");
        assertEquals(Arrays.asList("alpha", "beta", "gamma"), names);
    }

    @Test
    public void readSheetNames_singleSheet() throws Exception {
        byte[] xlsx = workbookWith("only");
        List<String> names = ExcelUtils.readSheetNames(new ByteArrayInputStream(xlsx), "xlsx");
        assertEquals(1, names.size());
        assertEquals("only", names.get(0));
    }

    private static byte[] workbookWith(String... sheetNames) throws Exception {
        try (org.apache.poi.ss.usermodel.Workbook wb = ExcelUtils.createWorkbook("xlsx")) {
            for (String name : sheetNames) {
                ExcelUtils.writeSheet(wb, name, Arrays.asList(Arrays.asList("a", "b")));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}
