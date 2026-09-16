package com.zifang.util.cli.exception;

/**
 * Thrown when an unrecognized option is encountered during parsing.
 */
public class UnrecognizedOptionException extends ParseException {

    private static final long serialVersionUID = 1L;
    private final String option;

    /**
     * UnrecognizedOptionException方法。
     * * @param message final类型参数
     */
    public UnrecognizedOptionException(final String message) {
        this(message, null);
    }

    /**
     * UnrecognizedOptionException方法。
     * * @param message final类型参数
     *
     * @param option final类型参数
     */
    public UnrecognizedOptionException(final String message, final String option) {
        super(message);
        this.option = option;
    }

    /**
     * getOption方法。
     *
     * @return String类型返回值
     */
    public String getOption() {
        return option;
    }
}
