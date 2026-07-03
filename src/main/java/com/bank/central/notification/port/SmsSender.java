package com.bank.central.notification.port;

public interface SmsSender {

    void sendOtp(String phone, String content);
}
