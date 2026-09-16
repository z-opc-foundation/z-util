package io.zifu.z.serialize.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 字段索引：记录每个字段在 body 中的 offset 和 length。
 *
 * <p>索引格式：[count varint][fieldId varint][offset varint][length varint] × count</p>
 */
public final class FieldIndex {

    /** 单条索引记录。 */
    public static final class Entry {
        public final int fieldId;
        public final int offset;
        public final int length;

        public Entry(int fieldId, int offset, int length) {
            this.fieldId = fieldId;
            this.offset = offset;
            this.length = length;
        }

        public int end() { return offset + length; }
    }

    /** 范围查询结果。 */
    public static final class Range {
        public final int fieldId;
        public final int offset;
        public final int length;

        Range(int fieldId, int offset, int length) {
            this.fieldId = fieldId;
            this.offset = offset;
            this.length = length;
        }

        public int fieldId() { return fieldId; }
        public int offset() { return offset; }
        public int length() { return length; }
        public int end() { return offset + length; }
    }

    private final List<Entry> entries;

    private FieldIndex(List<Entry> entries) {
        this.entries = Collections.unmodifiableList(entries);
    }

    public List<Entry> getEntries() { return entries; }

    public Entry findByFieldId(int fieldId) {
        for (Entry e : entries) {
            if (e.fieldId == fieldId) return e;
        }
        return null;
    }

    public List<Entry> findByRange(int minFieldId, int maxFieldId) {
        List<Entry> result = new ArrayList<>();
        for (Entry e : entries) {
            if (e.fieldId >= minFieldId && e.fieldId <= maxFieldId) {
                result.add(e);
            }
        }
        return result;
    }

    public List<Range> findRanges(int minFieldId, int maxFieldId) {
        List<Range> result = new ArrayList<>();
        for (Entry e : entries) {
            if (e.fieldId >= minFieldId && e.fieldId <= maxFieldId) {
                result.add(new Range(e.fieldId, e.offset, e.length));
            }
        }
        return result;
    }

    public int size() { return entries.size(); }

    // ==================== Encode ====================

    public byte[] encode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
        encodeTo(baos);
        return baos.toByteArray();
    }

    public void encodeTo(OutputStream out) throws IOException {
        writeVarInt(out, entries.size());
        for (Entry e : entries) {
            writeVarInt(out, e.fieldId);
            writeVarInt(out, e.offset);
            writeVarInt(out, e.length);
        }
    }

    // ==================== Decode ====================

    public static FieldIndex decode(byte[] data) throws IOException {
        return decode(data, 0, data.length);
    }

    public static FieldIndex decode(byte[] data, int offset, int length) throws IOException {
        int pos = offset;
        int count = readVarInt(data, pos);
        pos += varIntSize(data, pos);

        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int fieldId = readVarInt(data, pos);
            pos += varIntSize(data, pos);
            int fieldOffset = readVarInt(data, pos);
            pos += varIntSize(data, pos);
            int fieldLength = readVarInt(data, pos);
            pos += varIntSize(data, pos);
            entries.add(new Entry(fieldId, fieldOffset, fieldLength));
        }

        entries.sort(Comparator.comparingInt(e -> e.fieldId));
        return new FieldIndex(entries);
    }

    // ==================== Helpers ====================

    private static void writeVarInt(OutputStream out, int v) throws IOException {
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v);
    }

    private static int readVarInt(byte[] data, int pos) {
        int value = 0;
        int shift = 0;
        int b;
        do {
            b = data[pos++] & 0xFF;
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return value;
    }

    private static int varIntSize(byte[] data, int pos) {
        int size = 0;
        int b;
        do {
            b = data[pos++] & 0xFF;
            size++;
        } while ((b & 0x80) != 0);
        return size;
    }

    // ==================== Builder ====================

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final List<Entry> entries = new ArrayList<>();

        public Builder add(int fieldId, int offset, int length) {
            entries.add(new Entry(fieldId, offset, length));
            return this;
        }

        public FieldIndex build() {
            entries.sort(Comparator.comparingInt(e -> e.fieldId));
            return new FieldIndex(entries);
        }
    }
}
