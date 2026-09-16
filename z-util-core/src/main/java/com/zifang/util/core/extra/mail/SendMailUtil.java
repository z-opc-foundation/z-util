package com.zifang.util.core.extra.mail;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 邮件发送工具类
 * <p>
 * 支持：
 * <ul>
 *   <li>普通文本和HTML邮件</li>
 *   <li>附件发送</li>
 *   <li>抄送/密送</li>
 *   <li>SSL/TLS加密</li>
 *   <li>163/QQ/Gmail等主流邮箱</li>
 * </ul>
 *
 * @author zifang
 */
public class SendMailUtil {

    private SendMailUtil() {
    }

    /**
     * 使用配置发送邮件
     */
    public static void send(MailConfig config, Mail mail) {
        send(config, mail, null);
    }

    /**
     * 使用配置发送邮件（带回调）
     */
    public static void send(MailConfig config, Mail mail, SendCallback callback) {
        mail.validate();

        Properties props = config.toProperties();
        Session session = Session.getInstance(props, config.getAuthenticator());
        session.setDebug(config.isDebug());

        try {
            MimeMessage message = createMessage(session, config, mail);
            Transport transport = session.getTransport("smtp");
            transport.connect(config.getHost(), config.getUsername(), config.getPassword());
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            if (callback != null) {
                callback.onSuccess(mail);
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(mail, e);
            } else {
                throw new RuntimeException("Failed to send email", e);
            }
        }
    }

    /**
     * 发送简单文本邮件
     */
    public static void send(MailConfig config, String to, String subject, String content) {
        Mail mail = Mail.create()
                .to(to)
                .subject(subject)
                .content(content);
        send(config, mail);
    }

    /**
     * 发送HTML邮件
     */
    public static void sendHtml(MailConfig config, String to, String subject, String htmlContent) {
        Mail mail = Mail.create()
                .to(to)
                .subject(subject)
                .html(htmlContent);
        send(config, mail);
    }

    /**
     * 发送带附件的邮件
     */
    public static void sendWithAttachment(MailConfig config, String to, String subject, String content, File... attachments) {
        Mail mail = Mail.create()
                .to(to)
                .subject(subject)
                .content(content)
                .attach(attachments);
        send(config, mail);
    }

    private static MimeMessage createMessage(Session session, MailConfig config, Mail mail) throws Exception {
        MimeMessage message = new MimeMessage(session);

        // 设置发件人
        String from = mail.getFrom() != null ? mail.getFrom() : config.getFrom();
        String fromName = mail.getFromName() != null ? mail.getFromName() : config.getFromName();
        if (fromName != null) {
            message.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
        } else {
            message.setFrom(new InternetAddress(from));
        }

        // 设置收件人
        message.setRecipients(Message.RecipientType.TO, parseAddresses(mail.getTo()));

        // 设置抄送
        if (!mail.getCc().isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, parseAddresses(mail.getCc()));
        }

        // 设置密送
        if (!mail.getBcc().isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, parseAddresses(mail.getBcc()));
        }

        // 设置主题
        message.setSubject(mail.getSubject(), StandardCharsets.UTF_8.name());

        // 设置时间和内容
        message.setSentDate(new Date());

        // 构建邮件内容
        if (mail.getAttachments().isEmpty()) {
            // 无附件
            if (mail.isHtml()) {
                message.setContent(mail.getContent(), "text/html;charset=UTF-8");
            } else {
                message.setText(mail.getContent(), StandardCharsets.UTF_8.name());
            }
        } else {
            // 有附件
            Multipart multipart = new MimeMultipart();

            // 添加正文
            BodyPart contentPart = new MimeBodyPart();
            if (mail.isHtml()) {
                contentPart.setContent(mail.getContent(), "text/html;charset=UTF-8");
            } else {
                contentPart.setContent(mail.getContent(), "text/plain;charset=UTF-8");
            }
            multipart.addBodyPart(contentPart);

            // 添加附件
            for (File file : mail.getAttachments()) {
                BodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setFileName(MimeUtility.encodeWord(file.getName()));
                attachmentPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);
        }

        message.saveChanges();
        return message;
    }

    private static InternetAddress[] parseAddresses(List<String> addresses) throws UnsupportedEncodingException, AddressException {
        InternetAddress[] result = new InternetAddress[addresses.size()];
        for (int i = 0; i < addresses.size(); i++) {
            result[i] = new InternetAddress(addresses.get(i));
        }
        return result;
    }

    /**
     * 发送回调接口
     */
    public interface SendCallback {
        void onSuccess(Mail mail);

        void onError(Mail mail, Exception e);
    }
}