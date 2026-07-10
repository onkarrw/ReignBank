package com.bank.central.account.service;

import com.bank.central.common.constants.AppConstants;
import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.account.domain.Account;
import com.bank.central.account.dto.AccountCreationOtpResponse;
import com.bank.central.account.dto.AccountCreationOtpVerifyRequest;
import com.bank.central.account.dto.AccountSummaryDto;
import com.bank.central.account.dto.CreateAccountRequest;
import com.bank.central.account.dto.CreateAccountResponse;
import com.bank.central.account.repository.AccountRepository;
import com.bank.central.account.repository.CashAccountRequestRepository;
import com.bank.central.account.domain.CashAccountRequest;
import com.bank.central.customer.domain.Customer;
import com.bank.central.customer.repository.CustomerRepository;
import com.bank.central.otp.domain.OtpPurpose;
import com.bank.central.otp.dto.OtpVerifyResponse;
import com.bank.central.otp.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountCreationServiceImpl implements AccountCreationService {

    private static final Logger log = LoggerFactory.getLogger(AccountCreationServiceImpl.class);
    private static final String DEFAULT_ACCOUNT_TYPE = "SAVINGS";

    private final AccountRepository accountRepository;
    private final CashAccountRequestRepository cashAccountRequestRepository;
    private final CustomerRepository customerRepository;
    private final OtpService otpService;
    private final AccountSupport accountSupport;
    private final AccountOnboardingUpdater accountOnboardingUpdater;

    public AccountCreationServiceImpl(
            AccountRepository accountRepository,
            CashAccountRequestRepository cashAccountRequestRepository,
            CustomerRepository customerRepository,
            OtpService otpService,
            AccountSupport accountSupport,
            AccountOnboardingUpdater accountOnboardingUpdater
    ) {
        this.accountRepository = accountRepository;
        this.cashAccountRequestRepository = cashAccountRequestRepository;
        this.customerRepository = customerRepository;
        this.otpService = otpService;
        this.accountSupport = accountSupport;
        this.accountOnboardingUpdater = accountOnboardingUpdater;
    }

    @Override
    @Transactional
    public AccountSummaryDto getAccountSummary(Long customerId) {
        Account account = accountRepository.findFirstByCustomerIdOrderByCreatedAtAsc(customerId).orElse(null);
        if (account != null) {
            if (!AppConstants.ACTIVE.equals(account.status())) {
                throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
            }
            return new AccountSummaryDto(AppConstants.ACTIVE, AppConstants.RESPONSE_ACCOUNT_SUMMARY_ACTIVE, account.accountNumber(), account.accountType(), account.balance(), account.currency(), accountSupport.resolveIfsc(account.id()));
        }
        Optional<CashAccountRequest> pending = cashAccountRequestRepository.findByCustomerIdAndStatus(customerId, AppConstants.PENDING);
        if (pending.isPresent()) {
            CashAccountRequest p = pending.get();
            return new AccountSummaryDto(AppConstants.PENDING_APPROVAL, AppConstants.RESPONSE_ACCOUNT_SUMMARY_PENDING, null, p.accountType(), p.initialDeposit(), "INR", null);
        }
        return new AccountSummaryDto(AppConstants.NO_ACCOUNT, AppConstants.RESPONSE_ACCOUNT_SUMMARY_NONE, null, null, null, null, null);
    }

    @Override
    @Transactional
    public CreateAccountResponse createAccount(Long customerId, CreateAccountRequest request) {
        if (request.initialDeposit() == null || request.initialDeposit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS, AppConstants.INSUFFICIENT_FUNDS_NEGATIVE_DEPOSIT);
        }
        if (accountRepository.existsByCustomerId(customerId)) {
            Account existing = accountRepository.findFirstByCustomerIdOrderByCreatedAtAsc(customerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
            return new CreateAccountResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_ACCOUNT_ALREADY_EXISTS, existing.accountNumber(), existing.accountType(), existing.balance());
        }
        if (request.initialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            Optional<CashAccountRequest> existingPending = cashAccountRequestRepository.findByCustomerIdAndStatus(customerId, AppConstants.PENDING);
            if (existingPending.isPresent()) {
                return new CreateAccountResponse(AppConstants.PENDING_APPROVAL, AppConstants.RESPONSE_ACCOUNT_PENDING_ALREADY, null, DEFAULT_ACCOUNT_TYPE, request.initialDeposit());
            }
            cashAccountRequestRepository.save(CashAccountRequest.pending(customerId, DEFAULT_ACCOUNT_TYPE, request.initialDeposit()));
            return new CreateAccountResponse(AppConstants.PENDING_APPROVAL, AppConstants.RESPONSE_ACCOUNT_PENDING_SENT, null, DEFAULT_ACCOUNT_TYPE, request.initialDeposit());
        }
        Account account = Account.newActive(customerId, accountSupport.generateAccountNumber(), DEFAULT_ACCOUNT_TYPE, request.initialDeposit());
        Account saved = accountRepository.save(account);
        accountSupport.linkDefaultBranch(saved.id());
        accountOnboardingUpdater.updateAfterAccountCreated(customerId);
        log.info("Account created customerId={} accountNumber={}", customerId, saved.accountNumber());
        return new CreateAccountResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_ACCOUNT_CREATED, saved.accountNumber(), saved.accountType(), saved.balance());
    }

    @Override
    public AccountCreationOtpResponse requestAccountCreationOtp(Long customerId, String initiatorId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        UUID otpRequestId = otpService.generateAndSendAccountCreationEmailOtp(customerId, customer.email(), initiatorId);
        return new AccountCreationOtpResponse(otpRequestId, AppConstants.OTP_SENT, AppConstants.RESPONSE_ACCOUNT_OTP_SENT);
    }

    @Override
    public OtpVerifyResponse verifyAccountCreationOtp(Long customerId, String initiatorId, AccountCreationOtpVerifyRequest request) {
        otpService.verify(request.otpRequestId(), customerId, request.otp(), OtpPurpose.ACCOUNT_CREATION_EMAIL, initiatorId);
        return new OtpVerifyResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_ACCOUNT_OTP_VERIFIED);
    }
}
