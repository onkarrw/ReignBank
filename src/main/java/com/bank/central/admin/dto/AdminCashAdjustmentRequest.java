package com.bank.central.admin.dto;

import java.math.BigDecimal;

public record AdminCashAdjustmentRequest(
        BigDecimal amount,
        String note
) {}
