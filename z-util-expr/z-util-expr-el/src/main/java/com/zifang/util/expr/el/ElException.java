package com.zifang.util.expr.el;

/**
 * Exception thrown when EL expression evaluation fails.
 */
public class ElException extends RuntimeException {

    public ElException(String message) {
        super(message);
    }

    public ElException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElException(Throwable cause) {
        super(cause);
    }
}
