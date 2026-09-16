package com.zifang.util.core.pattern.composite.tree;

import com.zifang.util.core.lang.tuples.Triplet;

import java.util.*;

/**
 * 叶子节点包装器
 * <p>
 * 将任意对象包装为ILeaf节点
 *
 * @param <A> ID类型
 * @param <B> 父ID类型
 * @param <C> 实际对象类型
 * @author zifang
 */
public class LeafWrapper<A, B, C> extends Triplet<A, B, C> implements ILeaf {

    private String name;
    private ILeaf parent;
    private List<ILeaf> subLeaves;
    private Map<String, Object> metadata;

    /**
     * LeafWrapper方法。
     * * @param currentId A类型参数
     *
     * @param parentId B类型参数
     * @param bean     C类型参数
     */
    public LeafWrapper(A currentId, B parentId, C bean) {
        super(currentId, parentId, bean);
        this.name = bean != null ? bean.toString() : null;
    }

    @Override
    /**
     * getSubLeaves方法。
     * @return List<ILeaf>类型返回值
     */
    public List<ILeaf> getSubLeaves() {
        return subLeaves != null ? subLeaves : Collections.emptyList();
    }

    @Override
    /**
     * getParentLeaf方法。
     * @return ILeaf类型返回值
     */
    public ILeaf getParentLeaf() {
        return parent;
    }

    @Override
    /**
     * appendSubLeaf方法。
     *      * @param leaf ILeaf类型参数
     */
    public void appendSubLeaf(ILeaf leaf) {
        if (subLeaves == null) {
            subLeaves = new ArrayList<>();
        }
        subLeaves.add(leaf);
        leaf.setParent(this);
    }

    @Override
    /**
     * setParent方法。
     *      * @param leaf ILeaf类型参数
     */
    public void setParent(ILeaf leaf) {
        this.parent = leaf;
    }

    @Override
    /**
     * getName方法。
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * setName方法。
     * * @param name String类型参数
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    /**
     * getId方法。
     * @return String类型返回值
     */
    public String getId() {
        A id = getA();
        return id != null ? id.toString() : null;
    }

    /**
     * getParentId方法。
     *
     * @return B类型返回值
     */
    public B getParentId() {
        return getB();
    }

    /**
     * getBean方法。
     *
     * @return C类型返回值
     */
    public C getBean() {
        return getC();
    }

    /**
     * setMeta方法。
     * * @param key String类型参数
     *
     * @param value Object类型参数
     */
    public void setMeta(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * getMeta方法。
     * * @param key String类型参数
     *
     * @return Object类型返回值
     */
    public Object getMeta(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * getMetadata方法。
     *
     * @return Map<String, Object>类型返回值
     */
    public Map<String, Object> getMetadata() {
        return metadata != null ? Collections.unmodifiableMap(metadata) : Collections.emptyMap();
    }

    @Override
    /**
     * removeSubLeaf方法。
     *      * @param leaf ILeaf类型参数
     */
    public void removeSubLeaf(ILeaf leaf) {
        if (subLeaves != null) {
            subLeaves.remove(leaf);
            leaf.setParent(null);
        }
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "LeafWrapper{" +
                "id=" + getA() +
                ", name='" + name + '\'' +
                ", parentId=" + getB() +
                ", bean=" + getC() +
                ", childrenCount=" + (subLeaves != null ? subLeaves.size() : 0) +
                '}';
    }
}
