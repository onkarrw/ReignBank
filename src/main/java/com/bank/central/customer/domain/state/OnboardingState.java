package com.bank.central.customer.domain.state;

public enum OnboardingState {
    EMAIL_PENDING,
    EMAIL_VERIFIED,
    KYC_PENDING,
    KYC_APPROVED,
    COMPLETED,
    REJECTED
}
