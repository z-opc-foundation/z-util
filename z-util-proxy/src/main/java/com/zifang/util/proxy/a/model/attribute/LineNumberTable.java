package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 行号表属性
 * <p>
 * LineNumberTable_attribute用于关联字节码偏移和源码行号。
 * 用于调试时提供堆栈跟踪的源码位置信息。
 */
public class LineNumberTable extends AbstractAttribute {
    private U2 lineNumTableLength;
    private List<LineNumberInfo> lineNumberTable = new ArrayList<>();

    /**
     * LineNumberTable方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     */
    public LineNumberTable(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    /**
     * read方法。
     *      * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {
        lineNumTableLength = U2.read(inputStream);
        short value = lineNumTableLength.value;
        while (value > 0) {
            LineNumberInfo lineNumberInfo = new LineNumberInfo(U2.read(inputStream), U2.read(inputStream));
            lineNumberTable.add(lineNumberInfo);
            value--;
        }
    }

    /**
     * getLineNumTableLength方法。
     *
     * @return U2类型返回值
     */
    public U2 getLineNumTableLength() {
        return lineNumTableLength;
    }

    /**
     * getLineNumberTable方法。
     *
     * @return List<LineNumberInfo>类型返回值
     */
    public List<LineNumberInfo> getLineNumberTable() {
        return lineNumberTable;
    }

    /**
     * LineNumberInfo类。
     */
    public class LineNumberInfo {
        private U2 startPc;//字节码行号
        private U2 lineNumber;//java源码行号

        /**
         * LineNumberInfo方法。
         * * @param startPc U2类型参数
         *
         * @param lineNumber U2类型参数
         */
        public LineNumberInfo(U2 startPc, U2 lineNumber) {
            this.startPc = startPc;
            this.lineNumber = lineNumber;
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
         * getLineNumber方法。
         *
         * @return U2类型返回值
         */
        public U2 getLineNumber() {
            return lineNumber;
        }

        /**
         * setLineNumber方法。
         * * @param lineNumber U2类型参数
         */
        public void setLineNumber(U2 lineNumber) {
            this.lineNumber = lineNumber;
        }
    }
}
