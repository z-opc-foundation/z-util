package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Redirector类。
 */
public class Redirector {

    private static final Logger log = Logger.getLogger("Redirector");

    private final int port;
    private final String newSite;

    /**
     * Redirector方法。
     * * @param newSite String类型参数
     *
     * @param port int类型参数
     */
    public Redirector(String newSite, int port) {
        this.port = port;
        this.newSite = newSite;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        int thePort;
        String theSite;

        try {
            theSite = args[0];
            // trim trailing slash
            if (theSite.endsWith("/")) {
                theSite = theSite.substring(0, theSite.length() - 1);
            }
        } catch (RuntimeException ex) {
            System.out.println("Usage: java Redirector http://www.newsite.com/ port");
            return;
        }

        try {
            thePort = Integer.parseInt(args[1]);
        } catch (RuntimeException ex) {
            thePort = 80;
        }

        Redirector redirector = new Redirector(theSite, thePort);
        redirector.start();
    }

    /**
     * start方法。
     */
    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            log.info("Redirecting connections on port " + server.getLocalPort() + " to " + newSite);

            while (true) {
                try {
                    Socket s = server.accept();
                    Thread t = new RedirectThread(s);
                    t.start();
                } catch (IOException ex) {
                    log.warning("Exception accepting connection");
                } catch (RuntimeException ex) {
                    log.log(Level.SEVERE, "Unexpected error", ex);
                }
            }
        } catch (BindException ex) {
            log.log(Level.SEVERE, "Could not start server.", ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Error opening server socket", ex);
        }
    }

    private class RedirectThread extends Thread {

        private final Socket connection;

        RedirectThread(Socket s) {
            this.connection = s;
        }

        /**
         * run方法。
         */
        public void run() {
            try {
                Writer out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.US_ASCII));
                Reader in = new InputStreamReader(new BufferedInputStream(connection.getInputStream()));

                // read the first line only; that's all we need
                StringBuilder request = new StringBuilder(80);
                while (true) {
                    int c = in.read();
                    if (c == '\r' || c == '\n' || c == -1)
                        break;
                    request.append((char) c);
                }

                String get = request.toString();
                String[] pieces = get.split("\\w*");
                String theFile = pieces[1];

                // If this is HTTP/1.0 or later send a MIME header
                if (get.indexOf("HTTP") != -1) {
                    out.write("HTTP/1.0 302 FOUND\r\n");
                    Date now = new Date();
                    out.write("Date: " + now + "\r\n");
                    out.write("Server: Redirector 1.1\r\n");
                    out.write("Location: " + newSite + theFile + "\r\n");
                    out.write("Content-type: text/html\r\n\r\n");
                    out.flush();
                }
                // Not all browsers support redirection so we need to
                // produce HTML that says where the document has moved to.
                out.write("<HTML><HEAD><TITLE>Document moved</TITLE></HEAD>\r\n");
                out.write("<BODY><H1>Document moved</H1>\r\n");
                out.write("The document " + theFile + " has moved to\r\n<A HREF=\"" + newSite + theFile + "\">"
                        + newSite + theFile + "</A>.\r\n Please update your bookmarks<P>");
                out.write("</BODY></HTML>\r\n");
                out.flush();
                log.log(Level.INFO, "Redirected " + connection.getRemoteSocketAddress());
            } catch (IOException ex) {
                log.log(Level.WARNING, "Error talking to " + connection.getRemoteSocketAddress(), ex);
            } finally {
                try {
                    connection.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}