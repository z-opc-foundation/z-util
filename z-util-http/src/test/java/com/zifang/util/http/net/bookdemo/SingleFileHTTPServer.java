package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SingleFileHTTPServer类。
 */
public class SingleFileHTTPServer {

    private static final Logger log = Logger.getLogger("SingleFileHTTPServer");

    private final byte[] content;
    private final byte[] header;
    private final int port;
    private final String encoding;

    /**
     * SingleFileHTTPServer方法。
     * * @param data String类型参数
     *
     * @param encoding String类型参数
     * @param mimeType String类型参数
     * @param port     int类型参数
     */
    public SingleFileHTTPServer(String data, String encoding, String mimeType, int port)
            throws UnsupportedEncodingException {
        this(data.getBytes(encoding), encoding, mimeType, port);
    }

    /**
     * SingleFileHTTPServer方法。
     * * @param data byte[]类型参数
     *
     * @param encoding String类型参数
     * @param mimeType String类型参数
     * @param port     int类型参数
     */
    public SingleFileHTTPServer(byte[] data, String encoding, String mimeType, int port) {
        this.content = data;
        this.port = port;
        this.encoding = encoding;
        String header = "HTTP/1.0 200 OK\r\n" + "Server: OneFile 2.0\r\n" + "Content-length: " + this.content.length
                + "\r\n" + "Content-type: " + mimeType + "; charset=" + encoding + "\r\n\r\n";
        this.header = header.getBytes(Charset.forName("US-ASCII"));
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        // set the port to listen on
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 1 || port > 65535)
                port = 80;
        } catch (RuntimeException ex) {
            port = 80;
        }

        String encoding = "UTF-8";
        if (args.length > 2)
            encoding = args[2];

        try {
            Path path = Paths.get(args[0]);
            byte[] data = Files.readAllBytes(path);

            String contentType = URLConnection.getFileNameMap().getContentTypeFor(args[0]);
            SingleFileHTTPServer server = new SingleFileHTTPServer(data, encoding, contentType, port);
            server.start();

        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Usage: java SingleFileHTTPServer filename port encoding");
        } catch (IOException ex) {
            log.severe(ex.getMessage());
        }
    }

    /**
     * start方法。
     */
    public void start() {
        ExecutorService pool = Executors.newFixedThreadPool(100);
        try (ServerSocket server = new ServerSocket(this.port)) {
            log.info("Accepting connections on port " + server.getLocalPort());
            log.info("Data to be sent:");
            log.info(new String(this.content, encoding));

            while (true) {
                try {
                    Socket connection = server.accept();
                    pool.submit(new HTTPHandler(connection));
                } catch (IOException ex) {
                    log.log(Level.WARNING, "Exception accepting connection", ex);
                } catch (RuntimeException ex) {
                    log.log(Level.SEVERE, "Unexpected error", ex);
                }
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not start server", ex);
        }
    }

    private class HTTPHandler implements Callable<Void> {
        private final Socket connection;

        HTTPHandler(Socket connection) {
            this.connection = connection;
        }

        @Override
        /**
         * call方法。
         * @return Void类型返回值
         */
        public Void call() throws IOException {
            try {
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                InputStream in = new BufferedInputStream(connection.getInputStream());
                // read the first line only; that's all we need
                StringBuilder request = new StringBuilder(80);
                while (true) {
                    int c = in.read();
                    if (c == '\r' || c == '\n' || c == -1)
                        break;
                    request.append((char) c);
                }
                // If this is HTTP/1.0 or later send a MIME header
                if (request.toString().indexOf("HTTP/") != -1) {
                    out.write(header);
                }
                out.write(content);
                out.flush();
            } catch (IOException ex) {
                log.log(Level.WARNING, "Error writing to client", ex);
            } finally {
                connection.close();
            }
            return null;
        }
    }
}