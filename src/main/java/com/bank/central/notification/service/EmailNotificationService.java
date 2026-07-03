package com.bank.central.notification.service;

import com.bank.central.notification.port.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements MailSender {

    private final JavaMailSender mailSender;
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendMail(String toEmail, String content) {
        log.info("Sending mail to {}", toEmail);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Bank Notification");
        message.setText(content);

        try {
            mailSender.send(message);
            log.debug("Mail sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send mail to {}: {}", toEmail, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Async
    public void sendMail(String toEmail, String subject, String content) {
        log.info("Sending mail to {} subject={}", toEmail, subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
            log.debug("Mail sent to {} subject={}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send mail to {} subject={}: {}", toEmail, subject, e.getMessage(), e);
            throw e;
        }
    }
}
