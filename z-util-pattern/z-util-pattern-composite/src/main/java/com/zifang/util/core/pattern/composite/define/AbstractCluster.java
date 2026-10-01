package com.zifang.util.core.pattern.composite.define;

import java.util.Collection;

/**
 * 集群抽象基类。
 * <p>
 * 实现 ICluster 接口，提供集群的基本功能。
 *
 * @author zifang
 * @see ICluster
 */
public abstract class AbstractCluster implements ICluster {

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
     * members方法。
     * @return Collection<INode>类型返回值
     */
    public Collection<INode> members() {
        return null;
    }
}
