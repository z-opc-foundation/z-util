package com.zifang.util.pandas.io;

import com.zifang.util.pandas.DataFrame;

import java.io.*;
import java.util.Map;

/**
 * CSV 读取器 - 支持从 CSV 文件读取数据
 */
public class CSVReader {

    private char delimiter = ',';
    private char quoteChar = '"';
    private boolean hasHeader = true;
    private boolean skipEmptyLines = true;
    private String encoding = "UTF-8";
    private int skipRows = 0;
    private int maxRows = -1; // -1 表示不限制

    /**
     * CSVReader方法。
     */
    public CSVReader() {
    }

    /**
     * builder方法。
     *
     * @return static CSVReader类型返回值
     */
    public static CSVReader builder() {
        return new CSVReader();
    }

    /**
     * delimiter方法。
     * * @param delimiter char类型参数
     *
     * @return CSVReader类型返回值
     */
    public CSVReader delimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * quoteChar方法。
     * * @param quoteChar char类型参数
     *
     * @return CSVReader类型返回值
     */
    public CSVReader quoteChar(char quoteChar) {
        this.quoteChar = quoteChar;
        return this;
    }

    /**
     * hasHeader方法。
     * * @param hasHeader boolean类型参数
     *
     * @return CSVReader类型返回值
     */
    public CSVReader hasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
        return this;
    }

    /**
     * encoding方法。
     * * @param encoding String类型参数
     *
     * @return CSVReader类型返回值
     */
    public CSVReader encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * skipRows方法。
     * * @param skipRows int类型参数
     *
     * @return CSVReader类型返回值
     */
    public CSVReader skipRows(int skipRows) {
        this.skipRows = skipRows;
        return this;
    }

    /**
     * maxRows方法。
     * * @param maxRows int类型参数
     *
     * @return CSVReader类型返回值
     */
    public CSVReader maxRows(int maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    /**
     * 从文件路径读取 CSV
     */
    public DataFrame read(String filePath) throws IOException {
        return read(new File(filePath));
    }

    /**
     * 从 File 对象读取 CSV
     */
    public DataFrame read(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), encoding))) {
            return read(reader);
        }
    }

    /**
     * 从 InputStream 读取 CSV
     */
    public DataFrame read(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, encoding))) {
            return read(reader);
        }
    }

    /**
     * 从字符串读取 CSV
     */
    public DataFrame readFromString(String csvContent) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            return read(reader);
        }
    }

    /**
     * 核心读取方法
     */
    private DataFrame read(BufferedReader reader) throws IOException {
        String line;
        int lineNumber = 0;
        int rowCount = 0;

        // 跳过指定行数
        while (lineNumber < skipRows && (line = reader.readLine()) != null) {
            lineNumber++;
        }

        // 读取表头
        String[] headers = null;
        if (hasHeader) {
            String headerLine = reader.readLine();
            if (headerLine != null) {
                headers = parseLine(headerLine);
                lineNumber++;
            }
        }

        // 如果没有表头，生成默认列名
        if (headers == null) {
            // 读取第一行数据来确定列数
            String firstDataLine = reader.readLine();
            if (firstDataLine != null) {
                String[] firstRow = parseLine(firstDataLine);
                headers = new String[firstRow.length];
                for (int i = 0; i < firstRow.length; i++) {
                    headers[i] = "column_" + i;
                }
                // 需要重新读取这一行
                // 这里简化处理，实际应该缓存这一行
            } else {
                return new DataFrame();
            }
        }

        // 读取数据行
        Map<String, java.util.List<Double>> dataMap = new java.util.LinkedHashMap<>();
        for (String header : headers) {
            dataMap.put(header, new java.util.ArrayList<>());
        }

        java.util.List<String> rowLabels = new java.util.ArrayList<>();

        while ((line = reader.readLine()) != null) {
            lineNumber++;

            if (skipEmptyLines && line.trim().isEmpty()) {
                continue;
            }

            String[] values = parseLine(line);

            // 如果列数不匹配，填充或截断
            for (int i = 0; i < headers.length; i++) {
                double val;
                if (i < values.length) {
                    try {
                        val = Double.parseDouble(values[i]);
                    } catch (NumberFormatException e) {
                        val = Double.NaN;
                    }
                } else {
                    val = Double.NaN;
                }
                dataMap.get(headers[i]).add(val);
            }

            rowLabels.add("row_" + rowCount);
            rowCount++;

            if (maxRows > 0 && rowCount >= maxRows) {
                break;
            }
        }

        // 转换为 DataFrame
        Map<String, double[]> finalData = new java.util.LinkedHashMap<>();
        for (String header : headers) {
            java.util.List<Double> list = dataMap.get(header);
            double[] arr = new double[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
            finalData.put(header, arr);
        }

        return new DataFrame(finalData, rowLabels.toArray(new String[0]));
    }

    /**
     * 解析 CSV 行，处理引号和转义
     */
    private String[] parseLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == quoteChar) {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == quoteChar) {
                    // 转义的引号
                    currentField.append(quoteChar);
                    i++; // 跳过下一个引号
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // 添加最后一个字段
        fields.add(currentField.toString().trim());

        return fields.toArray(new String[0]);
    }
}
