package com.bank.central.customer.dto;

import java.util.UUID;

public record KycMobileOtpVerifyRequest(
        String otp,
        UUID otpRequestId
) {}
