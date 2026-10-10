package com.zifang.util.http.base.pojo;

/**
 * HTTP请求体
 * <p>
 * 用于存储HTTP请求的消息体内容，以字节数组形式保存。
 * </p>
 *
 * @author zifang
 */
public class HttpRequestBody {

    /**
     * 请求体的字节数据。
     */
    private byte[] body;

    /**
     * 获取请求体字节数组。
     *
     * @return 请求体字节数组
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * 设置请求体字节数组。
     *
     * @param body 请求体字节数组
     */
    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "HttpRequestBody{body=" + (body != null ? "byte[" + body.length + "]" : "null") + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequestBody that = (HttpRequestBody) o;
        if (body == null && that.body == null) return true;
        if (body == null || that.body == null) return false;
        if (body.length != that.body.length) return false;
        for (int i = 0; i < body.length; i++) {
            if (body[i] != that.body[i]) return false;
        }
        return true;
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        int result = 1;
        if (body != null) {
            for (byte b : body) {
                result = 31 * result + b;
            }
        }
        return result;
    }
}
