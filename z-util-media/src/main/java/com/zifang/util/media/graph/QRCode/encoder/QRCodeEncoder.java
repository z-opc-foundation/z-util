package com.zifang.util.media.graph.qrcode.encoder;

import java.nio.charset.Charset;

/**
 * QR Code encoder implementing ISO/IEC 18004.
 * Replaces com.google.zxing.MultiFormatWriter.
 */
public class QRCodeEncoder {

    // Mode indicators
    private static final int MODE_BYTE = 0x04;

    // Version capacities (for byte mode, L error correction)
    private static final int[] VERSION_CAPACITY = {
            17,     // Version 1
            32,     // Version 2
            53,     // Version 3
            78,     // Version 4
            106,    // Version 5
            134,    // Version 6
            154,    // Version 7
            192,    // Version 8
            230,    // Version 9
            271,    // Version 10
            321,    // Version 11
            367,    // Version 12
            425,    // Version 13
            458,    // Version 14
            520,    // Version 15
            586,    // Version 16
            644,    // Version 17
            718,    // Version 18
            792,    // Version 19
            858,    // Version 20
            929,    // Version 21
            1003,   // Version 22
            1091,   // Version 23
            1171,   // Version 24
            1273,   // Version 25
            1367,   // Version 26
            1465,   // Version 27
            1528,   // Version 28
            1592,   // Version 29
            1658,   // Version 30
            1728,   // Version 31
            1812,   // Version 32
            1911,   // Version 33
            1989,   // Version 34
            2099,   // Version 35
            2213,   // Version 36
            2331,   // Version 37
            2433,   // Version 38
            2563,   // Version 39
            2699,   // Version 40
    };

    // Data capacity for each version and error correction level (in bytes)
    private static final int[][] DATA_CAPACITY = {
            {19, 16, 13, 9},      // Version 1
            {34, 28, 22, 16},     // Version 2
            {55, 44, 34, 26},     // Version 3
            {80, 64, 48, 36},     // Version 4
            {108, 86, 62, 46},    // Version 5
            {136, 108, 76, 60},   // Version 6
            {156, 124, 88, 66},   // Version 7
            {194, 154, 110, 86},  // Version 8
            {232, 182, 132, 100}, // Version 9
            {274, 216, 154, 122}, // Version 10
            {324, 254, 180, 140}, // Version 11
            {370, 290, 206, 158}, // Version 12
            {428, 334, 244, 180}, // Version 13
            {461, 365, 261, 197}, // Version 14
            {523, 415, 295, 223}, // Version 15
            {589, 461, 325, 253}, // Version 16
            {647, 511, 367, 283}, // Version 17
            {721, 535, 397, 313}, // Version 18
            {795, 593, 445, 341}, // Version 19
            {861, 645, 485, 385}, // Version 20
            {932, 697, 535, 412}, // Version 21
            {1006, 763, 593, 450},// Version 22
            {1094, 831, 645, 502},// Version 23
            {1174, 895, 697, 560},// Version 24
            {1276, 967, 761, 624},// Version 25
            {1370, 1043, 845, 702},// Version 26
            {1468, 1115, 901, 750},// Version 27
            {1531, 1171, 961, 810},// Version 28
            {1595, 1231, 986, 868},// Version 29
            {1661, 1286, 1054, 908},// Version 30
            {1731, 1346, 1096, 982},// Version 31
            {1815, 1411, 1142, 1030},// Version 32
            {1914, 1474, 1220, 1088},// Version 33
            {1992, 1540, 1286, 1150},// Version 34
            {2102, 1595, 1354, 1214},// Version 35
            {2216, 1663, 1426, 1267},// Version 36
            {2334, 1735, 1502, 1323},// Version 37
            {2436, 1812, 1582, 1399},// Version 38
            {2566, 1911, 1666, 1480},// Version 39
            {2702, 1989, 1742, 1538},// Version 40
    };

    // Error correction codewords per block for each version and EC level
    private static final int[][] ECC_PER_BLOCK = {
            {7, 10, 13, 17},      // Version 1
            {10, 16, 22, 28},     // Version 2
            {15, 26, 18, 22},     // Version 3
            {20, 18, 26, 16},     // Version 4
            {26, 24, 18, 22},     // Version 5
            {18, 16, 24, 28},     // Version 6
            {20, 18, 18, 26},     // Version 7
            {24, 22, 22, 26},     // Version 8
            {30, 22, 20, 24},     // Version 9
            {18, 26, 24, 28},     // Version 10
            {20, 30, 28, 24},     // Version 11
            {24, 22, 26, 28},     // Version 12
            {26, 22, 24, 22},     // Version 13
            {30, 22, 20, 24},     // Version 14
            {22, 24, 20, 30},     // Version 15
            {24, 24, 28, 24},     // Version 16
            {28, 28, 24, 30},     // Version 17
            {30, 28, 28, 28},     // Version 18
            {30, 26, 26, 26},     // Version 19
            {28, 26, 26, 30},     // Version 20
            {28, 26, 30, 28},     // Version 21
            {28, 26, 28, 30},     // Version 22
            {28, 28, 30, 30},     // Version 23
            {28, 30, 30, 30},     // Version 24
            {28, 30, 30, 30},     // Version 25
            {28, 30, 30, 30},     // Version 26
            {28, 30, 30, 30},     // Version 27
            {28, 30, 30, 30},     // Version 28
            {28, 30, 30, 30},     // Version 29
            {28, 30, 30, 30},     // Version 30
            {28, 30, 30, 30},     // Version 31
            {28, 30, 30, 30},     // Version 32
            {28, 30, 30, 30},     // Version 33
            {28, 30, 30, 30},     // Version 34
            {28, 30, 30, 30},     // Version 35
            {28, 30, 30, 30},     // Version 36
            {28, 30, 30, 30},     // Version 37
            {28, 30, 30, 30},     // Version 38
            {28, 30, 30, 30},     // Version 39
            {28, 30, 30, 30},     // Version 40
    };

    // Number of blocks for each version and EC level
    private static final int[][] NUM_BLOCKS = {
            {1, 1, 1, 1},         // Version 1
            {1, 1, 1, 1},         // Version 2
            {1, 1, 2, 2},         // Version 3
            {1, 2, 2, 4},         // Version 4
            {1, 2, 2, 4},         // Version 5
            {2, 2, 2, 2},         // Version 6
            {2, 2, 2, 2},         // Version 7
            {4, 2, 2, 2},         // Version 8
            {4, 2, 2, 4},         // Version 9
            {4, 4, 2, 2},         // Version 10
            {4, 4, 4, 2},         // Version 11
            {4, 4, 4, 4},         // Version 12
            {4, 4, 4, 4},         // Version 13
            {6, 4, 4, 4},         // Version 14
            {6, 4, 4, 4},         // Version 15
            {6, 4, 4, 4},         // Version 16
            {6, 6, 4, 4},         // Version 17
            {6, 6, 6, 4},         // Version 18
            {6, 6, 6, 6},         // Version 19
            {6, 6, 6, 6},         // Version 20
            {6, 6, 6, 6},         // Version 21
            {6, 6, 6, 6},         // Version 22
            {6, 6, 6, 6},         // Version 23
            {6, 6, 6, 6},         // Version 24
            {6, 6, 6, 6},         // Version 25
            {6, 6, 6, 6},         // Version 26
            {6, 6, 6, 6},         // Version 27
            {6, 6, 6, 6},         // Version 28
            {6, 6, 6, 6},         // Version 29
            {6, 6, 6, 6},         // Version 30
            {6, 6, 6, 6},         // Version 31
            {6, 6, 6, 6},         // Version 32
            {6, 6, 6, 6},         // Version 33
            {6, 6, 6, 6},         // Version 34
            {6, 6, 6, 6},         // Version 35
            {6, 6, 6, 6},         // Version 36
            {6, 6, 6, 6},         // Version 37
            {6, 6, 6, 6},         // Version 38
            {6, 6, 6, 6},         // Version 39
            {6, 6, 6, 6},         // Version 40
    };

    // Alignment pattern centers for each version
    private static final int[][] ALIGNMENT_CENTERS = {
            {},                   // Version 0 (not used)
            {},                   // Version 1
            {6},                  // Version 2
            {6, 18},              // Version 3
            {6, 22},              // Version 4
            {6, 26},              // Version 5
            {6, 30},              // Version 6
            {6, 34},              // Version 7
            {6, 22, 38},          // Version 8
            {6, 24, 42},          // Version 9
            {6, 26, 46},          // Version 10
            {6, 28, 50},          // Version 11
            {6, 30, 54},          // Version 12
            {6, 32, 58},          // Version 13
            {6, 34, 62},          // Version 14
            {6, 26, 46, 66},      // Version 15
            {6, 26, 48, 70},      // Version 16
            {6, 26, 50, 74},      // Version 17
            {6, 30, 54, 78},      // Version 18
            {6, 30, 56, 82},      // Version 19
            {6, 30, 58, 86},      // Version 20
            {6, 34, 62, 90},      // Version 21
            {6, 28, 50, 72, 94},  // Version 22
            {6, 26, 50, 74, 98},  // Version 23
            {6, 30, 54, 78, 102}, // Version 24
            {6, 28, 54, 80, 106}, // Version 25
            {6, 32, 58, 84, 110}, // Version 26
            {6, 30, 58, 86, 114}, // Version 27
            {6, 34, 62, 90, 118}, // Version 28
            {6, 26, 50, 74, 98, 122}, // Version 29
            {6, 30, 54, 78, 102, 126}, // Version 30
            {6, 26, 52, 78, 104, 130}, // Version 31
            {6, 30, 56, 82, 108, 134}, // Version 32
            {6, 34, 60, 86, 112, 138}, // Version 33
            {6, 30, 58, 86, 114, 142}, // Version 34
            {6, 34, 62, 90, 118, 146}, // Version 35
            {6, 30, 54, 78, 102, 126, 150}, // Version 36
            {6, 24, 50, 76, 102, 128, 154}, // Version 37
            {6, 28, 54, 80, 106, 132, 158}, // Version 38
            {6, 32, 58, 84, 110, 136, 162}, // Version 39
            {6, 26, 54, 82, 110, 138, 166}, // Version 40
    };

    /**
     * Encode a string content into a QR code BitMatrix.
     *
     * @param content The content to encode
     * @param width   The desired width
     * @param height  The desired height
     * @param level   The error correction level
     * @param charset The charset to use (e.g., "GB18030" for Chinese, "UTF-8")
     * @return The BitMatrix representing the QR code
     */
    public static BitMatrix encode(String content, int width, int height, ErrorCorrectionLevel level, String charset) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height must be non-negative");
        }

        Charset cs = Charset.forName(charset != null ? charset : "UTF-8");
        byte[] data = content.getBytes(cs);

        // Find the minimum version that can hold the data
        int ecLevelIndex = level.ordinal();
        int version = findMinVersion(data.length, ecLevelIndex);

        // Calculate the matrix size for this version
        int matrixSize = getMatrixSize(version);

        // Get capacity info
        int[] capacity = DATA_CAPACITY[version - 1];
        int dataCapacity = capacity[ecLevelIndex];

        // Encode the data
        byte[] encodedData = encodeData(data, dataCapacity, version, level);

        // Create and populate the matrix
        BitMatrix matrix = new BitMatrix(matrixSize, matrixSize);
        buildMatrix(matrix, encodedData, version, level);

        // Scale if needed
        if (matrixSize != width || matrixSize != height) {
            matrix = scaleMatrix(matrix, width, height);
        }

        return matrix;
    }

    private static int findMinVersion(int dataLen, int ecLevelIndex) {
        for (int v = 1; v <= 40; v++) {
            if (DATA_CAPACITY[v - 1][ecLevelIndex] >= dataLen) {
                return v;
            }
        }
        return 40; // Maximum version
    }

    private static int getMatrixSize(int version) {
        return 17 + version * 4;
    }

    private static byte[] encodeData(byte[] rawData, int dataCapacity, int version, ErrorCorrectionLevel level) {
        ReedSolomonEncoder rs = new ReedSolomonEncoder();

        int ecLevelIndex = level.ordinal();
        int numBlocks = NUM_BLOCKS[version - 1][ecLevelIndex];
        int ecPerBlock = ECC_PER_BLOCK[version - 1][ecLevelIndex];
        int totalEc = numBlocks * ecPerBlock;
        int totalData = dataCapacity;

        // Build the full bit sequence: mode indicator + character count + data bits
        int characterCountBits = getCharacterCountBits(version, 2); // 2 = byte mode
        BitBuilder bits = new BitBuilder();
        bits.appendBits(4, 0x40); // Byte mode indicator = 0100
        bits.appendBits(characterCountBits, rawData.length);
        for (byte b : rawData) {
            bits.appendBits(8, b & 0xFF);
        }

        // Add terminator (up to 4 zeros)
        bits.appendBits(4, 0);

        // Pad to byte boundary (already aligned since we append in 8-bit chunks)

        // Calculate how many data bytes we need vs what we have
        int totalDataBits = totalData * 8;
        int bitsNeeded = totalDataBits - bits.bitCount(); // remaining bits to fill
        if (bitsNeeded < 0) {
            bitsNeeded = 0; // data itself exceeded capacity (shouldn't happen if version is right)
        }

        // Add padding bytes to fill remaining capacity
        byte[] padBytes = new byte[]{(byte) 0xEC, (byte) 0x11};
        int padIdx = 0;
        while (bits.bitCount() < totalDataBits) {
            bits.appendBits(8, padBytes[padIdx % padBytes.length] & 0xFF);
            padIdx++;
        }

        // Convert bits to bytes
        byte[] formattedData = bits.toBytes(totalData);

        // Split into blocks
        int dataPerBlock = totalData / numBlocks;
        int remainder = totalData % numBlocks;

        byte[][] blocks = new byte[numBlocks][];
        byte[][] ecBlocks = new byte[numBlocks][];

        int offset = 0;
        for (int b = 0; b < numBlocks; b++) {
            int blockSize = dataPerBlock + (b < remainder ? 1 : 0);
            blocks[b] = new byte[blockSize];
            System.arraycopy(formattedData, offset, blocks[b], 0, blockSize);
            offset += blockSize;

            ecBlocks[b] = rs.encode(blocks[b], ecPerBlock);
        }

        // Interleave data and ECC
        int totalLen = totalData + totalEc;
        byte[] result = new byte[totalLen];
        int pos = 0;

        for (int i = 0; i < dataPerBlock + 1; i++) {
            for (int b = 0; b < numBlocks; b++) {
                if (i < blocks[b].length) {
                    result[pos++] = blocks[b][i];
                }
            }
        }

        for (int i = 0; i < ecPerBlock; i++) {
            for (int b = 0; b < numBlocks; b++) {
                result[pos++] = ecBlocks[b][i];
            }
        }

        return result;
    }

    private static int getCharacterCountBits(int version, int mode) {
        if (version < 10) {
            return 8;
        } else if (version < 27) {
            return 16;
        } else {
            return 16;
        }
    }

    private static void buildMatrix(BitMatrix matrix, byte[] data, int version, ErrorCorrectionLevel level) {
        int size = matrix.getWidth();

        // Add finder patterns
        addFinderPatterns(matrix, size);

        // Add separators
        addSeparators(matrix, size);

        // Add alignment patterns
        addAlignmentPatterns(matrix, version, size);

        // Add timing patterns
        addTimingPatterns(matrix, size);

        // Add dark module
        matrix.set(size - 9, 8);

        // Reserve and add format info
        byte[] formatInfo = calculateFormatInfo(level, 0); // mask pattern 0
        addFormatInfo(matrix, formatInfo, size);

        // Add version info for version >= 7
        if (version >= 7) {
            byte[] versionInfo = calculateVersionInfo(version);
            addVersionInfo(matrix, versionInfo, size);
        }

        // Add data
        addData(matrix, data, size);
    }

    private static void addFinderPatterns(BitMatrix matrix, int size) {
        // Top-left finder pattern
        addFinderPattern(matrix, 0, 0);

        // Top-right finder pattern
        addFinderPattern(matrix, size - 7, 0);

        // Bottom-left finder pattern
        addFinderPattern(matrix, 0, size - 7);
    }

    private static void addFinderPattern(BitMatrix matrix, int row, int col) {
        // Outer black border (7x7)
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (r == 0 || r == 6 || c == 0 || c == 6) {
                    matrix.set(col + c, row + r);
                } else if (r >= 2 && r <= 4 && c >= 2 && c <= 4) {
                    matrix.set(col + c, row + r);
                }
            }
        }

        // White separator
        for (int i = 0; i < 8; i++) {
            if (col + 7 < matrix.getWidth()) matrix.unset(col + 7, i);
            if (row + 7 < matrix.getHeight()) matrix.unset(i, row + 7);
        }
    }

    private static void addSeparators(BitMatrix matrix, int size) {
        // Horizontal separators (top and bottom around finder patterns)
        for (int x = 0; x < 8; x++) {
            matrix.unset(x, 7);
            matrix.unset(x, size - 8);
        }
        for (int x = size - 8; x < size; x++) {
            matrix.unset(x, 7);
            matrix.unset(x, size - 8);
        }

        // Vertical separators (left and right)
        for (int y = 0; y < 8; y++) {
            matrix.unset(7, y);
            matrix.unset(size - 8, y);
        }
        for (int y = size - 8; y < size; y++) {
            matrix.unset(7, y);
            matrix.unset(size - 8, y);
        }
    }

    private static void addAlignmentPatterns(BitMatrix matrix, int version, int size) {
        if (version == 1) return;

        int[] centers = ALIGNMENT_CENTERS[version - 1];
        for (int row : centers) {
            for (int col : centers) {
                // Skip if overlapping with finder patterns
                if ((row < 9 && col < 9) ||
                        (row < 9 && col > size - 10) ||
                        (row > size - 10 && col < 9)) {
                    continue;
                }
                addAlignmentPattern(matrix, row, col);
            }
        }
    }

    private static void addAlignmentPattern(BitMatrix matrix, int row, int col) {
        for (int r = -2; r <= 2; r++) {
            for (int c = -2; c <= 2; c++) {
                if (Math.abs(r) == 2 || Math.abs(c) == 2 || (r == 0 && c == 0)) {
                    matrix.set(col + c, row + r);
                } else {
                    matrix.unset(col + c, row + r);
                }
            }
        }
    }

    private static void addTimingPatterns(BitMatrix matrix, int size) {
        for (int i = 8; i < size - 8; i++) {
            if (i % 2 == 0) {
                matrix.set(i, 6);  // Vertical timing
                matrix.set(6, i);  // Horizontal timing
            }
        }
    }

    private static byte[] calculateFormatInfo(ErrorCorrectionLevel level, int maskPattern) {
        // Format info encoding
        int data = (level.getBits() << 3) | maskPattern;

        // Calculate BCH(15,5) for format info
        int format = data << 10; // 5 bits -> 15 bits
        format |= calculateBCH(format, 0x537, 10);

        // XOR with mask 0x21510
        format ^= 0x5412;

        byte[] result = new byte[15];
        for (int i = 14; i >= 0; i--) {
            result[14 - i] = (byte) ((format >> i) & 1);
        }

        return result;
    }

    private static byte[] calculateVersionInfo(int version) {
        // Version info encoding (for version >= 7)
        int data = version << 12;
        data |= calculateBCH(data, 0x1f25, 12);

        byte[] result = new byte[18];
        for (int i = 17; i >= 0; i--) {
            result[17 - i] = (byte) ((data >> i) & 1);
        }

        return result;
    }

    private static int calculateBCH(int data, int poly, int bits) {
        int result = data;
        for (int i = bits - 1; i >= 0; i--) {
            if ((result & (1 << (i + bits))) != 0) {
                result ^= poly << i;
            }
        }
        return result;
    }

    private static void addFormatInfo(BitMatrix matrix, byte[] formatInfo, int size) {
        // Format info: 15 bits placed in a fixed L-shape around finder patterns
        // The 15 positions are (x,y) pairs, in order of bit index 0-14:
        // bits 0-12: the L-shape around the top-left finder
        // bits 13-14: continuation around bottom-left finder

        // Row y=8, x=size-1 down to x=0 (15 cells, skipping x=6 which is timing pattern)
        int idx = 0;
        for (int x = size - 1; x >= 0; x--) {
            if (x == 6) continue; // skip timing pattern column
            if (idx < 15) {
                matrix.set(x, 8, formatInfo[idx] == 1);
                idx++;
            }
        }

        // Column x=8, y=size-1 down to y=7 (continuing the L-shape)
        for (int y = size - 1; y >= 7; y--) {
            if (y == 6) continue;
            if (idx < 15) {
                matrix.set(8, y, formatInfo[idx] == 1);
                idx++;
            }
        }
    }

    private static void addVersionInfo(BitMatrix matrix, byte[] versionInfo, int size) {
        int idx = 0;

        // Top-right corner (above timing pattern)
        for (int y = 0; y < 6; y++) {
            for (int x = size - 11; x < size - 8; x++) {
                matrix.set(x, y, versionInfo[idx++] == 1);
            }
        }

        // Bottom-left corner (left of timing pattern)
        for (int x = 0; x < 6; x++) {
            for (int y = size - 11; y < size - 8; y++) {
                matrix.set(x, y, versionInfo[idx++] == 1);
            }
        }
    }

    private static void addData(BitMatrix matrix, byte[] data, int size) {
        int totalBits = data.length * 8;
        int bitIdx = 0;

        boolean upward = true;
        int col = size - 1;

        while (col > 0) {
            if (col == 6) col--; // Skip timing pattern column

            for (int row = 0; row < size; row++) {
                int y = upward ? size - 1 - row : row;

                for (int c = 0; c < 2; c++) {
                    int x = col - c;

                    // Skip if reserved
                    if (isReserved(x, y, size)) continue;

                    if (bitIdx < totalBits) {
                        int byteIdx = bitIdx / 8;
                        int bitOffset = 7 - (bitIdx % 8);
                        boolean bit = ((data[byteIdx] >> bitOffset) & 1) == 1;
                        matrix.set(x, y, bit);
                        bitIdx++;
                    }
                }
            }

            col -= 2;
            upward = !upward;
        }
    }

    private static boolean isReserved(int x, int y, int size) {
        // Finder patterns + separators
        if (y < 9) {
            if (x < 9 || x >= size - 8) return true;
        }
        if (y >= size - 8) {
            if (x < 9) return true;
        }
        if (x < 9) {
            if (y >= size - 8) return true;
        }

        // Timing patterns
        if (y == 6 || x == 6) return true;

        // Version info areas
        if (size >= 21) { // Version 2+
            if (y < 6 && x >= size - 11) return true;
            if (x < 6 && y >= size - 11) return true;
        }

        return false;
    }

    private static BitMatrix scaleMatrix(BitMatrix matrix, int targetWidth, int targetHeight) {
        int oldWidth = matrix.getWidth();
        int oldHeight = matrix.getHeight();

        // For simplicity, if the target is larger, just return the original
        // In a full implementation, you would scale the matrix
        if (targetWidth <= oldWidth && targetHeight <= oldHeight) {
            return matrix;
        }

        // Return original for now - actual scaling would be done by the caller
        return matrix;
    }

    private static class BitBuilder {
        private byte[] bytes = new byte[512];
        private int bitCount = 0;

        void appendBits(int numBits, int value) {
            for (int i = numBits - 1; i >= 0; i--) {
                if ((value & (1 << i)) != 0) {
                    setBit(bitCount, true);
                }
                bitCount++;
            }
        }

        private void ensureCapacity() {
            if (bitCount / 8 >= bytes.length) {
                byte[] nb = new byte[bytes.length * 2];
                System.arraycopy(bytes, 0, nb, 0, bytes.length);
                bytes = nb;
            }
        }

        void setBit(int pos, boolean value) {
            ensureCapacity();
            if (value) {
                bytes[pos / 8] |= (1 << (7 - (pos % 8)));
            }
        }

        int bitCount() {
            return bitCount;
        }

        byte[] toBytes(int totalDataBytes) {
            byte[] result = new byte[totalDataBytes];
            for (int i = 0; i < totalDataBytes * 8; i++) {
                if (i < bitCount && (bytes[i / 8] & (1 << (7 - (i % 8)))) != 0) {
                    result[i / 8] |= (1 << (7 - (i % 8)));
                }
            }
            return result;
        }
    }
}
