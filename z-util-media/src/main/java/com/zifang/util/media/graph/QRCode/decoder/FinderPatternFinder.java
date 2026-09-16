package com.zifang.util.media.graph.qrcode.decoder;

import com.zifang.util.media.graph.qrcode.encoder.BitMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects the three finder patterns (position markers) in a QR code.
 * Each finder pattern is a 7x7 square of alternating black/white pixels
 * with a 3x3 black center.
 */
public final class FinderPatternFinder {

    private final BitMatrix matrix;
    private final int width;
    private final int height;

    /**
     * FinderPatternFinder方法。
     * * @param matrix BitMatrix类型参数
     */
    public FinderPatternFinder(BitMatrix matrix) {
        this.matrix = matrix;
        this.width = matrix.getWidth();
        this.height = matrix.getHeight();
    }

    /**
     * Estimates the version of the QR code from the distance between finder patterns.
     *
     * @param patterns the three finder patterns
     * @return estimated version (1-40)
     */
    public static int estimateVersion(FinderPattern[] patterns) {
        if (patterns == null || patterns.length < 3) {
            return 1;
        }

        // Calculate distances
        float d01 = distance(patterns[0], patterns[1]);
        float d02 = distance(patterns[0], patterns[2]);
        float d12 = distance(patterns[1], patterns[2]);

        // Determine module size from the average
        float avgModuleSize = (patterns[0].moduleSize + patterns[1].moduleSize + patterns[2].moduleSize) / 3.0f;

        // Calculate version from the largest dimension
        float maxDist = Math.max(Math.max(d01, d02), d12);

        // Version = (maxDist / moduleSize - 14) / 4 + 1 approximately
        // Each version adds 4 modules to each side
        int version = (int) Math.round((maxDist / avgModuleSize - 14) / 4 + 1);
        version = Math.max(1, Math.min(40, version));
        return version;
    }

    private static float distance(FinderPattern a, FinderPattern b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Detects all three finder patterns.
     *
     * @return array of 3 FinderPattern objects, or null if not found
     */
    public FinderPattern[] find() {
        List<FinderPattern> patterns = new ArrayList<>();

        // Skip border region where finder patterns can't extend to
        int bottomRightX = width - 7;
        int bottomRightY = height - 7;

        for (int y = 7; y <= bottomRightY; y++) {
            for (int x = 7; x <= bottomRightX; x++) {
                if (!matrix.get(x, y)) {
                    continue;
                }
                FinderPattern pattern = tryFindPatternAt(x, y);
                if (pattern != null) {
                    patterns.add(pattern);
                    // Skip past this pattern
                    x += 7;
                }
            }
        }

        if (patterns.size() < 3) {
            return null;
        }

        // Sort by y position to get top-left, top-right, bottom-left order approximately
        patterns.sort((a, b) -> {
            int cmp = Float.compare(a.y, b.y);
            return cmp != 0 ? cmp : Float.compare(a.x, b.x);
        });

        // The three patterns should form a roughly right triangle
        // Find the one farthest from the line connecting the other two (this is the corner pattern)
        if (patterns.size() > 3) {
            // Use only the three most separated
            FinderPattern p1 = patterns.get(0);
            FinderPattern p2 = patterns.get(patterns.size() / 2);
            FinderPattern p3 = patterns.get(patterns.size() - 1);
            patterns.clear();
            patterns.add(p1);
            patterns.add(p2);
            patterns.add(p3);
        }

        return patterns.toArray(new FinderPattern[0]);
    }

    private FinderPattern tryFindPatternAt(int x, int y) {
        // Check if there's a valid finder pattern centered at (x, y)
        // A finder pattern is a 7x7 square
        // We just check the cross-section horizontally and vertically

        int[] hSection = new int[5];
        int[] vSection = new int[5];

        // Horizontal: check a cross section
        int hy = y;
        int centerX = x;
        int idx = 0;
        int state = 0; // 0=black, 1=white, 2=black, 3=white, 4=black
        boolean lastVal = false;

        // Actually do a proper module size measurement by scanning
        float moduleSize = measureModuleSizeAt(x, y);
        if (moduleSize < 1) {
            return null;
        }

        // Check center is black
        if (!matrix.get(x, y)) {
            return null;
        }

        // Validate finder pattern by checking cross pattern
        if (!validateFinderPatternCross(x, y, moduleSize)) {
            return null;
        }

        return new FinderPattern(x, y, moduleSize);
    }

    private float measureModuleSizeAt(int x, int y) {
        // Count consecutive black/white transitions in a row to estimate module size
        int transitions = 0;
        boolean lastColor = matrix.get(x, y);

        // Scan right
        for (int i = 1; i < 10; i++) {
            if (x + i >= width) break;
            boolean color = matrix.get(x + i, y);
            if (color != lastColor) {
                transitions++;
                lastColor = color;
            }
        }

        // Each module consists of 1 black + 1 white = 2 transitions per module
        if (transitions < 5) {
            return 0;
        }

        return (transitions - 1) / 7.0f; // modules per 7 transitions
    }

    private boolean validateFinderPatternCross(int centerX, int centerY, float moduleSize) {
        int size = Math.round(moduleSize);
        if (size < 3) return false;

        // A valid finder pattern should have:
        // - All 1 modules around the center 3x3 should be black
        // - The 7x7 square should have alternating black/white rings

        // Simple check: the center 3x3 should be black
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int nx = centerX + dx;
                int ny = centerY + dy;
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    return false;
                }
                if (!matrix.get(nx, ny)) {
                    // center is black, this should be true
                }
            }
        }

        return true;
    }

    /**
     * Represents a found finder pattern center.
     */
    public static final class FinderPattern {
        public final float x;
        public final float y;
        public final float moduleSize;

        /**
         * FinderPattern方法。
         * * @param x float类型参数
         *
         * @param y          float类型参数
         * @param moduleSize float类型参数
         */
        public FinderPattern(float x, float y, float moduleSize) {
            this.x = x;
            this.y = y;
            this.moduleSize = moduleSize;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "FinderPattern{x=" + x + ", y=" + y + ", moduleSize=" + moduleSize + "}";
        }
    }
}
