package com.zifang.util.core.meta.page;

import com.zifang.util.core.meta.BaseResponse;
import com.zifang.util.core.meta.ResultCode;

/**
 * 分页响应封装类。
 * <p>
 * 用于 API 分页数据的统一响应封装，包含数据、总页数、当前页等信息。
 *
 * @param <T> 数据类型
 * @author zifang
 * @see BaseResponse
 */
public class PaginationResponse<T> extends BaseResponse {

    private T data;//分页结果
    private int limit;//每页条数
    private int current;//当前页
    private int total;//总数

    /**
     * PaginationResponse方法。
     */
    public PaginationResponse() {
    }

    /**
     * PaginationResponse方法。
     * * @param message String类型参数
     *
     * @param code    ResultCode类型参数
     * @param data    T类型参数
     * @param limit   int类型参数
     * @param current int类型参数
     * @param total   int类型参数
     */
    public PaginationResponse(String message, ResultCode code, T data, int limit, int current, int total) {
        super(message, code);
        this.data = data;
        this.limit = limit;
        this.current = current;
        this.total = total;
    }

    /**
     * builder方法。
     *
     * @return static <T> PaginationResponse<T>类型返回值
     */
    public static <T> PaginationResponse<T> builder() {
        return new Builder<T>().build();
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
     * getLimit方法。
     *
     * @return int类型返回值
     */
    public int getLimit() {
        return limit;
    }

    /**
     * setLimit方法。
     * * @param limit int类型参数
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * getCurrent方法。
     *
     * @return int类型返回值
     */
    public int getCurrent() {
        return current;
    }

    /**
     * setCurrent方法。
     * * @param current int类型参数
     */
    public void setCurrent(int current) {
        this.current = current;
    }

    /**
     * getTotal方法。
     *
     * @return int类型返回值
     */
    public int getTotal() {
        return total;
    }

    /**
     * setTotal方法。
     * * @param total int类型参数
     */
    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "PaginationResponse{" + super.toString() + ", data=" + data +
                ", limit=" + limit + ", current=" + current + ", total=" + total + "}";
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
        if (!super.equals(o)) return false;
        PaginationResponse<?> that = (PaginationResponse<?>) o;
        return limit == that.limit && current == that.current && total == that.total &&
                java.util.Objects.equals(data, that.data);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), data, limit, current, total);
    }

    public static class Builder<T> {
        private String message;
        private ResultCode code = ResultCode.SUCCESS;
        private T data;
        private int limit;
        private int current;
        private int total;

        /**
         * message方法。
         * * @param message String类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        /**
         * code方法。
         * * @param code ResultCode类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> code(ResultCode code) {
            this.code = code;
            return this;
        }

        /**
         * data方法。
         * * @param data T类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        /**
         * limit方法。
         * * @param limit int类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * current方法。
         * * @param current int类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> current(int current) {
            this.current = current;
            return this;
        }

        /**
         * total方法。
         * * @param total int类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> total(int total) {
            this.total = total;
            return this;
        }

        /**
         * build方法。
         *
         * @return PaginationResponse<T>类型返回值
         */
        public PaginationResponse<T> build() {
            return new PaginationResponse<>(message, code, data, limit, current, total);
        }
    }
}