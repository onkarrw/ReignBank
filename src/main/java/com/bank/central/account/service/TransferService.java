package com.bank.central.account.service;

import com.bank.central.account.dto.SendMoneyRequest;
import com.bank.central.account.dto.SendMoneyResponse;
import com.bank.central.account.dto.AccountCreationOtpResponse;
import com.bank.central.account.dto.AccountCreationOtpVerifyRequest;
import com.bank.central.otp.dto.OtpVerifyResponse;

public interface TransferService {

    SendMoneyResponse sendMoney(Long customerId, SendMoneyRequest request);

    AccountCreationOtpResponse requestTransferOtp(Long customerId, String initiatorId);

    OtpVerifyResponse verifyTransferOtp(Long customerId, String initiatorId, AccountCreationOtpVerifyRequest request);
}
