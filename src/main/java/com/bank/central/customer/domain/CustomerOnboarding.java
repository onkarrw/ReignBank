package com.bank.central.customer.domain;

import com.bank.central.customer.domain.state.OnboardingState;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("customer_onboarding")
public record CustomerOnboarding(
        @Id Long id,
        Long customerId,
        OnboardingState state,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerOnboarding newFor(Long customerId, OnboardingState state) {
        LocalDateTime now = LocalDateTime.now();
        return new CustomerOnboarding(null, customerId, state, now, now);
    }

    public CustomerOnboarding withState(OnboardingState newState) {
        return new CustomerOnboarding(id, customerId, newState, createdAt, LocalDateTime.now());
    }
}
