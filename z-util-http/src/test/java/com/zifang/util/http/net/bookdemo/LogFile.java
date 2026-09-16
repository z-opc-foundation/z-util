package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.util.Date;

/**
 * LogFile类。
 */
public class LogFile {

    private Writer out;

    /**
     * LogFile方法。
     * * @param f File类型参数
     */
    public LogFile(File f) throws IOException {
        FileWriter fw = new FileWriter(f);
        this.out = new BufferedWriter(fw);
    }

    /**
     * writeEntry方法。
     * * @param message String类型参数
     */
    public void writeEntry(String message) throws IOException {
        Date d = new Date();
        out.write(d.toString());
        out.write('\t');
        out.write(message);
        out.write("\r\n");
    }

    /**
     * close方法。
     */
    public void close() throws IOException {
        out.flush();
        out.close();
    }
}