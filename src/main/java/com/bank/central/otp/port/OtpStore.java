package com.bank.central.otp.port;

import com.bank.central.otp.domain.OtpPurpose;
import com.bank.central.otp.domain.RedisOtpEntry;

import java.util.UUID;

public interface OtpStore {

    void save(RedisOtpEntry entry);

    RedisOtpEntry findByRequestId(UUID requestId);

    void update(RedisOtpEntry entry);

    void delete(UUID requestId, Long customerId, OtpPurpose purpose);

    void expirePending(Long customerId, OtpPurpose purpose);
}
