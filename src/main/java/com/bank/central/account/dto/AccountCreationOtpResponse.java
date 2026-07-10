package com.bank.central.account.dto;

import java.util.UUID;

public record AccountCreationOtpResponse(
        UUID otpRequestId,
        String status,
        String message
) {}
