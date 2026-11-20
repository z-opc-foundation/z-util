package com.zifang.util.doc.odf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;
import org.w3c.dom.Element;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenDocument 电子表格（.ods）读取器。零第三方依赖：JDK Zip + DOM。
 * 覆盖：多工作表、单元格类型（数值/字符串/布尔/日期/时间）、公式、合并单元格跨度、
 * 行列重复（table:number-*-repeated）、被覆盖单元格。
 */
public final class OdsDocReader implements DocReader {

    /** 行列重复展开上限，防止填充行把内存打爆。 */
    private static final int MAX_ROW_REPEAT = 1024;
    private static final int MAX_COL_REPEAT = 1024;

    @Override
    public DocFormat format() {
        return DocFormat.ODS;
    }

    @Override
    public boolean supports(byte[] data) {
        try {
            return OdfSupport.MIME_SPREADSHEET.equals(OdfSupport.mimetype(OdfSupport.entries(data)));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        Map<String, byte[]> entries = OdfSupport.entries(data);
        Element contentRoot = OdfSupport.contentRoot(entries);
        Element body = OdfSupport.bodyPart(contentRoot, "spreadsheet");
        DocDocument doc = new DocDocument();
        if (body != null) {
            for (Element table : OdfSupport.children(body, "table")) {
                doc.add(readTable(table));
            }
        }
        // master-page 页眉页脚（打印页眉）：[页眉]/[页脚] Quote 块
        Map<String, OdfSupport.Style> styles = OdfSupport.parseStyles(entries, contentRoot);
        for (OdfSupport.MasterHeaderFooter master : OdfSupport.masterHeaderFooters(entries, styles)) {
            for (String line : master.header()) {
                doc.add(new DocBlock.Quote(List.of(DocSpan.plain("[页眉] " + line))));
            }
            for (String line : master.footer()) {
                doc.add(new DocBlock.Quote(List.of(DocSpan.plain("[页脚] " + line))));
            }
        }
        return doc;
    }

    private DocBlock.Sheet readTable(Element table) {
        String name = OdfSupport.attr(table, "name");
        List<DocBlock.Sheet.SheetRow> rows = new ArrayList<>();
        int rowIndex = 0;
        for (Element row : OdfSupport.children(table, "table-row")) {
            int repeat = Math.max(1, OdfSupport.intAttr(row, "number-rows-repeated", 1));
            List<DocBlock.Sheet.SheetCell> cells = readCells(row);
            boolean empty = true;
            for (DocBlock.Sheet.SheetCell cell : cells) {
                if (!cell.blank()) {
                    empty = false;
                    break;
                }
            }
            if (empty) {
                // 填充用的空行不产出内容，只推进行号
                rowIndex += repeat;
                continue;
            }
            for (int i = 0; i < Math.min(repeat, MAX_ROW_REPEAT); i++) {
                rows.add(new DocBlock.Sheet.SheetRow(rowIndex + i + 1, cells));
            }
            rowIndex += repeat;
        }
        return new DocBlock.Sheet(name == null ? "" : name, rows, false);
    }

    private List<DocBlock.Sheet.SheetCell> readCells(Element row) {
        List<DocBlock.Sheet.SheetCell> cells = new ArrayList<>();
        int column = 0;
        for (Element cell : OdfSupport.childElements(row)) {
            String local = OdfSupport.localName(cell);
            boolean covered = "covered-table-cell".equals(local);
            if (!covered && !"table-cell".equals(local)) {
                continue;
            }
            int repeat = Math.max(1, OdfSupport.intAttr(cell, "number-columns-repeated", 1));
            int colspan = Math.max(1, OdfSupport.intAttr(cell, "number-columns-spanned", 1));
            int rowspan = Math.max(1, OdfSupport.intAttr(cell, "number-rows-spanned", 1));
            DocBlock.Sheet.SheetCell parsed = covered
                    ? new DocBlock.Sheet.SheetCell(0, DocBlock.Sheet.CellType.BLANK, "", null, colspan, rowspan)
                    : readCell(cell, 0, colspan, rowspan);
            for (int i = 0; i < Math.min(repeat, MAX_COL_REPEAT); i++) {
                cells.add(new DocBlock.Sheet.SheetCell(column, parsed.type(), parsed.text(),
                        parsed.formula(), parsed.colspan(), parsed.rowspan()));
                column++;
            }
        }
        return cells;
    }

    private DocBlock.Sheet.SheetCell readCell(Element cell, int column, int colspan, int rowspan) {
        String valueType = OdfSupport.attr(cell, "value-type");
        String formula = normalizeFormula(OdfSupport.attr(cell, "formula"));
        String display = displayText(cell);
        if (valueType == null || "void".equals(valueType)) {
            if (formula != null) {
                return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.FORMULA,
                        display, formula, colspan, rowspan);
            }
            return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.BLANK,
                    display, null, colspan, rowspan);
        }
        switch (valueType) {
            case "float", "percentage", "currency" -> {
                String text = numberText(OdfSupport.attr(cell, "value"));
                if (text == null || text.isEmpty()) {
                    text = display;
                }
                if (formula != null) {
                    return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.FORMULA,
                            text, formula, colspan, rowspan);
                }
                return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.NUMBER,
                        text, null, colspan, rowspan);
            }
            case "date" -> {
                String text = OdfSupport.attr(cell, "date-value");
                return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.DATE,
                        text == null ? display : text, formula, colspan, rowspan);
            }
            case "time" -> {
                String text = OdfSupport.attr(cell, "time-value");
                return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.TIME,
                        text == null ? display : text, formula, colspan, rowspan);
            }
            case "boolean" -> {
                String text = OdfSupport.attr(cell, "boolean-value");
                return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.BOOLEAN,
                        text == null ? display : text, formula, colspan, rowspan);
            }
            default -> {
                return new DocBlock.Sheet.SheetCell(column, DocBlock.Sheet.CellType.STRING,
                        display, formula, colspan, rowspan);
            }
        }
    }

    /** 单元格内 text:p 的显示文本，多段用换行连接。 */
    private String displayText(Element cell) {
        StringBuilder sb = new StringBuilder();
        for (Element paragraph : OdfSupport.children(cell, "p")) {
            String text = OdfSupport.text(paragraph);
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(text);
        }
        return sb.toString();
    }

    /** table:formula 形如 "of:=SUM([.A1:.A2])"，去掉命名空间前缀保留可读形式。 */
    private String normalizeFormula(String formula) {
        if (formula == null || formula.isEmpty()) {
            return null;
        }
        String value = formula.trim();
        int colon = value.indexOf(':');
        if (colon > 0 && colon + 1 < value.length() && value.charAt(colon + 1) == '=') {
            value = value.substring(colon + 1);
        }
        return value;
    }

    private String numberText(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            BigDecimal decimal = new BigDecimal(value.trim());
            return decimal.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return value.trim();
        }
    }
}
