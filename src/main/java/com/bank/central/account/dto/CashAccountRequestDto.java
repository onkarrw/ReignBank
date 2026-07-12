package com.bank.central.account.dto;

import java.math.BigDecimal;

public record CashAccountRequestDto(
        Long requestId,
        Long customerId,
        String accountType,
        BigDecimal initialDeposit,
        String status
) {}
