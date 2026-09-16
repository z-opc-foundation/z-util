package com.zifang.util.devops.docker;

/**
 * Docker 命令执行结果封装
 * <p>
 * 泛型封装 Docker 命令执行的成功/失败状态、标准输出、标准错误和附加数据。
 *
 * @param <T> 结果数据类型
 * @author zifang
 * @version 1.0.0
 */
public class DockerCommandResult<T> {

    private boolean success;
    private int exitCode;
    private String stdout;
    private String stderr;
    private T data;
    private String message;

    /**
     * DockerCommandResult方法。
     */
    public DockerCommandResult() {
    }

    /**
     * 创建包含数据的成功结果
     *
     * @param data 结果数据
     * @param <T>  数据类型
     * @return 成功结果对象
     */
    public static <T> DockerCommandResult<T> success(T data) {
        DockerCommandResult<T> result = new DockerCommandResult<>();
        result.success = true;
        result.data = data;
        return result;
    }

    /**
     * 创建包含标准输出的成功结果
     *
     * @param stdout 标准输出内容
     * @param <T>    数据类型
     * @return 成功结果对象
     */
    public static <T> DockerCommandResult<T> success(String stdout) {
        DockerCommandResult<T> result = new DockerCommandResult<>();
        result.success = true;
        result.stdout = stdout;
        return result;
    }

    /**
     * 创建包含退出码和错误信息的失败结果
     *
     * @param exitCode 退出码
     * @param stderr   标准错误内容
     * @param <T>      数据类型
     * @return 失败结果对象
     */
    public static <T> DockerCommandResult<T> fail(int exitCode, String stderr) {
        DockerCommandResult<T> result = new DockerCommandResult<>();
        result.success = false;
        result.exitCode = exitCode;
        result.stderr = stderr;
        return result;
    }

    /**
     * 创建包含错误消息的失败结果
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败结果对象
     */
    public static <T> DockerCommandResult<T> fail(String message) {
        DockerCommandResult<T> result = new DockerCommandResult<>();
        result.success = false;
        result.message = message;
        return result;
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
        return "DockerCommandResult{" +
                "success=" + success +
                ", exitCode=" + exitCode +
                ", stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
