package com.bank.central.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("user_credentials")
public record UserCredentials(

        @Id Long id,

        Long customerId,

        String username,
        String passwordHash,

        String role,

        LocalDateTime createdAt

) {
    public static UserCredentials createNew(Long customerId, String username, String passwordHash) {
        return new UserCredentials(null, customerId, username, passwordHash, "USER", LocalDateTime.now());
    }
}