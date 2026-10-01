package com.zifang.util.core.pattern.memento;

/**
 * 备忘录实现类
 *
 * @param <T> 状态类型
 * @author zifang
 */
public class MementoImpl<T> implements Memento<T> {

    private final T state;
    private final long timestamp;
    private final String label;
    private final String description;

    private MementoImpl(Builder<T> builder) {
        this.state = builder.state;
        this.timestamp = builder.timestamp;
        this.label = builder.label;
        this.description = builder.description;
    }

    /**
     * builder方法。
     *
     * @return static <T> Builder<T>类型返回值
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * of方法。
     * * @param state T类型参数
     *
     * @return static <T> MementoImpl<T>类型返回值
     */
    public static <T> MementoImpl<T> of(T state) {
        return new Builder<T>().state(state).build();
    }

    /**
     * of方法。
     * * @param state T类型参数
     *
     * @param label String类型参数
     * @return static <T> MementoImpl<T>类型返回值
     */
    public static <T> MementoImpl<T> of(T state, String label) {
        return new Builder<T>().state(state).label(label).build();
    }

    @Override
    /**
     * getState方法。
     * @return T类型返回值
     */
    public T getState() {
        return state;
    }

    @Override
    /**
     * getTimestamp方法。
     * @return long类型返回值
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    /**
     * getLabel方法。
     * @return String类型返回值
     */
    public String getLabel() {
        return label;
    }

    @Override
    /**
     * getDescription方法。
     * @return String类型返回值
     */
    public String getDescription() {
        return description;
    }

    public static class Builder<T> {
        private T state;
        private long timestamp = System.currentTimeMillis();
        private String label;
        private String description;

        private Builder() {
        }

        /**
         * state方法。
         * * @param state T类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> state(T state) {
            this.state = state;
            return this;
        }

        /**
         * timestamp方法。
         * * @param timestamp long类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * label方法。
         * * @param label String类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> label(String label) {
            this.label = label;
            return this;
        }

        /**
         * description方法。
         * * @param description String类型参数
         *
         * @return Builder<T>类型返回值
         */
        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        /**
         * build方法。
         *
         * @return MementoImpl<T>类型返回值
         */
        public MementoImpl<T> build() {
            return new MementoImpl<>(this);
        }
    }
}