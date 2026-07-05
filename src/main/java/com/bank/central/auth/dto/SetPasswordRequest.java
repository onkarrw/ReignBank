package com.bank.central.auth.dto;

public record SetPasswordRequest(
        String email,
        String username,
        String password
) {}
