package com.zifang.util.proxy.a.model.inter;

import com.zifang.util.proxy.a.model.readtype.U2;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口索引表
 * <p>
 * 存储ClassFile中所有实现的接口，包含接口计数和接口列表。
 */
public class InterfaceIndex {
    public U2 length;
    /**
     * ArrayList<>方法。
     *
     * @return List<Interface> list = new类型返回值
     */
    public List<Interface> list = new ArrayList<>();

    /**
     * InterfaceIndex方法。
     */
    public InterfaceIndex() {
    }

    /**
     * addIndex方法。
     * * @param index U2类型参数
     */
    public void addIndex(U2 index) {
        this.list.add(new Interface(index));
    }
}
