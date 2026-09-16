package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * FormPoster类。
 */
public class FormPoster {

    private URL url;
    // from Chapter 5, Example 5-8
    private QueryString query = new QueryString();

    /**
     * FormPoster方法。
     * * @param url URL类型参数
     */
    public FormPoster(URL url) {
        if (!url.getProtocol().toLowerCase().startsWith("http")) {
            throw new IllegalArgumentException("Posting only works for http URLs");
        }
        this.url = url;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        URL url;
        if (args.length > 0) {
            try {
                url = new URL(args[0]);
            } catch (MalformedURLException ex) {
                System.err.println("Usage: java FormPoster url");
                return;
            }
        } else {
            try {
                url = new URL("http://www.cafeaulait.org/books/jnp4/postquery.phtml");
            } catch (MalformedURLException ex) { // shouldn't happen
                System.err.println(ex);
                return;
            }
        }

        FormPoster poster = new FormPoster(url);
        poster.add("name", "Elliotte Rusty Harold");
        poster.add("email", "elharo@ibiblio.org");

        try (InputStream in = poster.post()) {
            // Read the response
            Reader r = new InputStreamReader(in);
            int c;
            while ((c = r.read()) != -1) {
                System.out.print((char) c);
            }
            System.out.println();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * add方法。
     * * @param name String类型参数
     *
     * @param value String类型参数
     */
    public void add(String name, String value) {
        query.add(name, value);
    }

    /**
     * getURL方法。
     *
     * @return URL类型返回值
     */
    public URL getURL() {
        return this.url;
    }

    /**
     * post方法。
     *
     * @return InputStream类型返回值
     */
    public InputStream post() throws IOException {

        // open the connection and prepare it to POST
        URLConnection uc = url.openConnection();
        uc.setDoOutput(true);
        try (OutputStreamWriter out = new OutputStreamWriter(uc.getOutputStream(), StandardCharsets.UTF_8)) {

            // The POST line, the Content-type header,
            // and the Content-length headers are sent by the URLConnection.
            // We just need to send the data
            out.write(query.toString());
            out.write("\r\n");
            out.flush();
        }

        // Return the response
        return uc.getInputStream();
    }
}