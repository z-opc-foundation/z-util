package com.zifang.util.parser.properties;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Properties 文件解析器，支持标准 JDK Properties 格式。
 * <p>
 * 支持的特性：
 * <ul>
 *   <li>key=value 和 key=value with comment (#/!)</li>
 *   <li>key with no value</li>
 *   <li>空白行</li>
 *   <li>转义字符：\\t \\n \\r \\" \\\\</li>
 *   <li>Unicode 转义：\\u005CuXXXX 格式</li>
 *   <li>多行续行：行尾反斜杠</li>
 * </ul>
 *
 * @author zifang
 */

/**
 * PropertiesParser类。
 */
public class PropertiesParser extends java.util.Properties {

    private final Map<String, String> comments = new LinkedHashMap<>();
    private final StringBuilder pendingComment = new StringBuilder();

    /**
     * 从字符串解析 Properties。
     *
     * @param content Properties 格式的字符串
     * @return 解析后的 PropertiesModel
     * @throws PropertiesException 如果解析失败
     */
    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return PropertiesModel类型返回值
     */
    public PropertiesModel parse(String content) {
        return parse(new CharSequenceReader(content));
    }

    /**
     * 从 File 解析 Properties（使用默认编码UTF-8）。
     *
     * @param file File 对象
     * @return 解析后的 PropertiesModel
     * @throws PropertiesException 如果解析失败或文件不存在
     */
    /**
     * parse方法。
     * * @param file java.io.File类型参数
     *
     * @return PropertiesModel类型返回值
     */
    public PropertiesModel parse(java.io.File file) {
        try {
            return parse(new java.io.FileReader(file));
        } catch (java.io.FileNotFoundException e) {
            throw new PropertiesException("File not found: " + file.getPath(), e);
        } catch (java.io.IOException e) {
            throw new PropertiesException("Failed to read file: " + file.getPath(), e);
        }
    }

    /**
     * 从 File 解析 Properties（指定编码）。
     *
     * @param file    File 对象
     * @param charset 字符编码
     * @return 解析后的 PropertiesModel
     * @throws PropertiesException 如果解析失败或文件不存在
     */
    /**
     * parse方法。
     * * @param file java.io.File类型参数
     *
     * @param charset String类型参数
     * @return PropertiesModel类型返回值
     */
    public PropertiesModel parse(java.io.File file, String charset) {
        try {
            return parse(new java.io.InputStreamReader(new java.io.FileInputStream(file), charset));
        } catch (java.io.FileNotFoundException e) {
            throw new PropertiesException("File not found: " + file.getPath(), e);
        } catch (java.io.IOException e) {
            throw new PropertiesException("Failed to read file: " + file.getPath(), e);
        }
    }

    /**
     * 从 InputStream 解析 Properties（使用默认编码）。
     *
     * @param in InputStream 对象
     * @return 解析后的 PropertiesModel
     * @throws PropertiesException 如果解析失败
     */
    /**
     * parse方法。
     * * @param in java.io.InputStream类型参数
     *
     * @return PropertiesModel类型返回值
     */
    public PropertiesModel parse(java.io.InputStream in) {
        return parse(new java.io.InputStreamReader(in));
    }

    /**
     * 从 InputStream 解析 Properties（指定编码）。
     *
     * @param in      InputStream 对象
     * @param charset 字符编码
     * @return 解析后的 PropertiesModel
     * @throws PropertiesException 如果解析失败
     */
    /**
     * parse方法。
     * * @param in java.io.InputStream类型参数
     *
     * @param charset String类型参数
     * @return PropertiesModel类型返回值
     */
    public PropertiesModel parse(java.io.InputStream in, String charset) {
        try {
            return parse(new java.io.InputStreamReader(in, charset));
        } catch (java.io.UnsupportedEncodingException e) {
            throw new PropertiesException("Unsupported charset: " + charset, e);
        }
    }

    /**
     * 从 Reader 解析 Properties。
     *
     * @param reader Reader 对象
     * @return 解析后的 PropertiesModel
     * @throws PropertiesException 如果解析失败
     */
    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return PropertiesModel类型返回值
     */
    public PropertiesModel parse(Reader reader) {
        PropertiesModel model = new PropertiesModel();
        LineNumberReader lnr = new LineNumberReader(reader);
        StringBuilder logicalLine = new StringBuilder();

        try {
            String line;
            while ((line = lnr.readLine()) != null) {
                String trimmed = filterComment(line);

                if (trimmed.isEmpty()) {
                    continue;
                }

                if (endsWithContinuation(line)) {
                    // 去掉行尾的反斜杠（readLine 已去掉 \n），再追加
                    logicalLine.append(stripContinuation(line));
                } else {
                    logicalLine.append(trimmed);
                    String fullLine = logicalLine.toString();
                    parseLine(fullLine, model);
                    logicalLine.setLength(0);
                }
            }

            if (logicalLine.length() > 0) {
                parseLine(logicalLine.toString(), model);
            }
        } catch (IOException e) {
            throw new PropertiesException("Failed to parse properties", e);
        }

        return model;
    }

    /**
     * 过滤行首注释和空白。
     *
     * @param line 原始行
     * @return 去除注释后的内容
     */
    private String filterComment(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("#") || trimmed.startsWith("!")) {
            String comment = trimmed.substring(1).trim();
            if (pendingComment.length() > 0) {
                pendingComment.append(" ").append(comment);
            } else {
                pendingComment.append(comment);
            }
            return "";
        }
        return trimmed;
    }

    /**
     * 检查行尾是否有续行符。
     *
     * @param line 行内容
     * @return 是否有续行
     */
    private boolean endsWithContinuation(String line) {
        if (line.isEmpty()) {
            return false;
        }
        int backslashes = 0;
        for (int i = line.length() - 1; i >= 0 && line.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return backslashes % 2 == 1;
    }

    private String stripContinuation(String line) {
        // 只去掉行尾的一个反斜杠（续行标记），保留内容中的其他反斜杠
        if (line.length() > 0 && line.charAt(line.length() - 1) == '\\') {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

    /**
     * 解析一行内容。
     *
     * @param line  完整行（已合并续行）
     * @param model 数据模型
     */
    private void parseLine(String line, PropertiesModel model) {
        int separatorIndex = findKeyValueSeparator(line);

        String key;
        String value = "";

        if (separatorIndex < 0) {
            key = line.trim();
        } else {
            key = line.substring(0, separatorIndex).trim();
            if (separatorIndex < line.length() - 1) {
                value = line.substring(separatorIndex + 1);
            }
        }

        if (key.isEmpty()) {
            return;
        }

        String comment = pendingComment.toString();
        if (!comment.isEmpty()) {
            comments.put(key, comment);
        }
        pendingComment.setLength(0);

        String decodedKey = decodeEscapeSequence(key);
        String decodedValue = decodeEscapeSequence(value);

        model.setProperty(decodedKey, decodedValue);
        model.setComment(decodedKey, comment);
    }

    /**
     * 查找键值分隔符的位置。
     * 遵循 java.util.Properties 的逻辑，从左到右扫描，跳过键中的转义字符。
     *
     * @param line 行内容
     * @return 分隔符位置，找不到返回 -1
     */
    private int findKeyValueSeparator(String line) {
        int len = line.length();
        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);
            if (c == '=' || c == ':') {
                return i;
            }
            if (c == '\\') {
                // Skip the escaped character
                i++; // skip the char after backslash
                if (i < len && line.charAt(i) == 'u') {
                    // Unicode escape: skip backslash-u-XXXX (5 chars total)
                    i += 4; // skip the 4 hex digits
                }
                continue;
            }
            // Any other character is part of key, continue searching
        }
        return -1;
    }

    /**
     * 解码转义序列。
     *
     * @param str 原始字符串
     * @return 解码后的字符串
     */
    private String decodeEscapeSequence(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        int len = str.length();
        int i = 0;

        while (i < len) {
            char c = str.charAt(i);

            if (c == '\\') {
                if (i + 1 >= len) {
                    sb.append(c);
                    i++;
                    continue;
                }

                char next = str.charAt(i + 1);

                switch (next) {
                    case 't':
                        sb.append('\t');
                        i += 2;
                        break;
                    case 'n':
                        sb.append('\n');
                        i += 2;
                        break;
                    case 'r':
                        sb.append('\r');
                        i += 2;
                        break;
                    case '"':
                        sb.append('"');
                        i += 2;
                        break;
                    case '\\':
                        sb.append('\\');
                        i += 2;
                        break;
                    case 'u':
                        if (i + 5 < len) {
                            String hex = str.substring(i + 2, i + 6);
                            try {
                                int codePoint = Integer.parseInt(hex, 16);
                                sb.appendCodePoint(codePoint);
                                i += 6;
                            } catch (NumberFormatException e) {
                                sb.append(next);
                                i += 2;
                            }
                        } else {
                            sb.append(next);
                            i += 2;
                        }
                        break;
                    default:
                        // Unrecognized escape: keep both backslash and the character
                        sb.append(c);
                        sb.append(next);
                        i += 2;
                        break;
                }
            } else {
                sb.append(c);
                i++;
            }
        }

        return sb.toString();
    }

    /**
     * 将 PropertiesModel 转换为字符串，保留注释。
     *
     * @param model 数据模型
     * @return Properties 格式字符串
     */
    /**
     * store方法。
     * * @param model PropertiesModel类型参数
     *
     * @return String类型返回值
     */
    public String store(PropertiesModel model) {
        StringBuilder sb = new StringBuilder();

        for (String key : model.getOrderedKeys()) {
            String comment = model.getComment(key);
            if (comment != null && !comment.isEmpty()) {
                String[] lines = comment.split("\n");
                for (String cline : lines) {
                    sb.append("#").append(cline).append("\n");
                }
            }

            String value = model.getProperty(key);
            if (value == null) {
                value = "";
            }

            sb.append(encodeKeyValue(key, value)).append("\n");
        }

        return sb.toString();
    }

    /**
     * 将 PropertiesModel 写入到 Writer，保留注释。
     *
     * @param model  数据模型
     * @param writer Writer 对象
     * @throws PropertiesException 如果写入失败
     */
    /**
     * storeToWriter方法。
     * * @param model PropertiesModel类型参数
     *
     * @param writer java.io.Writer类型参数
     */
    public void storeToWriter(PropertiesModel model, java.io.Writer writer) {
        try {
            writer.write(store(model));
            writer.flush();
        } catch (java.io.IOException e) {
            throw new PropertiesException("Failed to write properties", e);
        }
    }

    /**
     * 编码键值对，转义特殊字符。
     *
     * @param key   键
     * @param value 值
     * @return 编码后的字符串
     */
    private String encodeKeyValue(String key, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(encodeString(key));
        sb.append("=");
        sb.append(encodeString(value));
        return sb.toString();
    }

    /**
     * 编码字符串，转义特殊字符。
     *
     * @param str 原始字符串
     * @return 编码后的字符串
     */
    private String encodeString(String str) {
        StringBuilder sb = new StringBuilder();
        int len = str.length();

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);

            switch (c) {
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    if (c < 0x20 || c > 0x7e) {
                        sb.append("\\u");
                        String hex = Integer.toHexString(c);
                        while (hex.length() < 4) {
                            hex = "0" + hex;
                        }
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * 用于将 CharSequence 转换为 Reader 的内部类。
     */
    private static class CharSequenceReader extends Reader {
        private final CharSequence charSequence;
        private int position = 0;

        CharSequenceReader(CharSequence charSequence) {
            this.charSequence = charSequence;
        }

        @Override
        /**
         * read方法。
         *      * @param cbuf char[]类型参数
         * @param off int类型参数
         * @param len int类型参数
         * @return int类型返回值
         */
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (position >= charSequence.length()) {
                return -1;
            }
            int count = 0;
            int end = Math.min(charSequence.length(), position + len);
            for (int i = position; i < end; i++) {
                cbuf[off + count] = charSequence.charAt(i);
                count++;
            }
            position += count;
            return count;
        }

        @Override
        /**
         * close方法。
         */
        public void close() throws IOException {
        }
    }
}
