package com.zifang.util.core.pattern.command;

/**
 * 命令异常接收器接口，用于处理命令执行过程中发生的异常。
 *
 * @author zifang
 */
public interface IReceiver {

    /**
     * 处理命令执行过程中发生的异常。
     *
     * @param iCommand 发生异常的ICommand对象
     */
    void except(ICommand iCommand);

}