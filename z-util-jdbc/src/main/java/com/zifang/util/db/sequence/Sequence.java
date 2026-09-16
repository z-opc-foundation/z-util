package com.zifang.util.db.sequence;

/**
 * 分布式序号生成器接口
 */
public interface Sequence {

    /**
     * 生成下一个唯一序号
     *
     * @return 序号值
     */
    long next();

    /**
     * 批量生成序号
     *
     * @param count 数量（不超过批次上限）
     * @return 序号数组
     */
    long[] next(int count);
}
