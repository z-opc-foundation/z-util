package com.zifang.util.http.net.bookdemo;

import javax.xml.bind.DatatypeConverter;

/**
 * JoinDigestUserInterface类。
 */
public class JoinDigestUserInterface {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        ReturnDigest[] digestThreads = new ReturnDigest[args.length];

        for (int i = 0; i < args.length; i++) {
            // Calculate the digest
            digestThreads[i] = new ReturnDigest(args[i]);
            digestThreads[i].start();
        }

        for (int i = 0; i < args.length; i++) {
            try {
                digestThreads[i].join();
                // Now print the result
                StringBuffer result = new StringBuffer(args[i]);
                result.append(": ");
                byte[] digest = digestThreads[i].getDigest();
                result.append(DatatypeConverter.printHexBinary(digest));
                System.out.println(result);
            } catch (InterruptedException ex) {
                System.err.println("Thread Interrupted before completion");
            }
        }
    }
}