package com.bank.central.account.domain;

import com.bank.central.common.constants.AppConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("account")
public record Account(

        @Id Long id,

        Long customerId,

        String accountNumber,
        String accountType,

        BigDecimal balance,
        String currency,

        String status,

        LocalDateTime closedAt,
        String blockedReason,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
    public static Account newActive(Long customerId, String accountNumber, String accountType, BigDecimal balance) {
        LocalDateTime now = LocalDateTime.now();
        return new Account(null, customerId, accountNumber, accountType, balance, "INR", AppConstants.ACTIVE, null, null, now, now);
    }

    public Account withBalance(BigDecimal newBalance) {
        return new Account(id, customerId, accountNumber, accountType, newBalance, currency, status, closedAt, blockedReason, createdAt, LocalDateTime.now());
    }
}
