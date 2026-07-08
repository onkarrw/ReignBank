package com.bank.central.auth.domain;

public record UserIdentity(
        String username,
        String role,
        Long customerId,
        String email,
        String phone,
        String identityType
) {}
