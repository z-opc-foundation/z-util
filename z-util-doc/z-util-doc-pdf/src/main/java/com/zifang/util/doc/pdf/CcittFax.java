package com.zifang.util.doc.pdf;

import java.io.ByteArrayOutputStream;

/**
 * CCITT Group 3/4（T.4/T.6）传真位流解码，供 CCITTFaxDecode 过滤器使用。
 *
 * <p>输出按 PDF 约定：每行按 /Columns 位打包、字节对齐，1 位 = 白、0 位 = 黑（/BlackIs1 时取反）。
 * 支持 K=-1（G4/T.6 纯二维）、K=0（G3 一维 MH）、K&gt;0（G3 二维混合），以及
 * /EncodedByteAlign /EndOfLine /EndOfBlock /Rows。位流损坏或截断时按行补白提前收尾，不抛异常。</p>
 */
final class CcittFax {

    private CcittFax() {
    }

    // ---------------- T.4 行程码表（terminating 0..63 + makeup 64..2560） ----------------

    static final String[] WHITE_TERM = {
            "00110101", "000111", "0111", "1000", "1011", "1100",
            "1110", "1111", "10011", "10100", "00111", "01000",
            "001000", "000011", "110100", "110101", "101010", "101011",
            "0100111", "0001100", "0001000", "0010111", "0000011", "0000100",
            "0101000", "0101011", "0010011", "0100100", "0011000", "00000010",
            "00000011", "00011010", "00011011", "00010010", "00010011", "00010100",
            "00010101", "00010110", "00010111", "00101000", "00101001", "00101010",
            "00101011", "00101100", "00101101", "00000100", "00000101", "00001010",
            "00001011", "01010010", "01010011", "01010100", "01010101", "00100100",
            "00100101", "01011000", "01011001", "01011010", "01011011", "01001010",
            "01001011", "00110010", "00110011", "00110100",
    };

    static final String[] WHITE_MAKEUP = {
            "11011", "10010", "010111", "0110111", "00110110", "00110111",
            "01100100", "01100101", "01101000", "01100111", "011001100", "011001101",
            "011010010", "011010011", "011010100", "011010101", "011010110", "011010111",
            "011011000", "011011001", "011011010", "011011011", "010011000", "010011001",
            "010011010", "011000", "010011011", "00000001000", "00000001100", "00000001101",
            "000000010010", "000000010011", "000000010100", "000000010101", "000000010110", "000000010111",
            "000000011100", "000000011101", "000000011110", "000000011111",
    };

    static final String[] BLACK_TERM = {
            "0000110111", "010", "11", "10", "011", "0011",
            "0010", "00011", "000101", "000100", "0000100", "0000101",
            "0000111", "00000100", "00000111", "000011000", "0000010111", "0000011000",
            "0000001000", "00001100111", "00001101000", "00001101100", "00000110111", "00000101000",
            "00000010111", "00000011000", "000011001010", "000011001011", "000011001100", "000011001101",
            "000001101000", "000001101001", "000001101010", "000001101011", "000011010010", "000011010011",
            "000011010100", "000011010101", "000011010110", "000011010111", "000001101100", "000001101101",
            "000011011010", "000011011011", "000001010100", "000001010101", "000001010110", "000001010111",
            "000001100100", "000001100101", "000001010010", "000001010011", "000000100100", "000000110111",
            "000000111000", "000000100111", "000000101000", "000001011000", "000001011001", "000000101011",
            "000000101100", "000001011010", "000001100110", "000001100111",
    };

    static final String[] BLACK_MAKEUP = {
            "0000001111", "000011001000", "000011001001", "000001011011", "000000110011", "000000110100",
            "000000110101", "0000001101100", "0000001101101", "0000001001010", "0000001001011", "0000001001100",
            "0000001001101", "0000001110010", "0000001110011", "0000001110100", "0000001110101", "0000001110110",
            "0000001110111", "0000001010010", "0000001010011", "0000001010100", "0000001010101", "0000001011010",
            "0000001011011", "0000001100100", "0000001100101", "00000001000", "00000001100", "00000001101",
            "000000010010", "000000010011", "000000010100", "000000010101", "000000010110", "000000010111",
            "000000011100", "000000011101", "000000011110", "000000011111",
    };

    /** @return 解码后的打包位流；参数不完整或输入为空时原样返回。 */
    static byte[] decode(byte[] data, PdfDict parms) {
        if (data == null || data.length == 0 || parms == null) {
            return data;
        }
        int columns = (int) parms.getNumberOr("Columns", 0);
        if (columns <= 0) {
            return data;
        }
        int k = (int) parms.getNumberOr("K", 0);
        int rows = (int) parms.getNumberOr("Rows", 0);
        boolean blackIs1 = Boolean.TRUE.equals(parms.get("BlackIs1"));
        boolean byteAlign = Boolean.TRUE.equals(parms.get("EncodedByteAlign"));
        return new Instance(data, columns, rows, k, blackIs1, byteAlign).run();
    }

    private static final class Instance {

        private final byte[] data;
        private final int columns;
        private final int rows;
        private final int k;
        private final boolean blackIs1;
        private final boolean byteAlign;
        private final int totalBits;

        private long bp;
        private boolean eof;

        // 当前行/参考行的跳变点列表：偶数下标 = 进入黑、奇数下标 = 进入白
        private int[] ref = new int[64];
        private int refCount;
        private int[] cur = new int[64];
        private int curCount;
        private int hint;

        Instance(byte[] data, int columns, int rows, int k, boolean blackIs1, boolean byteAlign) {
            this.data = data;
            this.columns = columns;
            this.rows = rows;
            this.k = k;
            this.blackIs1 = blackIs1;
            this.byteAlign = byteAlign;
            this.totalBits = data.length * 8;
        }

        /** 位流不可解码时整体收尾，已解部分按行补白。 */
        private static final class Abort extends RuntimeException {
        }

        byte[] run() {
            int rowBytes = (columns + 7) / 8;
            int maxRows = rows > 0 ? rows : Integer.MAX_VALUE / Math.max(1, rowBytes);
            ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(1 << 20, maxRows * rowBytes));
            int decoded = 0;
            try {
                while (decoded < maxRows) {
                    if (byteAlign) {
                        align();
                    }
                    int eols = 0;
                    while (skipEol()) {
                        eols++;
                    }
                    if (eols >= 2 || (k < 0 && eols >= 1)) {
                        break;
                    }
                    if (bp >= totalBits) {
                        break;
                    }
                    if (k == 0) {
                        decode1D();
                    } else if (k < 0) {
                        decode2D();
                    } else {
                        boolean line1D = eols > 0 ? readBitChecked() == 1 : decoded % k == 0;
                        if (line1D) {
                            decode1D();
                        } else {
                            decode2D();
                        }
                    }
                    byte[] row = packRow(rowBytes);
                    out.write(row, 0, row.length);
                    decoded++;
                }
            } catch (Abort e) {
                // 位流耗尽或损坏：本行已有内容则按白补齐收尾，一个跳变都没解出来就当作数据自然结束
                if (curCount > 0) {
                    byte[] row = packRow(rowBytes);
                    out.write(row, 0, row.length);
                    decoded++;
                }
            }
            for (; rows > 0 && decoded < maxRows; decoded++) {
                byte[] row = new byte[rowBytes];
                java.util.Arrays.fill(row, (byte) 0xFF);
                out.write(row, 0, rowBytes);
            }
            byte[] bytes = out.toByteArray();
            if (blackIs1) {
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] ^= 0xFF;
                }
            }
            return bytes;
        }

        // ---------------- 行解码 ----------------

        /** G3 一维：白/黑行程链，交替到行尾。 */
        private void decode1D() {
            curCount = 0;
            int index = 0;
            boolean white = true;
            while (index < columns) {
                int run = readRun(white);
                index = Math.min(columns, index + run);
                addCur(index);
                white = !white;
            }
        }

        /**
         * G3 二维 / G4（T.6）：参照上一行的 Pass / Vertical / Horizontal 模式。
         * a0 采用「已解码像素位置」语义：初值 0，跳变点记录在行程结束处。
         */
        private void decode2D() {
            int[] t = ref;
            ref = cur;
            cur = t;
            refCount = curCount;
            curCount = 0;
            hint = 0;

            int index = 0;
            boolean white = true;
            int modes = 0;
            while (index < columns) {
                if (++modes > 2 * columns + 32) {
                    throw new Abort();
                }
                int bi = findB1(index, white);
                int b1 = bi < 0 ? columns : ref[bi];
                int b2 = bi < 0 || bi + 1 >= refCount ? columns : ref[bi + 1];
                int m = readMode();
                if (m == MODE_EOL) {
                    break;
                }
                if (m == MODE_EXT) {
                    throw new Abort();
                }
                if (m == MODE_PASS) {
                    index = bi < 0 ? columns : b2;
                } else if (m == MODE_H) {
                    index = Math.min(columns, index + readRun(white));
                    addCur(index);
                    index = Math.min(columns, index + readRun(!white));
                    addCur(index);
                } else {
                    int a1 = b1 + m;
                    if (a1 < index) {
                        a1 = Math.min(columns, index + 1);
                    }
                    if (a1 > columns) {
                        a1 = columns;
                    }
                    addCur(a1);
                    index = a1;
                    white = !white;
                }
                if (eof) {
                    throw new Abort();
                }
            }
        }

        /**
         * b1：参考行上 a0 右侧、与 a0 颜色相反的第一个跳变点下标；无则 -1。
         * 参考行第 0 列就变色且 a0 还在行首时直接命中该跳变（此时 ref[0] == a0 == 0）。
         */
        private int findB1(int a0, boolean white) {
            if (a0 == 0 && white && refCount > 0 && ref[0] == 0) {
                return 0;
            }
            int start = (hint & ~1) + (white ? 0 : 1);
            if (start > 2) {
                start -= 2;
            }
            for (int i = start; i < refCount; i += 2) {
                if (ref[i] > a0) {
                    hint = i;
                    return i;
                }
            }
            return -1;
        }

        private void addCur(int pos) {
            if (curCount == cur.length) {
                cur = java.util.Arrays.copyOf(cur, cur.length * 2);
            }
            cur[curCount++] = pos;
        }

        // ---------------- 码字读取 ----------------

        /** 行程 = makeup（≥64）+ terminating（0..63）链。 */
        private int readRun(boolean white) {
            int total = 0;
            while (true) {
                int run = readCode(white ? WHITE_TRIE : BLACK_TRIE);
                total += run;
                if (run < 64) {
                    return total;
                }
                if (total > 1 << 16) {
                    throw new Abort();
                }
            }
        }

        private int readCode(Trie trie) {
            Trie n = trie;
            while (n.run < 0) {
                int b = readBit();
                if (eof) {
                    throw new Abort();
                }
                n = n.next(b);
                if (n == null) {
                    throw new Abort();
                }
            }
            return n.run;
        }

        private static final int MODE_PASS = 200;
        private static final int MODE_H = 100;
        private static final int MODE_EXT = 300;
        private static final int MODE_EOL = 400;

        /** 2D 模式码：P=0001、H=001、V0=1、VR=011/000011/0000011、VL=010/000010/0000010。 */
        private int readMode() {
            int zeros = 0;
            while (readBit() == 0) {
                if (eof) {
                    throw new Abort();
                }
                if (++zeros > 11) {
                    return MODE_EXT;
                }
            }
            return switch (zeros) {
                case 0 -> 0;
                case 1 -> readBitChecked() == 1 ? 1 : -1;
                case 2 -> MODE_H;
                case 3 -> MODE_PASS;
                case 4 -> readBitChecked() == 1 ? 2 : -2;
                case 5 -> readBitChecked() == 1 ? 3 : -3;
                case 11 -> MODE_EOL;
                default -> MODE_EXT;
            };
        }

        private int readBitChecked() {
            int b = readBit();
            if (eof) {
                throw new Abort();
            }
            return b;
        }

        // ---------------- 位流底层 ----------------

        private int readBit() {
            if (bp >= totalBits) {
                eof = true;
                return 0;
            }
            int v = (data[(int) (bp >> 3)] & (0x80 >> ((int) (bp & 7)))) != 0 ? 1 : 0;
            bp++;
            return v;
        }

        private int peek(long off) {
            long p = bp + off;
            return p >= totalBits ? -1 : (data[(int) (p >> 3)] & (0x80 >> ((int) (p & 7)))) != 0 ? 1 : 0;
        }

        private void align() {
            bp = (bp + 7) & ~7L;
        }

        /** 前瞻并消费一个 EOL（11 个 0 + 1，前面可带填充 0）；没有则回退。 */
        private boolean skipEol() {
            int zeros = 0;
            for (int i = 0; i < 32; i++) {
                int b = peek(i);
                if (b < 0) {
                    return false;
                }
                if (b == 1) {
                    if (zeros >= 11) {
                        bp += zeros + 1;
                        return true;
                    }
                    return false;
                }
                zeros++;
            }
            return false;
        }

        // ---------------- 行打包：1 位 = 白 ----------------

        private byte[] packRow(int rowBytes) {
            byte[] row = new byte[rowBytes];
            int prev = 0;
            boolean white = true;
            for (int i = 0; i <= curCount; i++) {
                int t = i < curCount ? cur[i] : columns;
                if (t > columns) {
                    t = columns;
                }
                if (white && t > prev) {
                    for (int p = prev; p < t; p++) {
                        row[p >> 3] |= (byte) (0x80 >> (p & 7));
                    }
                }
                prev = t;
                white = !white;
            }
            return row;
        }

        private static final Trie WHITE_TRIE = build(WHITE_TERM, WHITE_MAKEUP);
        private static final Trie BLACK_TRIE = build(BLACK_TERM, BLACK_MAKEUP);

        private static Trie build(String[] term, String[] makeup) {
            Trie t = new Trie();
            for (int i = 0; i < term.length; i++) {
                t.add(term[i], i);
            }
            for (int i = 0; i < makeup.length; i++) {
                t.add(makeup[i], 64 + i * 64);
            }
            return t;
        }

        private static final class Trie {
            private final Trie[] next = new Trie[2];
            private final int run;

            Trie() {
                this.run = -1;
            }

            Trie(int run) {
                this.run = run;
            }

            void add(String code, int run) {
                Trie n = this;
                for (int i = 0; i < code.length(); i++) {
                    int b = code.charAt(i) - '0';
                    if (i == code.length() - 1) {
                        n.next[b] = new Trie(run);
                    } else if (n.next[b] == null) {
                        n.next[b] = new Trie();
                    }
                    n = n.next[b];
                }
            }

            Trie next(int b) {
                return next[b];
            }
        }
    }
}
