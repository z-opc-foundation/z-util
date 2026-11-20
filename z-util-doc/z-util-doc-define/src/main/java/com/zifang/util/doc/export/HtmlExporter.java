package com.zifang.util.doc.export;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocSpan;

import java.util.Base64;
import java.util.List;

/** DocDocument → 最小 HTML（内联标签，无外部 css）。 */
public class HtmlExporter {

    public String export(DocDocument doc) {
        StringBuilder out = new StringBuilder();
        out.append("<!DOCTYPE html>\n<html>\n<body>\n");
        int i = 0;
        List<DocBlock> blocks = doc.getBlocks();
        while (i < blocks.size()) {
            DocBlock block = blocks.get(i);
            if (block instanceof DocBlock.ListItem li) {
                i = appendList(out, blocks, i);
                continue;
            }
            appendBlock(out, block);
            i++;
        }
        out.append("</body>\n</html>\n");
        return out.toString();
    }

    private int appendList(StringBuilder out, List<DocBlock> blocks, int start) {
        boolean ordered = ((DocBlock.ListItem) blocks.get(start)).ordered();
        out.append(ordered ? "<ol>\n" : "<ul>\n");
        int i = start;
        while (i < blocks.size() && blocks.get(i) instanceof DocBlock.ListItem li && li.ordered() == ordered) {
            out.append("<li>").append(renderSpans(li.spans())).append("</li>\n");
            i++;
        }
        out.append(ordered ? "</ol>\n" : "</ul>\n");
        return i;
    }

    private void appendBlock(StringBuilder out, DocBlock block) {
        if (block instanceof DocBlock.Heading h) {
            out.append("<h").append(h.level()).append('>')
                    .append(renderSpans(h.spans()))
                    .append("</h").append(h.level()).append(">\n");
        } else if (block instanceof DocBlock.Paragraph p) {
            out.append("<p>").append(renderSpans(p.spans())).append("</p>\n");
        } else if (block instanceof DocBlock.Quote q) {
            out.append("<blockquote>").append(renderSpans(q.spans())).append("</blockquote>\n");
        } else if (block instanceof DocBlock.CodeBlock code) {
            out.append("<pre><code").append(code.language() == null ? "" : " class=\"language-" + code.language() + "\"")
                    .append('>').append(escape(code.text())).append("</code></pre>\n");
        } else if (block instanceof DocBlock.Table table) {
            out.append("<table>\n");
            for (DocBlock.Table.Row row : table.rows()) {
                out.append(row.header() ? "<thead><tr>" : "<tr>");
                for (DocBlock.Table.Cell cell : row.cells()) {
                    out.append("<td");
                    if (cell.colspan() > 1) {
                        out.append(" colspan=\"").append(cell.colspan()).append('"');
                    }
                    if (cell.rowspan() > 1) {
                        out.append(" rowspan=\"").append(cell.rowspan()).append('"');
                    }
                    out.append('>').append(cellHtml(cell)).append("</td>");
                }
                out.append(row.header() ? "</tr></thead>\n" : "</tr>\n");
            }
            out.append("</table>\n");
        } else if (block instanceof DocBlock.Image image) {
            out.append("<img alt=\"").append(escape(image.name() == null ? "image" : image.name()))
                    .append("\" src=\"data:").append(image.mimeType()).append(";base64,")
                    .append(Base64.getEncoder().encodeToString(image.data())).append("\"/>\n");
        } else if (block instanceof DocBlock.Sheet sheet) {
            appendSheet(out, sheet);
        } else if (block instanceof DocBlock.Slides slides) {
            appendSlides(out, slides);
        }
    }

    private void appendSheet(StringBuilder out, DocBlock.Sheet sheet) {
        if (sheet.name() != null && !sheet.name().isEmpty()) {
            out.append("<h3>").append(escape(sheet.name())).append("</h3>\n");
        }
        int cols = sheet.rows().stream()
                .flatMap(r -> r.cells().stream())
                .mapToInt(DocBlock.Sheet.SheetCell::column)
                .max().orElse(-1) + 1;
        out.append("<table>\n");
        for (int i = 0; i < sheet.rows().size(); i++) {
            List<DocBlock.Sheet.SheetCell> cells = sheet.rows().get(i).cells();
            out.append(i == 0 ? "<thead><tr>" : "<tr>");
            for (int c = 0; c < cols; c++) {
                out.append("<td");
                for (DocBlock.Sheet.SheetCell cell : cells) {
                    if (cell.column() == c) {
                        out.append(" data-type=\"").append(cell.type().name()).append('"');
                        if (cell.formula() != null) {
                            out.append(" data-formula=\"").append(escape(cell.formula())).append('"');
                        }
                        break;
                    }
                }
                out.append('>').append(escape(sheetCellText(cells, c))).append("</td>");
            }
            out.append(i == 0 ? "</tr></thead>\n" : "</tr>\n");
        }
        out.append("</table>\n");
    }

    private String sheetCellText(List<DocBlock.Sheet.SheetCell> cells, int column) {
        for (DocBlock.Sheet.SheetCell cell : cells) {
            if (cell.column() == column) {
                if (cell.text() != null && !cell.text().isEmpty()) {
                    return cell.text();
                }
                return cell.formula() == null ? "" : "=" + cell.formula();
            }
        }
        return "";
    }

    private void appendSlides(StringBuilder out, DocBlock.Slides slides) {
        for (DocBlock.Slides.Slide slide : slides.slides()) {
            out.append("<section class=\"slide\">\n<h2>");
            if (slide.title() != null && !slide.title().isEmpty()) {
                out.append(escape(slide.title()));
            } else {
                out.append("Slide ").append(slide.index());
            }
            out.append("</h2>\n");
            for (DocBlock block : slide.blocks()) {
                appendBlock(out, block);
            }
            out.append("</section>\n");
        }
    }

    private String cellHtml(DocBlock.Table.Cell cell) {
        StringBuilder sb = new StringBuilder();
        for (DocBlock b : cell.blocks()) {
            if (b instanceof DocBlock.Paragraph p) {
                sb.append(renderSpans(p.spans()));
            } else if (b instanceof DocBlock.Heading h) {
                sb.append(escape(plainText(h.spans())));
            } else if (b instanceof DocBlock.ListItem li) {
                sb.append(escape(plainText(li.spans())));
            } else if (b instanceof DocBlock.CodeBlock code) {
                sb.append("<code>").append(escape(code.text())).append("</code>");
            }
            sb.append(' ');
        }
        return sb.toString().strip();
    }

    private String plainText(List<DocSpan> spans) {
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            sb.append(span.text());
        }
        return sb.toString();
    }

    private String renderSpans(List<DocSpan> spans) {
        if (spans == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (DocSpan span : spans) {
            String text = escape(span.text());
            if (span.mono()) {
                sb.append("<code>").append(text).append("</code>");
            } else {
                if (span.bold()) {
                    sb.append("<strong>");
                }
                if (span.italic()) {
                    sb.append("<em>");
                }
                if (span.strike()) {
                    sb.append("<del>");
                }
                sb.append(text);
                if (span.strike()) {
                    sb.append("</del>");
                }
                if (span.italic()) {
                    sb.append("</em>");
                }
                if (span.bold()) {
                    sb.append("</strong>");
                }
            }
            if (span.link() != null) {
                sb.insert(0, "<a href=\"" + escape(span.link()) + "\">").append("</a>");
            }
        }
        return sb.toString();
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
