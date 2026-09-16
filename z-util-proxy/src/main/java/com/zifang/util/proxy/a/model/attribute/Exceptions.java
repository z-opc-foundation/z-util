package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 异常属性
 * <p>
 * Exceptions_attribute用于列出方法可能抛出的已检查异常。
 */
public class Exceptions extends AbstractAttribute {

    private U2 numberOfExceptions;//方法throw的异常数量;
    private List<U2> exceptionIndexTable = new ArrayList<>(numberOfExceptions.value);//指向常量池中constant_class_table型常量的索引;


    /**
     * Exceptions方法。
     * * @param attributeNameIndex U2类型参数
     *
     * @param attributeLength U4类型参数
     */
    public Exceptions(U2 attributeNameIndex, U4 attributeLength) {
        super(attributeNameIndex, attributeLength);
    }

    @Override
    /**
     * read方法。
     *      * @param inputStream InputStream类型参数
     */
    public void read(InputStream inputStream) {

    }

    /**
     * getNumberOfExceptions方法。
     *
     * @return U2类型返回值
     */
    public U2 getNumberOfExceptions() {
        return numberOfExceptions;
    }

    /**
     * setNumberOfExceptions方法。
     * * @param numberOfExceptions U2类型参数
     */
    public void setNumberOfExceptions(U2 numberOfExceptions) {
        this.numberOfExceptions = numberOfExceptions;
    }

    /**
     * getExceptionIndexTable方法。
     *
     * @return List<U2>类型返回值
     */
    public List<U2> getExceptionIndexTable() {
        return exceptionIndexTable;
    }

    /**
     * setExceptionIndexTable方法。
     * * @param exceptionIndexTable ListU2类型参数
     */
    public void setExceptionIndexTable(List<U2> exceptionIndexTable) {
        this.exceptionIndexTable = exceptionIndexTable;
    }
}
