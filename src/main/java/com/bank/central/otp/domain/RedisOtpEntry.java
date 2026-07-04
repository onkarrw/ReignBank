package com.bank.central.otp.domain;

import java.util.UUID;

public record RedisOtpEntry(
        UUID requestId,
        String initiatorId,
        Long customerId,
        OtpPurpose purpose,
        String otpHash,
        String salt,
        int attemptCount,
        OtpStatus status
) {
    public static final int MAX_ATTEMPTS = 3;

    public static RedisOtpEntry pending(
            UUID requestId,
            String initiatorId,
            Long customerId,
            OtpPurpose purpose,
            String otpHash,
            String salt
    ) {
        return new RedisOtpEntry(requestId, initiatorId, customerId, purpose, otpHash, salt, 0, OtpStatus.PENDING);
    }

    public RedisOtpEntry withFailedAttempt() {
        int nextAttempts = attemptCount + 1;
        OtpStatus nextStatus = nextAttempts >= MAX_ATTEMPTS ? OtpStatus.LOCKED : OtpStatus.PENDING;
        return new RedisOtpEntry(requestId, initiatorId, customerId, purpose, otpHash, salt, nextAttempts, nextStatus);
    }

    public boolean isLocked() {
        return status == OtpStatus.LOCKED;
    }

    public int remainingAttempts() {
        return MAX_ATTEMPTS - attemptCount;
    }
}
