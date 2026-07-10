package com.bank.central.account.service;

import com.bank.central.account.dto.CashAccountRequestDto;
import com.bank.central.account.dto.CreateAccountResponse;
import com.bank.central.otp.dto.OtpVerifyResponse;

import java.util.List;

public interface CashRequestService {

    List<CashAccountRequestDto> getPendingCashRequests();

    CreateAccountResponse approveCashRequest(Long requestId, String reviewerUsername);

    OtpVerifyResponse denyCashRequest(Long requestId, String reviewerUsername);
}
