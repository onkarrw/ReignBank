package com.bank.central.account.dto;

import java.math.BigDecimal;

public record SendMoneyResponse(
        String status,
        String message,
        String referenceId,
        BigDecimal newBalance
) {}
