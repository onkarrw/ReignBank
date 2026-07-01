package com.bank.central.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final boolean customMessage;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.customMessage = true;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getCode());
        this.errorCode = errorCode;
        this.customMessage = false;
    }

    public boolean hasCustomMessage() {
        return customMessage;
    }
}
