package com.zifang.util.media.graph.qrcode.decoder;

import com.zifang.util.media.graph.qrcode.encoder.BitMatrix;

import java.awt.image.BufferedImage;

/**
 * Converts a BufferedImage to a binary (black/white) BitMatrix.
 * Implements Otsu's method for automatic threshold selection.
 */
public final class Binarizer {

    private Binarizer() {
    }

    /**
     * Converts an image to a binary matrix using a threshold.
     *
     * @param image     the source image
     * @param threshold pixel value >= threshold is white, otherwise black (0-255)
     * @return binary matrix
     */
    public static BitMatrix binarize(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        BitMatrix matrix = new BitMatrix(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // Luminance using standard coefficients
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                matrix.set(x, y, gray >= threshold);
            }
        }
        return matrix;
    }

    /**
     * Converts an image to a binary matrix using Otsu's automatic threshold.
     */
    public static BitMatrix binarize(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // First pass: compute histogram
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int) (0.299 * ((rgb >> 16) & 0xFF)
                        + 0.587 * ((rgb >> 8) & 0xFF)
                        + 0.114 * (rgb & 0xFF));
                histogram[gray]++;
            }
        }

        // Otsu's threshold
        int total = width * height;
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;
        double maxVariance = 0;
        int threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0) continue;
            int wF = total - wB;
            if (wF == 0) break;
            sumB += t * histogram[t];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double variance = wB * wF * (mB - mF) * (mB - mF);
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = t;
            }
        }

        return binarize(image, threshold);
    }
}
