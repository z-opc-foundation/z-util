package com.zifang.util.http.net.bookdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * InstanceCallbackDigest类。
 */
public class InstanceCallbackDigest implements Runnable {

    private String filename;
    private InstanceCallbackDigestUserInterface callback;

    /**
     * InstanceCallbackDigest方法。
     * * @param filename String类型参数
     *
     * @param callback InstanceCallbackDigestUserInterface类型参数
     */
    public InstanceCallbackDigest(String filename, InstanceCallbackDigestUserInterface callback) {
        this.filename = filename;
        this.callback = callback;
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
            callback.receiveDigest(digest);
        } catch (IOException | NoSuchAlgorithmException ex) {
            System.err.println(ex);
        }
    }
}