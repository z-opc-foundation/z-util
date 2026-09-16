package com.zifang.util.http.net.bookdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * SafeBufferedReader类。
 */
public class SafeBufferedReader extends BufferedReader {

    private boolean lookingForLineFeed = false;

    /*
     * public SafeBufferedReader(Reader in, int bufferSize) { super(in,
     * bufferSize); }
     */

    /**
     * SafeBufferedReader方法。
     * * @param in Reader类型参数
     */
    public SafeBufferedReader(Reader in) {
        super(in);
    }

    @Override
    /**
     * read方法。
     * @return int类型返回值
     */
    public int read() throws IOException {
        int c = super.read();
        if (c == '\n') {
            if (lookingForLineFeed) {
                lookingForLineFeed = false;
                return super.read();
            } else {
                return c;
            }
        } else if (c == '\r') {
            lookingForLineFeed = true;
            return c;
        } else {
            lookingForLineFeed = false;
            return c;
        }
    }

    /**
     * readLine方法。
     *
     * @return String类型返回值
     */
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int c = super.read();
            if (c == -1) { // end of stream
                if (sb.length() == 0)
                    return null;
                return sb.toString();
            } else if (c == '\n') {
                if (lookingForLineFeed) {
                    lookingForLineFeed = false;
                    continue;
                } else {
                    return sb.toString();
                }
            } else if (c == '\r') {
                lookingForLineFeed = true;
                return sb.toString();
            } else {
                lookingForLineFeed = false;
                sb.append((char) c);
            }
        }
    }
}