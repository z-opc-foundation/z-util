package com.zifang.util.doc.pdf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocSpan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 版面组装：坐标文本片段 → 行 → 块（标题/段落/列表/简易表格/图片）。
 * 启发式全部纯 Java：字号定标题、行距并段、x 对齐定表格。
 */
public final class LayoutAssembler {

    private static final Pattern BULLET = Pattern.compile("^[-\\u2013\\u2014\\u2022\\u25CF\\u25CB*·>]\\s+");
    private static final Pattern NUMBERED = Pattern.compile("^(\\d{1,3})[.)、]\\s+");

    /** 单页拼装。 */
    public List<DocBlock> assemblePage(ContentExtractor.Page page) {
        List<DocBlock> out = new ArrayList<>();
        // 带坐标的"条目"流：文本行 + 图片，按 y 降序（PDF y 向上）
        List<Item> items = new ArrayList<>();
        for (ContentExtractor.TextPiece p : page.text) {
            items.add(new Item(p.y(), p));
        }
        for (ContentExtractor.ImagePiece img : page.images) {
            items.add(new Item(img.y(), img));
        }
        items.sort(Comparator.comparingDouble((Item i) -> i.y).reversed());
        // 文本片段聚行：同 y 附近合并
        List<Line> lines = groupLines(items);
        // 图片按 y 顺序追加在文本块之后（无精确行内定位能力）
        List<DocBlock.Image> images = new ArrayList<>();
        for (Item it : items) {
            if (it.image != null) {
                images.add(toImage(it.image));
            }
        }
        if (lines.isEmpty()) {
            out.addAll(images);
            return out;
        }
        double bodySize = bodySize(lines);
        List<Line> paragraphs = new ArrayList<>();
        List<Line> tableRun = new ArrayList<>();
        for (Line line : lines) {
            if (isTableRow(line)) {
                tableRun.add(line);
                continue;
            }
            flushTable(tableRun, out);
            paragraphs.add(line);
        }
        flushTable(tableRun, out);
        // 段落/标题/列表
        double headingBase = headingBaseSize(lines, bodySize);
        int i = 0;
        while (i < paragraphs.size()) {
            Line line = paragraphs.get(i);
            if (line.maxSize >= headingBase && line.text().length() < 120) {
                int level = headingLevel(line.maxSize, lines, bodySize);
                out.add(new DocBlock.Heading(level, List.of(DocSpan.plain(line.text()))));
                i++;
                continue;
            }
            var listMatch = listMarker(line.text());
            if (listMatch != null) {
                out.add(new DocBlock.ListItem(listMatch.ordered(), 0,
                        List.of(DocSpan.plain(listMatch.rest()))));
                i++;
                continue;
            }
            // 并段
            StringBuilder sb = new StringBuilder(line.text());
            double size = line.maxSize;
            i++;
            while (i < paragraphs.size()) {
                Line next = paragraphs.get(i);
                if (next.maxSize >= headingBase || listMarker(next.text()) != null) {
                    break;
                }
                if (xAligned(line, next) && gapOk(line, next)) {
                    sb.append(lineBreakOrSpace(line, next)).append(next.text());
                    line = next;
                    size = Math.max(size, next.maxSize);
                    i++;
                } else {
                    break;
                }
            }
            out.add(new DocBlock.Paragraph(List.of(DocSpan.plain(sb.toString().strip()))));
        }
        out.addAll(images);
        return out;
    }

    // ==================== 行聚合 ====================

    private static final class Item {
        final double y;
        final ContentExtractor.TextPiece piece;
        final ContentExtractor.ImagePiece image;

        Item(double y, ContentExtractor.TextPiece piece) {
            this.y = y;
            this.piece = piece;
            this.image = null;
        }

        Item(double y, ContentExtractor.ImagePiece image) {
            this.y = y;
            this.piece = null;
            this.image = image;
        }
    }

    static final class Line {
        final List<ContentExtractor.TextPiece> pieces = new ArrayList<>();
        double y;
        double maxSize;

        String text() {
            StringBuilder sb = new StringBuilder();
            double lastEnd = Double.NaN;
            for (ContentExtractor.TextPiece p : pieces) {
                if (!Double.isNaN(lastEnd) && p.x() - lastEnd > maxSize * 0.35) {
                    sb.append('\t'); // 大间隙先记 tab，供表格判定
                } else if (!Double.isNaN(lastEnd)) {
                    sb.append(' ');
                }
                sb.append(p.text());
                lastEnd = p.x() + p.text().length() * p.fontSize() * 0.5;
            }
            return sb.toString();
        }

        List<String> cells() {
            List<String> cells = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            double lastEnd = Double.NaN;
            for (ContentExtractor.TextPiece p : pieces) {
                boolean gap = !Double.isNaN(lastEnd) && p.x() - lastEnd > maxSize * 1.5;
                if (gap && !sb.isEmpty()) {
                    cells.add(sb.toString().strip());
                    sb.setLength(0);
                }
                sb.append(sb.isEmpty() ? "" : " ").append(p.text());
                lastEnd = p.x() + p.text().length() * p.fontSize() * 0.5;
            }
            if (!sb.isEmpty()) {
                cells.add(sb.toString().strip());
            }
            return cells;
        }

        double startX() {
            return pieces.isEmpty() ? 0 : pieces.get(0).x();
        }
    }

    private List<Line> groupLines(List<Item> items) {
        List<Line> lines = new ArrayList<>();
        Line current = null;
        for (Item it : items) {
            if (it.image != null) {
                continue;
            }
            ContentExtractor.TextPiece p = it.piece;
            if (current != null && Math.abs(p.y() - current.y) <= Math.max(2, current.maxSize * 0.6)) {
                current.pieces.add(p);
                current.maxSize = Math.max(current.maxSize, p.fontSize());
            } else {
                current = new Line();
                current.y = p.y();
                current.pieces.add(p);
                current.maxSize = p.fontSize();
                lines.add(current);
            }
        }
        for (Line l : lines) {
            l.pieces.sort(Comparator.comparingDouble(ContentExtractor.TextPiece::x));
        }
        return lines;
    }

    // ==================== 启发式 ====================

    private double bodySize(List<Line> lines) {
        Map<Integer, Integer> hist = new HashMap<>();
        for (Line l : lines) {
            hist.merge((int) Math.round(l.maxSize), 1, Integer::sum);
        }
        int best = 12;
        int bestCount = -1;
        for (var e : hist.entrySet()) {
            // 行数优先，同数取小号（大号更可能是标题）
            if (e.getValue() > bestCount || (e.getValue() == bestCount && e.getKey() < best)) {
                bestCount = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    /** 高于正文的字号里，最小一档算 heading 门槛。 */
    private double headingBaseSize(List<Line> lines, double bodySize) {
        double min = Double.MAX_VALUE;
        for (Line l : lines) {
            if (l.maxSize > bodySize * 1.15 && l.maxSize < min) {
                min = l.maxSize;
            }
        }
        return min == Double.MAX_VALUE ? Double.MAX_VALUE : min;
    }

    private int headingLevel(double size, List<Line> lines, double bodySize) {
        List<Double> sizes = new ArrayList<>();
        for (Line l : lines) {
            if (l.maxSize > bodySize * 1.15 && !sizes.contains(l.maxSize)) {
                sizes.add(l.maxSize);
            }
        }
        sizes.sort(Comparator.reverseOrder());
        int idx = 0;
        for (double s : sizes) {
            if (s >= size) {
                idx++;
            }
        }
        return Math.min(6, idx);
    }

    private record ListMarker(boolean ordered, String rest) {
    }

    private ListMarker listMarker(String text) {
        var m = NUMBERED.matcher(text);
        if (m.find()) {
            return new ListMarker(true, text.substring(m.end()));
        }
        m = BULLET.matcher(text);
        if (m.find()) {
            return new ListMarker(false, text.substring(m.end()));
        }
        return null;
    }

    private boolean xAligned(Line a, Line b) {
        return Math.abs(a.startX() - b.startX()) < Math.max(6, a.maxSize * 1.5);
    }

    private boolean gapOk(Line a, Line b) {
        // a 在 b 之上（y 降序遍历）
        return (a.y - b.y) < Math.max(a.maxSize, b.maxSize) * 1.9;
    }

    private String lineBreakOrSpace(Line a, Line b) {
        return (a.y - b.y) > a.maxSize * 1.2 ? "\n" : " ";
    }

    // ==================== 表格 ====================

    /** 行内有 ≥2 个大间隙分段，且与前一行首列对齐，视为表格行。 */
    private boolean isTableRow(Line line) {
        return line.cells().size() >= 2;
    }

    private void flushTable(List<Line> run, List<DocBlock> out) {
        if (run.isEmpty()) {
            return;
        }
        boolean all = run.stream().allMatch(l -> l.cells().size() >= 2);
        if (!all || run.size() < 2) {
            for (Line l : run) {
                out.add(new DocBlock.Paragraph(List.of(DocSpan.plain(l.text().replace('\t', ' ')))));
            }
            run.clear();
            return;
        }
        List<DocBlock.Table.Row> rows = new ArrayList<>();
        for (Line l : run) {
            List<DocBlock.Table.Cell> cells = new ArrayList<>();
            for (String c : l.cells()) {
                cells.add(new DocBlock.Table.Cell(List.of(new DocBlock.Paragraph(c)), 1, 1));
            }
            rows.add(new DocBlock.Table.Row(cells, rows.isEmpty()));
        }
        run.clear();
        if (rows.size() >= 2) {
            out.add(new DocBlock.Table(rows));
        } else {
            for (DocBlock.Table.Row r : rows) {
                StringBuilder sb = new StringBuilder();
                for (DocBlock.Table.Cell c : r.cells()) {
                    if (c.blocks().get(0) instanceof DocBlock.Paragraph p) {
                        sb.append(p.spans().get(0).text()).append(' ');
                    }
                }
                out.add(new DocBlock.Paragraph(List.of(DocSpan.plain(sb.toString().strip()))));
            }
        }
    }

    private DocBlock.Image toImage(ContentExtractor.ImagePiece img) {
        return new DocBlock.Image(img.name(), img.mimeType(), img.data(), null, null);
    }
}
