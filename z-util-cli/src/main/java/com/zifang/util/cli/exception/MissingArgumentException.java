package com.zifang.util.cli.exception;

import com.zifang.util.cli.model.Option;

/**
 * Thrown when an option requiring an argument is not provided with one.
 */
public class MissingArgumentException extends ParseException {

    private static final long serialVersionUID = 1L;
    private final Option option;

    /**
     * MissingArgumentException方法。
     * * @param option final类型参数
     */
    public MissingArgumentException(final Option option) {
        super("Missing argument for option: " + (option != null ? option.getKey() : "null"));
        this.option = option;
    }

    /**
     * MissingArgumentException方法。
     * * @param message final类型参数
     */
    public MissingArgumentException(final String message) {
        super(message);
        this.option = null;
    }

    /**
     * getOption方法。
     *
     * @return Option类型返回值
     */
    public Option getOption() {
        return option;
    }
}
