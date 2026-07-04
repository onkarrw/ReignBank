package com.bank.central.otp.service;

import com.bank.central.otp.domain.OtpPurpose;

import java.util.UUID;

public interface OtpService {

    UUID generateAndSendEmailOtp(Long customerId, String email, String initiatorId);

    UUID generateAndSendMobileKycOtp(Long customerId, String phone, String initiatorId);

    UUID generateAndSendAccountCreationEmailOtp(Long customerId, String email, String initiatorId);

    UUID generateAndSendTransferEmailOtp(Long customerId, String email, String initiatorId);

    void verify(UUID requestId, Long customerId, String otp, OtpPurpose expectedPurpose, String initiatorId);
}
