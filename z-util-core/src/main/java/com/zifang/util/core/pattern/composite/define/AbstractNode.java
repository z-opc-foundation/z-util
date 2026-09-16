package com.zifang.util.core.pattern.composite.define;

import java.util.List;

/**
 * 节点抽象基类。
 * <p>
 * 实现 INode 接口，提供节点的基本属性和默认实现。
 *
 * @author zifang
 * @see INode
 */
public class AbstractNode implements INode {

    protected String id;

    protected String name;

    @Override
    /**
     * getName方法。
     * @return String类型返回值
     */
    public String getName() {
        return null;
    }

    @Override
    /**
     * getId方法。
     * @return String类型返回值
     */
    public String getId() {
        return null;
    }

    @Override
    /**
     * getCombinedNode方法。
     * @return List<INode>类型返回值
     */
    public List<INode> getCombinedNode() {
        return null;
    }
}
