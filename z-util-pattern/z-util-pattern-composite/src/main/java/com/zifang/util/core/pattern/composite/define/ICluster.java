package com.zifang.util.core.pattern.composite.define;

import java.util.Collection;

/**
 * ICluster接口。
 */
public interface ICluster {

    /**
     * 获得群描述
     */
    String getName();

    /**
     * 群id
     */
    String getId();

    /**
     * 群下成员
     */
    Collection<INode> members();
}
