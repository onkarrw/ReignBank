package com.bank.central.account.dto;

import java.math.BigDecimal;

public record CreateAccountRequest(
        BigDecimal initialDeposit
) {}
