package com.bank.central.auth.security;

import com.bank.central.common.constants.AppConstants;
import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtTokenService {

    private final byte[] secret;
    private final long expirySeconds;

    public JwtTokenService(
            @Value("${auth.jwt.secret:change-me-super-secret-key}") String secret,
            @Value("${auth.jwt.expiry-seconds:3600}") long expirySeconds
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirySeconds = expirySeconds;
    }

    public String generateToken(String username, String role) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(username)
                .claim("role", role)
                .expirationTime(Date.from(Instant.now().plusSeconds(expirySeconds)))
                .build();
        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            signedJwt.sign(new MACSigner(secret));
            return signedJwt.serialize();
        } catch (JOSEException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public Map<String, String> parseToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            if (!signedJwt.verify(new MACVerifier(secret))) {
                throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED, AppConstants.AUTH_JWT_INVALID_SIGNATURE);
            }
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.toInstant().isBefore(Instant.now())) {
                throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED, AppConstants.AUTH_JWT_EXPIRED);
            }
            String username = claims.getSubject();
            String role = claims.getStringClaim("role");
            if (username == null || username.isBlank() || role == null || role.isBlank()) {
                throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED, AppConstants.AUTH_JWT_MISSING_CLAIMS);
            }
            return Map.of("username", username, "role", role);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message == null || message.isBlank()) {
                message = AppConstants.AUTH_UNAUTHORIZED;
            }
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED, message);
        }
    }
}
