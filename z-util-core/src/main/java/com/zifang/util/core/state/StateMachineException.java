package com.zifang.util.core.state;

/**
 * 状态机异常。
 * <p>
 * 用于包装状态机在 fire 过程中遇到的非法转移、守卫失败、动作异常等情况。
 */
public class StateMachineException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * StateMachineException方法。
     * * @param message String类型参数
     */
    public StateMachineException(String message) {
        super(message);
    }

    /**
     * StateMachineException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public StateMachineException(String message, Throwable cause) {
        super(message, cause);
    }
}
