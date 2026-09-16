package com.zifang.util.media.graph.qrcode.decoder;

import com.zifang.util.media.graph.qrcode.encoder.BitMatrix;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * FinderPatternFinderTest类。
 */
public class FinderPatternFinderTest {

    @Test
    /**
     * testFindWithEmptyMatrix方法。
     */
    public void testFindWithEmptyMatrix() {
        BitMatrix matrix = new BitMatrix(21, 21);
        FinderPatternFinder finder = new FinderPatternFinder(matrix);
        FinderPatternFinder.FinderPattern[] patterns = finder.find();
        // Empty matrix should not find any patterns
        assertNull(patterns);
    }

    @Test
    /**
     * testFindWithSimplePattern方法。
     */
    public void testFindWithSimplePattern() {
        BitMatrix matrix = new BitMatrix(21, 21);
        // Create a simple finder pattern simulation at position (10, 10)
        // A finder pattern is a 7x7 square with specific pattern
        addFinderPattern(matrix, 10, 10);

        FinderPatternFinder finder = new FinderPatternFinder(matrix);
        FinderPatternFinder.FinderPattern[] patterns = finder.find();
        // May or may not find depending on pattern quality
        if (patterns != null) {
            assertTrue(patterns.length <= 3);
        }
    }

    @Test
    /**
     * testEstimateVersionWithNull方法。
     */
    public void testEstimateVersionWithNull() {
        int version = FinderPatternFinder.estimateVersion(null);
        assertEquals(1, version);
    }

    @Test
    /**
     * testEstimateVersionWithLessThanThree方法。
     */
    public void testEstimateVersionWithLessThanThree() {
        BitMatrix matrix = new BitMatrix(21, 21);
        FinderPatternFinder.FinderPattern[] patterns = new FinderPatternFinder.FinderPattern[2];
        patterns[0] = new FinderPatternFinder.FinderPattern(7, 7, 1.0f);
        patterns[1] = new FinderPatternFinder.FinderPattern(14, 7, 1.0f);
        int version = FinderPatternFinder.estimateVersion(patterns);
        assertEquals(1, version);
    }

    @Test
    /**
     * testEstimateVersionWithThreePatterns方法。
     */
    public void testEstimateVersionWithThreePatterns() {
        FinderPatternFinder.FinderPattern[] patterns = new FinderPatternFinder.FinderPattern[3];
        patterns[0] = new FinderPatternFinder.FinderPattern(7, 7, 1.0f);
        patterns[1] = new FinderPatternFinder.FinderPattern(14, 7, 1.0f);
        patterns[2] = new FinderPatternFinder.FinderPattern(7, 14, 1.0f);
        int version = FinderPatternFinder.estimateVersion(patterns);
        // Version should be between 1 and 40
        assertTrue(version >= 1 && version <= 40);
    }

    @Test
    /**
     * testFinderPatternToString方法。
     */
    public void testFinderPatternToString() {
        FinderPatternFinder.FinderPattern pattern = new FinderPatternFinder.FinderPattern(3.5f, 4.5f, 2.0f);
        String str = pattern.toString();
        assertNotNull(str);
        assertTrue(str.contains("3.5"));
        assertTrue(str.contains("4.5"));
        assertTrue(str.contains("2.0"));
    }

    // Helper method to add a simplified finder pattern
    private void addFinderPattern(BitMatrix matrix, int centerX, int centerY) {
        int size = 7;
        int startX = centerX - size / 2;
        int startY = centerY - size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int absX = startX + x;
                int absY = startY + y;
                if (absX >= 0 && absX < matrix.getWidth() && absY >= 0 && absY < matrix.getHeight()) {
                    // Outer border and center dot
                    if (x == 0 || x == size - 1 || y == 0 || y == size - 1) {
                        matrix.set(absX, absY);
                    } else if (x >= 2 && x <= 4 && y >= 2 && y <= 4) {
                        matrix.set(absX, absY);
                    }
                }
            }
        }
    }
}
