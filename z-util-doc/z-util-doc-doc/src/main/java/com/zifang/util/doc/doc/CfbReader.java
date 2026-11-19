package com.zifang.util.doc.doc;

import com.zifang.util.doc.DocParseException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** OLE2 / Compound File Binary 容器读取：FAT / miniFAT / 目录 / 流提取。纯 JDK。 */
public final class CfbReader {

    private static final int FREE = 0xFFFFFFFF;
    private static final int ENDOFCHAIN = 0xFFFFFFFE;
    private static final int FATSECT = 0xFFFFFFFD;

    public static final class Entry {
        public final String name;
        public final int type; // 1 storage, 2 stream, 5 root
        public final long size;

        Entry(String name, int type, long size) {
            this.name = name;
            this.type = type;
            this.size = size;
        }

        public boolean isStream() {
            return type == 2;
        }
    }

    private final byte[] data;
    private final int sectorSize;
    private final int[] fat;
    private final int[] miniFat;
    private final byte[] miniStream;
    private final List<Entry> entries = new ArrayList<>();
    private final List<Integer> entryStartSectors = new ArrayList<>();

    public CfbReader(byte[] data) {
        this.data = data;
        byte[] sig = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};
        if (data.length < 512) {
            throw new DocParseException("OLE2 file too small");
        }
        for (int i = 0; i < 8; i++) {
            if (data[i] != sig[i]) {
                throw new DocParseException("not an OLE2/CFB container");
            }
        }
        int major = u16(26);
        sectorSize = major >= 4 ? 4096 : 512;
        fat = loadFat();
        int firstDir = u32(48);
        loadDirectory(firstDir);
        miniFat = loadTable(u32(60), u32(64));
        miniStream = loadMiniStream();
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public boolean hasStream(String name) {
        for (Entry e : entries) {
            if (e.isStream() && e.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /** 按名字读流；找不到返回 null。 */
    public byte[] readStream(String name) {
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            if (e.isStream() && e.name.equals(name)) {
                int start = entryStartSectors.get(i);
                long size = e.size;
                if (size < 4096) {
                    return readMiniChain(start, (int) size);
                }
                return readChain(start, size, fat, sectorSize, 0);
            }
        }
        return null;
    }

    // ==================== 内部 ====================

    private int u16(int off) {
        return (data[off] & 0xFF) | ((data[off + 1] & 0xFF) << 8);
    }

    private int u32(int off) {
        return (data[off] & 0xFF) | ((data[off + 1] & 0xFF) << 8)
                | ((data[off + 2] & 0xFF) << 16) | ((data[off + 3] & 0xFF) << 24);
    }

    private int sectorOffset(int sector) {
        return (sector + 1) * sectorSize;
    }

    private int[] loadFat() {
        List<Integer> fatSectors = new ArrayList<>();
        int difatCount = u32(72);
        int difatSector = u32(68);
        for (int i = 0; i < 109; i++) {
            int s = u32(76 + i * 4);
            if (s != FREE && s != ENDOFCHAIN) {
                fatSectors.add(s);
            }
        }
        int guard = 0;
        while (difatSector != ENDOFCHAIN && difatSector != FREE && guard++ < 4096) {
            int base = sectorOffset(difatSector);
            int per = sectorSize / 4 - 1;
            for (int i = 0; i < per; i++) {
                int s = u32(base + i * 4);
                if (s != FREE && s != ENDOFCHAIN) {
                    fatSectors.add(s);
                }
            }
            difatSector = u32(base + per * 4);
            difatCount--;
            if (difatCount <= 0) {
                break;
            }
        }
        int entriesCount = fatSectors.size() * (sectorSize / 4);
        int[] fat = new int[entriesCount];
        int p = 0;
        for (int s : fatSectors) {
            int base = sectorOffset(s);
            for (int i = 0; i < sectorSize / 4; i++) {
                fat[p++] = u32(base + i * 4);
            }
        }
        return fat;
    }

    private int[] loadTable(int firstSector, int count) {
        if (firstSector == ENDOFCHAIN || firstSector == FREE || count == 0) {
            return new int[0];
        }
        List<Integer> values = new ArrayList<>();
        int sector = firstSector;
        int guard = 0;
        while (sector != ENDOFCHAIN && sector != FREE && guard++ < 65536) {
            int base = sectorOffset(sector);
            for (int i = 0; i < sectorSize / 4; i++) {
                values.add(u32(base + i * 4));
            }
            sector = fat[sector];
        }
        int[] arr = new int[values.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = values.get(i);
        }
        return arr;
    }

    private void loadDirectory(int firstDir) {
        List<Integer> dirSectors = new ArrayList<>();
        int sector = firstDir;
        int guard = 0;
        while (sector != ENDOFCHAIN && sector != FREE && guard++ < 65536) {
            dirSectors.add(sector);
            sector = fat[sector];
        }
        for (int s : dirSectors) {
            int base = sectorOffset(s);
            for (int i = 0; i < sectorSize / 128; i++) {
                int off = base + i * 128;
                int nameLen = u16(off + 64);
                int type = data[off + 66] & 0xFF;
                if (type == 0 || nameLen < 2) {
                    continue;
                }
                String name = new String(data, off, Math.min(64, nameLen - 2), StandardCharsets.UTF_16LE);
                entries.add(new Entry(name, type, u64(off + 120)));
                entryStartSectors.add(u32(off + 116));
            }
        }
    }

    private long u64(int off) {
        return (u32(off) & 0xFFFFFFFFL) | ((long) u32(off + 4) << 32);
    }

    private byte[] loadMiniStream() {
        // 根目录项（第一个 entry）的流链就是 mini stream 容器
        if (entries.isEmpty()) {
            return new byte[0];
        }
        int start = entryStartSectors.get(0);
        long size = entries.get(0).size;
        if (size == 0 || start == ENDOFCHAIN || start == FREE) {
            return new byte[0];
        }
        return readChain(start, size, fat, sectorSize, 0);
    }

    private byte[] readChain(int start, long size, int[] table, int ss, int base) {
        byte[] out = new byte[(int) Math.min(size, Integer.MAX_VALUE)];
        int sector = start;
        int pos = 0;
        int guard = 0;
        while (sector != ENDOFCHAIN && sector != FREE && pos < out.length && guard++ < 10_000_000) {
            int src = (sector + 1) * ss;
            int n = Math.min(ss, out.length - pos);
            System.arraycopy(data, src, out, pos, n);
            pos += n;
            sector = sector < table.length ? table[sector] : ENDOFCHAIN;
        }
        if (pos < out.length) {
            // 链短于声明大小：保留已读部分
            byte[] trimmed = new byte[pos];
            System.arraycopy(out, 0, trimmed, 0, pos);
            return trimmed;
        }
        return out;
    }

    private byte[] readMiniChain(int start, int size) {
        byte[] out = new byte[size];
        int mini = start;
        int pos = 0;
        int guard = 0;
        while (mini != ENDOFCHAIN && mini != FREE && pos < size && guard++ < 10_000_000) {
            int src = mini * 64;
            int n = Math.min(64, size - pos);
            if (src + n <= miniStream.length) {
                System.arraycopy(miniStream, src, out, pos, n);
            }
            pos += n;
            mini = mini < miniFat.length ? miniFat[mini] : ENDOFCHAIN;
        }
        return out;
    }
}
