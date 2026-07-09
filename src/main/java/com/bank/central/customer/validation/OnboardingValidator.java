package com.bank.central.customer.validation;

import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.customer.dto.CustomerOnboardingRequest;
import org.springframework.stereotype.Component;

@Component
public class OnboardingValidator {

    public void validate(CustomerOnboardingRequest request) {
        if (isBlank(request.firstName())) {
            throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_FIRST_NAME_REQUIRED);
        }
        if (isBlank(request.lastName())) {
            throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_LAST_NAME_REQUIRED);
        }
        if (isBlank(request.email())) {
            throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_EMAIL_REQUIRED);
        }
        if (!request.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_INVALID_EMAIL);
        }
        if (isBlank(request.phone()) || !request.phone().matches("^\\d{10,15}$")) {
            throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_INVALID_PHONE);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
