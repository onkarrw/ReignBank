package com.bank.central.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INTERNAL_ERROR(999, "INTERNAL_ERROR");

    private final int id;
    private final String code;

    ErrorCode(int id, String code) {
        this.id = id;
        this.code = code;
    }
}
