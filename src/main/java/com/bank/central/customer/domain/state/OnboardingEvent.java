package com.bank.central.customer.domain.state;

public enum OnboardingEvent {
    VERIFY_EMAIL,
    START_KYC,
    APPROVE_KYC,
    REJECT_KYC,
    COMPLETE
}
