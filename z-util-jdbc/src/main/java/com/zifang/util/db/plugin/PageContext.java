package com.zifang.util.db.plugin;

/**
 * 分页上下文
 * <p>
 * 基于ThreadLocal保存当前线程的分页参数，供分页插件在SQL执行阶段读取。
 * 典型用法：
 * <pre>
 * PageContext.PageParam param = PageContext.start(2, 10); // 第2页，每页10条
 * try {
 *     List&lt;Item&gt; items = mapper.selectItems(); // 被分页插件拦截改写
 *     long total = param.getTotal();              // 插件回填的总记录数
 * } finally {
 *     PageContext.clear();
 * }
 * </pre>
 * <p>
 * 注意：start之后务必在finally中调用{@link #clear()}，避免线程复用时残留分页状态。
 *
 * @author zifang
 * @see MyBatisPageInterceptor
 */
public class PageContext {

    /**
     * 当前线程的分页参数
     */
    private static final ThreadLocal<PageParam> HOLDER = new ThreadLocal<>();

    /**
     * PageContext工具类私有构造。
     */
    private PageContext() {
    }

    /**
     * 开启一次分页查询
     *
     * @param page 页码，从1开始
     * @param size 每页记录数，须为正数
     * @return 本次分页参数对象，查询结束后可通过其{@link PageParam#getTotal()}获取总记录数
     * @throws IllegalArgumentException 页码小于1或每页记录数小于1时抛出
     */
    public static PageParam start(long page, long size) {
        if (page < 1) {
            throw new IllegalArgumentException("page must be >= 1, but got " + page);
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be >= 1, but got " + size);
        }
        PageParam param = new PageParam(page, size);
        HOLDER.set(param);
        return param;
    }

    /**
     * 获取当前线程的分页参数
     *
     * @return 当前分页参数；当前线程未开启分页时返回null
     */
    public static PageParam current() {
        return HOLDER.get();
    }

    /**
     * 清除当前线程的分页参数
     */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 分页参数
     * <p>
     * 持有页码与每页记录数；总记录数由分页插件在执行count查询后回填。
     */
    public static class PageParam {

        /**
         * 页码，从1开始
         */
        private final long page;

        /**
         * 每页记录数
         */
        private final long size;

        /**
         * 总记录数，由插件回填
         */
        private Long total;

        /**
         * PageParam构造。
         *
         * @param page 页码，从1开始
         * @param size 每页记录数
         */
        public PageParam(long page, long size) {
            this.page = page;
            this.size = size;
        }

        /**
         * getPage方法。
         *
         * @return long类型返回值
         */
        public long getPage() {
            return page;
        }

        /**
         * getSize方法。
         *
         * @return long类型返回值
         */
        public long getSize() {
            return size;
        }

        /**
         * getTotal方法。
         *
         * @return Long类型返回值
         */
        public Long getTotal() {
            return total;
        }

        /**
         * setTotal方法。
         *
         * @param total Long类型参数
         */
        public void setTotal(Long total) {
            this.total = total;
        }

        /**
         * 计算当前页在结果集中的起始偏移量（从0开始）
         *
         * @return 起始偏移量
         */
        public long getOffset() {
            return (page - 1) * size;
        }
    }
}
