package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 局部变量表属性
 * <p>
 * LocalVariableTable_attribute用于描述方法局部变量的信息。
 * 包括变量名、类型、作用域范围等。
 */
public class LocalVariableTable extends AbstractAttribute {
    private U2 localVariableTableLength;//局部变量槽的数量
    private List<LocalVariableInfo> localVariableTable = new ArrayList<>();//局部变量表的详细信息


    /**
     * LocalVariableTable方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     */
    public LocalVariableTable(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    /**
     * read方法。
     *      * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        localVariableTableLength = U2.read(inputStream);
        short value = localVariableTableLength.value;
        while (value > 0) {
            LocalVariableInfo localVariableInfo = new LocalVariableInfo(U2.read(inputStream), U2.read(inputStream), U2.read(inputStream)
                    , U2.read(inputStream), U2.read(inputStream));
            localVariableTable.add(localVariableInfo);
            value--;
        }
    }

    /**
     * getLocalVariableTableLength方法。
     *
     * @return U2类型返回值
     */
    public U2 getLocalVariableTableLength() {
        return localVariableTableLength;
    }

    /**
     * getLocalVariableTable方法。
     *
     * @return List<LocalVariableInfo>类型返回值
     */
    public List<LocalVariableInfo> getLocalVariableTable() {
        return localVariableTable;
    }

    /**
     * LocalVariableInfo类。
     */
    public class LocalVariableInfo {
        private U2 startPc;//开始行号
        private U2 length;//偏移量   startPc+length 就是这个局部变量的作用范围
        private U2 nameIndex;  //名字
        private U2 descriptorIndex;//描述(变量类型)
        private U2 index;//位于第几个变量槽


        /**
         * LocalVariableInfo方法。
         * * @param startPc U2类型参数
         *
         * @param length          U2类型参数
         * @param nameIndex       U2类型参数
         * @param descriptorIndex U2类型参数
         * @param index           U2类型参数
         */
        public LocalVariableInfo(U2 startPc, U2 length, U2 nameIndex, U2 descriptorIndex, U2 index) {
            this.startPc = startPc;
            this.length = length;
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
            this.index = index;
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
         * setStartPc方法。
         * * @param startPc U2类型参数
         */
        public void setStartPc(U2 startPc) {
            this.startPc = startPc;
        }

        /**
         * getLength方法。
         *
         * @return U2类型返回值
         */
        public U2 getLength() {
            return length;
        }

        /**
         * setLength方法。
         * * @param length U2类型参数
         */
        public void setLength(U2 length) {
            this.length = length;
        }

        /**
         * getNameIndex方法。
         *
         * @return U2类型返回值
         */
        public U2 getNameIndex() {
            return nameIndex;
        }

        /**
         * setNameIndex方法。
         * * @param nameIndex U2类型参数
         */
        public void setNameIndex(U2 nameIndex) {
            this.nameIndex = nameIndex;
        }

        /**
         * getDescriptorIndex方法。
         *
         * @return U2类型返回值
         */
        public U2 getDescriptorIndex() {
            return descriptorIndex;
        }

        /**
         * setDescriptorIndex方法。
         * * @param descriptorIndex U2类型参数
         */
        public void setDescriptorIndex(U2 descriptorIndex) {
            this.descriptorIndex = descriptorIndex;
        }

        /**
         * getIndex方法。
         *
         * @return U2类型返回值
         */
        public U2 getIndex() {
            return index;
        }

        /**
         * setIndex方法。
         * * @param index U2类型参数
         */
        public void setIndex(U2 index) {
            this.index = index;
        }
    }
}
