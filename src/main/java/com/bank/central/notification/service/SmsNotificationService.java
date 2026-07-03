package com.bank.central.notification.service;

import com.bank.central.notification.port.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    @Override
    @Async
    public void sendOtp(String phone, String content) {
        log.info("Sending mobile OTP to phone={}", phone);
        log.debug("SMS content for phone={} content={}", phone, content);
    }
}
