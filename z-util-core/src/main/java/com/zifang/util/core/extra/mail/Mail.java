package com.zifang.util.core.extra.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件消息类
 */
public class Mail {

    private String subject;
    private String from;
    private String fromName;
    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private String content;
    private boolean html = false;
    private List<File> attachments = new ArrayList<>();

    /**
     * Mail方法。
     */
    public Mail() {
    }

    /**
     * create方法。
     *
     * @return static Mail类型返回值
     */
    public static Mail create() {
        return new Mail();
    }

    /**
     * subject方法。
     * * @param subject String类型参数
     *
     * @return Mail类型返回值
     */
    public Mail subject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * from方法。
     * * @param from String类型参数
     *
     * @return Mail类型返回值
     */
    public Mail from(String from) {
        this.from = from;
        return this;
    }

    /**
     * fromName方法。
     * * @param fromName String类型参数
     *
     * @return Mail类型返回值
     */
    public Mail fromName(String fromName) {
        this.fromName = fromName;
        return this;
    }

    /**
     * to方法。
     * * @param to String...类型参数
     *
     * @return Mail类型返回值
     */
    public Mail to(String... to) {
        for (String t : to) {
            this.to.add(t);
        }
        return this;
    }

    /**
     * to方法。
     * * @param to ListString类型参数
     *
     * @return Mail类型返回值
     */
    public Mail to(List<String> to) {
        this.to.addAll(to);
        return this;
    }

    /**
     * cc方法。
     * * @param cc String...类型参数
     *
     * @return Mail类型返回值
     */
    public Mail cc(String... cc) {
        for (String c : cc) {
            this.cc.add(c);
        }
        return this;
    }

    /**
     * cc方法。
     * * @param cc ListString类型参数
     *
     * @return Mail类型返回值
     */
    public Mail cc(List<String> cc) {
        this.cc.addAll(cc);
        return this;
    }

    /**
     * bcc方法。
     * * @param bcc String...类型参数
     *
     * @return Mail类型返回值
     */
    public Mail bcc(String... bcc) {
        for (String b : bcc) {
            this.bcc.add(b);
        }
        return this;
    }

    /**
     * bcc方法。
     * * @param bcc ListString类型参数
     *
     * @return Mail类型返回值
     */
    public Mail bcc(List<String> bcc) {
        this.bcc.addAll(bcc);
        return this;
    }

    /**
     * content方法。
     * * @param content String类型参数
     *
     * @return Mail类型返回值
     */
    public Mail content(String content) {
        this.content = content;
        return this;
    }

    /**
     * html方法。
     * * @param htmlContent String类型参数
     *
     * @return Mail类型返回值
     */
    public Mail html(String htmlContent) {
        this.content = htmlContent;
        this.html = true;
        return this;
    }

    /**
     * attach方法。
     * * @param attachments File...类型参数
     *
     * @return Mail类型返回值
     */
    public Mail attach(File... attachments) {
        for (File a : attachments) {
            this.attachments.add(a);
        }
        return this;
    }

    /**
     * attach方法。
     * * @param attachments ListFile类型参数
     *
     * @return Mail类型返回值
     */
    public Mail attach(List<File> attachments) {
        this.attachments.addAll(attachments);
        return this;
    }

    /**
     * getSubject方法。
     *
     * @return String类型返回值
     */
    public String getSubject() {
        return subject;
    }

    /**
     * getFrom方法。
     *
     * @return String类型返回值
     */
    public String getFrom() {
        return from;
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
     * getTo方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getTo() {
        return to;
    }

    /**
     * getCc方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getCc() {
        return cc;
    }

    /**
     * getBcc方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getBcc() {
        return bcc;
    }

    /**
     * getContent方法。
     *
     * @return String类型返回值
     */
    public String getContent() {
        return content;
    }

    /**
     * isHtml方法。
     *
     * @return boolean类型返回值
     */
    public boolean isHtml() {
        return html;
    }

    /**
     * getAttachments方法。
     *
     * @return List<File>类型返回值
     */
    public List<File> getAttachments() {
        return attachments;
    }

    /**
     * validate方法。
     */
    public void validate() {
        if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
            throw new IllegalStateException("At least one recipient (To/CC/BCC) is required");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalStateException("Subject is required");
        }
    }

    /**
     * 发送邮件的便捷方法
     */
    public void send(MailConfig config) {
        SendMailUtil.send(config, this);
    }
}