package com.bank.central.auth.dto;

public record LoginResponse(
        String status,
        String message,
        String token,
        String role
) {}
