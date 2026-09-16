package com.zifang.util.expr.groovy;

/**
 * Exception thrown when Groovy script execution errors occur.
 */
public class GroovyException extends RuntimeException {

    public GroovyException(String message) {
        super(message);
    }

    public GroovyException(String message, Throwable cause) {
        super(message, cause);
    }
}
