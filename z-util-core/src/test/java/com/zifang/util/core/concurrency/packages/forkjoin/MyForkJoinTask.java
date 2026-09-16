package com.zifang.util.core.concurrency.packages.forkjoin;

import java.util.concurrent.ForkJoinTask;

/**
 * MyForkJoinTask类。
 */
public class MyForkJoinTask<V> extends ForkJoinTask<V> {


    private static final long serialVersionUID = -6161392123639719799L;

    private V value;

    private boolean success = false;

    @Override
    /**
     * getRawResult方法。
     * @return V类型返回值
     */
    public V getRawResult() {
        return value;
    }

    @Override
    /**
     * setRawResult方法。
     *      * @param value V类型参数
     */
    protected void setRawResult(V value) {
        this.value = value;
    }

    @Override
    /**
     * exec方法。
     * @return boolean类型返回值
     */
    protected boolean exec() {
        System.out.println("exec");
        return this.success;
    }

    /**
     * isSuccess方法。
     *
     * @return boolean类型返回值
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * setSuccess方法。
     * * @param isSuccess boolean类型参数
     */
    public void setSuccess(boolean isSuccess) {
        this.success = isSuccess;
    }
}
