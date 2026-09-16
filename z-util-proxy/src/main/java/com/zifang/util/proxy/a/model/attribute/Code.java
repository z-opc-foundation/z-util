package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.constantpool.AbstractConstantPool;
import com.zifang.util.proxy.a.model.readtype.U1;
import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Code属性
 * <p>
 * 方法的字节码指令存储在这个属性中。
 */
public class Code extends AbstractAttribute {
    private U2 maxStack;
    private U2 maxLocals;
    private U4 codeLength;
    private List<U1> code = new ArrayList<>();
    private U2 exceptionTableLength;
    private List<ExceptionInfo> exceptionTable = new ArrayList<>();
    private U2 attributesCount;
    private List<AbstractAttribute> attributes = new ArrayList<>();
    private List<AbstractConstantPool> poolList;

    /**
     * Code方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     * @param poolList        ListAbstractConstantPool类型参数
     */
    public Code(U2 attributeNameIndex, U4 attributeLength, List<AbstractConstantPool> poolList) {
        super(attributeNameIndex, attributeLength);
        this.poolList = poolList;
    }

    @Override
    /**
     * read方法。
     *      * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        maxStack = U2.read(inputStream);
        maxLocals = U2.read(inputStream);
        codeLength = U4.read(inputStream);
        int value = codeLength.value;
        while (value > 0) {
            code.add(U1.read(inputStream));
            value--;
        }
        exceptionTableLength = U2.read(inputStream);
        short exLength = exceptionTableLength.value;
        while (exLength > 0) {
            ExceptionInfo exceptionInfo = new ExceptionInfo(
                    U2.read(inputStream),
                    U2.read(inputStream),
                    U2.read(inputStream),
                    U2.read(inputStream)
            );
            exceptionTable.add(exceptionInfo);
            exLength--;
        }
        attributesCount = U2.read(inputStream);
        short attrCount = attributesCount.value;
        while (attrCount > 0) {
            AbstractAttribute attributeTable = AttributeFactory.getAttributeTable(inputStream, poolList);
            if (attributeTable != null) {
                attributes.add(attributeTable);
            }
            attrCount--;
        }
    }

    /**
     * getMaxStack方法。
     *
     * @return U2类型返回值
     */
    public U2 getMaxStack() {
        return maxStack;
    }

    /**
     * getMaxLocals方法。
     *
     * @return U2类型返回值
     */
    public U2 getMaxLocals() {
        return maxLocals;
    }

    /**
     * getCodeLength方法。
     *
     * @return U4类型返回值
     */
    public U4 getCodeLength() {
        return codeLength;
    }

    /**
     * getCode方法。
     *
     * @return List<U1>类型返回值
     */
    public List<U1> getCode() {
        return code;
    }

    /**
     * getExceptionTableLength方法。
     *
     * @return U2类型返回值
     */
    public U2 getExceptionTableLength() {
        return exceptionTableLength;
    }

    /**
     * getExceptionTable方法。
     *
     * @return List<ExceptionInfo>类型返回值
     */
    public List<ExceptionInfo> getExceptionTable() {
        return exceptionTable;
    }

    /**
     * getAttributesCount方法。
     *
     * @return U2类型返回值
     */
    public U2 getAttributesCount() {
        return attributesCount;
    }

    /**
     * getAttributes方法。
     *
     * @return List<AbstractAttribute>类型返回值
     */
    public List<AbstractAttribute> getAttributes() {
        return attributes;
    }

    /**
     * 异常表项
     */
    public class ExceptionInfo {
        private U2 startPc;
        private U2 endPc;
        private U2 handlerPc;
        private U2 catchPc;

        /**
         * ExceptionInfo方法。
         * * @param startPc U2类型参数
         *
         * @param endPc     U2类型参数
         * @param handlerPc U2类型参数
         * @param catchPc   U2类型参数
         */
        public ExceptionInfo(U2 startPc, U2 endPc, U2 handlerPc, U2 catchPc) {
            this.startPc = startPc;
            this.endPc = endPc;
            this.handlerPc = handlerPc;
            this.catchPc = catchPc;
        }

        /**
         * getStartPc方法。
         *
         * @return U2类型返回值
         */
        public U2 getStartPc() {
            return startPc;
        }

        /**
         * getEndPc方法。
         *
         * @return U2类型返回值
         */
        public U2 getEndPc() {
            return endPc;
        }

        /**
         * getHandlerPc方法。
         *
         * @return U2类型返回值
         */
        public U2 getHandlerPc() {
            return handlerPc;
        }

        /**
         * getCatchPc方法。
         *
         * @return U2类型返回值
         */
        public U2 getCatchPc() {
            return catchPc;
        }
    }
}
