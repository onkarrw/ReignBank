package com.bank.central.account.domain;

import com.bank.central.common.constants.AppConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("cash_account_request")
public record CashAccountRequest(
        @Id Long id,
        Long customerId,
        String accountType,
        BigDecimal initialDeposit,
        String status,
        String reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {
    public static CashAccountRequest pending(Long customerId, String accountType, BigDecimal initialDeposit) {
        return new CashAccountRequest(null, customerId, accountType, initialDeposit, AppConstants.PENDING, null, null, LocalDateTime.now());
    }

    public CashAccountRequest withReview(String newStatus, String reviewer) {
        return new CashAccountRequest(id, customerId, accountType, initialDeposit, newStatus, reviewer, LocalDateTime.now(), createdAt);
    }
}
