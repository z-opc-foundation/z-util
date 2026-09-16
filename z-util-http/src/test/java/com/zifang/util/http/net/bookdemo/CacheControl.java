package com.zifang.util.http.net.bookdemo;

import java.util.Date;
import java.util.Locale;

/**
 * CacheControl类。
 */
public class CacheControl {

    private Date maxAge = null;
    private Date sMaxAge = null;
    private boolean mustRevalidate = false;
    private boolean noCache = false;
    private boolean noStore = false;
    private boolean proxyRevalidate = false;
    private boolean publicCache = false;
    private boolean privateCache = false;

    /**
     * CacheControl方法。
     * * @param s String类型参数
     */
    public CacheControl(String s) {
        if (s == null || !s.contains(":")) {
            return; // default policy
        }

        String value = s.split(":")[1].trim();
        String[] components = value.split(",");

        Date now = new Date();
        for (String component : components) {
            try {
                component = component.trim().toLowerCase(Locale.US);
                if (component.startsWith("max-age=")) {
                    int secondsInTheFuture = Integer.parseInt(component.substring(8));
                    maxAge = new Date(now.getTime() + 1000 * secondsInTheFuture);
                } else if (component.startsWith("s-maxage=")) {
                    int secondsInTheFuture = Integer.parseInt(component.substring(8));
                    sMaxAge = new Date(now.getTime() + 1000 * secondsInTheFuture);
                } else if (component.equals("must-revalidate")) {
                    mustRevalidate = true;
                } else if (component.equals("proxy-revalidate")) {
                    proxyRevalidate = true;
                } else if (component.equals("no-cache")) {
                    noCache = true;
                } else if (component.equals("public")) {
                    publicCache = true;
                } else if (component.equals("private")) {
                    privateCache = true;
                }
            } catch (RuntimeException ex) {
                continue;
            }
        }
    }

    /**
     * getMaxAge方法。
     *
     * @return Date类型返回值
     */
    public Date getMaxAge() {
        return maxAge;
    }

    /**
     * getSharedMaxAge方法。
     *
     * @return Date类型返回值
     */
    public Date getSharedMaxAge() {
        return sMaxAge;
    }

    /**
     * mustRevalidate方法。
     *
     * @return boolean类型返回值
     */
    public boolean mustRevalidate() {
        return mustRevalidate;
    }

    /**
     * proxyRevalidate方法。
     *
     * @return boolean类型返回值
     */
    public boolean proxyRevalidate() {
        return proxyRevalidate;
    }

    /**
     * noStore方法。
     *
     * @return boolean类型返回值
     */
    public boolean noStore() {
        return noStore;
    }

    /**
     * noCache方法。
     *
     * @return boolean类型返回值
     */
    public boolean noCache() {
        return noCache;
    }

    /**
     * publicCache方法。
     *
     * @return boolean类型返回值
     */
    public boolean publicCache() {
        return publicCache;
    }

    /**
     * privateCache方法。
     *
     * @return boolean类型返回值
     */
    public boolean privateCache() {
        return privateCache;
    }
}