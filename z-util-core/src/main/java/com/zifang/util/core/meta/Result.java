package com.zifang.util.core.meta;

import com.zifang.util.core.Const;
import com.zifang.util.core.lang.StringUtil;
import com.zifang.util.core.lang.exception.BaseException;

import java.io.Serializable;


/**
 * 接口返回参数封装类
 *
 * @param <T> 返回数据的类型
 */
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1444605237688228650L;
    private T data;
    private boolean success = Boolean.FALSE;
    private int code = BaseStatusCode.OK.getCode();
    private String message;

    private Result() {
    }

    /**
     * 创建成功的返回结果，不包含数据
     *
     * @param <T> 数据类型
     * @return 成功的Result实例
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.data(null).success(Boolean.TRUE);
        return result;
    }

    /**
     * 创建成功的返回结果，包含数据
     *
     * @param <T>  数据类型
     * @param data 返回的数据
     * @return 成功的Result实例
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.data(data).success(Boolean.TRUE);
        return result;
    }


    /**
     * 创建失败的返回结果，使用指定的StatusCode和动态参数
     *
     * @param <T>        数据类型
     * @param statusCode 状态码枚举
     * @param params     动态参数，用于格式化消息字符串
     * @return 失败的Result实例
     */
    public static <T> Result<T> error(StatusCode statusCode, Object... params) {
        Result<T> result = new Result<>();
        result.code(statusCode.getCode());
        String statusMessage = statusCode.getMessage();
        statusMessage = buildMessage(statusMessage, params);

        result.message(statusMessage);
        return result;
    }

    /**
     * 创建失败的返回结果，使用指定的BaseException
     *
     * @param <T>           数据类型
     * @param baseException 基础异常，包含错误码和消息
     * @param params        动态参数，用于格式化消息字符串
     * @return 失败的Result实例
     */
    public static <T> Result<T> error(BaseException baseException, Object... params) {
        String baseMessage = baseException.getMessage();
        baseMessage = buildMessage(baseMessage, params);

        return error(baseException.getCode(), baseMessage);
    }

    private static <T> Result<T> error(int code, String message) {
        return new Result<T>().code(code).message(message);
    }

    /**
     * 创建失败的返回结果，使用默认的失败状态码
     *
     * @param <T>     数据类型
     * @param message 错误消息
     * @return 失败的Result实例
     */
    public static <T> Result<T> fail(String message) {
        return error(BaseStatusCode.FAIL, message);
    }

    /**
     * 构建格式化消息，支持占位符替换和参数拼接
     * <p>
     * 如果消息中包含{}占位符，则使用String.format进行格式化；
     * 否则，使用|符号拼接所有参数。
     * </p>
     *
     * @param statusMessage 原始消息字符串
     * @param params        动态参数
     * @return 格式化后的消息字符串
     */
    public static String buildMessage(String statusMessage, Object... params) {
        StringBuilder message = new StringBuilder();
        boolean isFormat = Boolean.FALSE;
        if (null != statusMessage) {
            message.append(statusMessage);
            if (statusMessage.contains(Const.Symbol.CURLY_BRACE)) {
                statusMessage = StringUtil.replace(statusMessage,
                        "\\{\\}",
                        "%s", false);
            }
            isFormat = StringUtil.isFormat(statusMessage);

            if (isFormat) {
                try {
                    message = new StringBuilder(String.format(statusMessage, params));
                } catch (Exception e) {
                    // 下游传递的message里面可能包含%s，比如"Format specifier '%s'"，这种场景在这里format也会抛异常
                    // ignore
                }
            }
        }
        // 不是format信息使用|拼接
        if (!isFormat) {
            if (params != null) {
                for (Object param : params) {
                    message.append("|").append(param);
                }
            }
        }

        return message.toString();
    }

    /**
     * 获取返回的数据
     *
     * @return 返回的数据
     */
    public T getData() {
        return data;
    }

    /**
     * 判断请求是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取消息
     *
     * @return 消息内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置返回数据
     *
     * @param data 返回数据
     * @return 返回对象本身，用于链式调用
     */
    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    /**
     * 设置错误信息
     *
     * @param message 错误信息
     * @return 返回对象本身，用于链式调用
     */
    private Result<T> message(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置状态码
     *
     * @param code 状态码
     * @return 返回对象本身，用于链式调用
     */
    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * 设置是否成功
     *
     * @param success 是否成功
     * @return 返回对象本身，用于链式调用
     */
    private Result<T> success(Boolean success) {
        this.success = success;
        return this;
    }

    /**
     * 返回Result的字符串表示
     *
     * @return 字符串表示
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Result{" + "data=" + data + ", success=" + success + ", code=" + code + ", message='" + message + '\'' + '}';
    }
}
