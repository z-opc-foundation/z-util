package com.zifang.util.doc.xls;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XlsDocReaderTest {

    private final XlsDocReader reader = new XlsDocReader();

    private byte[] corpus(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/corpus/" + name)) {
            assertNotNull(in, "corpus 缺失: " + name);
            return in.readAllBytes();
        }
    }

    private List<DocBlock.Sheet> sheets(DocDocument doc) {
        return doc.getBlocks().stream().map(b -> (DocBlock.Sheet) b).toList();
    }

    private DocBlock.Sheet.SheetCell cell(DocBlock.Sheet sheet, int row, int column) {
        return sheet.rows().stream()
                .filter(r -> r.index() == row)
                .flatMap(r -> r.cells().stream())
                .filter(c -> c.column() == column)
                .findFirst()
                .orElseThrow(() -> new AssertionError("没有单元格 " + row + "," + column));
    }

    @Test
    void readsSheetNamesAndStrings() throws Exception {
        byte[] data = corpus("namesdemo.xls");
        assertTrue(reader.supports(data));
        List<DocBlock.Sheet> sheets = sheets(reader.read(data));
        assertEquals(4, sheets.size());
        assertEquals("Sheet1", sheets.get(0).name());
        assertEquals("Seamus O'Reilly", sheets.get(3).name());

        DocBlock.Sheet sheet3 = sheets.get(2);
        assertEquals("Jan", cell(sheet3, 1, 1).text());
        assertEquals("Dec", cell(sheet3, 1, 12).text());
        assertEquals("Year Tot", cell(sheet3, 1, 13).text());
        assertEquals("Sales", cell(sheet3, 2, 0).text());
        assertEquals("4321", cell(sheet3, 4, 12).text());
        assertEquals("42", cell(sheets.get(3), 1, 0).text());
    }

    @Test
    void recognisesDatesAndTimesByNumberFormat() throws Exception {
        List<DocBlock.Sheet> sheets = sheets(reader.read(corpus("Formate.xls")));
        assertEquals("Blätt1", sheets.get(0).name());
        assertEquals("ÖÄÜ", sheets.get(1).name());

        DocBlock.Sheet first = sheets.get(0);
        DocBlock.Sheet.SheetCell date = cell(first, 1, 1);
        assertEquals(DocBlock.Sheet.CellType.DATE, date.type());
        assertEquals("1907-07-03", date.text());

        DocBlock.Sheet.SheetCell time = cell(first, 4, 1);
        assertEquals(DocBlock.Sheet.CellType.TIME, time.type());
        assertEquals("06:34", time.text());

        DocBlock.Sheet.SheetCell plain = cell(first, 7, 1);
        assertEquals(DocBlock.Sheet.CellType.NUMBER, plain.type());
        assertEquals("0.974", plain.text());
    }

    @Test
    void readsFormulaCachedValues() throws Exception {
        List<DocBlock.Sheet> sheets = sheets(reader.read(corpus("formula_test_sjmachin.xls")));
        DocBlock.Sheet sheet = sheets.get(0);
        assertEquals("Sheet1", sheet.name());
        assertEquals("МОСКВА Москва", cell(sheet, 2, 1).text());

        DocBlock.Sheet.SheetCell number = cell(sheet, 3, 1);
        assertEquals(DocBlock.Sheet.CellType.NUMBER, number.type());
        assertEquals("0.14285714285714285", number.text());

        assertEquals("ABCDEF", cell(sheet, 4, 1).text());
        assertEquals(DocBlock.Sheet.CellType.BOOLEAN, cell(sheet, 6, 1).type());
        assertEquals("TRUE", cell(sheet, 6, 1).text());
        assertEquals(DocBlock.Sheet.CellType.ERROR, cell(sheet, 7, 1).type());
        assertEquals("#DIV/0!", cell(sheet, 7, 1).text());
    }

    @Test
    void decodesRkNumbersWithHundredScaling() throws Exception {
        List<DocBlock.Sheet> sheets = sheets(reader.read(corpus("profiles.xls")));
        DocBlock.Sheet sheet = sheets.stream()
                .filter(s -> "TRAVERSALCHAINAGE".equals(s.name()))
                .findFirst()
                .orElseThrow();
        assertEquals("13.5", cell(sheet, 2, 12).text());
    }

    @Test
    void readsRaggedRows() throws Exception {
        List<DocBlock.Sheet> sheets = sheets(reader.read(corpus("ragged.xls")));
        DocBlock.Sheet sheet = sheets.get(0);
        assertEquals("a", cell(sheet, 1, 0).text());
        assertEquals("c", cell(sheet, 1, 2).text());
        assertEquals("j", cell(sheet, 4, 3).text());
    }

    @Test
    void readsLegacyBiff4RawStream() throws Exception {
        byte[] data = corpus("biff4_no_format_no_window2.xls");
        assertTrue(reader.supports(data), "BIFF2-4 裸记录流也应支持");
        List<DocBlock.Sheet> sheets = sheets(reader.read(data));
        assertEquals(1, sheets.size());
        DocBlock.Sheet sheet = sheets.get(0);
        assertEquals("ID", cell(sheet, 1, 0).text());
        assertEquals("TOTAL", cell(sheet, 1, 1).text());
        assertEquals("DOG & CAT PRODUCTS", cell(sheet, 2, 1).text());
    }

    @Test
    void rejectsNonWorkbookFiles() throws Exception {
        assertFalse(reader.supports("not a workbook".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertEquals(DocFormat.XLS, reader.format());
    }

    @Test
    void exportsToMarkdownTable() throws Exception {
        String md = reader.read(corpus("namesdemo.xls")).toMarkdown();
        assertTrue(md.contains("### Sheet1"), md);
        assertTrue(md.contains("| Jan |"), md);
    }
}
