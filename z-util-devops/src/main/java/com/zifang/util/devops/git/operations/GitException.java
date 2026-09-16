package com.zifang.util.devops.git.operations;

/**
 * git 操作统一异常
 * <p>
 * 用于封装 JGit、shell 调用和参数校验过程中的错误。
 * 调用方可以通过 {@link #getResult()} 获取更详细的结果信息。
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final GitResult<?> result;

    /**
     * 仅含消息的异常
     */
    public GitException(String message) {
        super(message);
        this.result = null;
    }

    /**
     * 含原因的异常
     */
    public GitException(String message, Throwable cause) {
        super(message, cause);
        this.result = null;
    }

    /**
     * 含 GitResult 的异常
     */
    public GitException(String message, GitResult<?> result) {
        super(message);
        this.result = result;
    }

    /**
     * 获取相关的结果（可能为 null）
     */
    public GitResult<?> getResult() {
        return result;
    }
}
