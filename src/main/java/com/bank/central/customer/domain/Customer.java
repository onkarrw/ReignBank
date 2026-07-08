package com.bank.central.customer.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("customer")
public record Customer(

        @Id Long id,

        String firstName,
        String lastName,

        String email,
        String phone,

        String status,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
    public Customer withStatus(String newStatus) {
        return new Customer(id, firstName, lastName, email, phone, newStatus, createdAt, LocalDateTime.now());
    }
}