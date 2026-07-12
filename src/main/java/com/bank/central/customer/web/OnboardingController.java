package com.bank.central.customer.web;

import com.bank.central.auth.service.AuthService;
import com.bank.central.customer.dto.CustomerOnboardingRequest;
import com.bank.central.customer.dto.CustomerOnboardingResponse;
import com.bank.central.customer.dto.KycMobileOtpVerifyRequest;
import com.bank.central.otp.dto.OtpVerifyRequest;
import com.bank.central.otp.dto.OtpVerifyResponse;
import com.bank.central.customer.dto.KycMobileOtpResponse;
import com.bank.central.customer.service.OnboardingService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class OnboardingController {

    private final OnboardingService service;
    private final AuthService authService;
    private static final Logger log = LoggerFactory.getLogger(OnboardingController.class);

    public OnboardingController(OnboardingService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @PostMapping("/onboard")
    public CustomerOnboardingResponse onboard(@RequestBody CustomerOnboardingRequest request, HttpServletRequest servletRequest) {
        String initiatorId = servletRequest.getSession(true).getId();
        log.info("Received onboarding request for email={}", request.email());
        log.debug("Onboarding payload firstName={}, lastName={}, phone={}", request.firstName(), request.lastName(), request.phone());
        return service.onboard(request, initiatorId);
    }

    @PostMapping("/verify-otp")
    public OtpVerifyResponse verifyOtp(@RequestBody OtpVerifyRequest request, HttpServletRequest servletRequest) {
        String initiatorId = servletRequest.getSession(true).getId();
        log.info("Received OTP verification request for email={}, requestId={}", request.email(), request.otpRequestId());
        return service.verifyOtp(request, initiatorId);
    }

    @PostMapping("/kyc/mobile-otp/request")
    public KycMobileOtpResponse requestMobileKycOtp() {
        Long customerId = authService.getCurrentCustomerId();
        String initiatorId = authService.getCurrentInitiatorId();
        log.info("Received mobile KYC OTP request for customerId={}", customerId);
        return service.requestMobileKycOtp(customerId, initiatorId);
    }

    @PostMapping("/kyc/mobile-otp/verify")
    public OtpVerifyResponse verifyMobileKycOtp(@RequestBody KycMobileOtpVerifyRequest request) {
        Long customerId = authService.getCurrentCustomerId();
        String initiatorId = authService.getCurrentInitiatorId();
        log.info("Received mobile KYC OTP verify request for customerId={}, requestId={}", customerId, request.otpRequestId());
        return service.verifyMobileKycOtp(customerId, request, initiatorId);
    }
}
