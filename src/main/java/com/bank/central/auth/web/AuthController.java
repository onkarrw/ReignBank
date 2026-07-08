package com.bank.central.auth.web;

import com.bank.central.auth.dto.AuthResponse;
import com.bank.central.auth.dto.LoginRequest;
import com.bank.central.auth.dto.LoginResponse;
import com.bank.central.auth.dto.SetPasswordRequest;
import com.bank.central.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/set-password")
    public AuthResponse setPassword(@RequestBody SetPasswordRequest request) {
        return authService.setPassword(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(request, servletRequest);
    }

    @PostMapping("/logout")
    public AuthResponse logout(HttpServletRequest servletRequest) {
        return authService.logout(servletRequest);
    }
}
