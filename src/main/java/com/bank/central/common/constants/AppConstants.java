package com.bank.central.common.constants;

import com.bank.central.common.exception.ErrorCode;

public interface AppConstants {

    String ACTIVE = "ACTIVE";
    String SUCCESS = "SUCCESS";
    String OTP_SENT = "OTP_SENT";
    String ACCOUNT_CREATION_PENDING = "ACCOUNT_CREATION_PENDING";
    String STAFF = "STAFF";
    String CUSTOMER = "CUSTOMER";

    String CUSTOMER_ONBOARDING_FIRST_NAME_REQUIRED = "First name is required";
    String CUSTOMER_ONBOARDING_LAST_NAME_REQUIRED = "Last name is required";
    String CUSTOMER_ONBOARDING_EMAIL_REQUIRED = "Email is required";
    String CUSTOMER_ONBOARDING_INVALID_EMAIL = "Invalid email format";
    String CUSTOMER_ONBOARDING_INVALID_PHONE = "Invalid phone number";
    String CUSTOMER_ONBOARDING_EMAIL_EXISTS = "Email already registered";
    String CUSTOMER_ONBOARDING_PHONE_LIMIT_REACHED = "Maximum registrations reached for this phone";
    String CUSTOMER_NOT_FOUND = "Customer not found";
    String CUSTOMER_INACTIVE = "Customer account is inactive";
    String ONBOARDING_NOT_FOUND = "Onboarding record not found";
    String ONBOARDING_INVALID_STATE = "Invalid onboarding state for this action";
    String OTP_NOT_FOUND = "OTP request not found";
    String OTP_REQUEST_MISMATCH = "OTP request ID does not match";
    String OTP_INVALID = "Invalid OTP";
    String OTP_LOCKED = "OTP verification locked after too many failed attempts";
    String OTP_INVALID_WITH_ATTEMPTS = "Invalid OTP. %s attempt(s) remaining.";
    String AUTH_INVALID_CREDENTIALS = "Invalid credentials";
    String AUTH_PASSWORD_ALREADY_SET = "Password already set";
    String AUTH_PASSWORD_NOT_ALLOWED = "Password setup not allowed at current onboarding stage";
    String AUTH_UNAUTHORIZED = "Unauthorized";
    String AUTH_USERNAME_REQUIRED = "Username is required";
    String AUTH_PASSWORD_TOO_SHORT = "Password must be at least 8 characters";
    String AUTH_USERNAME_EXISTS = "Username already exists";
    String AUTH_JWT_INVALID_SIGNATURE = "Invalid JWT signature";
    String AUTH_JWT_EXPIRED = "JWT has expired";
    String AUTH_JWT_MISSING_CLAIMS = "JWT missing required claims";
    String INTERNAL_ERROR = "Unexpected internal error";

    String RESPONSE_AUTH_PASSWORD_SET = "Password set successfully. Please login.";
    String RESPONSE_AUTH_LOGIN_SUCCESS = "Login successful";
    String RESPONSE_AUTH_LOGOUT_SUCCESS = "Logout successful";
    String RESPONSE_ONBOARDING_OTP_SENT = "OTP sent to your email. Please verify to continue.";
    String RESPONSE_ONBOARDING_OTP_RESEND = "Previous onboarding found. New OTP sent to your email.";
    String RESPONSE_ONBOARDING_ACCOUNT_PENDING = "Email already verified. Continue with account creation.";
    String RESPONSE_ONBOARDING_EMAIL_VERIFIED = "Email verified successfully. You can proceed with onboarding.";
    String RESPONSE_ONBOARDING_KYC_OTP_SENT = "Mobile KYC OTP sent.";
    String RESPONSE_ONBOARDING_KYC_VERIFIED = "Mobile KYC verified successfully.";

    static String errorMessage(ErrorCode code) {
        return switch (code) {
            case CUSTOMER_ONBOARDING_FIRST_NAME_REQUIRED -> CUSTOMER_ONBOARDING_FIRST_NAME_REQUIRED;
            case CUSTOMER_ONBOARDING_LAST_NAME_REQUIRED -> CUSTOMER_ONBOARDING_LAST_NAME_REQUIRED;
            case CUSTOMER_ONBOARDING_EMAIL_REQUIRED -> CUSTOMER_ONBOARDING_EMAIL_REQUIRED;
            case CUSTOMER_ONBOARDING_INVALID_EMAIL -> CUSTOMER_ONBOARDING_INVALID_EMAIL;
            case CUSTOMER_ONBOARDING_INVALID_PHONE -> CUSTOMER_ONBOARDING_INVALID_PHONE;
            case CUSTOMER_ONBOARDING_EMAIL_EXISTS -> CUSTOMER_ONBOARDING_EMAIL_EXISTS;
            case CUSTOMER_ONBOARDING_PHONE_LIMIT_REACHED -> CUSTOMER_ONBOARDING_PHONE_LIMIT_REACHED;
            case CUSTOMER_NOT_FOUND -> CUSTOMER_NOT_FOUND;
            case ONBOARDING_NOT_FOUND -> ONBOARDING_NOT_FOUND;
            case ONBOARDING_INVALID_STATE -> ONBOARDING_INVALID_STATE;
            case CUSTOMER_INACTIVE -> CUSTOMER_INACTIVE;
            case OTP_NOT_FOUND -> OTP_NOT_FOUND;
            case OTP_REQUEST_MISMATCH -> OTP_REQUEST_MISMATCH;
            case OTP_INVALID -> OTP_INVALID;
            case OTP_LOCKED -> OTP_LOCKED;
            case AUTH_INVALID_CREDENTIALS -> AUTH_INVALID_CREDENTIALS;
            case AUTH_PASSWORD_ALREADY_SET -> AUTH_PASSWORD_ALREADY_SET;
            case AUTH_PASSWORD_NOT_ALLOWED -> AUTH_PASSWORD_NOT_ALLOWED;
            case AUTH_UNAUTHORIZED -> AUTH_UNAUTHORIZED;
            case INTERNAL_ERROR -> INTERNAL_ERROR;
        };
    }
}
