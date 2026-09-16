package com.zifang.util.http.net.bookdemo;

import java.io.*;

/**
 * SafePrintWriter类。
 */
public class SafePrintWriter extends Writer {

    protected Writer out;

    private boolean autoFlush = false;
    private String lineSeparator;
    private boolean closed = false;

    /**
     * SafePrintWriter方法。
     * * @param out Writer类型参数
     *
     * @param lineSeparator String类型参数
     */
    public SafePrintWriter(Writer out, String lineSeparator) {
        this(out, false, lineSeparator);
    }

    /**
     * SafePrintWriter方法。
     * * @param out Writer类型参数
     *
     * @param lineSeparator char类型参数
     */
    public SafePrintWriter(Writer out, char lineSeparator) {
        this(out, false, String.valueOf(lineSeparator));
    }

    /**
     * SafePrintWriter方法。
     * * @param out Writer类型参数
     *
     * @param autoFlush     boolean类型参数
     * @param lineSeparator String类型参数
     */
    public SafePrintWriter(Writer out, boolean autoFlush, String lineSeparator) {
        super(out);
        this.out = out;
        this.autoFlush = autoFlush;
        if (lineSeparator == null) {
            throw new NullPointerException("Null line separator");
        }
        this.lineSeparator = lineSeparator;
    }

    /**
     * SafePrintWriter方法。
     * * @param out OutputStream类型参数
     *
     * @param autoFlush     boolean类型参数
     * @param encoding      String类型参数
     * @param lineSeparator String类型参数
     */
    public SafePrintWriter(OutputStream out, boolean autoFlush, String encoding, String lineSeparator)
            throws UnsupportedEncodingException {
        this(new OutputStreamWriter(out, encoding), autoFlush, lineSeparator);
    }

    /**
     * flush方法。
     */
    public void flush() throws IOException {
        synchronized (lock) {
            if (closed)
                throw new IOException("Stream closed");
            out.flush();
        }
    }

    /**
     * close方法。
     */
    public void close() throws IOException {
        try {
            this.flush();
        } catch (IOException ex) {
        }

        synchronized (lock) {
            out.close();
            this.closed = true;
        }
    }

    /**
     * write方法。
     * * @param c int类型参数
     */
    public void write(int c) throws IOException {
        synchronized (lock) {
            if (closed)
                throw new IOException("Stream closed");
            out.write(c);
        }
    }

    /**
     * write方法。
     * * @param text char[]类型参数
     *
     * @param offset int类型参数
     * @param length int类型参数
     */
    public void write(char[] text, int offset, int length) throws IOException {
        synchronized (lock) {
            if (closed)
                throw new IOException("Stream closed");
            out.write(text, offset, length);
        }
    }

    /**
     * write方法。
     * * @param text char[]类型参数
     */
    public void write(char[] text) throws IOException {
        synchronized (lock) {
            if (closed)
                throw new IOException("Stream closed");
            out.write(text, 0, text.length);
        }
    }

    /**
     * write方法。
     * * @param s String类型参数
     *
     * @param offset int类型参数
     * @param length int类型参数
     */
    public void write(String s, int offset, int length) throws IOException {
        synchronized (lock) {
            if (closed)
                throw new IOException("Stream closed");
            out.write(s, offset, length);
        }
    }

    /**
     * print方法。
     * * @param b boolean类型参数
     */
    public void print(boolean b) throws IOException {
        if (b)
            this.write("true");
        else
            this.write("false");
    }

    /**
     * println方法。
     * * @param b boolean类型参数
     */
    public void println(boolean b) throws IOException {
        synchronized (lock) {
            this.print(b);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * print方法。
     * * @param c char类型参数
     */
    public void print(char c) throws IOException {
        this.write(String.valueOf(c));
    }

    /**
     * println方法。
     * * @param c char类型参数
     */
    public void println(char c) throws IOException {
        synchronized (lock) {
            this.print(c);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * print方法。
     * * @param i int类型参数
     */
    public void print(int i) throws IOException {
        this.write(String.valueOf(i));
    }

    /**
     * println方法。
     * * @param i int类型参数
     */
    public void println(int i) throws IOException {
        synchronized (lock) {
            this.print(i);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * print方法。
     * * @param l long类型参数
     */
    public void print(long l) throws IOException {
        this.write(String.valueOf(l));
    }

    /**
     * println方法。
     * * @param l long类型参数
     */
    public void println(long l) throws IOException {
        synchronized (lock) {
            this.print(l);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * print方法。
     * * @param f float类型参数
     */
    public void print(float f) throws IOException {
        this.write(String.valueOf(f));
    }

    /**
     * println方法。
     * * @param f float类型参数
     */
    public void println(float f) throws IOException {
        synchronized (lock) {
            this.print(f);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * print方法。
     * * @param d double类型参数
     */
    public void print(double d) throws IOException {
        this.write(String.valueOf(d));
    }

    /**
     * println方法。
     * * @param d double类型参数
     */
    public void println(double d) throws IOException {
        synchronized (lock) {
            this.print(d);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * print方法。
     * * @param text char[]类型参数
     */
    public void print(char[] text) throws IOException {
        this.write(text);
    }

    /**
     * println方法。
     * * @param text char[]类型参数
     */
    public void println(char[] text) throws IOException {
        synchronized (lock) {
            this.print(text);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * print方法。
     * * @param s String类型参数
     */
    public void print(String s) throws IOException {
        if (s == null)
            this.write("null");
        else
            this.write(s);
    }

    /**
     * println方法。
     * * @param s String类型参数
     */
    public void println(String s) throws IOException {
        synchronized (lock) {
            this.print(s);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * print方法。
     * * @param o Object类型参数
     */
    public void print(Object o) throws IOException {
        if (o == null)
            this.write("null");
        else
            this.write(o.toString());
    }

    /**
     * println方法。
     * * @param o Object类型参数
     */
    public void println(Object o) throws IOException {
        synchronized (lock) {
            this.print(o);
            this.write(lineSeparator);
        }
        if (autoFlush)
            out.flush();
    }

    /**
     * println方法。
     */
    public void println() throws IOException {
        this.write(lineSeparator);
        if (autoFlush)
            out.flush();
    }
}