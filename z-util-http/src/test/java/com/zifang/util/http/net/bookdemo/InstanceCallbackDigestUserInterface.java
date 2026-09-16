package com.zifang.util.http.net.bookdemo;

import javax.xml.bind.DatatypeConverter;

/**
 * InstanceCallbackDigestUserInterface类。
 */
public class InstanceCallbackDigestUserInterface {

    private String filename;
    private byte[] digest;

    /**
     * InstanceCallbackDigestUserInterface方法。
     * * @param filename String类型参数
     */
    public InstanceCallbackDigestUserInterface(String filename) {
        this.filename = filename;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        for (String filename : args) {
            // Calculate the digest
            InstanceCallbackDigestUserInterface d = new InstanceCallbackDigestUserInterface(filename);
            d.calculateDigest();
        }
    }

    /**
     * calculateDigest方法。
     */
    public void calculateDigest() {
        InstanceCallbackDigest cb = new InstanceCallbackDigest(filename, this);
        Thread t = new Thread(cb);
        t.start();
    }

    void receiveDigest(byte[] digest) {
        this.digest = digest;
        System.out.println(this);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        String result = filename + ": ";
        if (digest != null) {
            result += DatatypeConverter.printHexBinary(digest);
        } else {
            result += "digest not available";
        }
        return result;
    }
}