package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * SecureSourceViewer类。
 */
public class SecureSourceViewer {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // args = new
        // String[]{"http://192.168.10.125:8080/mss3.1/meta/mssDataLogAction!mssDataLog.action"};
        args = new String[]{"https://dev.infcn.com.cn:6249/svn/2019/青岛政务搜索"};

        Authenticator.setDefault(new DialogAuthenticator());

        for (int i = 0; i < args.length; i++) {
            try {
                // Open the URL for reading
                URL u = new URL(args[i]);
                try (InputStream in = new BufferedInputStream(u.openStream())) {
                    // chain the InputStream to a Reader
                    Reader r = new InputStreamReader(in);
                    int c;
                    while ((c = r.read()) != -1) {
                        System.out.print((char) c);
                    }
                }
            } catch (MalformedURLException ex) {
                System.err.println(args[0] + " is not a parseable URL");
            } catch (IOException ex) {
                System.err.println(ex);
            }

            // print a blank line to separate pages
            System.out.println();
        }

        // Since we used the AWT, we have to explicitly exit.
        System.exit(0);
    }
}