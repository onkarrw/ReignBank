package com.bank.central.account.service;

import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class OtpSessionGate {

    public void requireVerified(HttpServletRequest request, String sessionKey, ErrorCode errorCode) {
        Object otpVerified = request.getSession(true).getAttribute(sessionKey);
        if (!(otpVerified instanceof Boolean) || !((Boolean) otpVerified)) {
            throw new BusinessException(errorCode);
        }
    }

    public void markVerified(HttpServletRequest request, String sessionKey) {
        request.getSession(true).setAttribute(sessionKey, true);
    }

    public void clearVerified(HttpServletRequest request, String sessionKey) {
        request.getSession(true).setAttribute(sessionKey, false);
    }
}
