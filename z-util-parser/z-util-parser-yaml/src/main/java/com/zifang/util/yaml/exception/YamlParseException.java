package com.zifang.util.yaml.exception;

/**
 * YAML 解析异常，当 YAML 字符串格式非法时抛出。
 *
 * @author zifang
 */
/**
 * YamlParseException类。
 */

/**
 * YamlParseException类。
 */
public class YamlParseException extends RuntimeException {

    private final int line;
    private final int column;

    /**
     * YamlParseException方法。
     *      * @param message String类型参数
     */
    /**
     * YamlParseException方法。
     * * @param message String类型参数
     */
    public YamlParseException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }

    /**
     * YamlParseException方法。
     *      * @param message String类型参数
     * @param cause Throwable类型参数
     */
    /**
     * YamlParseException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public YamlParseException(String message, Throwable cause) {
        super(message, cause);
        this.line = -1;
        this.column = -1;
    }

    /**
     * YamlParseException方法。
     *      * @param message String类型参数
     * @param line int类型参数
     * @param column int类型参数
     */
    /**
     * YamlParseException方法。
     * * @param message String类型参数
     *
     * @param line   int类型参数
     * @param column int类型参数
     */
    public YamlParseException(String message, int line, int column) {
        super(message + " (line " + line + ", column " + column + ")");
        this.line = line;
        this.column = column;
    }

    /**
     * YamlParseException方法。
     *      * @param message String类型参数
     * @param line int类型参数
     * @param column int类型参数
     * @param cause Throwable类型参数
     */
    /**
     * YamlParseException方法。
     * * @param message String类型参数
     *
     * @param line   int类型参数
     * @param column int类型参数
     * @param cause  Throwable类型参数
     */
    public YamlParseException(String message, int line, int column, Throwable cause) {
        super(message + " (line " + line + ", column " + column + ")", cause);
        this.line = line;
        this.column = column;
    }

    /**
     * getLine方法。
     * @return int类型返回值
     */
    /**
     * getLine方法。
     *
     * @return int类型返回值
     */
    public int getLine() {
        return line;
    }

    /**
     * getColumn方法。
     * @return int类型返回值
     */
    /**
     * getColumn方法。
     *
     * @return int类型返回值
     */
    public int getColumn() {
        return column;
    }
}
