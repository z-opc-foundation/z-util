package com.zifang.util.doc.export;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocSpan;

import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/** DocDocument → Markdown。 */
public class MarkdownExporter {

    private final boolean inlineImages;

    public MarkdownExporter() {
        this(false);
    }

    public MarkdownExporter(boolean inlineImages) {
        this.inlineImages = inlineImages;
    }

    public String export(DocDocument doc) {
        StringBuilder out = new StringBuilder();
        for (DocBlock block : doc.getBlocks()) {
            appendBlock(out, block);
        }
        return out.toString().stripTrailing() + "\n";
    }

    private void appendBlock(StringBuilder out, DocBlock block) {
        if (block instanceof DocBlock.Heading h) {
            out.append("#".repeat(h.level())).append(' ')
                    .append(renderSpans(h.spans())).append("\n\n");
        } else if (block instanceof DocBlock.Paragraph p) {
            out.append(renderSpans(p.spans())).append("\n\n");
        } else if (block instanceof DocBlock.ListItem li) {
            String body = renderSpans(li.spans());
            // 解析器已注入真实编号/符号标记（如 "2.3.1 " 或 "• "）时不再叠加自身标记。
            String prefix = li.ordered() ? "1. " : "- ";
            if (startsMarker(body)) {
                prefix = "";
            }
            out.append("  ".repeat(li.level())).append(prefix).append(body).append("\n\n");
        } else if (block instanceof DocBlock.Quote q) {
            out.append("> ").append(renderSpans(q.spans())).append("\n\n");
        } else if (block instanceof DocBlock.CodeBlock code) {
            out.append("```").append(code.language() == null ? "" : code.language()).append('\n')
                    .append(code.text()).append("\n```\n\n");
        } else if (block instanceof DocBlock.Table table) {
            appendTable(out, table);
        } else if (block instanceof DocBlock.Image image) {
            appendImage(out, image);
        } else if (block instanceof DocBlock.Sheet sheet) {
            appendSheet(out, sheet);
        } else if (block instanceof DocBlock.Slides slides) {
            appendSlides(out, slides);
        }
    }

    private void appendSheet(StringBuilder out, DocBlock.Sheet sheet) {
        if (sheet.name() != null && !sheet.name().isEmpty()) {
            out.append("### ").append(escape(sheet.name())).append("\n\n");
        }
        if (sheet.rows().isEmpty()) {
            return;
        }
        int cols = sheet.rows().stream()
                .flatMap(r -> r.cells().stream())
                .mapToInt(DocBlock.Sheet.SheetCell::column)
                .max().orElse(-1) + 1;
        for (int i = 0; i < sheet.rows().size(); i++) {
            List<DocBlock.Sheet.SheetCell> cells = sheet.rows().get(i).cells();
            out.append("|");
            for (int c = 0; c < cols; c++) {
                String text = "";
                for (DocBlock.Sheet.SheetCell cell : cells) {
                    if (cell.column() == c) {
                        text = sheetCellText(cell);
                        break;
                    }
                }
                out.append(' ').append(text.isEmpty() ? " " : text).append(" |");
            }
            out.append('\n');
            if (i == 0) {
                out.append("|");
                for (int c = 0; c < cols; c++) {
                    out.append(" --- |");
                }
                out.append('\n');
            }
        }
        out.append('\n');
    }

    private String sheetCellText(DocBlock.Sheet.SheetCell cell) {
        String text = cell.text();
        if (text == null || text.isEmpty()) {
            text = cell.formula() == null ? "" : "=" + cell.formula();
        }
        return text.replace("|", "\\|");
    }

    private void appendSlides(StringBuilder out, DocBlock.Slides slides) {
        for (DocBlock.Slides.Slide slide : slides.slides()) {
            out.append("## ");
            if (slide.title() != null && !slide.title().isEmpty()) {
                out.append(escape(slide.title()));
            } else {
                out.append("Slide ").append(slide.index());
            }
            out.append("\n\n");
            for (DocBlock block : slide.blocks()) {
                appendBlock(out, block);
            }
        }
    }

    private void appendTable(StringBuilder out, DocBlock.Table table) {
        List<DocBlock.Table.Row> rows = table.rows();
        if (rows.isEmpty()) {
            return;
        }
        int cols = rows.stream().mapToInt(r -> r.cells().size()).max().orElse(0);
        for (int i = 0; i < rows.size(); i++) {
            DocBlock.Table.Row row = rows.get(i);
            out.append("|");
            for (int c = 0; c < cols; c++) {
                String text = c < row.cells().size() ? cellText(row.cells().get(c)) : "";
                out.append(' ').append(text.isEmpty() ? " " : text).append(" |");
            }
            out.append('\n');
            if (i == 0) {
                out.append("|");
                for (int c = 0; c < cols; c++) {
                    out.append(" --- |");
                }
                out.append('\n');
            }
        }
        out.append('\n');
    }

    private String cellText(DocBlock.Table.Cell cell) {
        StringBuilder sb = new StringBuilder();
        for (DocBlock b : cell.blocks()) {
            if (b instanceof DocBlock.Paragraph p) {
                sb.append(renderSpans(p.spans()));
            } else if (b instanceof DocBlock.Heading h) {
                sb.append(renderSpans(h.spans()));
            } else if (b instanceof DocBlock.ListItem li) {
                sb.append(renderSpans(li.spans()));
            } else if (b instanceof DocBlock.CodeBlock code) {
                sb.append(code.text().replace('\n', ' '));
            }
            sb.append(' ');
        }
        return sb.toString().strip().replace("|", "\\|");
    }

    private void appendImage(StringBuilder out, DocBlock.Image image) {
        String alt = image.name() == null ? "image" : image.name();
        if (inlineImages && image.data() != null) {
            String mime = image.mimeType() == null ? "application/octet-stream" : image.mimeType();
            out.append("![").append(alt).append("](data:").append(mime)
                    .append(";base64,").append(Base64.getEncoder().encodeToString(image.data()))
                    .append(")\n\n");
        } else {
            out.append("![").append(alt).append("]()\n\n");
        }
    }

    private String renderSpans(List<DocSpan> spans) {
        if (spans == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            String text = escape(span.text());
            if (span.mono()) {
                sb.append('`').append(text).append('`');
                continue;
            }
            if (span.bold()) {
                sb.append("**");
            } else if (span.italic()) {
                sb.append('*');
            } else if (span.strike()) {
                sb.append("~~");
            }
            sb.append(text);
            if (span.bold()) {
                sb.append("**");
            } else if (span.italic()) {
                sb.append('*');
            } else if (span.strike()) {
                sb.append("~~");
            }
            if (span.link() != null) {
                sb.insert(0, "[").append("](").append(span.link()).append(')');
            }
        }
        return sb.toString();
    }

    private static final Pattern MARKER_PREFIX = Pattern.compile(
            "^(\\d+(\\.\\d+)*[.)]?\\s|[-•●■◦‣⁃]\\s)");

    private boolean startsMarker(String body) {
        if (body == null) {
            return false;
        }
        var m = MARKER_PREFIX.matcher(body);
        return m.find() && m.start() == 0;
    }

    private String escape(String text) {
        if (text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }
        StringBuilder sb = new StringBuilder(text.length() + 8);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' || c == '*' || c == '_' || c == '`' || c == '[' || c == ']') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
