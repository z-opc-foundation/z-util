package com.zifang.util.core.meta;

/**
 * 通用响应封装类，包含泛型数据。
 * <p>
 * 用于 API 响应数据的统一封装，提供 Builder 模式构建响应对象。
 *
 * @param <T> 数据类型
 * @author zifang
 * @see BaseResponse
 */
public class GenericResponse<T> extends BaseResponse {

    private T data;

    /**
     * GenericResponse方法。
     */
    public GenericResponse() {
    }

    /**
     * GenericResponse方法。
     * * @param message String类型参数
     *
     * @param code ResultCode类型参数
     * @param data T类型参数
     */
    public GenericResponse(String message, ResultCode code, T data) {
        super(message, code);
        this.data = data;
    }

    /**
     * builder方法。
     *
     * @return static <T> GenericResponse<T>类型返回值
     */
    public static <T> GenericResponse<T> builder() {
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

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "GenericResponse{" + super.toString() + ", data=" + data + "}";
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
        GenericResponse<?> that = (GenericResponse<?>) o;
        return java.util.Objects.equals(data, that.data);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), data);
    }

    public static class Builder<T> {
        private String message;
        private ResultCode code = ResultCode.SUCCESS;
        private T data;

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
         * build方法。
         *
         * @return GenericResponse<T>类型返回值
         */
        public GenericResponse<T> build() {
            return new GenericResponse<>(message, code, data);
        }
    }
}