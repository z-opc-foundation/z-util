package com.zifang.util.db.sequence;

/**
 * 序列生成异常
 */
public class SequenceException extends RuntimeException {
    /**
     * SequenceException方法。
     * * @param message String类型参数
     */
    public SequenceException(String message) {
        super(message);
    }

    /**
     * SequenceException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public SequenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
