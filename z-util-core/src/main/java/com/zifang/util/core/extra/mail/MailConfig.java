package com.zifang.util.core.extra.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * 邮件配置类
 */
public class MailConfig {

    private String host;
    private int port = 25;
    private String username;
    private String password;
    private boolean useSsl = false;
    private boolean useTls = false;
    private String from;
    private String fromName;
    private boolean debug = false;

    /**
     * MailConfig方法。
     */
    public MailConfig() {
    }

    /**
     * of方法。
     * * @param host String类型参数
     *
     * @param username String类型参数
     * @param password String类型参数
     * @return static MailConfig类型返回值
     */
    public static MailConfig of(String host, String username, String password) {
        MailConfig config = new MailConfig();
        config.host = host;
        config.username = username;
        config.password = password;
        return config;
    }

    /**
     * of163方法。
     * * @param username String类型参数
     *
     * @param password String类型参数
     * @return static MailConfig类型返回值
     */
    public static MailConfig of163(String username, String password) {
        MailConfig config = new MailConfig();
        config.host = "smtp.163.com";
        config.port = 25;
        config.username = username;
        config.password = password;
        config.from = username;
        return config;
    }

    /**
     * ofQQ方法。
     * * @param username String类型参数
     *
     * @param password String类型参数
     * @return static MailConfig类型返回值
     */
    public static MailConfig ofQQ(String username, String password) {
        MailConfig config = new MailConfig();
        config.host = "smtp.qq.com";
        config.port = 465;
        config.username = username;
        config.password = password;
        config.from = username;
        config.useSsl = true;
        return config;
    }

    /**
     * ofGmail方法。
     * * @param username String类型参数
     *
     * @param password String类型参数
     * @return static MailConfig类型返回值
     */
    public static MailConfig ofGmail(String username, String password) {
        MailConfig config = new MailConfig();
        config.host = "smtp.gmail.com";
        config.port = 587;
        config.username = username;
        config.password = password;
        config.from = username;
        config.useTls = true;
        return config;
    }

    /**
     * host方法。
     * * @param host String类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig host(String host) {
        this.host = host;
        return this;
    }

    /**
     * port方法。
     * * @param port int类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig port(int port) {
        this.port = port;
        return this;
    }

    /**
     * username方法。
     * * @param username String类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig username(String username) {
        this.username = username;
        return this;
    }

    /**
     * password方法。
     * * @param password String类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig password(String password) {
        this.password = password;
        return this;
    }

    /**
     * useSsl方法。
     * * @param useSsl boolean类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig useSsl(boolean useSsl) {
        this.useSsl = useSsl;
        return this;
    }

    /**
     * useTls方法。
     * * @param useTls boolean类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig useTls(boolean useTls) {
        this.useTls = useTls;
        return this;
    }

    /**
     * from方法。
     * * @param from String类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig from(String from) {
        this.from = from;
        return this;
    }

    /**
     * fromName方法。
     * * @param fromName String类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig fromName(String fromName) {
        this.fromName = fromName;
        return this;
    }

    /**
     * debug方法。
     * * @param debug boolean类型参数
     *
     * @return MailConfig类型返回值
     */
    public MailConfig debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * getHost方法。
     *
     * @return String类型返回值
     */
    public String getHost() {
        return host;
    }

    /**
     * getPort方法。
     *
     * @return int类型返回值
     */
    public int getPort() {
        return port;
    }

    /**
     * getUsername方法。
     *
     * @return String类型返回值
     */
    public String getUsername() {
        return username;
    }

    /**
     * getPassword方法。
     *
     * @return String类型返回值
     */
    public String getPassword() {
        return password;
    }

    /**
     * isUseSsl方法。
     *
     * @return boolean类型返回值
     */
    public boolean isUseSsl() {
        return useSsl;
    }

    /**
     * isUseTls方法。
     *
     * @return boolean类型返回值
     */
    public boolean isUseTls() {
        return useTls;
    }

    /**
     * getFrom方法。
     *
     * @return String类型返回值
     */
    public String getFrom() {
        return from != null ? from : username;
    }

    /**
     * getFromName方法。
     *
     * @return String类型返回值
     */
    public String getFromName() {
        return fromName;
    }

    /**
     * isDebug方法。
     *
     * @return boolean类型返回值
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * getAuthenticator方法。
     *
     * @return Authenticator类型返回值
     */
    public Authenticator getAuthenticator() {
        return new Authenticator() {
            @Override
            /**
             * getPasswordAuthentication方法。
             * @return PasswordAuthentication类型返回值
             */
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    /**
     * toProperties方法。
     *
     * @return java.util.Properties类型返回值
     */
    public java.util.Properties toProperties() {
        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");

        if (useSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        if (useTls) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        if (debug) {
            props.put("mail.debug", "true");
        }

        return props;
    }
}