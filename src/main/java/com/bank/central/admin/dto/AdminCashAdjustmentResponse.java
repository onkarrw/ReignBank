package com.bank.central.admin.dto;

import java.math.BigDecimal;

public record AdminCashAdjustmentResponse(
        String status,
        String message,
        BigDecimal newBalance,
        Long auditId
) {}
