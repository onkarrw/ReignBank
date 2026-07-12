package com.bank.central.account.dto;

import java.math.BigDecimal;

public record AccountSummaryDto(
        String status,
        String message,
        String accountNumber,
        String accountType,
        BigDecimal balance,
        String currency,
        String ifscCode
) {}
