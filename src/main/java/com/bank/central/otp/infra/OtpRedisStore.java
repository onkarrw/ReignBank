package com.bank.central.otp.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.bank.central.otp.domain.OtpPurpose;
import com.bank.central.otp.domain.RedisOtpEntry;
import com.bank.central.otp.port.OtpStore;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class OtpRedisStore implements OtpStore {

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;
    private final Duration otpTtl;

    public OtpRedisStore(
            StringRedisTemplate redis,
            JsonMapper jsonMapper,
            @Value("${otp.redis.ttl-minutes:10}") long otpTtlMinutes
    ) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
        this.otpTtl = Duration.ofMinutes(otpTtlMinutes);
    }

    public void save(RedisOtpEntry entry) {
        String requestKey = requestKey(entry.requestId());
        String pendingKey = pendingKey(entry.customerId(), entry.purpose());
        redis.opsForValue().set(requestKey, serialize(entry), otpTtl);
        redis.opsForValue().set(pendingKey, entry.requestId().toString(), otpTtl);
    }

    public RedisOtpEntry findByRequestId(UUID requestId) {
        String raw = redis.opsForValue().get(requestKey(requestId));
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return deserialize(raw);
    }

    public void update(RedisOtpEntry entry) {
        String requestKey = requestKey(entry.requestId());
        Long ttlSeconds = redis.getExpire(requestKey, TimeUnit.SECONDS);
        redis.opsForValue().set(requestKey, serialize(entry));
        if (ttlSeconds != null && ttlSeconds > 0) {
            redis.expire(requestKey, Duration.ofSeconds(ttlSeconds));
        }
    }

    public void delete(UUID requestId, Long customerId, OtpPurpose purpose) {
        redis.delete(requestKey(requestId));
        redis.delete(pendingKey(customerId, purpose));
    }

    public void expirePending(Long customerId, OtpPurpose purpose) {
        String pendingKey = pendingKey(customerId, purpose);
        String existingRequestId = redis.opsForValue().get(pendingKey);
        if (existingRequestId != null) {
            redis.delete(requestKey(UUID.fromString(existingRequestId)));
        }
        redis.delete(pendingKey);
    }

    private String requestKey(UUID requestId) {
        return "otp:req:" + requestId;
    }

    private String pendingKey(Long customerId, OtpPurpose purpose) {
        return "otp:pending:" + customerId + ":" + purpose.name();
    }

    private String serialize(RedisOtpEntry entry) {
        return jsonMapper.writeValueAsString(entry);
    }

    private RedisOtpEntry deserialize(String raw) {
        return jsonMapper.readValue(raw, RedisOtpEntry.class);
    }
}
