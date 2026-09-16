package com.zifang.util.parser.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Parser supporting RFC 4180 format.
 * Features:
 * - Custom delimiter (default comma)
 * - Quoted fields
 * - Escaped quotes ("")
 * - First line as column names
 * - Skip empty lines
 * - Trim fields
 */

/**
 * CsvParser类。
 */
public class CsvParser {

    private char delimiter;
    private boolean firstLineAsHeader;
    private boolean skipEmptyLines;
    private boolean trimFields;

    private CsvParser(Builder builder) {
        this.delimiter = builder.delimiter;
        this.firstLineAsHeader = builder.firstLineAsHeader;
        this.skipEmptyLines = builder.skipEmptyLines;
        this.trimFields = builder.trimFields;
    }

    /**
     * builder方法。
     *
     * @return static Builder类型返回值
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return List<String[]>类型返回值
     */
    public List<String[]> parse(Reader reader) {
        List<String[]> result = new ArrayList<>();
        BufferedReader br = new BufferedReader(reader);
        try {
            String line;
            boolean firstLine = true;
            while ((line = readCsvLine(br)) != null) {
                // Strip BOM (U+FEFF) from first line
                if (firstLine && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }
                firstLine = false;
                if (skipEmptyLines && line.trim().isEmpty()) {
                    continue;
                }
                String[] fields = parseLine(line);
                result.add(fields);
            }
        } catch (IOException e) {
            throw new CsvException("Error reading CSV", e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return result;
    }

    /**
     * Parse CSV content from a Reader
     */

    /**
     * Read a CSV line — handles CRLF and quoted fields with embedded newlines.
     * Quote pairs ("") inside quoted fields are NOT consumed here;
     * they are passed through to parseLine which handles them per RFC 4180.
     */
    private String readCsvLine(BufferedReader br) throws IOException {
        StringBuilder line = new StringBuilder();
        int quoteCount = 0;
        int ch;
        while ((ch = br.read()) != -1) {
            char c = (char) ch;
            if (c == '"') {
                quoteCount++;
                line.append(c);
            } else if ((c == '\n' || c == '\r') && quoteCount % 2 == 0) {
                // Even quote count → not inside a quoted field → end of record
                if (c == '\r') {
                    br.mark(1);
                    int peek = br.read();
                    if (peek != '\n') {
                        br.reset();
                    }
                }
                return line.toString();
            } else {
                line.append(c);
            }
        }
        return line.length() > 0 ? line.toString() : null;
    }

    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return List<String[]>类型返回值
     */
    public List<String[]> parse(String content) {
        return parse(new StringReader(content));
    }

    /**
     * Parse CSV content from a String
     */

    /**
     * parseWithHeader方法。
     * * @param reader Reader类型参数
     *
     * @return ParseResult类型返回值
     */
    public ParseResult parseWithHeader(Reader reader) {
        List<String[]> allLines = parse(reader);
        if (allLines.isEmpty()) {
            return new ParseResult(new String[0], new ArrayList<String[]>());
        }

        String[] headers = allLines.get(0);
        List<String[]> data = allLines.subList(1, allLines.size());

        List<String[]> result = new ArrayList<>();
        for (String[] row : data) {
            result.add(row);
        }

        return new ParseResult(headers, result);
    }

    /**
     * Parse CSV content and return as list of String arrays, with first line as headers
     */

    /**
     * parseWithHeader方法。
     * * @param content String类型参数
     *
     * @return ParseResult类型返回值
     */
    public ParseResult parseWithHeader(String content) {
        return parseWithHeader(new StringReader(content));
    }

    /**
     * Parse CSV content and return as list of String arrays, with first line as headers
     */

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

    /**
     * Builder for CsvParser configuration
     */
    public static class Builder {
        private char delimiter = ',';
        private boolean firstLineAsHeader = false;
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
         * firstLineAsHeader方法。
         * * @param firstLineAsHeader boolean类型参数
         *
         * @return Builder类型返回值
         */
        public Builder firstLineAsHeader(boolean firstLineAsHeader) {
            this.firstLineAsHeader = firstLineAsHeader;
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
         *
         * @return CsvParser类型返回值
         */
        public CsvParser build() {
            return new CsvParser(this);
        }
    }

    /**
     * Result of parsing CSV with headers
     */
    public static class ParseResult {
        private final String[] headers;
        private final List<String[]> data;

        /**
         * ParseResult方法。
         * * @param headers String[]类型参数
         *
         * @param data ListString[]类型参数
         */
        public ParseResult(String[] headers, List<String[]> data) {
            this.headers = headers;
            this.data = data;
        }

        /**
         * getHeaders方法。
         *
         * @return String[]类型返回值
         */
        public String[] getHeaders() {
            return headers;
        }

        /**
         * getData方法。
         *
         * @return List<String[]>类型返回值
         */
        public List<String[]> getData() {
            return data;
        }

        /**
         * Get row as Map (header name -> value)
         */
        /**
         * getRowAsMap方法。
         * * @param index int类型参数
         *
         * @return String[]类型返回值
         */
        public String[] getRowAsMap(int index) {
            if (index < 0 || index >= data.size()) {
                throw new CsvException("Row index out of bounds: " + index);
            }
            return data.get(index);
        }
    }
}
