package com.bank.central.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("admin_user")
public record AdminUser(
        @Id Long id,
        String username,
        String passwordHash,
        String role,
        String status,
        LocalDateTime createdAt
) {}
