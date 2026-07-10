package com.bank.central.account.service;

import com.bank.central.common.constants.AppConstants;
import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.account.domain.Account;
import com.bank.central.account.domain.CashAccountRequest;
import com.bank.central.account.dto.CashAccountRequestDto;
import com.bank.central.account.dto.CreateAccountResponse;
import com.bank.central.account.repository.AccountRepository;
import com.bank.central.account.repository.CashAccountRequestRepository;
import com.bank.central.otp.dto.OtpVerifyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CashRequestServiceImpl implements CashRequestService {

    private static final Logger log = LoggerFactory.getLogger(CashRequestServiceImpl.class);

    private final CashAccountRequestRepository cashAccountRequestRepository;
    private final AccountRepository accountRepository;
    private final AccountOnboardingUpdater accountOnboardingUpdater;
    private final AccountSupport accountSupport;

    public CashRequestServiceImpl(
            CashAccountRequestRepository cashAccountRequestRepository,
            AccountRepository accountRepository,
            AccountOnboardingUpdater accountOnboardingUpdater,
            AccountSupport accountSupport
    ) {
        this.cashAccountRequestRepository = cashAccountRequestRepository;
        this.accountRepository = accountRepository;
        this.accountOnboardingUpdater = accountOnboardingUpdater;
        this.accountSupport = accountSupport;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashAccountRequestDto> getPendingCashRequests() {
        return cashAccountRequestRepository.findAllByStatusOrderByCreatedAtAsc(AppConstants.PENDING)
                .stream()
                .map(item -> new CashAccountRequestDto(item.id(), item.customerId(), item.accountType(), item.initialDeposit(), item.status()))
                .toList();
    }

    @Override
    @Transactional
    public CreateAccountResponse approveCashRequest(Long requestId, String reviewerUsername) {
        CashAccountRequest request = cashAccountRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, AppConstants.CASH_REQUEST_NOT_FOUND));
        if (!AppConstants.PENDING.equals(request.status())) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE, AppConstants.CASH_REQUEST_NOT_PENDING);
        }
        if (accountRepository.existsByCustomerId(request.customerId())) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, AppConstants.ACCOUNT_ALREADY_EXISTS_CUSTOMER);
        }

        Account account = Account.newActive(request.customerId(), accountSupport.generateAccountNumber(), request.accountType(), request.initialDeposit());
        Account saved = accountRepository.save(account);
        accountSupport.linkDefaultBranch(saved.id());
        accountOnboardingUpdater.updateAfterAccountCreated(request.customerId());
        cashAccountRequestRepository.save(request.withReview(AppConstants.APPROVED, reviewerUsername));
        log.info("Cash request approved requestId={} customerId={} accountNumber={} reviewer={}", requestId, request.customerId(), saved.accountNumber(), reviewerUsername);
        return new CreateAccountResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_CASH_APPROVED, saved.accountNumber(), saved.accountType(), saved.balance());
    }

    @Override
    @Transactional
    public OtpVerifyResponse denyCashRequest(Long requestId, String reviewerUsername) {
        CashAccountRequest request = cashAccountRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, AppConstants.CASH_REQUEST_NOT_FOUND));
        if (!AppConstants.PENDING.equals(request.status())) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE, AppConstants.CASH_REQUEST_NOT_PENDING);
        }
        cashAccountRequestRepository.save(request.withReview(AppConstants.DENIED, reviewerUsername));
        return new OtpVerifyResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_CASH_DENIED);
    }
}
