package com.zifang.util.doc.xlsx;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * xlsx 写入器：零第三方依赖生成 Excel 2007+ 工作簿。
 * 支持字符串、数字、布尔、日期、公式、多工作表；产物可被 Excel/WPS/LibreOffice 正常打开。
 */
public final class XlsxWriter {

    private static final String NS_MAIN = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String NS_REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String NS_PKG_REL = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final String NS_CT = "http://schemas.openxmlformats.org/package/2006/content-types";

    private final List<SheetData> sheets = new ArrayList<>();
    private final Map<String, Integer> sharedStrings = new LinkedHashMap<>();
    private final List<String> sharedStringValues = new ArrayList<>();
    private SheetData current;

    public XlsxWriter() {
        sheet("Sheet1");
    }

    /** 新建（并切换到）一个工作表。 */
    public XlsxWriter sheet(String name) {
        current = new SheetData(name);
        sheets.add(current);
        return this;
    }

    public XlsxWriter text(int row, int column, String value) {
        current.cells.computeIfAbsent(row, k -> new TreeMap<>())
                .put(column, Cell.of(CellKind.STRING, value));
        return this;
    }

    public XlsxWriter number(int row, int column, double value) {
        current.cells.computeIfAbsent(row, k -> new TreeMap<>())
                .put(column, Cell.of(CellKind.NUMBER, trimNumber(value)));
        return this;
    }

    public XlsxWriter bool(int row, int column, boolean value) {
        current.cells.computeIfAbsent(row, k -> new TreeMap<>())
                .put(column, Cell.of(CellKind.BOOLEAN, value ? "1" : "0"));
        return this;
    }

    public XlsxWriter date(int row, int column, LocalDate value) {
        return dateTime(row, column, value.atStartOfDay());
    }

    public XlsxWriter dateTime(int row, int column, LocalDateTime value) {
        current.cells.computeIfAbsent(row, k -> new TreeMap<>())
                .put(column, Cell.of(CellKind.DATE, trimNumber(XlsxSupport.dateTimeToSerial(value))));
        return this;
    }

    /** 写公式（不写缓存值，由 Excel 重算）。 */
    public XlsxWriter formula(int row, int column, String formula) {
        current.cells.computeIfAbsent(row, k -> new TreeMap<>())
                .put(column, Cell.of(CellKind.FORMULA, formula));
        return this;
    }

    public byte[] build() throws IOException {
        // 工作表先渲染，共享字符串在渲染过程中收集
        Map<String, byte[]> sheetXmls = new LinkedHashMap<>();
        for (int i = 0; i < sheets.size(); i++) {
            sheetXmls.put("xl/worksheets/sheet" + (i + 1) + ".xml", bytes(sheetXml(sheets.get(i))));
        }
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("[Content_Types].xml", bytes(contentTypes()));
        entries.put("_rels/.rels", bytes(rootRels()));
        entries.put("docProps/core.xml", bytes(coreProperties()));
        entries.put("xl/workbook.xml", bytes(workbookXml()));
        entries.put("xl/_rels/workbook.xml.rels", bytes(workbookRels()));
        entries.put("xl/styles.xml", bytes(stylesXml()));
        entries.put("xl/sharedStrings.xml", bytes(sharedStringsXml()));
        entries.putAll(sheetXmls);
        return XlsxSupport.writeZip(entries);
    }

    private String contentTypes() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<Types xmlns=\"").append(NS_CT).append("\">")
                .append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
                .append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
                .append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        for (int i = 0; i < sheets.size(); i++) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i + 1)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        sb.append("<Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>")
                .append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>")
                .append("<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>")
                .append("</Types>");
        return sb.toString();
    }

    private String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"" + NS_PKG_REL + "\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/>"
                + "</Relationships>";
    }

    private String coreProperties() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\""
                + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"
                + "<dc:title></dc:title><dc:creator>z-util</dc:creator>"
                + "</cp:coreProperties>";
    }

    private String workbookXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<workbook xmlns=\"").append(NS_MAIN).append("\" xmlns:r=\"").append(NS_REL).append("\">")
                .append("<sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            sb.append("<sheet name=\"").append(escape(sheets.get(i).name))
                    .append("\" sheetId=\"").append(i + 1)
                    .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        sb.append("</sheets></workbook>");
        return sb.toString();
    }

    private String workbookRels() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<Relationships xmlns=\"").append(NS_PKG_REL).append("\">");
        for (int i = 0; i < sheets.size(); i++) {
            sb.append("<Relationship Id=\"rId").append(i + 1)
                    .append("\" Type=\"").append(NS_REL).append("/worksheet\" Target=\"worksheets/sheet")
                    .append(i + 1).append(".xml\"/>");
        }
        sb.append("<Relationship Id=\"rId").append(sheets.size() + 1)
                .append("\" Type=\"").append(NS_REL).append("/sharedStrings\" Target=\"sharedStrings.xml\"/>")
                .append("<Relationship Id=\"rId").append(sheets.size() + 2)
                .append("\" Type=\"").append(NS_REL).append("/styles\" Target=\"styles.xml\"/>")
                .append("</Relationships>");
        return sb.toString();
    }

    private String stylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"" + NS_MAIN + "\">"
                + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"yyyy\\-mm\\-dd\\ hh:mm:ss\"/></numFmts>"
                + "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill></fills>"
                + "<borders count=\"1\"><border/></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"2\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    private String sharedStringsXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<sst xmlns=\"").append(NS_MAIN).append("\" count=\"")
                .append(sharedStringValues.size()).append("\" uniqueCount=\"")
                .append(sharedStringValues.size()).append("\">");
        for (String value : sharedStringValues) {
            sb.append("<si><t xml:space=\"preserve\">").append(escape(value)).append("</t></si>");
        }
        sb.append("</sst>");
        return sb.toString();
    }

    private String sheetXml(SheetData sheet) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<worksheet xmlns=\"").append(NS_MAIN).append("\"><sheetData>");
        for (Map.Entry<Integer, Map<Integer, Cell>> row : sheet.cells.entrySet()) {
            sb.append("<row r=\"").append(row.getKey()).append("\">");
            for (Map.Entry<Integer, Cell> cell : row.getValue().entrySet()) {
                String ref = XlsxSupport.cellRef(row.getKey(), cell.getKey());
                Cell value = cell.getValue();
                switch (value.kind) {
                    case STRING -> {
                        int index = sharedStrings.computeIfAbsent(value.value, s -> {
                            sharedStringValues.add(s);
                            return sharedStringValues.size() - 1;
                        });
                        sb.append("<c r=\"").append(ref).append("\" t=\"s\"><v>")
                                .append(index).append("</v></c>");
                    }
                    case NUMBER -> sb.append("<c r=\"").append(ref).append("\"><v>")
                            .append(value.value).append("</v></c>");
                    case BOOLEAN -> sb.append("<c r=\"").append(ref).append("\" t=\"b\"><v>")
                            .append(value.value).append("</v></c>");
                    case DATE -> sb.append("<c r=\"").append(ref).append("\" s=\"1\"><v>")
                            .append(value.value).append("</v></c>");
                    case FORMULA -> sb.append("<c r=\"").append(ref).append("\"><f>")
                            .append(escape(value.value)).append("</f></c>");
                }
            }
            sb.append("</row>");
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    private static String trimNumber(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&apos;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static byte[] bytes(String text) {
        return text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private enum CellKind { STRING, NUMBER, BOOLEAN, DATE, FORMULA }

    private record Cell(CellKind kind, String value) {
        static Cell of(CellKind kind, String value) {
            return new Cell(kind, value);
        }
    }

    private static final class SheetData {
        private final String name;
        private final Map<Integer, Map<Integer, Cell>> cells = new TreeMap<>();

        private SheetData(String name) {
            this.name = name;
        }
    }
}
