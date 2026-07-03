package com.bank.central.notification.port;

public interface MailSender {

    void sendMail(String toEmail, String content);

    void sendMail(String toEmail, String subject, String content);
}
