package com.zifang.util.core.pattern.chain;

/**
 * 处理器执行结果
 *
 * @author zifang
 */
public enum ProcessorResult {

    /**
     * 处理已完成，不再继续传递给后续处理器
     */
    FINISHED(true),

    /**
     * 处理继续，传递给下一个处理器
     */
    CONTINUE(true),

    /**
     * 处理跳过，当前处理器不处理，传递给下一个
     */
    SKIP(true),

    /**
     * 处理失败
     */
    FAILURE(false),

    /**
     * 处理错误
     */
    ERROR(false);

    private final boolean success;

    ProcessorResult(boolean success) {
        this.success = success;
    }

    /**
     * isSuccess方法。
     *
     * @return boolean类型返回值
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 是否继续传递给下一个处理器
     */
    public boolean shouldContinue() {
        return this == CONTINUE || this == SKIP;
    }

    /**
     * 转换为成功状态
     */
    public boolean isFinished() {
        return this == FINISHED;
    }

    /**
     * 组合结果：只有两者都成功才成功
     */
    public ProcessorResult and(ProcessorResult other) {
        if (!this.success || !other.success) {
            return FAILURE;
        }
        return this == FINISHED || other == FINISHED ? FINISHED : CONTINUE;
    }
}
