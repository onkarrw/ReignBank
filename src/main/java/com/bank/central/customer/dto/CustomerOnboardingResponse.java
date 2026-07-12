package com.bank.central.customer.dto;

import java.util.UUID;

public record CustomerOnboardingResponse(
        Long customerId,
        UUID otpRequestId,
        String status,
        String message
) {}
