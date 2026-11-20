package com.zifang.util.doc.ppt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PowerPoint 97-2003（HSLF）记录流解析。
 *
 * <p>结构：OLE 里的 "PowerPoint Document" 流是一条记录流。保存过的文件会留下多份
 * 「UserEditAtom → 持久化目录（6002）」的链，每一版持久化目录把 sheet id 映射到
 * 该版本里各记录在流内的偏移；「Current User」流里记着最新一版的 UserEditAtom 位置。
 * 播放顺序记在 Document 容器里 SlideListWithText（instance=0）的 SlidePersistAtom 顺序表上，
 * 同样靠持久化目录里的 sheet id 找到对应的 Slide 记录。</p>
 *
 * <p>幻灯片的文字有两个来源：绘图记录里的文本框，以及 SlideListWithText 里跟在该页
 * SlidePersistAtom 之后的「大纲文本」（老格式的占位符文字存在这里）。两者都取，
 * 重复的按文本去重。</p>
 */
final class HslfDocument {

    /** 一页幻灯片：标题 + 形状文本 + 备注文本。 */
    record Slide(int index, String title, List<Shape> shapes, List<Shape> notesShapes) {
    }

    /** 一个带文字的形状：占位符类型 + 文本（段落已按 CR 切开）。 */
    record Shape(int placeholderType, List<String> paragraphs) {
    }

    /** 一页在 SlideListWithText 里的登记项：sheet id + 大纲文本。 */
    private static final class SlideSet {
        final int refId;
        final int slideId;
        final List<Shape> shapes = new ArrayList<>();

        SlideSet(int refId, int slideId) {
            this.refId = refId;
            this.slideId = slideId;
        }
    }

    private final List<Slide> slides;

    private HslfDocument(List<Slide> slides) {
        this.slides = slides;
    }

    List<Slide> slides() {
        return slides;
    }

    static HslfDocument parse(byte[] stream, byte[] currentUser) {
        Map<Integer, Integer> locations = persistLocations(stream, currentUser);
        HslfRecord document = findDocument(stream, locations);
        List<SlideSet> slideSets = readSlideSets(document, 0);
        Map<Integer, HslfRecord> notesBySlideId = notesBySlideId(document, locations, stream);

        List<Slide> result = new ArrayList<>();
        int index = 1;
        for (SlideSet set : slideSets) {
            HslfRecord record = slideRecord(set, locations, stream);
            List<Shape> shapes = new ArrayList<>(set.shapes);
            if (record != null) {
                for (Shape shape : shapesOf(record)) {
                    if (!containsText(shapes, shape)) {
                        shapes.add(shape);
                    }
                }
            }
            String title = null;
            for (Shape shape : shapes) {
                if (isTitle(shape)) {
                    title = shape.paragraphs().get(0);
                    break;
                }
            }
            List<Shape> notesShapes = notesShapesOf(record, notesBySlideId);
            result.add(new Slide(index++, title, shapes, notesShapes));
        }
        if (result.isEmpty()) {
            // 顺序表缺失时按文件顺序兜底
            for (HslfRecord record : parseTopLevel(stream)) {
                if (record.type == HslfRecord.TYPE_SLIDE) {
                    List<Shape> shapes = shapesOf(record);
                    String title = null;
                    for (Shape shape : shapes) {
                        if (isTitle(shape)) {
                            title = shape.paragraphs().get(0);
                            break;
                        }
                    }
                    result.add(new Slide(index++, title, shapes, List.of()));
                }
            }
        }
        return new HslfDocument(result);
    }

    private static boolean containsText(List<Shape> shapes, Shape candidate) {
        for (Shape shape : shapes) {
            if (Objects.equals(shape.paragraphs(), candidate.paragraphs())) {
                return true;
            }
        }
        return false;
    }

    private static HslfRecord slideRecord(SlideSet set, Map<Integer, Integer> locations, byte[] stream) {
        Integer offset = locations.get(set.refId);
        if (offset == null) {
            return null;
        }
        HslfRecord record = parseRecord(stream, offset, stream.length);
        return record != null && record.type == HslfRecord.TYPE_SLIDE ? record : null;
    }

    /**
     * sheet id → 记录偏移。沿 UserEditAtom 链从新到旧遍历，最新一版胜出。
     */
    private static Map<Integer, Integer> persistLocations(byte[] stream, byte[] currentUser) {
        Map<Integer, Integer> locations = new LinkedHashMap<>();
        if (currentUser == null || currentUser.length < 20) {
            return locations;
        }
        int offset = intAt(currentUser, 16);
        int guard = 0;
        while (offset > 0 && offset + 8 <= stream.length && guard++ < 64) {
            HslfRecord userEdit = parseRecord(stream, offset, stream.length);
            if (userEdit == null || userEdit.type != HslfRecord.TYPE_USER_EDIT
                    || userEdit.data.length < 20) {
                break;
            }
            int holderOffset = HslfRecord.int32(userEdit.data, 12);
            HslfRecord holder = parseRecord(stream, holderOffset, stream.length);
            if (holder != null && holder.type == HslfRecord.TYPE_PERSIST_PTR) {
                for (Map.Entry<Integer, Integer> entry : readPersistDirectory(holder.data).entrySet()) {
                    locations.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
            offset = HslfRecord.int32(userEdit.data, 8);
        }
        return locations;
    }

    /** 持久化目录（6002）的条目：sheet id → 偏移。 */
    private static Map<Integer, Integer> readPersistDirectory(byte[] data) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        int pos = 0;
        while (pos + 4 <= data.length) {
            int info = HslfRecord.int32(data, pos);
            pos += 4;
            int base = info & 0x000FFFFF;
            int count = (info >> 20) & 0xFFF;
            for (int i = 0; i < count && pos + 4 <= data.length; i++) {
                result.put(base + i, HslfRecord.int32(data, pos));
                pos += 4;
            }
        }
        return result;
    }

    /** Document 容器：流内偏移最大的那份即是当前版本（增量保存会留下多份）。 */
    private static HslfRecord findDocument(byte[] stream, Map<Integer, Integer> locations) {
        HslfRecord document = null;
        for (Integer offset : locations.values()) {
            HslfRecord record = parseRecord(stream, offset, stream.length);
            if (record != null && record.type == HslfRecord.TYPE_DOCUMENT
                    && (document == null || record.offset > document.offset)) {
                document = record;
            }
        }
        if (document != null) {
            return document;
        }
        for (HslfRecord record : parseTopLevel(stream)) {
            if (record.type == HslfRecord.TYPE_DOCUMENT) {
                document = record;
            }
        }
        return document;
    }

    /**
     * 读 SlideListWithText：instance=0 是幻灯片（顺序即播放顺序），instance=2 是备注。
     * 每个 SlidePersistAtom 之后的文本原子属于该页。
     */
    private static List<SlideSet> readSlideSets(HslfRecord document, int instance) {
        List<SlideSet> sets = new ArrayList<>();
        if (document == null) {
            return sets;
        }
        for (HslfRecord slwt : document.children) {
            if (slwt.type != HslfRecord.TYPE_SLIDE_LIST_WITH_TEXT || slwt.instance != instance) {
                continue;
            }
            SlideSet current = null;
            int placeholderType = -1;
            StringBuilder text = null;
            for (HslfRecord child : slwt.children) {
                if (child.type == HslfRecord.TYPE_SLIDE_PERSIST_ATOM) {
                    // 上一页的形状在这里收尾，再开新的一页
                    flushShape(current, placeholderType, text);
                    current = new SlideSet(HslfRecord.int32(child.data, 0), HslfRecord.int32(child.data, 12));
                    sets.add(current);
                    placeholderType = -1;
                    text = null;
                    continue;
                }
                if (current == null) {
                    continue;
                }
                if (child.type == HslfRecord.TYPE_TEXT_HEADER && child.data.length >= 4) {
                    flushShape(current, placeholderType, text);
                    placeholderType = HslfRecord.int32(child.data, 0);
                    text = new StringBuilder();
                } else if (child.type == HslfRecord.TYPE_TEXT_CHARS
                        || child.type == HslfRecord.TYPE_TEXT_BYTES) {
                    if (text == null) {
                        text = new StringBuilder();
                    }
                    text.append(textOf(child));
                }
            }
            flushShape(current, placeholderType, text);
        }
        return sets;
    }

    private static void flushShape(SlideSet set, int placeholderType, StringBuilder text) {
        if (set == null || text == null) {
            return;
        }
        List<String> paragraphs = splitParagraphs(text.toString());
        if (!paragraphs.isEmpty()) {
            set.shapes.add(new Shape(placeholderType, paragraphs));
        }
    }

    /** 备注：SlideListWithText(instance=2) 给出「幻灯片 id → 备注记录」。 */
    private static Map<Integer, HslfRecord> notesBySlideId(HslfRecord document,
                                                           Map<Integer, Integer> locations,
                                                           byte[] stream) {
        Map<Integer, HslfRecord> result = new HashMap<>();
        if (document == null) {
            return result;
        }
        for (SlideSet set : readSlideSets(document, 2)) {
            Integer offset = locations.get(set.refId);
            if (offset == null) {
                continue;
            }
            HslfRecord notes = parseRecord(stream, offset, stream.length);
            if (notes != null && notes.type == HslfRecord.TYPE_NOTES) {
                result.put(set.slideId, notes);
            }
        }
        return result;
    }

    /** 一页里的带字形状。 */
    private static List<Shape> shapesOf(HslfRecord record) {
        List<Shape> shapes = new ArrayList<>();
        collectShapes(record, shapes);
        if (shapes.isEmpty()) {
            // 老式绘图记录里文本不在 TextInfoAtom 下，退化为直接找文本原子
            List<String> loose = new ArrayList<>();
            record.walk(child -> {
                if (child.type == HslfRecord.TYPE_TEXT_CHARS || child.type == HslfRecord.TYPE_TEXT_BYTES) {
                    for (String paragraph : splitParagraphs(textOf(child))) {
                        if (!paragraph.isBlank()) {
                            loose.add(paragraph);
                        }
                    }
                }
            });
            if (!loose.isEmpty()) {
                shapes.add(new Shape(-1, loose));
            }
        }
        return shapes;
    }

    private static void collectShapes(HslfRecord record, List<Shape> out) {
        for (HslfRecord child : record.children) {
            if (child.type == HslfRecord.TYPE_TEXT_INFO) {
                Shape shape = shapeOf(child);
                if (shape != null) {
                    out.add(shape);
                }
                continue;
            }
            collectShapes(child, out);
        }
    }

    private static Shape shapeOf(HslfRecord textInfo) {
        int placeholderType = -1;
        StringBuilder text = new StringBuilder();
        for (HslfRecord child : textInfo.children) {
            if (child.type == HslfRecord.TYPE_TEXT_HEADER && child.data.length >= 4) {
                placeholderType = HslfRecord.int32(child.data, 0);
            } else if (child.type == HslfRecord.TYPE_TEXT_CHARS
                    || child.type == HslfRecord.TYPE_TEXT_BYTES) {
                text.append(textOf(child));
            }
        }
        List<String> paragraphs = splitParagraphs(text.toString());
        if (paragraphs.isEmpty()) {
            return null;
        }
        return new Shape(placeholderType, paragraphs);
    }

    private static boolean isTitle(Shape shape) {
        return shape.placeholderType() == HslfRecord.PLACEHOLDER_TITLE
                || shape.placeholderType() == HslfRecord.PLACEHOLDER_CENTER_TITLE;
    }

    /** 备注页：SlideAtom 的 notesID 指向幻灯片 id。 */
    private static List<Shape> notesShapesOf(HslfRecord slide, Map<Integer, HslfRecord> notesBySlideId) {
        if (slide == null) {
            return List.of();
        }
        int notesId = 0;
        for (HslfRecord child : slide.children) {
            if (child.type == HslfRecord.TYPE_SLIDE_ATOM && child.data.length >= 20) {
                notesId = HslfRecord.int32(child.data, 16);
            }
        }
        if (notesId == 0) {
            return List.of();
        }
        HslfRecord notes = notesBySlideId.get(notesId);
        return notes == null ? List.of() : shapesOf(notes);
    }

    private static String textOf(HslfRecord record) {
        if (record.type == HslfRecord.TYPE_TEXT_CHARS) {
            return new String(record.data, StandardCharsets.UTF_16LE);
        }
        return new String(record.data, StandardCharsets.ISO_8859_1);
    }

    /** CR 分段，VT 是段内软换行。 */
    private static List<String> splitParagraphs(String text) {
        List<String> result = new ArrayList<>();
        for (String part : text.split("\r", -1)) {
            String cleaned = part.replace((char) 0x0B, '\n').trim();
            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }
        return result;
    }

    private static int intAt(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24);
    }

    /**
     * 顺序解析顶层记录（容错路径用）。长度越界或结构不合法时截断，不抛异常。
     */
    private static List<HslfRecord> parseTopLevel(byte[] data) {
        List<HslfRecord> records = new ArrayList<>();
        int pos = 0;
        while (pos + 8 <= data.length) {
            HslfRecord record = parseRecord(data, pos, data.length);
            if (record == null) {
                break;
            }
            records.add(record);
            int next = record.endOffset();
            if (next <= pos) {
                break;
            }
            pos = next;
        }
        return records;
    }

    private static HslfRecord parseRecord(byte[] data, int offset, int limit) {
        if (offset < 0 || offset + 8 > limit || offset + 8 > data.length) {
            return null;
        }
        int verInstance = HslfRecord.uint16(data, offset);
        int version = verInstance & 0x0F;
        int instance = (verInstance >> 4) & 0x0FFF;
        int type = HslfRecord.uint16(data, offset + 2);
        int length = HslfRecord.int32(data, offset + 4);
        if (length < 0 || offset + 8L + length > limit || offset + 8L + length > data.length) {
            length = Math.max(0, Math.min(limit - offset - 8, data.length - offset - 8));
        }
        if (version == 0x0F) {
            List<HslfRecord> children = new ArrayList<>();
            int pos = offset + 8;
            int end = offset + 8 + length;
            while (pos + 8 <= end) {
                HslfRecord child = parseRecord(data, pos, end);
                if (child == null) {
                    break;
                }
                children.add(child);
                int next = child.endOffset();
                if (next <= pos) {
                    break;
                }
                pos = next;
            }
            return new HslfRecord(type, version, instance, offset, length, new byte[0], children);
        }
        byte[] payload = new byte[length];
        System.arraycopy(data, offset + 8, payload, 0, length);
        return new HslfRecord(type, version, instance, offset, length, payload, List.of());
    }
}
