package com.zifang.util.parser.csv;

import java.io.*;
import java.util.List;

/**
 * CSV Writer for producing RFC 4180 compliant CSV output.
 * Features:
 * - Custom delimiter (default comma)
 * - Automatic quoting of fields containing delimiters or quotes
 * - Escaping of quotes by doubling
 * - Support for writing String[] rows
 */

/**
 * CsvWriter类。
 */
public class CsvWriter implements Closeable, Flushable {

    private final Writer writer;
    private final char delimiter;
    private final boolean trimFields;

    /**
     * Create a CsvWriter to an OutputStream with default settings
     */
    /**
     * CsvWriter方法。
     * * @param outputStream OutputStream类型参数
     */
    public CsvWriter(OutputStream outputStream) {
        this(outputStream, ',', false);
    }

    /**
     * Create a CsvWriter to an OutputStream with custom delimiter
     */
    /**
     * CsvWriter方法。
     * * @param outputStream OutputStream类型参数
     *
     * @param delimiter char类型参数
     */
    public CsvWriter(OutputStream outputStream, char delimiter) {
        this(outputStream, delimiter, false);
    }

    /**
     * Create a CsvWriter to an OutputStream with custom settings
     */
    /**
     * CsvWriter方法。
     * * @param outputStream OutputStream类型参数
     *
     * @param delimiter  char类型参数
     * @param trimFields boolean类型参数
     */
    public CsvWriter(OutputStream outputStream, char delimiter, boolean trimFields) {
        this.writer = new OutputStreamWriter(outputStream);
        this.delimiter = delimiter;
        this.trimFields = trimFields;
    }

    /**
     * Create a CsvWriter to a Writer with default settings
     */
    /**
     * CsvWriter方法。
     * * @param writer Writer类型参数
     */
    public CsvWriter(Writer writer) {
        this(writer, ',', false);
    }

    /**
     * Create a CsvWriter to a Writer with custom delimiter
     */
    /**
     * CsvWriter方法。
     * * @param writer Writer类型参数
     *
     * @param delimiter char类型参数
     */
    public CsvWriter(Writer writer, char delimiter) {
        this(writer, delimiter, false);
    }

    /**
     * Create a CsvWriter to a Writer with custom settings
     */
    /**
     * CsvWriter方法。
     * * @param writer Writer类型参数
     *
     * @param delimiter  char类型参数
     * @param trimFields boolean类型参数
     */
    public CsvWriter(Writer writer, char delimiter, boolean trimFields) {
        this.writer = writer;
        this.delimiter = delimiter;
        this.trimFields = trimFields;
    }

    /**
     * Create a CsvWriter to a file
     */
    /**
     * CsvWriter方法。
     * * @param filePath String类型参数
     */
    public CsvWriter(String filePath) throws FileNotFoundException {
        this(new FileOutputStream(filePath), ',', false);
    }

    /**
     * Create a CsvWriter to a file with custom delimiter
     */
    /**
     * CsvWriter方法。
     * * @param filePath String类型参数
     *
     * @param delimiter char类型参数
     */
    public CsvWriter(String filePath, char delimiter) throws FileNotFoundException {
        this(new FileOutputStream(filePath), delimiter, false);
    }

    /**
     * Write a single row
     */

    /**
     * builder方法。
     *
     * @return static Builder类型返回值
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Write a single row from a list
     */

    /**
     * writeRow方法。
     * * @param fields String...类型参数
     */
    public void writeRow(String... fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                writer.write(delimiter);
            }
            String field = trimFields ? (fields[i] != null ? fields[i].trim() : "") : (fields[i] != null ? fields[i] : "");
            writer.write(escapeField(field));
        }
        writer.write("\n");
    }

    /**
     * Write multiple rows
     */

    /**
     * writeRow方法。
     * * @param fields ListString类型参数
     */
    public void writeRow(List<String> fields) throws IOException {
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                writer.write(delimiter);
            }
            String field = trimFields ? (fields.get(i) != null ? fields.get(i).trim() : "") : (fields.get(i) != null ? fields.get(i) : "");
            writer.write(escapeField(field));
        }
        writer.write("\n");
    }

    /**
     * Write multiple rows from varargs
     */

    /**
     * writeRows方法。
     * * @param rows ListString[]类型参数
     */
    public void writeRows(List<String[]> rows) throws IOException {
        for (String[] row : rows) {
            writeRow(row);
        }
    }

    /**
     * writeRows方法。
     * * @param rows String[]...类型参数
     */
    public void writeRows(String[]... rows) throws IOException {
        for (String[] row : rows) {
            writeRow(row);
        }
    }

    /**
     * Escape a field according to RFC 4180:
     * - Fields containing quotes must be wrapped in quotes
     * - Quotes within fields must be doubled
     * - Fields containing delimiter, quotes, or newlines must be quoted
     */
    private String escapeField(String field) {
        if (field == null) {
            return "";
        }

        boolean needsQuoting = false;
        int quoteCount = 0;

        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c == '"') {
                quoteCount++;
                needsQuoting = true;
            } else if (c == delimiter || c == '\n' || c == '\r') {
                needsQuoting = true;
            }
        }

        if (!needsQuoting) {
            return field;
        }

        // Build escaped string with quotes and doubled quotes
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c == '"') {
                sb.append('"'); // double the quote
            }
            sb.append(c);
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Check if a field needs quoting (for number fields that should not be quoted)
     */
    private boolean isNumeric(String field) {
        if (field == null || field.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(field);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    /**
     * flush方法。
     */
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    /**
     * close方法。
     */
    public void close() throws IOException {
        writer.close();
    }

    // Builder pattern for configuration
    public static class Builder {
        private char delimiter = ',';
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
         * * @param outputStream OutputStream类型参数
         *
         * @return CsvWriter类型返回值
         */
        public CsvWriter build(OutputStream outputStream) {
            return new CsvWriter(outputStream, delimiter, trimFields);
        }

        /**
         * build方法。
         * * @param writer Writer类型参数
         *
         * @return CsvWriter类型返回值
         */
        public CsvWriter build(Writer writer) {
            return new CsvWriter(writer, delimiter, trimFields);
        }

        /**
         * build方法。
         * * @param filePath String类型参数
         *
         * @return CsvWriter类型返回值
         */
        public CsvWriter build(String filePath) throws FileNotFoundException {
            return new CsvWriter(new FileOutputStream(filePath), delimiter, trimFields);
        }
    }
}
