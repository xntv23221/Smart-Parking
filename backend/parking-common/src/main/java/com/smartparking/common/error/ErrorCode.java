package com.smartparking.common.error;

/**
 * Stable error codes for API responses and cross-layer exception handling.
 */
public enum ErrorCode {
    VALIDATION_ERROR(40001),
    UNAUTHORIZED(40100),
    FORBIDDEN(40300),
    NOT_FOUND(40400),
    CONFLICT(40900),
    PAYMENT_REQUIRED(40200),
    TOO_MANY_REQUESTS(42900),
    INTERNAL_ERROR(50000);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
