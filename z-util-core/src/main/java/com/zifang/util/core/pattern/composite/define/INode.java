package com.zifang.util.core.pattern.composite.define;

import java.util.List;

/**
 * 网状结点 没有上下级别关系
 *
 * @author zifang
 */
public interface INode {

    /**
     * 获得群描述
     */
    String getName();

    /**
     * 群id
     */
    String getId();

    /**
     * @return 列表
     */
    List<INode> getCombinedNode();
}
