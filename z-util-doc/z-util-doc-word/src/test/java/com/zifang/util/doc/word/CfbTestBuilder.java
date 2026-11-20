package com.zifang.util.doc.doc;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 测试专用：合成最小 CFB 容器（512 字节扇区，单 FAT 扇区）。 */
public final class CfbTestBuilder {

    private static final int FREE = 0xFFFFFFFF;
    private static final int ENDOFCHAIN = 0xFFFFFFFE;
    private static final int FATSECT = 0xFFFFFFFD;

    private final Map<String, byte[]> streams = new LinkedHashMap<>();

    public CfbTestBuilder add(String name, byte[] data) {
        streams.put(name, data);
        return this;
    }

    public byte[] build() {
        List<String> miniNames = new ArrayList<>();
        List<String> regularNames = new ArrayList<>();
        for (var e : streams.entrySet()) {
            (e.getValue().length < 4096 ? miniNames : regularNames).add(e.getKey());
        }
        // mini stream 拼装（64 字节 minisector 对齐）
        ByteArrayOutputStream mini = new ByteArrayOutputStream();
        Map<String, int[]> miniChains = new LinkedHashMap<>();
        for (String name : miniNames) {
            byte[] b = streams.get(name);
            int start = mini.size() / 64;
            mini.write(b, 0, b.length);
            while (mini.size() % 64 != 0) {
                mini.write(0);
            }
            int count = (b.length + 63) / 64;
            int[] chain = new int[count];
            for (int i = 0; i < count; i++) {
                chain[i] = start + i;
            }
            miniChains.put(name, chain);
        }
        int miniLen = mini.size();
        int miniSectorCount = miniLen / 64;

        int entryCount = streams.size() + 1;
        int nDir = (entryCount * 128 + 511) / 512;
        int nMfat = miniSectorCount == 0 ? 0
                : Math.max(1, (miniSectorCount * 4 + 511) / 512);
        int containerSectors = miniLen == 0 ? 0 : (miniLen + 511) / 512;
        Map<String, int[]> regularChains = new LinkedHashMap<>();
        int regularSectorCount = 0;
        for (String name : regularNames) {
            byte[] b = streams.get(name);
            int count = (b.length + 511) / 512;
            int[] chain = new int[count];
            for (int i = 0; i < count; i++) {
                chain[i] = -2; // 占位，分配时填
            }
            regularChains.put(name, chain);
            regularSectorCount += count;
        }
        int total = 1 + nDir + nMfat + regularSectorCount + containerSectors;
        int[] fat = new int[total];
        java.util.Arrays.fill(fat, FREE);
        fat[0] = FATSECT;
        for (int s = 1; s < 1 + nDir; s++) {
            fat[s] = s + 1 <= nDir ? s + 1 : ENDOFCHAIN;
        }
        int cursor = 1 + nDir;
        int firstMfat = nMfat == 0 ? ENDOFCHAIN : cursor;
        for (int s = 0; s < nMfat; s++) {
            fat[cursor + s] = s + 1 < nMfat ? cursor + s + 1 : ENDOFCHAIN;
        }
        cursor += nMfat;
        for (String name : regularNames) {
            int[] chain = regularChains.get(name);
            for (int i = 0; i < chain.length; i++) {
                chain[i] = cursor + i;
                fat[cursor + i] = i + 1 < chain.length ? cursor + i + 1 : ENDOFCHAIN;
            }
            cursor += chain.length;
        }
        int containerStart = cursor;
        for (int s = 0; s < containerSectors; s++) {
            fat[cursor + s] = s + 1 < containerSectors ? cursor + s + 1 : ENDOFCHAIN;
        }

        // miniFAT
        int[] miniFat = new int[Math.max(0, nMfat * 128)];
        java.util.Arrays.fill(miniFat, FREE);
        int mp = 0;
        for (int[] chain : miniChains.values()) {
            for (int i = 0; i < chain.length; i++) {
                miniFat[chain[i]] = i + 1 < chain.length ? chain[i + 1] : ENDOFCHAIN;
                mp = Math.max(mp, chain[i] + 1);
            }
        }

        // 输出
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] header = new byte[512];
        byte[] sig = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};
        System.arraycopy(sig, 0, header, 0, 8);
        u16(header, 24, 0x003E);
        u16(header, 26, 3);
        u16(header, 28, 0xFFFE);
        u16(header, 30, 9);
        u16(header, 32, 6);
        u32(header, 44, 1);
        u32(header, 48, 1);
        u32(header, 56, 4096);
        u32(header, 60, firstMfat);
        u32(header, 64, nMfat);
        u32(header, 68, ENDOFCHAIN);
        u32(header, 72, 0);
        for (int i = 0; i < 109; i++) {
            u32(header, 76 + i * 4, i == 0 ? 0 : FREE);
        }
        out.writeBytes(header);
        // FAT 扇区
        byte[] fatSector = new byte[512];
        for (int i = 0; i < total; i++) {
            u32(fatSector, i * 4, fat[i]);
        }
        out.writeBytes(fatSector);
        // 目录
        byte[] dir = new byte[nDir * 512];
        writeDirEntry(dir, 0, "Root Entry", 5, miniLen == 0 ? ENDOFCHAIN : containerStart, miniLen,
                streams.isEmpty() ? ENDOFCHAIN : 1, ENDOFCHAIN);
        int idx = 1;
        int prev = -1;
        for (String name : streams.keySet()) {
            int type = streams.get(name).length < 4096 ? 2 : 2;
            int start;
            long size;
            if (miniChains.containsKey(name)) {
                start = miniChains.get(name)[0];
                size = streams.get(name).length;
            } else {
                start = regularChains.get(name)[0];
                size = streams.get(name).length;
            }
            int right = idx + 1 <= streams.size() ? idx + 1 : ENDOFCHAIN;
            writeDirEntry(dir, idx * 128, name, type, start, size, ENDOFCHAIN, right);
            prev = idx;
            idx++;
        }
        out.writeBytes(dir);
        // miniFAT 扇区
        if (nMfat > 0) {
            byte[] mf = new byte[nMfat * 512];
            for (int i = 0; i < miniFat.length; i++) {
                u32(mf, i * 4, miniFat[i]);
            }
            out.writeBytes(mf);
        }
        // 常规流
        for (String name : regularNames) {
            byte[] b = streams.get(name);
            out.writeBytes(b);
            for (int i = b.length % 512; i != 0 && i < 512; i++) {
                out.write(0);
            }
        }
        // mini 容器
        if (miniLen > 0) {
            out.writeBytes(mini.toByteArray());
            for (int i = miniLen % 512; i != 0 && i < 512; i++) {
                out.write(0);
            }
        }
        return out.toByteArray();
    }

    private static void writeDirEntry(byte[] dir, int off, String name, int type,
                                      int start, long size, int left, int right) {
        byte[] n = name.getBytes(StandardCharsets.UTF_16LE);
        System.arraycopy(n, 0, dir, off, n.length);
        u16(dir, off + 64, n.length + 2);
        dir[off + 66] = (byte) type;
        dir[off + 67] = 1;
        u32(dir, off + 68, left);
        u32(dir, off + 72, right);
        u32(dir, off + 76, ENDOFCHAIN);
        u32(dir, off + 116, start);
        u32(dir, off + 120, (int) size);
    }

    private static void u16(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
    }

    private static void u32(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
        b[off + 2] = (byte) (v >> 16);
        b[off + 3] = (byte) (v >> 24);
    }
}
