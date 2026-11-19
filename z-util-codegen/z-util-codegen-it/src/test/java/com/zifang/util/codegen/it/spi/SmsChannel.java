package com.zifang.util.codegen.it.spi;

import com.zifang.util.codegen.annotation.ZAutoService;

/**
 * 同一个 service 的第二个 provider：验证多 provider 合并进同一份注册文件。
 */
@ZAutoService(AlertChannel.class)
public class SmsChannel implements AlertChannel {

    @Override
    public String send(String message) {
        return "sms:" + message;
    }
}
