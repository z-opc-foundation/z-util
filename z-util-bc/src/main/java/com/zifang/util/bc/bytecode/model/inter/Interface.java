package com.zifang.util.bc.bytecode.model.inter;

import com.zifang.util.bc.bytecode.model.readtype.U2;

/**
 * 接口信息
 * <p>
 * 表示ClassFile中实现的单个接口的引用。
 */
public class Interface {
    public U2 index;

    /**
     * Interface方法。
     * * @param index U2类型参数
     */
    public Interface(U2 index) {
        this.index = index;
    }
}
