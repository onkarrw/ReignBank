package com.bank.central.account.service;

import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.customer.domain.CustomerOnboarding;
import com.bank.central.customer.domain.state.OnboardingEvent;
import com.bank.central.customer.domain.state.OnboardingState;
import com.bank.central.customer.domain.state.OnboardingStateMachine;
import com.bank.central.customer.repository.CustomerOnboardingRepository;
import org.springframework.stereotype.Component;

@Component
public class AccountOnboardingUpdater {

    private final CustomerOnboardingRepository onboardingRepository;
    private final OnboardingStateMachine onboardingStateMachine;

    public AccountOnboardingUpdater(CustomerOnboardingRepository onboardingRepository, OnboardingStateMachine onboardingStateMachine) {
        this.onboardingRepository = onboardingRepository;
        this.onboardingStateMachine = onboardingStateMachine;
    }

    public void updateAfterAccountCreated(Long customerId) {
        CustomerOnboarding onboarding = onboardingRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ONBOARDING_NOT_FOUND));
        OnboardingState current = onboarding.state();
        OnboardingState next = current;
        if (current == OnboardingState.KYC_APPROVED) {
            next = onboardingStateMachine.transition(current, OnboardingEvent.COMPLETE);
        }
        if (current == OnboardingState.EMAIL_VERIFIED) {
            next = OnboardingState.EMAIL_VERIFIED;
        }
        onboardingRepository.save(onboarding.withState(next));
    }
}
