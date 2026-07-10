package com.bank.central.account.dto;

import java.math.BigDecimal;

public record CreateAccountResponse(
        String status,
        String message,
        String accountNumber,
        String accountType,
        BigDecimal balance
) {}
