package com.bank.central.common.constants;

import com.bank.central.common.exception.ErrorCode;

public interface AppConstants {

    String ACTIVE = "ACTIVE";

    String OTP_NOT_FOUND = "OTP request not found";
    String OTP_REQUEST_MISMATCH = "OTP request ID does not match";
    String OTP_INVALID = "Invalid OTP";
    String OTP_LOCKED = "OTP verification locked after too many failed attempts";
    String OTP_INVALID_WITH_ATTEMPTS = "Invalid OTP. %s attempt(s) remaining.";
    String AUTH_UNAUTHORIZED = "Unauthorized";
    String AUTH_JWT_INVALID_SIGNATURE = "Invalid JWT signature";
    String AUTH_JWT_EXPIRED = "JWT has expired";
    String AUTH_JWT_MISSING_CLAIMS = "JWT missing required claims";
    String INTERNAL_ERROR = "Unexpected internal error";

    static String errorMessage(ErrorCode code) {
        return switch (code) {
            case OTP_NOT_FOUND -> OTP_NOT_FOUND;
            case OTP_REQUEST_MISMATCH -> OTP_REQUEST_MISMATCH;
            case OTP_INVALID -> OTP_INVALID;
            case OTP_LOCKED -> OTP_LOCKED;
            case AUTH_UNAUTHORIZED -> AUTH_UNAUTHORIZED;
            case INTERNAL_ERROR -> INTERNAL_ERROR;
        };
    }
}
