package com.bank.central.auth.dto;

public record LoginRequest(
        String username,
        String password
) {}
