package com.zifang.util.yaml.define;

/**
 * YAML 节点类型枚举，对应 YAML 1.1 规范中的主要节点类型。
 *
 * @author zifang
 */
/**
 * YamlNodeType枚举。
 */

/**
 * YamlNodeType枚举。
 */
public enum YamlNodeType {
    /**
     * 空节点（null 或空）
     */
    NULL,
    /**
     * 标量节点（字符串、数字、布尔等）
     */
    SCALAR,
    /**
     * 映射节点（键值对集合）
     */
    MAP,
    /**
     * 序列节点（列表）
     */
    SEQUENCE,
    /**
     * 锚点引用节点
     */
    ALIAS
}
