package com.bank.central.common.constants;

import com.bank.central.common.exception.ErrorCode;

public interface AppConstants {

    String ACTIVE = "ACTIVE";

    String INTERNAL_ERROR = "Unexpected internal error";

    static String errorMessage(ErrorCode code) {
        return switch (code) {
            case INTERNAL_ERROR -> INTERNAL_ERROR;
        };
    }
}
