package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.DatagramChannel;

/**
 * DefaultSocketOptionValues类。
 */
public class DefaultSocketOptionValues {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            for (SocketOption<?> option : channel.supportedOptions()) {
                System.out.println(option.name() + ": " + channel.getOption(option));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}