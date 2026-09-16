package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * GZipRunnable类。
 */
public class GZipRunnable implements Runnable {

    private final File input;

    /**
     * GZipRunnable方法。
     * * @param input File类型参数
     */
    public GZipRunnable(File input) {
        this.input = input;
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        // don't compress an already compressed file
        if (!input.getName().endsWith(".gz")) {
            File output = new File(input.getParent(), input.getName() + ".gz");
            if (!output.exists()) { // Don't overwrite an existing file
                try (InputStream in = new BufferedInputStream(new FileInputStream(input));
                     OutputStream out = new BufferedOutputStream(
                             new GZIPOutputStream(new FileOutputStream(output)))) {
                    int b;
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                    out.flush();
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }
        }
    }
}