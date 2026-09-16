package com.zifang.util.core.pattern.composite.graph;

import com.zifang.util.core.pattern.composite.define.INode;

import java.util.List;

/**
 * 在网状结构体的 基础上 增加方向属性
 */
public interface IVectorNode extends INode {

    List<IVectorNode> getInputVectorNode();

    @Override
    List<INode> getCombinedNode();


    List<IVectorNode> getOutputVectorNode();

}
