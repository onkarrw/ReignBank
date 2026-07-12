package com.bank.central.admin.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("admin_customer_audit")
public record AdminCustomerAudit(
        @Id Long id,
        String adminUsername,
        Long customerId,
        Long accountId,
        String actionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String note,
        LocalDateTime createdAt
) {
    public static AdminCustomerAudit cashAdd(String adminUsername, Long customerId, Long accountId, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String note) {
        return entry(adminUsername, customerId, accountId, "CASH_ADD", amount, balanceBefore, balanceAfter, note);
    }

    public static AdminCustomerAudit cashRemove(String adminUsername, Long customerId, Long accountId, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String note) {
        return entry(adminUsername, customerId, accountId, "CASH_REMOVE", amount, balanceBefore, balanceAfter, note);
    }

    public static AdminCustomerAudit activate(String adminUsername, Long customerId, String note) {
        return entry(adminUsername, customerId, null, "ACTIVATE", null, null, null, note);
    }

    public static AdminCustomerAudit deactivate(String adminUsername, Long customerId, String note) {
        return entry(adminUsername, customerId, null, "DEACTIVATE", null, null, null, note);
    }

    private static AdminCustomerAudit entry(String adminUsername, Long customerId, Long accountId, String actionType, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String note) {
        return new AdminCustomerAudit(null, adminUsername, customerId, accountId, actionType, amount, balanceBefore, balanceAfter, note, LocalDateTime.now());
    }
}
