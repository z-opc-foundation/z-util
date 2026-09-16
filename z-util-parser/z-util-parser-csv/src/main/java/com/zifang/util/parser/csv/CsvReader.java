package com.zifang.util.parser.csv;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * CSV Reader that implements Iterator for streaming large files.
 * Supports RFC 4180 format with custom delimiter, quoting, and escaping.
 */

/**
 * CsvReader类。
 */
public class CsvReader implements Iterator<String[]> {

    private final BufferedReader reader;
    private final char delimiter;
    private final boolean skipEmptyLines;
    private final boolean trimFields;

    private String[] nextLine;
    private boolean hasNext;
    private boolean closed;

    /**
     * Create a CsvReader from an InputStream with default settings
     */
    /**
     * CsvReader方法。
     * * @param inputStream InputStream类型参数
     */
    public CsvReader(InputStream inputStream) {
        this(inputStream, ',', true, false);
    }

    /**
     * Create a CsvReader from an InputStream with custom settings
     */
    /**
     * CsvReader方法。
     * * @param inputStream InputStream类型参数
     *
     * @param delimiter      char类型参数
     * @param skipEmptyLines boolean类型参数
     * @param trimFields     boolean类型参数
     */
    public CsvReader(InputStream inputStream, char delimiter, boolean skipEmptyLines, boolean trimFields) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        this.delimiter = delimiter;
        this.skipEmptyLines = skipEmptyLines;
        this.trimFields = trimFields;
        this.closed = false;
        advance();
    }

    /**
     * Create a CsvReader from a Reader with default settings
     */
    /**
     * CsvReader方法。
     * * @param reader Reader类型参数
     */
    public CsvReader(Reader reader) {
        this(reader, ',', true, false);
    }

    /**
     * Create a CsvReader from a Reader with custom settings
     */
    /**
     * CsvReader方法。
     * * @param reader Reader类型参数
     *
     * @param delimiter      char类型参数
     * @param skipEmptyLines boolean类型参数
     * @param trimFields     boolean类型参数
     */
    public CsvReader(Reader reader, char delimiter, boolean skipEmptyLines, boolean trimFields) {
        this.reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        this.delimiter = delimiter;
        this.skipEmptyLines = skipEmptyLines;
        this.trimFields = trimFields;
        this.closed = false;
        advance();
    }

    /**
     * Create a CsvReader from a file path
     */
    /**
     * CsvReader方法。
     * * @param filePath String类型参数
     */
    public CsvReader(String filePath) throws FileNotFoundException {
        this(new FileInputStream(filePath), ',', true, false);
    }

    /**
     * Create a CsvReader from a File
     */
    /**
     * CsvReader方法。
     * * @param file File类型参数
     */
    public CsvReader(File file) throws FileNotFoundException {
        this(new FileInputStream(file), ',', true, false);
    }

    /**
     * builder方法。
     *
     * @return static Builder类型返回值
     */
    public static Builder builder() {
        return new Builder();
    }

    private void advance() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (skipEmptyLines && line.trim().isEmpty()) {
                    continue;
                }
                nextLine = parseLine(line);
                hasNext = true;
                return;
            }
            nextLine = null;
            hasNext = false;
        } catch (IOException e) {
            throw new CsvException("Error reading CSV", e);
        }
    }

    @Override
    /**
     * hasNext方法。
     * @return boolean类型返回值
     */
    public boolean hasNext() {
        return hasNext && !closed;
    }

    /**
     * Read all remaining lines
     */

    @Override
    /**
     * next方法。
     * @return String[]类型返回值
     */
    public String[] next() {
        if (!hasNext) {
            throw new NoSuchElementException("No more lines in CSV");
        }
        String[] result = nextLine;
        advance();
        return result;
    }

    /**
     * Close the reader
     */

    /**
     * readAll方法。
     *
     * @return List<String[]>类型返回值
     */
    public List<String[]> readAll() {
        List<String[]> lines = new ArrayList<>();
        while (hasNext()) {
            lines.add(next());
        }
        return lines;
    }

    /**
     * close方法。
     */
    public void close() {
        if (!closed) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new CsvException("Error closing CSV reader", e);
            } finally {
                closed = true;
            }
        }
    }

    /**
     * Parse a single CSV line using state machine
     */
    private String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int len = line.length();

        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < len && line.charAt(i + 1) == '"') {
                        // Escaped quote ""
                        field.append('"');
                        i++;
                    } else {
                        // End of quoted field
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == delimiter) {
                    // End of field
                    fields.add(trimFields ? field.toString().trim() : field.toString());
                    field = new StringBuilder();
                } else {
                    field.append(c);
                }
            }
        }

        // Add last field
        fields.add(trimFields ? field.toString().trim() : field.toString());

        return fields.toArray(new String[0]);
    }

    // Builder pattern for configuration
    public static class Builder {
        private char delimiter = ',';
        private boolean skipEmptyLines = true;
        private boolean trimFields = false;

        /**
         * delimiter方法。
         * * @param delimiter char类型参数
         *
         * @return Builder类型返回值
         */
        public Builder delimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * skipEmptyLines方法。
         * * @param skipEmptyLines boolean类型参数
         *
         * @return Builder类型返回值
         */
        public Builder skipEmptyLines(boolean skipEmptyLines) {
            this.skipEmptyLines = skipEmptyLines;
            return this;
        }

        /**
         * trimFields方法。
         * * @param trimFields boolean类型参数
         *
         * @return Builder类型返回值
         */
        public Builder trimFields(boolean trimFields) {
            this.trimFields = trimFields;
            return this;
        }

        /**
         * build方法。
         * * @param inputStream InputStream类型参数
         *
         * @return CsvReader类型返回值
         */
        public CsvReader build(InputStream inputStream) {
            return new CsvReader(inputStream, delimiter, skipEmptyLines, trimFields);
        }

        /**
         * build方法。
         * * @param reader Reader类型参数
         *
         * @return CsvReader类型返回值
         */
        public CsvReader build(Reader reader) {
            return new CsvReader(reader, delimiter, skipEmptyLines, trimFields);
        }

        /**
         * build方法。
         * * @param filePath String类型参数
         *
         * @return CsvReader类型返回值
         */
        public CsvReader build(String filePath) throws FileNotFoundException {
            return new CsvReader(new FileInputStream(filePath), delimiter, skipEmptyLines, trimFields);
        }
    }
}
