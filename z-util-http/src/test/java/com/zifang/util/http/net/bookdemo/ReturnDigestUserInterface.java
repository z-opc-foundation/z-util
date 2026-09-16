package com.zifang.util.http.net.bookdemo;

import javax.xml.bind.DatatypeConverter;

/**
 * ReturnDigestUserInterface类。
 */
public class ReturnDigestUserInterface {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        for (String filename : args) {
            // Calculate the digest
            ReturnDigest dr = new ReturnDigest(filename);
            dr.start();

            // Now print the result
            StringBuilder result = new StringBuilder(filename);
            result.append(": ");
            byte[] digest = dr.getDigest();
            result.append(DatatypeConverter.printHexBinary(digest));
            System.out.println(result);
        }
    }
}