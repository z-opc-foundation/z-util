package com.zifang.util.proxy.a.decompile.core;

/**
 * 数据处理器接口
 * <p>
 * 用于处理字节码数据转换的回调接口。
 */
public abstract class DataHandler {

    /**
     * handle方法。
     * * @param cutStr String类型参数
     *
     * @return abstract Object类型返回值
     */
    public abstract Object handle(String cutStr);

}

