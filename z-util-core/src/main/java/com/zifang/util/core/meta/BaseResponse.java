package com.zifang.util.core.meta;

/**
 * 基础响应封装类
 * <p>
 * 提供通用的响应消息和状态码封装，用于业务层统一返回格式。
 * </p>
 */
public class BaseResponse {

    private String message;

    private ResultCode code = ResultCode.SUCCESS;

    /**
     * BaseResponse方法。
     */
    public BaseResponse() {
    }

    /**
     * 构造一个包含消息和状态码的响应对象
     *
     * @param message 响应消息
     * @param code    响应状态码
     */
    public BaseResponse(String message, ResultCode code) {
        this.message = message;
        this.code = code;
    }

    /**
     * 获取响应消息
     *
     * @return 消息内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息
     *
     * @param message 消息内容
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取响应状态码
     *
     * @return 状态码枚举
     */
    public ResultCode getCode() {
        return code;
    }

    /**
     * 设置响应状态码
     *
     * @param code 状态码枚举
     */
    public void setCode(ResultCode code) {
        this.code = code;
    }

    /**
     * 判断请求是否成功
     *
     * @return 是否成功，当状态码为SUCCESS时返回true
     */
    public boolean isSuccess() {
        return code == ResultCode.SUCCESS;
    }

    /**
     * 返回BaseResponse的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "BaseResponse{message=" + message + ", code=" + code + "}";
    }

    /**
     * 判断两个BaseResponse对象是否相等
     *
     * @param o 待比较的对象
     * @return 是否相等
     */
    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseResponse that = (BaseResponse) o;
        return java.util.Objects.equals(message, that.message) &&
                code == that.code;
    }

    /**
     * 返回对象的哈希码
     *
     * @return 哈希码
     */
    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(message, code);
    }
}
