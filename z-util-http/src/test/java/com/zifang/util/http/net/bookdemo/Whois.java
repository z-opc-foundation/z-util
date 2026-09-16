package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Whois类。
 */
public class Whois {

    public final static int DEFAULT_PORT = 43;
    public final static String DEFAULT_HOST = "whois.internic.net";

    private int port = DEFAULT_PORT;
    private InetAddress host;

    /**
     * Whois方法。
     * * @param host InetAddress类型参数
     *
     * @param port int类型参数
     */
    public Whois(InetAddress host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Whois方法。
     * * @param host InetAddress类型参数
     */
    public Whois(InetAddress host) {
        this(host, DEFAULT_PORT);
    }

    /**
     * Whois方法。
     * * @param hostname String类型参数
     *
     * @param port int类型参数
     */
    public Whois(String hostname, int port) throws UnknownHostException {
        this(InetAddress.getByName(hostname), port);
    }

    /**
     * Whois方法。
     * * @param hostname String类型参数
     */
    public Whois(String hostname) throws UnknownHostException {
        this(InetAddress.getByName(hostname), DEFAULT_PORT);
    }

    /**
     * Whois方法。
     */
    public Whois() throws UnknownHostException {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    // Items to search for

    /**
     * lookUpNames方法。
     * * @param target String类型参数
     *
     * @param category   SearchFor类型参数
     * @param group      SearchIn类型参数
     * @param exactMatch boolean类型参数
     * @return String类型返回值
     */
    public String lookUpNames(String target, SearchFor category, SearchIn group, boolean exactMatch)
            throws IOException {

        String suffix = "";
        if (!exactMatch)
            suffix = ".";

        String prefix = category.label + " " + group.label;
        String query = prefix + target + suffix;

        Socket socket = new Socket();
        try {
            SocketAddress address = new InetSocketAddress(host, port);
            socket.connect(address);
            Writer out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            out.write(query + "\r\n");
            out.flush();

            StringBuilder response = new StringBuilder();
            String theLine = null;
            while ((theLine = in.readLine()) != null) {
                response.append(theLine);
                response.append("\r\n");
            }
            return response.toString();
        } finally {
            socket.close();
        }
    }

    // Categories to search in

    /**
     * getHost方法。
     *
     * @return InetAddress类型返回值
     */
    public InetAddress getHost() {
        return this.host;
    }

    /**
     * setHost方法。
     * * @param host String类型参数
     */
    public void setHost(String host) throws UnknownHostException {
        this.host = InetAddress.getByName(host);
    }

    /**
     * SearchFor枚举。
     */
    public enum SearchFor {
        ANY("Any"), NETWORK("Network"), PERSON("Person"), HOST("Host"), DOMAIN("Domain"), ORGANIZATION(
                "Organization"), GROUP("Group"), GATEWAY("Gateway"), ASN("ASN");

        private String label;

        SearchFor(String label) {
            this.label = label;
        }
    }

    /**
     * SearchIn枚举。
     */
    public enum SearchIn {
        ALL(""), NAME("Name"), MAILBOX("Mailbox"), HANDLE("!");

        private String label;

        SearchIn(String label) {
            this.label = label;
        }
    }
}