package com.zifang.util.core.pattern.composite.tree;

import java.util.List;

/**
 * 哨兵 根节点
 */
public abstract class RootLeaf implements ILeaf {

    @Override
    /**
     * getSubLeaves方法。
     * @return abstract List<ILeaf>类型返回值
     */
    public abstract List<ILeaf> getSubLeaves();

    @Override
    /**
     * getParentLeaf方法。
     * @return ILeaf类型返回值
     */
    public ILeaf getParentLeaf() {
        return null;
    }
}
