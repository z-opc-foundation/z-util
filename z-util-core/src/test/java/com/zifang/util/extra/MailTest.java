package com.zifang.util.extra;

import com.zifang.util.core.extra.mail.Mail;
import com.zifang.util.core.extra.mail.MailConfig;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 邮件工具测试
 */

/**
 * MailTest类。
 */
public class MailTest {

    @Test
    /**
     * testMailConfigOf163方法。
     */
    public void testMailConfigOf163() {
        MailConfig config = MailConfig.of163("test@163.com", "password");
        assertEquals("smtp.163.com", config.getHost());
        assertEquals(25, config.getPort());
        assertEquals("test@163.com", config.getUsername());
        assertEquals("test@163.com", config.getFrom());
        assertFalse(config.isUseSsl());
        assertFalse(config.isUseTls());
    }

    @Test
    /**
     * testMailConfigOfQQ方法。
     */
    public void testMailConfigOfQQ() {
        MailConfig config = MailConfig.ofQQ("123456@qq.com", "password");
        assertEquals("smtp.qq.com", config.getHost());
        assertEquals(465, config.getPort());
        assertTrue(config.isUseSsl());
    }

    @Test
    /**
     * testMailConfigBuilder方法。
     */
    public void testMailConfigBuilder() {
        MailConfig config = MailConfig.of("smtp.example.com", "user", "pass")
                .port(587)
                .useTls(true)
                .from("sender@example.com")
                .fromName("Sender")
                .debug(true);

        assertEquals("smtp.example.com", config.getHost());
        assertEquals(587, config.getPort());
        assertTrue(config.isUseTls());
        assertEquals("sender@example.com", config.getFrom());
        assertEquals("Sender", config.getFromName());
        assertTrue(config.isDebug());
    }

    @Test
    /**
     * testMailBuilder方法。
     */
    public void testMailBuilder() {
        Mail mail = Mail.create()
                .subject("Test Subject")
                .from("sender@example.com")
                .to("recipient@example.com")
                .cc("cc@example.com")
                .bcc("bcc@example.com")
                .content("Hello World");

        assertEquals("Test Subject", mail.getSubject());
        assertEquals("sender@example.com", mail.getFrom());
        assertEquals(1, mail.getTo().size());
        assertEquals("recipient@example.com", mail.getTo().get(0));
        assertEquals(1, mail.getCc().size());
        assertEquals(1, mail.getBcc().size());
        assertFalse(mail.isHtml());
    }

    @Test
    /**
     * testMailHtml方法。
     */
    public void testMailHtml() {
        Mail mail = Mail.create()
                .to("recipient@example.com")
                .subject("HTML Test")
                .html("<h1>Hello</h1><p>World</p>");

        assertTrue(mail.isHtml());
        assertTrue(mail.getContent().contains("<h1>"));
    }

    @Test(expected = IllegalStateException.class)
    /**
     * testMailValidateNoRecipient方法。
     */
    public void testMailValidateNoRecipient() {
        Mail mail = Mail.create()
                .subject("Test")
                .content("Content");
        mail.validate();
    }

    @Test(expected = IllegalStateException.class)
    /**
     * testMailValidateNoSubject方法。
     */
    public void testMailValidateNoSubject() {
        Mail mail = Mail.create()
                .to("recipient@example.com")
                .content("Content");
        mail.validate();
    }

    @Test
    /**
     * testMailWithAttachment方法。
     */
    public void testMailWithAttachment() {
        Mail mail = Mail.create()
                .to("recipient@example.com")
                .subject("With Attachment")
                .content("See attachment")
                .attach(new java.io.File("test.txt"));

        assertEquals(1, mail.getAttachments().size());
    }

    @Test
    /**
     * testMailMultipleRecipients方法。
     */
    public void testMailMultipleRecipients() {
        Mail mail = Mail.create()
                .to("a@example.com", "b@example.com", "c@example.com")
                .subject("Multi-recipient")
                .content("Test");

        assertEquals(3, mail.getTo().size());
    }
}