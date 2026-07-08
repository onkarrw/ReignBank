package com.bank.central.branch.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("branch")
public record Branch(

        @Id Long id,

        String name,
        String ifscCode,
        String address,

        LocalDateTime createdAt

) {}