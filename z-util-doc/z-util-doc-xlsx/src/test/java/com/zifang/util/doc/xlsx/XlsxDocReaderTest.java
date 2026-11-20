package com.zifang.util.doc.xlsx;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XlsxDocReaderTest {

    private final XlsxDocReader reader = new XlsxDocReader();

    private byte[] corpus(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/corpus/" + name)) {
            assertNotNull(in, "corpus 缺失: " + name);
            return in.readAllBytes();
        }
    }

    @Test
    void readsRealWorkbookFromCorpus() throws Exception {
        byte[] data = corpus("xlsx-populate-basic.xlsx");
        assertTrue(reader.supports(data));
        DocDocument doc = reader.read(data);
        List<DocBlock> blocks = doc.getBlocks();
        assertEquals(1, blocks.size());
        DocBlock.Sheet sheet = (DocBlock.Sheet) blocks.get(0);
        assertNotNull(sheet.name());
        assertFalse(sheet.rows().isEmpty());
        assertEquals("This is neat!", sheet.rows().get(0).cells().get(0).text());
    }

    @Test
    void everyCorpusWorkbookParses() throws Exception {
        for (String name : List.of("xlsx-populate-basic.xlsx", "xlsx-populate-styles.xlsx",
                "xlsx-populate-ranges.xlsx", "xlsx-populate-ranges-template.xlsx")) {
            byte[] data = corpus(name);
            assertTrue(reader.supports(data), name);
            DocDocument doc = reader.read(data);
            assertFalse(doc.getBlocks().isEmpty(), name + " 没有解析出任何工作表");
        }
    }

    @Test
    void encryptedWorkbookIsRejectedCleanly() throws Exception {
        byte[] data = corpus("xlsx-populate-encrypted.xlsx");
        assertFalse(reader.supports(data), "加密工作簿不是 zip 容器，不能被误判为可读");
        assertThrows(DocParseException.class, () -> reader.read(data));
    }

    @Test
    void writerRoundTrip() throws Exception {
        byte[] data = new XlsxWriter()
                .text(1, 1, "name")
                .text(1, 2, "score")
                .text(2, 1, "张三")
                .number(2, 2, 95.5)
                .bool(3, 1, true)
                .date(4, 1, LocalDate.of(2023, 1, 1))
                .formula(5, 2, "SUM(B2:B2)")
                .build();

        DocDocument doc = reader.read(data);
        DocBlock.Sheet sheet = (DocBlock.Sheet) doc.getBlocks().get(0);
        assertEquals("Sheet1", sheet.name());
        assertEquals(5, sheet.rows().size());

        DocBlock.Sheet.SheetCell name = sheet.rows().get(0).cells().get(0);
        assertEquals(DocBlock.Sheet.CellType.STRING, name.type());
        assertEquals("name", name.text());

        DocBlock.Sheet.SheetCell score = sheet.rows().get(1).cells().get(1);
        assertEquals(DocBlock.Sheet.CellType.NUMBER, score.type());
        assertEquals("95.5", score.text());

        DocBlock.Sheet.SheetCell flag = sheet.rows().get(2).cells().get(0);
        assertEquals(DocBlock.Sheet.CellType.BOOLEAN, flag.type());
        assertEquals("TRUE", flag.text());

        DocBlock.Sheet.SheetCell date = sheet.rows().get(3).cells().get(0);
        assertEquals(DocBlock.Sheet.CellType.DATE, date.type());
        assertEquals("2023-01-01", date.text());

        DocBlock.Sheet.SheetCell formula = sheet.rows().get(4).cells().get(0);
        assertEquals("SUM(B2:B2)", formula.formula());
    }

    @Test
    void readsInlineBooleanErrorAndDates() throws Exception {
        byte[] data = workbook(
                "<?xml version=\"1.0\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\""
                        + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                        + "<sheets><sheet name=\"数据\" sheetId=\"1\" r:id=\"rId1\"/>"
                        + "<sheet name=\"隐藏\" sheetId=\"2\" state=\"hidden\" r:id=\"rId2\"/></sheets></workbook>",
                "<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                        + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                        + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/>"
                        + "</Relationships>",
                "<?xml version=\"1.0\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                        + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"yyyy-mm-dd\"/></numFmts>"
                        + "<cellXfs count=\"3\">"
                        + "<xf numFmtId=\"0\"/><xf numFmtId=\"14\"/><xf numFmtId=\"164\"/></cellXfs></styleSheet>",
                "<?xml version=\"1.0\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                        + "<sheetData>"
                        + "<row r=\"1\"><c r=\"A1\" t=\"inlineStr\"><is><t>内联文本</t></is></c></row>"
                        + "<row r=\"2\"><c r=\"B2\" t=\"b\"><v>1</v></c></row>"
                        + "<row r=\"3\"><c r=\"C3\" t=\"e\"><v>#DIV/0!</v></c></row>"
                        + "<row r=\"4\"><c r=\"D4\" s=\"1\"><v>44927</v></c></row>"
                        + "<row r=\"5\"><c r=\"E5\" s=\"2\"><v>45351</v></c></row>"
                        + "</sheetData>"
                        + "<mergeCells count=\"1\"><mergeCell ref=\"A1:B1\"/></mergeCells></worksheet>",
                "<?xml version=\"1.0\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                        + "<sheetData><row r=\"1\"><c r=\"A1\" t=\"inlineStr\"><is><t>隐藏表</t></is></c></row></sheetData></worksheet>");

        DocDocument doc = reader.read(data);
        assertEquals(2, doc.getBlocks().size());
        DocBlock.Sheet first = (DocBlock.Sheet) doc.getBlocks().get(0);
        assertEquals("数据", first.name());
        assertFalse(first.hidden());
        assertEquals("内联文本", first.rows().get(0).cells().get(0).text());
        assertEquals(2, first.rows().get(0).cells().get(0).colspan());
        assertEquals(DocBlock.Sheet.CellType.BOOLEAN, first.rows().get(1).cells().get(0).type());
        assertEquals("TRUE", first.rows().get(1).cells().get(0).text());
        assertEquals(DocBlock.Sheet.CellType.ERROR, first.rows().get(2).cells().get(0).type());
        assertEquals("#DIV/0!", first.rows().get(2).cells().get(0).text());
        assertEquals(DocBlock.Sheet.CellType.DATE, first.rows().get(3).cells().get(0).type());
        assertEquals("2023-01-01", first.rows().get(3).cells().get(0).text());
        assertEquals("2024-02-29", first.rows().get(4).cells().get(0).text());

        DocBlock.Sheet hidden = (DocBlock.Sheet) doc.getBlocks().get(1);
        assertEquals("隐藏", hidden.name());
        assertTrue(hidden.hidden());
    }

    @Test
    void sheetExportsToMarkdownTable() throws Exception {
        byte[] data = new XlsxWriter().text(1, 1, "name").text(1, 2, "age")
                .text(2, 1, "张三").number(2, 2, 30).build();
        String md = reader.read(data).toMarkdown();
        assertTrue(md.contains("### Sheet1"), md);
        assertTrue(md.contains("| name | age |"), md);
        assertTrue(md.contains("| 张三 | 30 |"), md);
    }

    @Test
    void formatIsXlsxAndDetectionRejectsOtherZip() throws Exception {
        assertEquals(DocFormat.XLSX, reader.format());
        assertFalse(reader.supports("not a workbook".getBytes(StandardCharsets.UTF_8)));
    }

    private byte[] workbook(String workbookXml, String relsXml, String stylesXml,
                             String sheet1Xml, String sheet2Xml) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("[Content_Types].xml", bytes("<Types/>"));
        entries.put("xl/workbook.xml", bytes(workbookXml));
        entries.put("xl/_rels/workbook.xml.rels", bytes(relsXml));
        entries.put("xl/styles.xml", bytes(stylesXml));
        entries.put("xl/worksheets/sheet1.xml", bytes(sheet1Xml));
        entries.put("xl/worksheets/sheet2.xml", bytes(sheet2Xml));
        return XlsxSupport.writeZip(entries);
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
