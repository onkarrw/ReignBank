package com.bank.central.otp.dto;

import java.util.UUID;

public record OtpVerifyRequest(
        String email,
        String otp,
        UUID otpRequestId
) {}
