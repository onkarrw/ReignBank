package com.bank.central.transaction.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("bank_transaction")
public record BankTransaction(

        @Id Long id,

        Long fromAccountId,
        Long toAccountId,

        BigDecimal amount,
        String currency,

        String type,
        String status,

        String referenceId,
        String description,

        LocalDateTime createdAt

) {
    public static BankTransaction transfer(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            String currency,
            String referenceId,
            String description
    ) {
        return new BankTransaction(null, fromAccountId, toAccountId, amount, currency, "TRANSFER", "SUCCESS", referenceId, description, LocalDateTime.now());
    }

    public static BankTransaction adminDeposit(Long toAccountId, BigDecimal amount, String currency, String referenceId, String description) {
        return new BankTransaction(null, null, toAccountId, amount, currency, "DEPOSIT", "SUCCESS", referenceId, description, LocalDateTime.now());
    }

    public static BankTransaction adminWithdraw(Long fromAccountId, BigDecimal amount, String currency, String referenceId, String description) {
        return new BankTransaction(null, fromAccountId, null, amount, currency, "WITHDRAW", "SUCCESS", referenceId, description, LocalDateTime.now());
    }
}
