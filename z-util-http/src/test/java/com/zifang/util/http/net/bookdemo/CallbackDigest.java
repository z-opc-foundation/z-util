package com.zifang.util.http.net.bookdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * CallbackDigest类。
 */
public class CallbackDigest implements Runnable {

    private String filename;

    /**
     * CallbackDigest方法。
     * * @param filename String类型参数
     */
    public CallbackDigest(String filename) {
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
            byte[] digest = sha.digest();
            CallbackDigestUserInterface.receiveDigest(digest, filename);
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex);
        }
    }
}