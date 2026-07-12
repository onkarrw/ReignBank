package com.bank.central.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminAuditDto(
        Long id,
        String adminUsername,
        Long customerId,
        Long accountId,
        String actionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String note,
        LocalDateTime createdAt
) {}
