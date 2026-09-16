package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.*;

/**
 * OptionSupport类。
 */
public class OptionSupport {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException {
        printOptions(SocketChannel.open());
        printOptions(ServerSocketChannel.open());
        printOptions(AsynchronousSocketChannel.open());
        printOptions(AsynchronousServerSocketChannel.open());
        printOptions(DatagramChannel.open());
    }

    private static void printOptions(NetworkChannel channel) throws IOException {
        System.out.println(channel.getClass().getSimpleName() + " supports:");
        for (SocketOption<?> option : channel.supportedOptions()) {
            System.out.println(option.name() + ": " + channel.getOption(option));
        }
        System.out.println();
        channel.close();
    }

}
