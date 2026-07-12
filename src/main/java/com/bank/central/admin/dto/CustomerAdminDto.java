package com.bank.central.admin.dto;

public record CustomerAdminDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String status
) {}
