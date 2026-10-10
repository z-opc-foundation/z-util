package com.zifang.util.http.base.pojo;

/**
 * 所有注解获得到的信息都会在这个地方存储起来
 */
public class HttpRequestDefinition {

    /**
     * 请求行
     */
    private HttpRequestLine httpRequestLine;

    /**
     * 请求头
     */
    private HttpRequestHeader httpRequestHeader;

    /**
     * 请求体
     */
    private HttpRequestBody httpRequestBody;

    /**
     * getHttpRequestLine方法。
     *
     * @return HttpRequestLine类型返回值
     */
    public HttpRequestLine getHttpRequestLine() {
        return httpRequestLine;
    }

    /**
     * setHttpRequestLine方法。
     * * @param httpRequestLine HttpRequestLine类型参数
     */
    public void setHttpRequestLine(HttpRequestLine httpRequestLine) {
        this.httpRequestLine = httpRequestLine;
    }

    /**
     * getHttpRequestHeader方法。
     *
     * @return HttpRequestHeader类型返回值
     */
    public HttpRequestHeader getHttpRequestHeader() {
        return httpRequestHeader;
    }

    /**
     * setHttpRequestHeader方法。
     * * @param httpRequestHeader HttpRequestHeader类型参数
     */
    public void setHttpRequestHeader(HttpRequestHeader httpRequestHeader) {
        this.httpRequestHeader = httpRequestHeader;
    }

    /**
     * getHttpRequestBody方法。
     *
     * @return HttpRequestBody类型返回值
     */
    public HttpRequestBody getHttpRequestBody() {
        return httpRequestBody;
    }

    /**
     * setHttpRequestBody方法。
     * * @param httpRequestBody HttpRequestBody类型参数
     */
    public void setHttpRequestBody(HttpRequestBody httpRequestBody) {
        this.httpRequestBody = httpRequestBody;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "HttpRequestDefinition{httpRequestLine=" + httpRequestLine + ", httpRequestHeader=" + httpRequestHeader + ", httpRequestBody=" + httpRequestBody + "}";
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
        HttpRequestDefinition that = (HttpRequestDefinition) o;
        if (httpRequestLine != null ? !httpRequestLine.equals(that.httpRequestLine) : that.httpRequestLine != null)
            return false;
        if (httpRequestHeader != null ? !httpRequestHeader.equals(that.httpRequestHeader) : that.httpRequestHeader != null)
            return false;
        return httpRequestBody != null ? httpRequestBody.equals(that.httpRequestBody) : that.httpRequestBody == null;
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        int result = httpRequestLine != null ? httpRequestLine.hashCode() : 0;
        result = 31 * result + (httpRequestHeader != null ? httpRequestHeader.hashCode() : 0);
        result = 31 * result + (httpRequestBody != null ? httpRequestBody.hashCode() : 0);
        return result;
    }
}
