package com.zifang.util.devops.git.operations;

/**
 * git 操作统一结果封装
 * <p>
 * 与 DockerCommandResult 风格保持一致：泛型封装执行的成功/失败状态、
 * 标准输出、标准错误、退出码和附加数据。所有 GitClient 方法均返回此类型。
 *
 * @param <T> 结果数据类型
 * @author zifang
 * @version 1.0.0
 */
public class GitResult<T> {

    private boolean success;
    private int exitCode;
    private String stdout;
    private String stderr;
    private T data;
    private String message;

    /**
     * GitResult方法。
     */
    public GitResult() {
    }

    /**
     * 创建成功结果
     *
     * @param data 结果数据
     * @param <T>  数据类型
     * @return 成功结果对象
     */
    public static <T> GitResult<T> success(T data) {
        GitResult<T> r = new GitResult<>();
        r.success = true;
        r.data = data;
        return r;
    }

    /**
     * 创建仅含标准输出的成功结果（无 data）
     *
     * @param stdout 标准输出
     * @param <T>    数据类型
     * @return 成功结果对象
     */
    public static <T> GitResult<T> successStdout(String stdout) {
        GitResult<T> r = new GitResult<>();
        r.success = true;
        r.stdout = stdout;
        return r;
    }

    /**
     * 创建成功结果（含退出码）
     *
     * @param data     结果数据
     * @param exitCode 退出码
     * @param <T>      数据类型
     * @return 成功结果对象
     */
    public static <T> GitResult<T> success(T data, int exitCode) {
        GitResult<T> r = new GitResult<>();
        r.success = true;
        r.data = data;
        r.exitCode = exitCode;
        return r;
    }

    /**
     * 创建失败结果
     *
     * @param exitCode 退出码
     * @param stderr   标准错误内容
     * @param <T>      数据类型
     * @return 失败结果对象
     */
    public static <T> GitResult<T> fail(int exitCode, String stderr) {
        GitResult<T> r = new GitResult<>();
        r.success = false;
        r.exitCode = exitCode;
        r.stderr = stderr;
        return r;
    }

    /**
     * 创建失败结果
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败结果对象
     */
    public static <T> GitResult<T> fail(String message) {
        GitResult<T> r = new GitResult<>();
        r.success = false;
        r.message = message;
        return r;
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * setSuccess方法。
     * * @param success boolean类型参数
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * getExitCode方法。
     *
     * @return int类型返回值
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * setExitCode方法。
     * * @param exitCode int类型参数
     */
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * getStdout方法。
     *
     * @return String类型返回值
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * setStdout方法。
     * * @param stdout String类型参数
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    /**
     * getStderr方法。
     *
     * @return String类型返回值
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * setStderr方法。
     * * @param stderr String类型参数
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * getData方法。
     *
     * @return T类型返回值
     */
    public T getData() {
        return data;
    }

    /**
     * setData方法。
     * * @param data T类型参数
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * getMessage方法。
     *
     * @return String类型返回值
     */
    public String getMessage() {
        return message;
    }

    /**
     * setMessage方法。
     * * @param message String类型参数
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "GitResult{" +
                "success=" + success +
                ", exitCode=" + exitCode +
                ", message='" + message + '\'' +
                ", stdout='" + (stdout == null ? "" : stdout.replace("\n", "\\n")) + '\'' +
                ", stderr='" + (stderr == null ? "" : stderr.replace("\n", "\\n")) + '\'' +
                '}';
    }
}
