package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Options类。
 */
public class Options {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        try {
            args = new String[]{"http://localhost:9999/webDemo/test"};
            URL u = new URL(args[0]);
            HttpURLConnection http = (HttpURLConnection) u.openConnection();
            //GET、POST、PUT、DELETE、HEAD、OPTIONS、TRACE
            http.setRequestMethod("DELETE");
//			http.setDoOutput(true);
//			Writer w = new OutputStreamWriter(http.getOutputStream());
//			w.write("name=jijs");
//			w.flush();
//			w.close();
            Map<String, List<String>> headers = http.getHeaderFields();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getKey() != null) {
                    System.out.println(header.getKey() + ": " + join(header.getValue()));
                } else {
                    System.out.println(join(header.getValue()));
                }
            }
        } catch (MalformedURLException ex) {
            System.err.println(args[0] + " is not a parseable URL");
        } catch (IOException ex) {
            System.err.println(ex);
        }
        System.out.println();
    }

    private static String join(List<String> list) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext())
                builder.append(", ");
        }
        return builder.toString();
    }
}