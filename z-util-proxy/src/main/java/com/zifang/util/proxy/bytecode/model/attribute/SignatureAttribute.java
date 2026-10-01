package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.constantpool.AbstractConstantPool;
import com.zifang.util.proxy.bytecode.model.constantpool.Utf8Info;
import com.zifang.util.proxy.bytecode.model.readtype.U2;
import com.zifang.util.proxy.bytecode.model.readtype.U4;

import java.io.InputStream;
import java.util.List;

/**
 * Signature 属性（4.7.9）：携带字段/方法/类的真实泛型签名。
 * <p>
 * 解码后保留常量池 index 和文本（解析阶段直接用 poolList 把文本读出来）。
 */
public class SignatureAttribute extends AbstractAttribute {

    private U2 signatureIndex;
    private String signatureText;

    public SignatureAttribute(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    public void read(InputStream inputStream) {
        signatureIndex = U2.read(inputStream);
    }

    /**
     * 解析属性文本：需要常量池支持（read 后单独调用）。
     */
    public void resolve(List<AbstractConstantPool> poolList) {
        if (signatureIndex == null) {
            return;
        }
        int idx = signatureIndex.value - 1;
        if (idx < 0 || idx >= poolList.size()) {
            return;
        }
        AbstractConstantPool p = poolList.get(idx);
        if (p instanceof Utf8Info) {
            signatureText = ((Utf8Info) p).getValue();
        }
    }

    public U2 getSignatureIndex() {
        return signatureIndex;
    }

    public String getSignatureText() {
        return signatureText;
    }
}