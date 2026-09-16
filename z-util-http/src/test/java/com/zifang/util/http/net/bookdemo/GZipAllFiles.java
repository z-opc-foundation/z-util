package com.zifang.util.http.net.bookdemo;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GZipAllFiles类。
 */
public class GZipAllFiles {

    public final static int THREAD_COUNT = 4;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);

        for (String filename : args) {
            File f = new File(filename);
            if (f.exists()) {
                if (f.isDirectory()) {
                    File[] files = f.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        if (!files[i].isDirectory()) { // don't recurse
                            // directories
                            Runnable task = new GZipRunnable(files[i]);
                            pool.submit(task);
                        }
                    }
                } else {
                    Runnable task = new GZipRunnable(f);
                    pool.submit(task);
                }
            }
        }

        pool.shutdown();
    }
}