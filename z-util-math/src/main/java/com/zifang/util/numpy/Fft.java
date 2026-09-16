package com.zifang.util.numpy;

/**
 * FFT operations for 1D and 2D signals.
 * Provides numpy.fft equivalent functionality.
 */
public class Fft {

    private static final double TWO_PI = 2.0 * Math.PI;

    private static double getDouble(NdArray a, int... indices) {
        Object val = a.get(indices);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        throw new IllegalArgumentException("Expected numeric value");
    }

    /**
     * 1D Discrete Fourier Transform.
     * Returns the FFT of the input array.
     */
    public static ComplexNdArray fft(ComplexNdArray x) {
        int n = x.length();
        if (n == 1) {
            return new ComplexNdArray(new double[]{x.re(0)}, new double[]{x.im(0)});
        }

        // Cooley-Tukey FFT (radix-2 DIT)
        if ((n & (n - 1)) == 0) {
            return fftRadix2(x);
        }

        // Fall back to naive DFT for non-power-of-2
        return fftNaive(x);
    }

    /**
     * fft方法。
     * * @param x NdArray类型参数
     *
     * @return static ComplexNdArray类型返回值
     */
    public static ComplexNdArray fft(NdArray x) {
        return fft(toComplex(x));
    }

    private static ComplexNdArray fftRadix2(ComplexNdArray x) {
        int n = x.length();
        if (n == 1) {
            return x.copy();
        }

        int halfN = n / 2;
        ComplexNdArray even = new ComplexNdArray(new double[halfN], new double[halfN]);
        ComplexNdArray odd = new ComplexNdArray(new double[halfN], new double[halfN]);

        for (int i = 0; i < halfN; i++) {
            even.set(i, x.re(2 * i), x.im(2 * i));
            odd.set(i, x.re(2 * i + 1), x.im(2 * i + 1));
        }

        ComplexNdArray evenFFT = fftRadix2(even);
        ComplexNdArray oddFFT = fftRadix2(odd);

        double[] resultRe = new double[n];
        double[] resultIm = new double[n];

        for (int i = 0; i < halfN; i++) {
            double theta = -TWO_PI * i / n;
            double wRe = Math.cos(theta);
            double wIm = Math.sin(theta);
            double oddRe = oddFFT.re(i);
            double oddIm = oddFFT.im(i);

            resultRe[i] = evenFFT.re(i) + wRe * oddRe - wIm * oddIm;
            resultIm[i] = evenFFT.im(i) + wRe * oddIm + wIm * oddRe;

            resultRe[i + halfN] = evenFFT.re(i) - wRe * oddRe + wIm * oddIm;
            resultIm[i + halfN] = evenFFT.im(i) - wRe * oddIm - wIm * oddRe;
        }

        return new ComplexNdArray(resultRe, resultIm);
    }

    private static ComplexNdArray fftNaive(ComplexNdArray x) {
        int n = x.length();
        double[] resultRe = new double[n];
        double[] resultIm = new double[n];

        for (int k = 0; k < n; k++) {
            double sumRe = 0;
            double sumIm = 0;
            for (int t = 0; t < n; t++) {
                double theta = -TWO_PI * t * k / n;
                sumRe += x.re(t) * Math.cos(theta) - x.im(t) * Math.sin(theta);
                sumIm += x.re(t) * Math.sin(theta) + x.im(t) * Math.cos(theta);
            }
            resultRe[k] = sumRe;
            resultIm[k] = sumIm;
        }

        return new ComplexNdArray(resultRe, resultIm);
    }

    /**
     * 1D Inverse Discrete Fourier Transform.
     */
    public static ComplexNdArray ifft(ComplexNdArray x) {
        int n = x.length();
        // Conjugate, FFT, conjugate, scale
        ComplexNdArray conj = x.conjugate();
        ComplexNdArray fftConj = fft(conj);
        double[] resultRe = new double[n];
        double[] resultIm = new double[n];
        for (int i = 0; i < n; i++) {
            resultRe[i] = fftConj.re(i) / n;
            resultIm[i] = fftConj.im(i) / n;
        }
        return new ComplexNdArray(resultRe, resultIm);
    }

    /**
     * ifft方法。
     * * @param x NdArray类型参数
     *
     * @return static ComplexNdArray类型返回值
     */
    public static ComplexNdArray ifft(NdArray x) {
        return ifft(toComplex(x));
    }

    /**
     * 2D Discrete Fourier Transform.
     */
    public static ComplexNdArray fft2(ComplexNdArray x) {
        int rows = x.rows();
        int cols = x.cols();

        // FFT along rows
        ComplexNdArray rowFFT = new ComplexNdArray(rows, cols);
        for (int i = 0; i < rows; i++) {
            ComplexNdArray row = x.getRow(i);
            ComplexNdArray rowResult = fft(row);
            for (int j = 0; j < cols; j++) {
                rowFFT.set(i, j, rowResult.re(j), rowResult.im(j));
            }
        }

        // FFT along columns
        ComplexNdArray colFFT = new ComplexNdArray(rows, cols);
        for (int j = 0; j < cols; j++) {
            ComplexNdArray col = rowFFT.getColumn(j);
            ComplexNdArray colResult = fft(col);
            for (int i = 0; i < rows; i++) {
                colFFT.set(i, j, colResult.re(i), colResult.im(i));
            }
        }

        return colFFT;
    }

    /**
     * fft2方法。
     * * @param x NdArray类型参数
     *
     * @return static ComplexNdArray类型返回值
     */
    public static ComplexNdArray fft2(NdArray x) {
        return fft2(toComplex(x));
    }

    /**
     * 2D Inverse Discrete Fourier Transform.
     */
    public static ComplexNdArray ifft2(ComplexNdArray x) {
        int rows = x.rows();
        int cols = x.cols();

        // IFFT along rows
        ComplexNdArray rowIFFT = new ComplexNdArray(rows, cols);
        for (int i = 0; i < rows; i++) {
            ComplexNdArray row = x.getRow(i);
            ComplexNdArray rowResult = ifft(row);
            for (int j = 0; j < cols; j++) {
                rowIFFT.set(i, j, rowResult.re(j), rowResult.im(j));
            }
        }

        // IFFT along columns
        ComplexNdArray colIFFT = new ComplexNdArray(rows, cols);
        for (int j = 0; j < cols; j++) {
            ComplexNdArray col = rowIFFT.getColumn(j);
            ComplexNdArray colResult = ifft(col);
            for (int i = 0; i < rows; i++) {
                colIFFT.set(i, j, colResult.re(i), colResult.im(i));
            }
        }

        return colIFFT;
    }

    /**
     * ifft2方法。
     * * @param x NdArray类型参数
     *
     * @return static ComplexNdArray类型返回值
     */
    public static ComplexNdArray ifft2(NdArray x) {
        return ifft2(toComplex(x));
    }

    /**
     * FFT frequency bins.
     * Returns the frequencies for the FFT output.
     *
     * @param n Number of samples
     * @param d Sample spacing
     */
    public static NdArray fftfreq(int n, double d) {
        double[] freqs = new double[n];
        double halfN = n / 2.0;

        for (int i = 0; i < n; i++) {
            if (i < halfN) {
                freqs[i] = i / (n * d);
            } else {
                freqs[i] = (i - n) / (n * d);
            }
        }

        return NdArray.create(freqs, DType.FLOAT64, new Shape(n));
    }

    /**
     * Real FFT (only positive frequencies for real input).
     * Returns only the positive half of the frequency spectrum.
     */
    public static ComplexNdArray rfft(NdArray x) {
        int cols = x.ndim() == 1 ? x.getShape().get(0) : x.getShape().get(1);
        ComplexNdArray fullFFT = fft(x);
        int halfN = cols / 2 + 1;

        double[] resultRe = new double[halfN];
        double[] resultIm = new double[halfN];

        for (int i = 0; i < halfN; i++) {
            resultRe[i] = fullFFT.re(i);
            resultIm[i] = fullFFT.im(i);
        }

        return new ComplexNdArray(resultRe, resultIm);
    }

    private static ComplexNdArray toComplex(NdArray x) {
        int len;
        if (x.ndim() == 1) {
            len = x.getShape().get(0);
        } else if (x.ndim() == 2 && x.getShape().get(0) == 1) {
            len = x.getShape().get(1);
        } else if (x.ndim() == 2 && x.getShape().get(1) == 1) {
            len = x.getShape().get(0);
        } else {
            throw new IllegalArgumentException("Input must be 1D array");
        }

        double[] re = new double[len];
        double[] im = new double[len];
        for (int i = 0; i < len; i++) {
            re[i] = x.ndim() == 1 ? getDouble(x, i) : getDouble(x, 0, i);
            im[i] = 0;
        }
        return new ComplexNdArray(re, im);
    }

    /**
     * Complex number array for FFT results.
     */
    public static class ComplexNdArray {
        private final double[] re;
        private final double[] im;
        private final int length;
        private final int rows;
        private final int cols;

        /**
         * ComplexNdArray方法。
         * * @param real double[]类型参数
         *
         * @param imag double[]类型参数
         */
        public ComplexNdArray(double[] real, double[] imag) {
            this.re = real.clone();
            this.im = imag.clone();
            this.length = real.length;
            this.rows = 1;
            this.cols = real.length;
        }

        /**
         * ComplexNdArray方法。
         * * @param rows int类型参数
         *
         * @param cols int类型参数
         */
        public ComplexNdArray(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
            this.length = rows * cols;
            this.re = new double[length];
            this.im = new double[length];
        }

        /**
         * re方法。
         * * @param i int类型参数
         *
         * @return double类型返回值
         */
        public double re(int i) {
            return re[i];
        }

        /**
         * im方法。
         * * @param i int类型参数
         *
         * @return double类型返回值
         */
        public double im(int i) {
            return im[i];
        }

        /**
         * length方法。
         *
         * @return int类型返回值
         */
        public int length() {
            return length;
        }

        /**
         * rows方法。
         *
         * @return int类型返回值
         */
        public int rows() {
            return rows;
        }

        /**
         * cols方法。
         *
         * @return int类型返回值
         */
        public int cols() {
            return cols;
        }

        /**
         * set方法。
         * * @param i int类型参数
         *
         * @param real double类型参数
         * @param imag double类型参数
         */
        public void set(int i, double real, double imag) {
            re[i] = real;
            im[i] = imag;
        }

        /**
         * set方法。
         * * @param row int类型参数
         *
         * @param col  int类型参数
         * @param real double类型参数
         * @param imag double类型参数
         */
        public void set(int row, int col, double real, double imag) {
            re[row * cols + col] = real;
            im[row * cols + col] = imag;
        }

        /**
         * getRow方法。
         * * @param row int类型参数
         *
         * @return ComplexNdArray类型返回值
         */
        public ComplexNdArray getRow(int row) {
            double[] rowRe = new double[cols];
            double[] rowIm = new double[cols];
            for (int j = 0; j < cols; j++) {
                rowRe[j] = re[row * cols + j];
                rowIm[j] = im[row * cols + j];
            }
            return new ComplexNdArray(rowRe, rowIm);
        }

        /**
         * getColumn方法。
         * * @param col int类型参数
         *
         * @return ComplexNdArray类型返回值
         */
        public ComplexNdArray getColumn(int col) {
            double[] colRe = new double[rows];
            double[] colIm = new double[rows];
            for (int i = 0; i < rows; i++) {
                colRe[i] = re[i * cols + col];
                colIm[i] = im[i * cols + col];
            }
            return new ComplexNdArray(colRe, colIm);
        }

        /**
         * copy方法。
         *
         * @return ComplexNdArray类型返回值
         */
        public ComplexNdArray copy() {
            return new ComplexNdArray(re.clone(), im.clone());
        }

        /**
         * conjugate方法。
         *
         * @return ComplexNdArray类型返回值
         */
        public ComplexNdArray conjugate() {
            double[] conjRe = re.clone();
            double[] conjIm = new double[length];
            for (int i = 0; i < length; i++) {
                conjIm[i] = -im[i];
            }
            return new ComplexNdArray(conjRe, conjIm);
        }

        /**
         * magnitude方法。
         * * @param i int类型参数
         *
         * @return double类型返回值
         */
        public double magnitude(int i) {
            return Math.sqrt(re[i] * re[i] + im[i] * im[i]);
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < length; i++) {
                if (i > 0) sb.append(" ");
                sb.append(String.format("%.6f+%.6fj", re[i], im[i]));
                if (i < length - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
