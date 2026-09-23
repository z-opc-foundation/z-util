package com.zifang.util.db.query;

/**
 * 条件树节点标记：叶子 {@link Criterion} 或分组 {@link PredicateGroup}。
 * <p>
 * 实现者只负责描述结构，SQL 文本统一由 {@link QueryCompiler} 生成。
 *
 * @author zifang
 */
public interface Predicate {
}
