package com.zifang.util.core.pattern.composite.tree;

import java.util.List;
import java.util.function.Predicate;

/**
 * @param <T> t
 * @param <R> r
 * @author zifang
 */
public interface ILeafFetch<T, R> {

    /**
     * 深度优先 获取单个符合条件的项
     */
    default R bfs(Predicate<T> predicate) {
        List<R> rs = bfsBatch(predicate);
        if (rs != null && rs.size() > 1) {
            return rs.get(0);
        }
        return null;
    }

    /**
     * 广度优先 获取单个符合条件的项
     */
    default R dfs(Predicate<T> predicate) {
        List<R> rs = dfsBatch(predicate);
        if (rs != null && rs.size() > 1) {
            return rs.get(0);
        }
        return null;
    }

    /**
     * 深度优先，符合单个符合条件的项目
     */
    List<R> bfsBatch(Predicate<T> predicate);

    /**
     * 广度优先 获取所有符合条件的项
     */
    List<R> dfsBatch(Predicate<T> predicate);
}
