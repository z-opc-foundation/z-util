package com.zifang.util.http.net.bookdemo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * QueryString类。
 */
public class QueryString {

    private StringBuilder query = new StringBuilder();

    /**
     * QueryString方法。
     */
    public QueryString() {
    }

    /**
     * add方法。
     * * @param name String类型参数
     *
     * @param value String类型参数
     * @return synchronized void类型返回值
     */
    public synchronized void add(String name, String value) {
        query.append('&');
        encode(name, value);
    }

    private synchronized void encode(String name, String value) {
        try {
            query.append(URLEncoder.encode(name, "UTF-8"));
            query.append('=');
            query.append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Broken VM does not support UTF-8");
        }
    }

    /**
     * getQuery方法。
     *
     * @return synchronized String类型返回值
     */
    public synchronized String getQuery() {
        return query.toString();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return getQuery();
    }
}