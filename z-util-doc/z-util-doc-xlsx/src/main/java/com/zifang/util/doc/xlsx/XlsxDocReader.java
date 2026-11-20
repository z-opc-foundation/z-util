package com.zifang.util.doc.xlsx;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocParseException;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.xml.XmlSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Excel 2007+（xlsx）读取器。零第三方依赖：JDK Zip 解包 + DOM 解析。
 * 覆盖：多工作表、共享字符串、内联字符串、公式与其缓存值、数字/布尔/错误、日期序列号与自定义日期格式、
 * 合并单元格、隐藏工作表、docProps 元数据。
 */
public final class XlsxDocReader implements DocReader {

    @Override
    public DocFormat format() {
        return DocFormat.XLSX;
    }

    @Override
    public boolean supports(byte[] data) {
        return DocFormatDetector.detectZipContainer(data) == DocFormat.XLSX;
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        Map<String, byte[]> entries = XlsxSupport.readZip(data);
        byte[] workbookXml = entries.get("xl/workbook.xml");
        if (workbookXml == null) {
            throw new DocParseException("不是 xlsx：缺少 xl/workbook.xml");
        }
        List<String> sharedStrings = readSharedStrings(entries.get("xl/sharedStrings.xml"));
        Map<Integer, NumFmt> styleFormats = readStyleFormats(entries.get("xl/styles.xml"));
        Map<String, String> rels = readWorkbookRels(entries.get("xl/_rels/workbook.xml.rels"));

        Document workbook = XmlSupport.parse(workbookXml);
        DocDocument doc = new DocDocument();
        readCoreProperties(entries.get("docProps/core.xml")).ifPresent(props -> {
            doc.getMeta().setTitle(props.get("title"));
            doc.getMeta().setAuthor(props.get("creator"));
            doc.getMeta().setSubject(props.get("subject"));
            doc.getMeta().setKeywords(props.get("keywords"));
        });

        Element sheetsRoot = XmlSupport.child(workbook.getDocumentElement(), "sheets");
        for (Element sheet : XmlSupport.children(sheetsRoot, "sheet")) {
            String name = XmlSupport.attr(sheet, "name");
            String relId = XmlSupport.attr(sheet, "id");
            boolean hidden = "hidden".equals(XmlSupport.attr(sheet, "state"))
                    || "veryHidden".equals(XmlSupport.attr(sheet, "state"));
            String target = rels.get(relId);
            if (target == null) {
                continue;
            }
            byte[] sheetXml = entries.get(normalizeTarget(target));
            if (sheetXml == null) {
                continue;
            }
            doc.add(readSheet(name, hidden, sheetXml, sharedStrings, styleFormats));
        }
        return doc;
    }

    /** 工作表 → 带类型的行/单元格。 */
    private DocBlock.Sheet readSheet(String name, boolean hidden, byte[] sheetXml,
                                     List<String> sharedStrings, Map<Integer, NumFmt> styleFormats) throws IOException {
        Document sheet = XmlSupport.parse(sheetXml);
        Element root = sheet.getDocumentElement();

        Map<String, int[]> merges = new HashMap<>();
        Element mergeCells = XmlSupport.child(root, "mergeCells");
        for (Element merge : XmlSupport.children(mergeCells, "mergeCell")) {
            String ref = XmlSupport.attr(merge, "ref");
            if (ref == null || !ref.contains(":")) {
                continue;
            }
            String[] parts = ref.split(":");
            int c1 = XlsxSupport.columnOf(parts[0]);
            int r1 = XlsxSupport.rowOf(parts[0]);
            int c2 = XlsxSupport.columnOf(parts[1]);
            int r2 = XlsxSupport.rowOf(parts[1]);
            merges.put(parts[0], new int[]{c2 - c1 + 1, r2 - r1 + 1});
        }
        Set<String> covered = new HashSet<>();
        for (Map.Entry<String, int[]> e : merges.entrySet()) {
            String ref = e.getKey();
            int c1 = XlsxSupport.columnOf(ref);
            int r1 = XlsxSupport.rowOf(ref);
            for (int r = r1; r < r1 + e.getValue()[1]; r++) {
                for (int c = c1; c < c1 + e.getValue()[0]; c++) {
                    String cellRef = XlsxSupport.cellRef(r, c);
                    if (!cellRef.equals(ref)) {
                        covered.add(cellRef);
                    }
                }
            }
        }

        Element sheetData = XmlSupport.child(root, "sheetData");
        List<DocBlock.Sheet.SheetRow> rows = new ArrayList<>();
        for (Element row : XmlSupport.children(sheetData, "row")) {
            int rowIndex = intAttr(row, "r", rows.size() + 1);
            List<DocBlock.Sheet.SheetCell> cells = new ArrayList<>();
            int autoColumn = 0;
            for (Element cell : XmlSupport.children(row, "c")) {
                String ref = XmlSupport.attr(cell, "r");
                int column = ref != null ? XlsxSupport.columnOf(ref) : autoColumn;
                autoColumn = column + 1;
                if (covered.contains(XlsxSupport.cellRef(rowIndex, column))) {
                    continue;
                }
                DocBlock.Sheet.SheetCell parsed = readCell(cell, column, sharedStrings, styleFormats);
                if (parsed == null) {
                    continue;
                }
                int[] span = ref == null ? null : merges.get(ref);
                cells.add(span == null
                        ? parsed
                        : new DocBlock.Sheet.SheetCell(parsed.column(), parsed.type(), parsed.text(),
                                parsed.formula(), span[0], span[1]));
            }
            if (!cells.isEmpty()) {
                rows.add(new DocBlock.Sheet.SheetRow(rowIndex, cells));
            }
        }
        return new DocBlock.Sheet(name, rows, hidden);
    }

    private DocBlock.Sheet.SheetCell readCell(Element cell, int column, List<String> sharedStrings,
                                              Map<Integer, NumFmt> styleFormats) {
        String type = XmlSupport.attr(cell, "t");
        String styleRef = XmlSupport.attr(cell, "s");
        int styleIndex = styleRef == null ? 0 : parseInt(styleRef, 0);
        NumFmt fmt = styleFormats.get(styleIndex);
        String formatCode = fmt == null ? null : fmt.code();

        String formula = null;
        Element f = XmlSupport.child(cell, "f");
        if (f != null) {
            formula = XmlSupport.text(f);
            if (formula == null || formula.isEmpty()) {
                formula = null;
            }
        }
        Element v = XmlSupport.child(cell, "v");
        String raw = v == null ? null : XmlSupport.text(v);

        if ("inlineStr".equals(type)) {
            Element is = XmlSupport.child(cell, "is");
            String text = is == null ? "" : XmlSupport.text(is);
            return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.STRING, text, formula);
        }
        if ("s".equals(type)) {
            int index = parseInt(raw, -1);
            String text = index >= 0 && index < sharedStrings.size() ? sharedStrings.get(index) : "";
            return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.STRING, text, formula);
        }
        if ("str".equals(type)) {
            return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.STRING, raw == null ? "" : raw, formula);
        }
        if ("b".equals(type)) {
            String text = "1".equals(raw) ? "TRUE" : "FALSE";
            return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.BOOLEAN, text, formula);
        }
        if ("e".equals(type)) {
            return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.ERROR, raw == null ? "" : raw, formula);
        }
        if ("d".equals(type)) {
            String text = raw == null ? "" : raw;
            return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.DATE, text, formula);
        }
        if (raw == null || raw.isEmpty()) {
            if (formula == null) {
                return null;
            }
            return DocBlock.Sheet.SheetCell.formula(column, formula, "");
        }
        double number = parseDouble(raw);
        if (XlsxSupport.isDateFormat(fmt == null ? 0 : fmt.numFmtId(), formatCode)) {
            LocalDateTime dateTime = XlsxSupport.serialToDateTime(number);
            boolean timeOnly = XlsxSupport.isTimeOnly(fmt == null ? 0 : fmt.numFmtId(), formatCode);
            String text = XlsxSupport.formatDate(dateTime, timeOnly);
            return new DocBlock.Sheet.SheetCell(column,
                    timeOnly ? DocBlock.Sheet.CellType.TIME : DocBlock.Sheet.CellType.DATE, text, formula);
        }
        String text = number == Math.rint(number) && !raw.contains("E") && !raw.contains("e")
                ? Long.toString((long) number)
                : raw;
        if (formula != null) {
            return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.FORMULA, text, formula);
        }
        return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.NUMBER, text, formula);
    }

    private List<String> readSharedStrings(byte[] xml) throws IOException {
        List<String> strings = new ArrayList<>();
        if (xml == null) {
            return strings;
        }
        Document sst = XmlSupport.parse(xml);
        for (Element si : XmlSupport.children(sst.getDocumentElement(), "si")) {
            StringBuilder sb = new StringBuilder();
            for (Element t : XmlSupport.descendants(si, "t")) {
                sb.append(XmlSupport.text(t));
            }
            strings.add(sb.toString());
        }
        return strings;
    }

    /** 样式索引 → 数字格式（内置日期格式靠 numFmtId，自定义靠 formatCode）。 */
    private Map<Integer, NumFmt> readStyleFormats(byte[] xml) throws IOException {
        Map<Integer, NumFmt> result = new HashMap<>();
        if (xml == null) {
            return result;
        }
        Document styles = XmlSupport.parse(xml);
        Element root = styles.getDocumentElement();
        Map<Integer, String> customFmts = new HashMap<>();
        Element numFmts = XmlSupport.child(root, "numFmts");
        for (Element numFmt : XmlSupport.children(numFmts, "numFmt")) {
            int id = intAttr(numFmt, "numFmtId", -1);
            if (id >= 0) {
                customFmts.put(id, XmlSupport.attr(numFmt, "formatCode"));
            }
        }
        Element cellXfs = XmlSupport.child(root, "cellXfs");
        List<Element> xfs = XmlSupport.children(cellXfs, "xf");
        for (int i = 0; i < xfs.size(); i++) {
            int numFmtId = intAttr(xfs.get(i), "numFmtId", 0);
            result.put(i, new NumFmt(numFmtId, customFmts.get(numFmtId)));
        }
        return result;
    }

    private record NumFmt(int numFmtId, String code) {
    }

    private Map<String, String> readWorkbookRels(byte[] xml) throws IOException {
        Map<String, String> rels = new HashMap<>();
        if (xml == null) {
            return rels;
        }
        Document doc = XmlSupport.parse(xml);
        for (Element rel : XmlSupport.children(doc.getDocumentElement(), "Relationship")) {
            String id = XmlSupport.attr(rel, "Id");
            String target = XmlSupport.attr(rel, "Target");
            if (id != null && target != null) {
                rels.put(id, target);
            }
        }
        return rels;
    }

    private java.util.Optional<Map<String, String>> readCoreProperties(byte[] xml) throws IOException {
        if (xml == null) {
            return java.util.Optional.empty();
        }
        Document doc = XmlSupport.parse(xml);
        Map<String, String> props = new HashMap<>();
        Element root = doc.getDocumentElement();
        props.put("title", XmlSupport.text(XmlSupport.child(root, "title")));
        props.put("creator", XmlSupport.text(XmlSupport.child(root, "creator")));
        props.put("subject", XmlSupport.text(XmlSupport.child(root, "subject")));
        props.put("keywords", XmlSupport.text(XmlSupport.child(root, "keywords")));
        return java.util.Optional.of(props);
    }

    /** rels 里的 Target 可能是 "worksheets/sheet1.xml" 或 "/xl/worksheets/sheet1.xml"。 */
    private String normalizeTarget(String target) {
        String t = target.startsWith("/") ? target.substring(1) : "xl/" + target;
        return t.replace("/./", "/");
    }

    private static int intAttr(Element element, String name, int fallback) {
        String value = XmlSupport.attr(element, name);
        return value == null ? fallback : parseInt(value, fallback);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
