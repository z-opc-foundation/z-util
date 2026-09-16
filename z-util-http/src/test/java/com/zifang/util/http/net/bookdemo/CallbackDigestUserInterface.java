package com.zifang.util.http.net.bookdemo;

import javax.xml.bind.DatatypeConverter;

/**
 * CallbackDigestUserInterface类。
 */
public class CallbackDigestUserInterface {

    /**
     * receiveDigest方法。
     * * @param digest byte[]类型参数
     *
     * @param name String类型参数
     * @return static void类型返回值
     */
    public static void receiveDigest(byte[] digest, String name) {
        StringBuilder result = new StringBuilder(name);
        result.append(": ");
        result.append(DatatypeConverter.printHexBinary(digest));
        System.out.println(result);
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        String filename = "D:\\test.txt";
        // Calculate the digest
        // CallbackDigest cb = new CallbackDigest(filename);
        // Thread t = new Thread(cb);
        // t.start();
    }
}