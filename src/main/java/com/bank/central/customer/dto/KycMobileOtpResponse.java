package com.bank.central.customer.dto;

import java.util.UUID;

public record KycMobileOtpResponse(
        UUID otpRequestId,
        String status,
        String message
) {}
