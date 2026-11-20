package com.zifang.util.doc;

import java.util.List;

/** 文档块级元素。sealed 限定到本包的七种实现，导出器按类型分发。 */
public sealed interface DocBlock {

    /** 普通段落。 */
    record Paragraph(List<DocSpan> spans) implements DocBlock {
        public Paragraph(String text) {
            this(List.of(DocSpan.plain(text)));
        }
    }

    /** 标题，level 1..6。 */
    record Heading(int level, List<DocSpan> spans) implements DocBlock {
        public Heading {
            if (level < 1) {
                level = 1;
            }
            if (level > 6) {
                level = 6;
            }
        }
        public Heading(int level, String text) {
            this(level, List.of(DocSpan.plain(text)));
        }
    }

    /** 列表项，ordered=false 圆点 / true 有序；level 从 0 起的嵌套层级。 */
    record ListItem(boolean ordered, int level, List<DocSpan> spans) implements DocBlock {
    }

    /** 引用块。 */
    record Quote(List<DocSpan> spans) implements DocBlock {
    }

    /** 代码块。 */
    record CodeBlock(String language, String text) implements DocBlock {
    }

    /** 表格。rows 至少一行；header 行由读取器判定，导出器用它生成表头。 */
    record Table(List<Row> rows) implements DocBlock {

        public record Row(List<Cell> cells, boolean header) {
        }

        public record Cell(List<DocBlock> blocks, int colspan, int rowspan) {
            public Cell growRowspan() {
                return new Cell(blocks, colspan, rowspan + 1);
            }

            public static Cell of(String text) {
                return new Cell(List.of(new Paragraph(text)), 1, 1);
            }
        }

        public static Table of(List<List<String>> rows) {
            List<Row> r = rows.stream()
                    .map(row -> new Row(row.stream().map(Cell::of).toList(), false))
                    .toList();
            return new Table(r);
        }
    }

    /** 图片：原始字节 + 类型，不负责解码渲染。width/height 可空（像素）。 */
    record Image(String name, String mimeType, byte[] data,
                 Integer width, Integer height) implements DocBlock {
    }

    /** 电子表格页（Excel 工作表）：保留行列位置与单元格类型，区别于无类型的 Table。 */
    record Sheet(String name, List<SheetRow> rows, boolean hidden) implements DocBlock {

        public Sheet {
            rows = rows == null ? List.of() : List.copyOf(rows);
        }

        /** 一行；index 是 1 起的 Excel 行号。 */
        public record SheetRow(int index, List<SheetCell> cells) {
            public SheetRow {
                cells = cells == null ? List.of() : List.copyOf(cells);
            }
        }

        /** 一个单元格；column 是 0 起的列号（A=0），text 是显示值，formula 非空表示公式单元格。 */
        public record SheetCell(int column, CellType type, String text, String formula,
                                int colspan, int rowspan) {

            public SheetCell(int column, CellType type, String text, String formula) {
                this(column, type, text, formula, 1, 1);
            }

            public SheetCell {
                text = text == null ? "" : text;
                colspan = Math.max(1, colspan);
                rowspan = Math.max(1, rowspan);
            }

            public static SheetCell of(int column, CellType type, String text) {
                return new SheetCell(column, type, text, null, 1, 1);
            }

            public static SheetCell formula(int column, String formula, String cachedText) {
                return new SheetCell(column, CellType.FORMULA, cachedText, formula, 1, 1);
            }

            public boolean blank() {
                return type == CellType.BLANK && text.isEmpty() && formula == null;
            }
        }

        /** 单元格类型。 */
        public enum CellType {
            BLANK, NUMBER, STRING, BOOLEAN, DATE, TIME, ERROR, FORMULA
        }
    }

    /** 演示文稿：一串幻灯片。 */
    record Slides(List<Slide> slides) implements DocBlock {

        public Slides {
            slides = slides == null ? List.of() : List.copyOf(slides);
        }

        /** 一页幻灯片；index 从 1 起，title 可空。 */
        public record Slide(int index, String title, List<DocBlock> blocks) {
            public Slide {
                blocks = blocks == null ? List.of() : List.copyOf(blocks);
            }
        }
    }
}
