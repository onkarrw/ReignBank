package com.bank.central.customer.service;

import com.bank.central.customer.dto.CustomerOnboardingRequest;
import com.bank.central.customer.dto.CustomerOnboardingResponse;
import com.bank.central.customer.dto.KycMobileOtpResponse;
import com.bank.central.customer.dto.KycMobileOtpVerifyRequest;
import com.bank.central.otp.dto.OtpVerifyRequest;
import com.bank.central.otp.dto.OtpVerifyResponse;

public interface OnboardingService {

    CustomerOnboardingResponse onboard(CustomerOnboardingRequest request, String initiatorId);

    OtpVerifyResponse verifyOtp(OtpVerifyRequest request, String initiatorId);

    KycMobileOtpResponse requestMobileKycOtp(Long customerId, String initiatorId);

    OtpVerifyResponse verifyMobileKycOtp(Long customerId, KycMobileOtpVerifyRequest request, String initiatorId);
}
