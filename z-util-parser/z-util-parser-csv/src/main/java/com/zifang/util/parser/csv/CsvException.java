package com.zifang.util.parser.csv;

/**
 * Exception thrown when CSV parsing errors occur.
 */

/**
 * CsvException类。
 */
public class CsvException extends RuntimeException {

    /**
     * CsvException方法。
     * * @param message String类型参数
     */
    public CsvException(String message) {
        super(message);
    }

    /**
     * CsvException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public CsvException(String message, Throwable cause) {
        super(message, cause);
    }
}
