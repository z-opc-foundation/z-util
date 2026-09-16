package com.zifang.util.http.net.bookdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ReturnDigest类。
 */
public class ReturnDigest extends Thread {

    private String filename;
    private byte[] digest;

    /**
     * ReturnDigest方法。
     * * @param filename String类型参数
     */
    public ReturnDigest(String filename) {
        this.filename = filename;
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        try {
            FileInputStream in = new FileInputStream(filename);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            DigestInputStream din = new DigestInputStream(in, sha);
            while (din.read() != -1)
                ; // read entire file
            din.close();
            digest = sha.digest();
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex);
        }
    }

    /**
     * getDigest方法。
     *
     * @return byte[]类型返回值
     */
    public byte[] getDigest() {
        return digest;
    }
}