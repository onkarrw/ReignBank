package com.bank.central.customer.domain.state;

import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class OnboardingStateMachine {

    public OnboardingState transition(OnboardingState current, OnboardingEvent event) {
        OnboardingState next = switch (current) {
            case EMAIL_PENDING -> event == OnboardingEvent.VERIFY_EMAIL
                    ? OnboardingState.EMAIL_VERIFIED : null;
            case EMAIL_VERIFIED -> event == OnboardingEvent.START_KYC
                    ? OnboardingState.KYC_PENDING : null;
            case KYC_PENDING -> switch (event) {
                case APPROVE_KYC -> OnboardingState.KYC_APPROVED;
                case REJECT_KYC -> OnboardingState.REJECTED;
                default -> null;
            };
            case KYC_APPROVED -> event == OnboardingEvent.COMPLETE
                    ? OnboardingState.COMPLETED : null;
            default -> null;
        };

        if (next == null) {
            throw new BusinessException(ErrorCode.ONBOARDING_INVALID_STATE);
        }
        return next;
    }
}
