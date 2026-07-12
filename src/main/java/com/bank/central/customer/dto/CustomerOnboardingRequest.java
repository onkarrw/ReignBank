package com.bank.central.customer.dto;

public record CustomerOnboardingRequest(
        String firstName,
        String lastName,
        String email,
        String phone
) {}
