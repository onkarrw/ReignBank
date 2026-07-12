package com.bank.central.account.service;

import com.bank.central.account.dto.AccountCreationOtpResponse;
import com.bank.central.account.dto.AccountCreationOtpVerifyRequest;
import com.bank.central.account.dto.AccountSummaryDto;
import com.bank.central.account.dto.CreateAccountRequest;
import com.bank.central.account.dto.CreateAccountResponse;
import com.bank.central.otp.dto.OtpVerifyResponse;

public interface AccountCreationService {

    AccountSummaryDto getAccountSummary(Long customerId);

    CreateAccountResponse createAccount(Long customerId, CreateAccountRequest request);

    AccountCreationOtpResponse requestAccountCreationOtp(Long customerId, String initiatorId);

    OtpVerifyResponse verifyAccountCreationOtp(Long customerId, String initiatorId, AccountCreationOtpVerifyRequest request);
}
