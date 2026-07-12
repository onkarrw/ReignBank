package com.bank.central.account.dto;

import java.math.BigDecimal;

public record SendMoneyRequest(
        String toAccountNumber,
        BigDecimal amount,
        String description
) {}
