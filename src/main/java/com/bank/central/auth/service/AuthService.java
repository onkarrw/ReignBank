package com.bank.central.auth.service;

import com.bank.central.auth.dto.AuthResponse;
import com.bank.central.auth.dto.LoginRequest;
import com.bank.central.auth.dto.LoginResponse;
import com.bank.central.auth.dto.SetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse setPassword(SetPasswordRequest request);

    LoginResponse login(LoginRequest request, HttpServletRequest servletRequest);

    AuthResponse logout(HttpServletRequest servletRequest);

    Long getCurrentCustomerId();

    String getCurrentInitiatorId();

    String getCurrentUsername();
}
