package com.zifang.util.cli.exception;

/**
 * Base exception for command-line parsing errors.
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * ParseException方法。
     * * @param message final类型参数
     */
    public ParseException(final String message) {
        super(message);
    }

    /**
     * ParseException方法。
     * * @param message final类型参数
     *
     * @param cause final类型参数
     */
    public ParseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
