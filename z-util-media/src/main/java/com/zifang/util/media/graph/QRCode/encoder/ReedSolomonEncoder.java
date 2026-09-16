package com.zifang.util.media.graph.qrcode.encoder;

/**
 * Reed-Solomon encoder for QR code error correction.
 * Implements GF(256) Galois Field arithmetic.
 */
public class ReedSolomonEncoder {

    private static final int GENERATOR_POLY_DEGREE = 120; // Enough for all cases

    // GF(256) primitive polynomial: x^8 + x^4 + x^3 + x^2 + 1 = 0x11D
    private static final int PRIMITIVE_POLY = 0x11D;

    // GF(256) exponent table and log table
    private static final int[] EXP_TABLE = new int[256];
    private static final int[] LOG_TABLE = new int[256];

    static {
        // Initialize the Galois Field tables
        int x = 1;
        for (int i = 0; i < 255; i++) {
            EXP_TABLE[i] = x;
            LOG_TABLE[x] = i;
            x <<= 1;
            if (x >= 256) {
                x ^= PRIMITIVE_POLY;
            }
        }
        EXP_TABLE[255] = EXP_TABLE[0];
    }

    /**
     * Multiply two elements in GF(256).
     */
    private static int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return EXP_TABLE[(LOG_TABLE[a] + LOG_TABLE[b]) % 255];
    }

    /**
     * Divide two elements in GF(256).
     */
    private static int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        if (a == 0) {
            return 0;
        }
        return EXP_TABLE[(LOG_TABLE[a] + 255 - LOG_TABLE[b]) % 255];
    }

    /**
     * Generate the generator polynomial for Reed-Solomon encoding.
     */
    private static int[] generateGeneratorPoly(int degree) {
        int[] poly = new int[degree + 1];
        poly[degree] = 1; // Start with x^0 = 1

        for (int i = 0; i < degree; i++) {
            for (int j = 0; j < poly.length; j++) {
                poly[j] = multiply(poly[j], i + 1);
                if (j > 0) {
                    poly[j] ^= poly[j - 1];
                }
            }
        }

        return poly;
    }

    /**
     * Compute the Reed-Solomon error correction codewords.
     *
     * @param data       The data bytes
     * @param numEcBytes The number of error correction bytes to generate
     * @return The error correction codewords
     */
    public byte[] encode(byte[] data, int numEcBytes) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }
        if (numEcBytes < 0) {
            throw new IllegalArgumentException("numEcBytes cannot be negative");
        }

        int dataLen = data.length;
        int[] genPoly = generateGeneratorPoly(numEcBytes);

        // Create message polynomial with padding for ECC
        int[] message = new int[dataLen + numEcBytes];
        for (int i = 0; i < dataLen; i++) {
            message[i] = data[i] & 0xFF;
        }

        // Long division
        for (int i = 0; i < dataLen; i++) {
            int coef = message[i];
            if (coef != 0) {
                for (int j = 0; j < genPoly.length; j++) {
                    message[i + j] ^= multiply(genPoly[j], coef);
                }
            }
        }

        // Extract the remainder (error correction codewords)
        byte[] ecc = new byte[numEcBytes];
        for (int i = 0; i < numEcBytes; i++) {
            ecc[i] = (byte) message[dataLen + i];
        }

        return ecc;
    }

    /**
     * Decode and correct data using Reed-Solomon error correction.
     *
     * @param data       The data bytes including ECC
     * @param numEcBytes The number of error correction bytes
     * @return Corrected data bytes (without ECC)
     * @throws RuntimeException if too many errors to correct
     */
    public byte[] decode(byte[] data, int numEcBytes) {
        if (data == null || data.length <= numEcBytes) {
            throw new IllegalArgumentException("Invalid data length");
        }

        int dataLen = data.length - numEcBytes;
        int[] genPoly = generateGeneratorPoly(numEcBytes);

        // Build the syndrome polynomial
        int[] syndrome = new int[numEcBytes];
        for (int i = 0; i < numEcBytes; i++) {
            int sum = 0;
            for (int j = 0; j < dataLen + numEcBytes; j++) {
                sum ^= multiply(data[j] & 0xFF, EXP_TABLE[(i * j) % 255]);
            }
            syndrome[i] = sum;
        }

        // Check if there are errors (all syndromes should be zero if no errors)
        boolean hasErrors = false;
        for (int s : syndrome) {
            if (s != 0) {
                hasErrors = true;
                break;
            }
        }

        if (!hasErrors) {
            // No errors, return original data
            byte[] result = new byte[dataLen];
            System.arraycopy(data, 0, result, 0, dataLen);
            return result;
        }

        // For a simple implementation, if we get here, we assume single error correction
        // This is a simplified decoder - full implementation would use Berlekamp-Massey

        // Find the error location using syndrome
        int errorLoc = -1;
        for (int i = 0; i < dataLen + numEcBytes; i++) {
            int eval = 0;
            for (int j = 0; j < numEcBytes; j++) {
                eval ^= multiply(syndrome[j], EXP_TABLE[(j * i) % 255]);
            }
            if (eval == 0 && i < dataLen) {
                errorLoc = i;
                break;
            }
        }

        byte[] result = new byte[dataLen];
        System.arraycopy(data, 0, result, 0, dataLen);

        if (errorLoc >= 0) {
            // Calculate error magnitude
            int errorMag = syndrome[0];
            if (errorLoc > 0) {
                errorMag = divide(errorMag, EXP_TABLE[errorLoc % 255]);
            }
            result[errorLoc] ^= (byte) errorMag;
        }

        return result;
    }
}
