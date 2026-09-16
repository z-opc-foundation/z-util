package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SocketChannel;

/**
 * OptionSetExample类。
 */
public class OptionSetExample {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException {
        NetworkChannel channel = SocketChannel.open();
        channel.setOption(StandardSocketOptions.SO_LINGER, 240);
    }

}
