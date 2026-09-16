package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LocalProxySelector类。
 */
public class LocalProxySelector extends ProxySelector {

    private List<URI> failed = new ArrayList<URI>();

    /**
     * select方法。
     * * @param uri URI类型参数
     *
     * @return List<Proxy>类型返回值
     */
    public List<Proxy> select(URI uri) {

        List<Proxy> result = new ArrayList<Proxy>();
        if (failed.contains(uri) || !"http".equalsIgnoreCase(uri.getScheme())) {
            result.add(Proxy.NO_PROXY);
        } else {
            SocketAddress proxyAddress = new InetSocketAddress("proxy.example.com", 8000);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
            result.add(proxy);
        }

        return result;
    }

    /**
     * connectFailed方法。
     * * @param uri URI类型参数
     *
     * @param address SocketAddress类型参数
     * @param ex      IOException类型参数
     */
    public void connectFailed(URI uri, SocketAddress address, IOException ex) {
        failed.add(uri);
    }
}