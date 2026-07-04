package com.bank.central.otp.service;

import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.common.constants.AppConstants;
import com.bank.central.notification.port.MailSender;
import com.bank.central.notification.port.SmsSender;
import com.bank.central.otp.domain.OtpPurpose;
import com.bank.central.otp.domain.OtpStatus;
import com.bank.central.otp.domain.RedisOtpEntry;
import com.bank.central.otp.port.OtpStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class OtpServiceImpl implements OtpService {

    private static final int EXPIRY_MINUTES = 10;
    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);

    private final OtpStore otpStore;
    private final MailSender mailSender;
    private final SmsSender smsSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpServiceImpl(OtpStore otpStore, MailSender mailSender, SmsSender smsSender) {
        this.otpStore = otpStore;
        this.mailSender = mailSender;
        this.smsSender = smsSender;
    }

    @Override
    public UUID generateAndSendEmailOtp(Long customerId, String email, String initiatorId) {
        log.debug("Generating email OTP for customerId={}", customerId);
        return generateAndSend(customerId, email, initiatorId, OtpPurpose.EMAIL_VERIFICATION, true);
    }

    @Override
    public UUID generateAndSendMobileKycOtp(Long customerId, String phone, String initiatorId) {
        log.debug("Generating mobile KYC OTP for customerId={}", customerId);
        otpStore.expirePending(customerId, OtpPurpose.MOBILE_KYC);
        String otp = generateOtp();
        UUID requestId = saveOtp(customerId, initiatorId, OtpPurpose.MOBILE_KYC, otp);
        smsSender.sendOtp(
                phone,
                "Your mobile KYC OTP is " + otp + ". Valid for " + EXPIRY_MINUTES + " minutes."
        );
        log.info("Generated mobile KYC OTP requestId={} customerId={}", requestId, customerId);
        return requestId;
    }

    @Override
    public UUID generateAndSendAccountCreationEmailOtp(Long customerId, String email, String initiatorId) {
        log.debug("Generating account creation OTP for customerId={}", customerId);
        return generateAndSend(customerId, email, initiatorId, OtpPurpose.ACCOUNT_CREATION_EMAIL, true);
    }

    @Override
    public UUID generateAndSendTransferEmailOtp(Long customerId, String email, String initiatorId) {
        log.debug("Generating transfer OTP for customerId={}", customerId);
        return generateAndSend(customerId, email, initiatorId, OtpPurpose.SEND_MONEY_EMAIL, true);
    }

    @Override
    public void verify(UUID requestId, Long customerId, String otp, OtpPurpose expectedPurpose, String initiatorId) {
        log.debug("Verifying OTP for requestId={} customerId={}", requestId, customerId);
        RedisOtpEntry record = otpStore.findByRequestId(requestId);
        if (record == null) {
            throw new BusinessException(ErrorCode.OTP_NOT_FOUND);
        }

        if (!record.customerId().equals(customerId)) {
            log.warn("OTP requestId={} does not match customerId={}", requestId, customerId);
            throw new BusinessException(ErrorCode.OTP_REQUEST_MISMATCH);
        }
        if (!record.initiatorId().equals(initiatorId)) {
            log.warn("OTP requestId={} initiator mismatch", requestId);
            throw new BusinessException(ErrorCode.OTP_REQUEST_MISMATCH);
        }
        if (record.purpose() != expectedPurpose) {
            log.warn("OTP requestId={} purpose mismatch expected={}", requestId, expectedPurpose);
            throw new BusinessException(ErrorCode.OTP_REQUEST_MISMATCH);
        }
        if (record.status() == OtpStatus.LOCKED) {
            log.warn("OTP requestId={} is locked", requestId);
            throw new BusinessException(ErrorCode.OTP_LOCKED);
        }
        if (record.status() != OtpStatus.PENDING) {
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        if (!otp.equals(record.otpHash())) {
            handleFailedAttempt(record);
        }

        otpStore.delete(requestId, customerId, expectedPurpose);
        log.info("OTP verified for requestId={} customerId={}", requestId, customerId);
    }

    private UUID generateAndSend(Long customerId, String email, String initiatorId, OtpPurpose purpose, boolean sendEmail) {
        otpStore.expirePending(customerId, purpose);
        String otp = generateOtp();
        UUID requestId = saveOtp(customerId, initiatorId, purpose, otp);
        if (sendEmail) {
            String subject = switch (purpose) {
                case ACCOUNT_CREATION_EMAIL -> "Account Creation OTP";
                case SEND_MONEY_EMAIL -> "Transfer OTP";
                default -> "Email Verification OTP";
            };
            String body = switch (purpose) {
                case ACCOUNT_CREATION_EMAIL -> "Your OTP for account creation is: " + otp + ". Valid for " + EXPIRY_MINUTES + " minutes.";
                case SEND_MONEY_EMAIL -> "Your OTP to confirm this transfer is: " + otp + ". Valid for " + EXPIRY_MINUTES + " minutes.";
                default -> "Your OTP for bank onboarding is: " + otp + ". Valid for " + EXPIRY_MINUTES + " minutes.";
            };
            mailSender.sendMail(email, subject, body);
        }
        log.info("Sending OTP requestId={} to email={}", requestId, email);
        return requestId;
    }

    private UUID saveOtp(Long customerId, String initiatorId, OtpPurpose purpose, String otp) {
        UUID requestId = UUID.randomUUID();
        RedisOtpEntry entry = RedisOtpEntry.pending(
                requestId,
                initiatorId,
                customerId,
                purpose,
                otp,
                ""
        );
        otpStore.save(entry);
        log.info("OTP generated customerId={} purpose={} requestId={} otp={}", customerId, purpose, requestId, otp);
        return requestId;
    }

    private void handleFailedAttempt(RedisOtpEntry record) {
        RedisOtpEntry updated = record.withFailedAttempt();
        otpStore.update(updated);
        if (updated.isLocked()) {
            throw new BusinessException(ErrorCode.OTP_LOCKED);
        }
        throw new BusinessException(ErrorCode.OTP_INVALID, String.format(AppConstants.OTP_INVALID_WITH_ATTEMPTS, updated.remainingAttempts()));
    }

    private String generateOtp() {
        int value = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(value);
    }

    private String generateSalt() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashOtp(String salt, String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((salt + otp).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
