package com.zifang.util.proxy.a.model.constantpool;

import com.zifang.util.proxy.a.model.readtype.U1;
import com.zifang.util.proxy.a.model.readtype.U2;

import java.io.InputStream;

/**
 * 方法句柄信息
 * <p>
 * CONSTANT_MethodHandle_info用于表示方法句柄，
 * 是JVM用于实现invokedynamic指令的关键数据结构。
 */
public class ConstantMethodHandleInfo extends AbstractConstantPool {
    private U1 referenceKind;
    private U2 referenceIndex;


    /**
     * ConstantMethodHandleInfo方法。
     * * @param tag byte类型参数
     */
    public ConstantMethodHandleInfo(byte tag) {
        super(tag);
    }

    /**
     * read方法。
     * * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        this.referenceKind = U1.read(inputStream);
        this.referenceIndex = U2.read(inputStream);
    }

    /**
     * getReferenceKind方法。
     *
     * @return U1类型返回值
     */
    public U1 getReferenceKind() {
        return referenceKind;
    }

    /**
     * getReferenceIndex方法。
     *
     * @return U2类型返回值
     */
    public U2 getReferenceIndex() {
        return referenceIndex;
    }
}
