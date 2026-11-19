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
}
