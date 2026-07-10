package com.bank.central.account.dto;

import java.util.UUID;

public record AccountCreationOtpVerifyRequest(
        String otp,
        UUID otpRequestId
) {}
