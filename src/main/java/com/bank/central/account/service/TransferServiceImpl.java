package com.bank.central.account.service;

import com.bank.central.common.constants.AppConstants;
import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.account.domain.Account;
import com.bank.central.account.dto.AccountCreationOtpResponse;
import com.bank.central.account.dto.AccountCreationOtpVerifyRequest;
import com.bank.central.account.dto.SendMoneyRequest;
import com.bank.central.account.dto.SendMoneyResponse;
import com.bank.central.account.repository.AccountRepository;
import com.bank.central.customer.domain.Customer;
import com.bank.central.customer.repository.CustomerRepository;
import com.bank.central.otp.domain.OtpPurpose;
import com.bank.central.otp.dto.OtpVerifyResponse;
import com.bank.central.otp.service.OtpService;
import com.bank.central.transaction.domain.BankTransaction;
import com.bank.central.transaction.repository.BankTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final OtpService otpService;
    private final BankTransactionRepository bankTransactionRepository;

    public TransferServiceImpl(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            OtpService otpService,
            BankTransactionRepository bankTransactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.otpService = otpService;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    @Override
    public SendMoneyResponse sendMoney(Long customerId, SendMoneyRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER, AppConstants.INVALID_TRANSFER_AMOUNT);
        }
        if (request.toAccountNumber() == null || request.toAccountNumber().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER, AppConstants.INVALID_TRANSFER_RECIPIENT_REQUIRED);
        }
        Account fromAccount = accountRepository.findFirstByCustomerIdOrderByCreatedAtAsc(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (!AppConstants.ACTIVE.equals(fromAccount.status())) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        }
        Account toAccount = accountRepository.findByAccountNumber(request.toAccountNumber().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND, AppConstants.ACCOUNT_NOT_FOUND_RECIPIENT));
        if (fromAccount.id().equals(toAccount.id())) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER, AppConstants.INVALID_TRANSFER_SAME_ACCOUNT);
        }
        if (!AppConstants.ACTIVE.equals(toAccount.status())) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE, AppConstants.ACCOUNT_INACTIVE_RECIPIENT);
        }
        if (fromAccount.balance().compareTo(request.amount()) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        Account updatedFrom = fromAccount.withBalance(fromAccount.balance().subtract(request.amount()));
        Account updatedTo = toAccount.withBalance(toAccount.balance().add(request.amount()));
        accountRepository.save(updatedFrom);
        accountRepository.save(updatedTo);

        String referenceId = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        bankTransactionRepository.save(BankTransaction.transfer(
                fromAccount.id(),
                toAccount.id(),
                request.amount(),
                fromAccount.currency(),
                referenceId,
                request.description()
        ));

        log.info("Transfer completed customerId={} fromAccount={} toAccount={} amount={} referenceId={}",
                customerId, fromAccount.accountNumber(), toAccount.accountNumber(), request.amount(), referenceId);
        return new SendMoneyResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_TRANSFER_SUCCESS, referenceId, updatedFrom.balance());
    }

    @Override
    public AccountCreationOtpResponse requestTransferOtp(Long customerId, String initiatorId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        UUID otpRequestId = otpService.generateAndSendTransferEmailOtp(customerId, customer.email(), initiatorId);
        return new AccountCreationOtpResponse(otpRequestId, AppConstants.OTP_SENT, AppConstants.RESPONSE_TRANSFER_OTP_SENT);
    }

    @Override
    public OtpVerifyResponse verifyTransferOtp(Long customerId, String initiatorId, AccountCreationOtpVerifyRequest request) {
        otpService.verify(request.otpRequestId(), customerId, request.otp(), OtpPurpose.SEND_MONEY_EMAIL, initiatorId);
        return new OtpVerifyResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_TRANSFER_OTP_VERIFIED);
    }
}
