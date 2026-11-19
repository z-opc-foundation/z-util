package com.zifang.util.doc.export;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocSpan;

import java.util.List;

/** DocDocument → 纯文本（去样式，表格按 tab 对齐）。 */
public class PlainTextExporter {

    public String export(DocDocument doc) {
        StringBuilder out = new StringBuilder();
        for (DocBlock block : doc.getBlocks()) {
            if (block instanceof DocBlock.Heading h) {
                out.append(render(h.spans())).append("\n\n");
            } else if (block instanceof DocBlock.Paragraph p) {
                out.append(render(p.spans())).append("\n\n");
            } else if (block instanceof DocBlock.ListItem li) {
                out.append("  ".repeat(li.level()))
                        .append(li.ordered() ? "* " : "- ")
                        .append(render(li.spans())).append('\n');
            } else if (block instanceof DocBlock.Quote q) {
                out.append(render(q.spans())).append("\n\n");
            } else if (block instanceof DocBlock.CodeBlock code) {
                out.append(code.text()).append("\n\n");
            } else if (block instanceof DocBlock.Table table) {
                for (DocBlock.Table.Row row : table.rows()) {
                    List<String> cells = row.cells().stream().map(this::cellText).toList();
                    out.append(String.join("\t", cells)).append('\n');
                }
                out.append('\n');
            } else if (block instanceof DocBlock.Image image) {
                out.append("[image: ").append(image.name()).append("]\n\n");
            }
        }
        return out.toString().stripTrailing() + "\n";
    }

    private String cellText(DocBlock.Table.Cell cell) {
        StringBuilder sb = new StringBuilder();
        for (DocBlock b : cell.blocks()) {
            if (b instanceof DocBlock.Paragraph p) {
                sb.append(render(p.spans()));
            } else if (b instanceof DocBlock.Heading h) {
                sb.append(render(h.spans()));
            } else if (b instanceof DocBlock.ListItem li) {
                sb.append(render(li.spans()));
            } else if (b instanceof DocBlock.CodeBlock code) {
                sb.append(code.text().replace('\n', ' '));
            }
        }
        return sb.toString().strip();
    }

    private String render(List<DocSpan> spans) {
        if (spans == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            sb.append(span.text());
        }
        return sb.toString();
    }
}
