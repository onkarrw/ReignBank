package com.bank.central.account.web;

import com.bank.central.account.dto.AccountCreationOtpResponse;
import com.bank.central.account.dto.AccountCreationOtpVerifyRequest;
import com.bank.central.account.dto.AccountSummaryDto;
import com.bank.central.account.dto.CashAccountRequestDto;
import com.bank.central.account.dto.CreateAccountRequest;
import com.bank.central.account.dto.CreateAccountResponse;
import com.bank.central.account.dto.SendMoneyRequest;
import com.bank.central.account.dto.SendMoneyResponse;
import com.bank.central.account.service.AccountCreationService;
import com.bank.central.account.service.CashRequestService;
import com.bank.central.account.service.OtpSessionGate;
import com.bank.central.account.service.TransferService;
import com.bank.central.auth.service.AuthService;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.otp.dto.OtpVerifyResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final String SEND_MONEY_OTP_KEY = "SEND_MONEY_OTP_VERIFIED";
    private static final String ACCOUNT_CREATE_OTP_KEY = "ACCOUNT_CREATE_OTP_VERIFIED";

    private final AccountCreationService accountCreationService;
    private final TransferService transferService;
    private final CashRequestService cashRequestService;
    private final AuthService authService;
    private final OtpSessionGate otpSessionGate;

    public AccountController(
            AccountCreationService accountCreationService,
            TransferService transferService,
            CashRequestService cashRequestService,
            AuthService authService,
            OtpSessionGate otpSessionGate
    ) {
        this.accountCreationService = accountCreationService;
        this.transferService = transferService;
        this.cashRequestService = cashRequestService;
        this.authService = authService;
        this.otpSessionGate = otpSessionGate;
    }

    @GetMapping("/me")
    public AccountSummaryDto getMyAccount() {
        return accountCreationService.getAccountSummary(authService.getCurrentCustomerId());
    }

    @PostMapping("/transfer")
    public SendMoneyResponse sendMoney(@RequestBody SendMoneyRequest request, HttpServletRequest servletRequest) {
        otpSessionGate.requireVerified(servletRequest, SEND_MONEY_OTP_KEY, ErrorCode.SEND_MONEY_OTP_NOT_VERIFIED);
        SendMoneyResponse response = transferService.sendMoney(authService.getCurrentCustomerId(), request);
        otpSessionGate.clearVerified(servletRequest, SEND_MONEY_OTP_KEY);
        return response;
    }

    @PostMapping("/transfer-otp/request")
    public AccountCreationOtpResponse requestTransferOtp() {
        return transferService.requestTransferOtp(authService.getCurrentCustomerId(), authService.getCurrentInitiatorId());
    }

    @PostMapping("/transfer-otp/verify")
    public OtpVerifyResponse verifyTransferOtp(@RequestBody AccountCreationOtpVerifyRequest request, HttpServletRequest servletRequest) {
        OtpVerifyResponse response = transferService.verifyTransferOtp(authService.getCurrentCustomerId(), authService.getCurrentInitiatorId(), request);
        otpSessionGate.markVerified(servletRequest, SEND_MONEY_OTP_KEY);
        return response;
    }

    @PostMapping("/create-otp/request")
    public AccountCreationOtpResponse requestCreateAccountOtp() {
        return accountCreationService.requestAccountCreationOtp(authService.getCurrentCustomerId(), authService.getCurrentInitiatorId());
    }

    @PostMapping("/create-otp/verify")
    public OtpVerifyResponse verifyCreateAccountOtp(@RequestBody AccountCreationOtpVerifyRequest request, HttpServletRequest servletRequest) {
        OtpVerifyResponse response = accountCreationService.verifyAccountCreationOtp(authService.getCurrentCustomerId(), authService.getCurrentInitiatorId(), request);
        otpSessionGate.markVerified(servletRequest, ACCOUNT_CREATE_OTP_KEY);
        return response;
    }

    @PostMapping("/create")
    public CreateAccountResponse createAccount(@RequestBody CreateAccountRequest request, HttpServletRequest servletRequest) {
        otpSessionGate.requireVerified(servletRequest, ACCOUNT_CREATE_OTP_KEY, ErrorCode.ACCOUNT_OTP_NOT_VERIFIED);
        CreateAccountResponse response = accountCreationService.createAccount(authService.getCurrentCustomerId(), request);
        otpSessionGate.clearVerified(servletRequest, ACCOUNT_CREATE_OTP_KEY);
        return response;
    }

    @GetMapping("/cash-requests/pending")
    public List<CashAccountRequestDto> getPendingCashRequests() {
        return cashRequestService.getPendingCashRequests();
    }

    @PostMapping("/cash-requests/{requestId}/approve")
    public CreateAccountResponse approveCashRequest(@PathVariable Long requestId) {
        return cashRequestService.approveCashRequest(requestId, authService.getCurrentUsername());
    }

    @PostMapping("/cash-requests/{requestId}/deny")
    public OtpVerifyResponse denyCashRequest(@PathVariable Long requestId) {
        return cashRequestService.denyCashRequest(requestId, authService.getCurrentUsername());
    }
}
