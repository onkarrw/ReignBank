package com.bank.central.customer.service;

import com.bank.central.common.constants.AppConstants;
import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.account.repository.AccountRepository;
import com.bank.central.customer.domain.Customer;
import com.bank.central.customer.domain.CustomerStatus;
import com.bank.central.customer.dto.CustomerOnboardingRequest;
import com.bank.central.customer.dto.CustomerOnboardingResponse;
import com.bank.central.customer.dto.KycMobileOtpResponse;
import com.bank.central.customer.dto.KycMobileOtpVerifyRequest;
import com.bank.central.otp.dto.OtpVerifyRequest;
import com.bank.central.otp.dto.OtpVerifyResponse;
import com.bank.central.otp.domain.OtpPurpose;
import com.bank.central.customer.domain.CustomerOnboarding;
import com.bank.central.customer.domain.state.OnboardingEvent;
import com.bank.central.customer.domain.state.OnboardingState;
import com.bank.central.customer.domain.state.OnboardingStateMachine;
import com.bank.central.otp.service.OtpService;
import com.bank.central.customer.validation.OnboardingValidator;
import com.bank.central.customer.repository.CustomerOnboardingRepository;
import com.bank.central.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OnboardingServiceImpl implements OnboardingService {

    private final CustomerRepository customerRepository;
    private final CustomerOnboardingRepository onboardingRepository;
    private final AccountRepository accountRepository;
    private final OnboardingStateMachine stateMachine;
    private final OnboardingValidator validator;
    private final OtpService otpService;
    private final int maxPhoneEntries;
    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);

    public OnboardingServiceImpl(
            CustomerRepository customerRepository,
            CustomerOnboardingRepository onboardingRepository,
            AccountRepository accountRepository,
            OnboardingStateMachine stateMachine,
            OnboardingValidator validator,
            OtpService otpService,
            @Value("${onboarding.phone.max-entries:4}") int maxPhoneEntries
    ) {
        this.customerRepository = customerRepository;
        this.onboardingRepository = onboardingRepository;
        this.accountRepository = accountRepository;
        this.stateMachine = stateMachine;
        this.validator = validator;
        this.otpService = otpService;
        this.maxPhoneEntries = maxPhoneEntries;
    }

    @Transactional
    public CustomerOnboardingResponse onboard(CustomerOnboardingRequest request, String initiatorId) {
        log.info("Starting onboard flow for email={}", request.email());
        validator.validate(request);

        Customer existingCustomer = customerRepository.findByEmail(request.email()).orElse(null);
        if (existingCustomer != null) {
            return handleExistingCustomer(existingCustomer, initiatorId);
        }
        if (customerRepository.countByPhone(request.phone()) >= maxPhoneEntries) {
            log.warn("Phone limit reached for phone={} limit={}", request.phone(), maxPhoneEntries);
            throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_PHONE_LIMIT_REACHED);
        }

        Customer customer = new Customer(null, request.firstName(), request.lastName(), request.email(), request.phone(), CustomerStatus.PENDING_VERIFICATION.name(), LocalDateTime.now(), LocalDateTime.now());

        Customer savedCustomer = saveCustomer(customer);
        log.info("Saved customer id={} email={}", savedCustomer.id(), savedCustomer.email());

        CustomerOnboarding onboarding = CustomerOnboarding.newFor(savedCustomer.id(), OnboardingState.EMAIL_PENDING);

        onboardingRepository.save(onboarding);
        log.debug("Created onboarding record for customerId={} state={}", savedCustomer.id(), onboarding.state());

        UUID otpRequestId = otpService.generateAndSendEmailOtp(savedCustomer.id(), savedCustomer.email(), initiatorId);
        log.info("Generated OTP requestId={} for customerId={}", otpRequestId, savedCustomer.id());

        return new CustomerOnboardingResponse(savedCustomer.id(), otpRequestId, AppConstants.OTP_SENT, AppConstants.RESPONSE_ONBOARDING_OTP_SENT);
    }

    private Customer saveCustomer(Customer customer) {
        try {
            return customerRepository.save(customer);
        } catch (DataIntegrityViolationException ex) {
            log.error("Customer save failed for email={} phone={}", customer.email(), customer.phone(), ex);
            throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_EMAIL_EXISTS);
        }
    }

    private CustomerOnboardingResponse handleExistingCustomer(Customer customer, String initiatorId) {
        CustomerOnboarding onboarding = onboardingRepository.findByCustomerId(customer.id()).orElse(null);
        if (onboarding != null && onboarding.state() == OnboardingState.EMAIL_PENDING) {
            UUID otpRequestId = otpService.generateAndSendEmailOtp(customer.id(), customer.email(), initiatorId);
            log.info("Resent OTP requestId={} for existing pending customerId={}", otpRequestId, customer.id());
            return new CustomerOnboardingResponse(customer.id(), otpRequestId, AppConstants.OTP_SENT, AppConstants.RESPONSE_ONBOARDING_OTP_RESEND);
        }
        if (onboarding != null && onboarding.state() == OnboardingState.EMAIL_VERIFIED) {
            boolean hasAccount = accountRepository.existsByCustomerId(customer.id());
            if (!hasAccount) {
                log.info("Existing verified customer without account customerId={}", customer.id());
                return new CustomerOnboardingResponse(customer.id(), null, AppConstants.ACCOUNT_CREATION_PENDING, AppConstants.RESPONSE_ONBOARDING_ACCOUNT_PENDING);
            }
        }
        log.warn("Onboard attempted with existing completed email={}", customer.email());
        throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_EMAIL_EXISTS);
    }

    @Transactional
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request, String initiatorId) {
        log.info("Verifying OTP for email={}, requestId={}", request.email(), request.otpRequestId());
        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        otpService.verify(request.otpRequestId(), customer.id(), request.otp(), OtpPurpose.EMAIL_VERIFICATION, initiatorId);
        log.info("OTP verified for customerId={}", customer.id());

        CustomerOnboarding onboarding = onboardingRepository.findByCustomerId(customer.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.ONBOARDING_NOT_FOUND));

        if (onboarding.state() != OnboardingState.EMAIL_PENDING) {
            throw new BusinessException(ErrorCode.ONBOARDING_INVALID_STATE);
        }

        OnboardingState nextState = stateMachine.transition(onboarding.state(), OnboardingEvent.VERIFY_EMAIL);

        onboardingRepository.save(onboarding.withState(nextState));
        log.info("Onboarding state transitioned to {} for customerId={}", nextState, customer.id());

        return new OtpVerifyResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_ONBOARDING_EMAIL_VERIFIED);
    }

    @Transactional
    public KycMobileOtpResponse requestMobileKycOtp(Long customerId, String initiatorId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        CustomerOnboarding onboarding = onboardingRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ONBOARDING_NOT_FOUND));

        OnboardingState state = onboarding.state();
        if (state == OnboardingState.EMAIL_VERIFIED) {
            OnboardingState nextState = stateMachine.transition(state, OnboardingEvent.START_KYC);
            onboarding = onboardingRepository.save(onboarding.withState(nextState));
            state = onboarding.state();
        }

        if (state != OnboardingState.KYC_PENDING) {
            throw new BusinessException(ErrorCode.ONBOARDING_INVALID_STATE);
        }

        UUID otpRequestId = otpService.generateAndSendMobileKycOtp(customer.id(), customer.phone(), initiatorId);
        return new KycMobileOtpResponse(otpRequestId, AppConstants.OTP_SENT, AppConstants.RESPONSE_ONBOARDING_KYC_OTP_SENT);
    }

    @Transactional
    public OtpVerifyResponse verifyMobileKycOtp(Long customerId, KycMobileOtpVerifyRequest request, String initiatorId) {
        CustomerOnboarding onboarding = onboardingRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ONBOARDING_NOT_FOUND));

        if (onboarding.state() != OnboardingState.KYC_PENDING) {
            throw new BusinessException(ErrorCode.ONBOARDING_INVALID_STATE);
        }

        otpService.verify(request.otpRequestId(), customerId, request.otp(), OtpPurpose.MOBILE_KYC, initiatorId);

        OnboardingState nextState = stateMachine.transition(onboarding.state(), OnboardingEvent.APPROVE_KYC);

        onboardingRepository.save(onboarding.withState(nextState));

        return new OtpVerifyResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_ONBOARDING_KYC_VERIFIED);
    }
}
