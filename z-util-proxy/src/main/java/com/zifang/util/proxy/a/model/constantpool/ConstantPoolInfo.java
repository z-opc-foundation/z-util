package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.util.ArrayList;
import java.util.List;

/**
 * 常量池信息
 * <p>
 * 存储ClassFile中完整的常量池数据，包括常量池大小和常量项列表。
 */
public class ConstantPoolInfo {
    private U2 poolSize;
    private List<AbstractConstantPool> poolList;

    /**
     * ConstantPoolInfo方法。
     * * @param poolSize short类型参数
     */
    public ConstantPoolInfo(short poolSize) {
        this.poolSize = new U2(poolSize);
        poolList = new ArrayList<AbstractConstantPool>(poolSize);
    }

    /**
     * getPoolSize方法。
     *
     * @return U2类型返回值
     */
    public U2 getPoolSize() {
        return poolSize;
    }

    /**
     * setPoolSize方法。
     * * @param poolSize U2类型参数
     */
    public void setPoolSize(U2 poolSize) {
        this.poolSize = poolSize;
    }

    /**
     * getPoolList方法。
     *
     * @return List<AbstractConstantPool>类型返回值
     */
    public List<AbstractConstantPool> getPoolList() {
        return poolList;
    }

    /**
     * setPoolList方法。
     * * @param poolList ListAbstractConstantPool类型参数
     */
    public void setPoolList(List<AbstractConstantPool> poolList) {
        this.poolList = poolList;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ConstantPoolInfo{" +
                "poolSize=" + poolSize.value +
                ", poolList=" + poolList +
                '}';
    }
}
