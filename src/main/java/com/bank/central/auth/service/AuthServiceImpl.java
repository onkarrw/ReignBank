package com.bank.central.auth.service;

import com.bank.central.common.constants.AppConstants;
import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.auth.repository.AdminUserRepository;
import com.bank.central.auth.domain.UserCredentials;
import com.bank.central.auth.repository.UserCredentialsRepository;
import com.bank.central.auth.dto.AuthResponse;
import com.bank.central.auth.dto.LoginRequest;
import com.bank.central.auth.dto.LoginResponse;
import com.bank.central.auth.dto.SetPasswordRequest;
import com.bank.central.auth.security.JwtTokenService;
import com.bank.central.customer.domain.state.OnboardingState;
import com.bank.central.customer.domain.CustomerStatus;
import com.bank.central.customer.repository.CustomerOnboardingRepository;
import com.bank.central.customer.repository.CustomerRepository;
import com.bank.central.auth.domain.AdminUser;
import com.bank.central.auth.domain.UserIdentity;
import com.bank.central.customer.domain.Customer;
import com.bank.central.customer.domain.CustomerOnboarding;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserCredentialsRepository userCredentialsRepository;
    private final AdminUserRepository adminUserRepository;
    private final CustomerRepository customerRepository;
    private final CustomerOnboardingRepository customerOnboardingRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserIdentityService userIdentityService;

    public AuthServiceImpl(
            UserCredentialsRepository userCredentialsRepository,
            AdminUserRepository adminUserRepository,
            CustomerRepository customerRepository,
            CustomerOnboardingRepository customerOnboardingRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            UserIdentityService userIdentityService
    ) {
        this.userCredentialsRepository = userCredentialsRepository;
        this.adminUserRepository = adminUserRepository;
        this.customerRepository = customerRepository;
        this.customerOnboardingRepository = customerOnboardingRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.userIdentityService = userIdentityService;
    }

    @Transactional
    public AuthResponse setPassword(SetPasswordRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new BusinessException(ErrorCode.CUSTOMER_ONBOARDING_EMAIL_REQUIRED);
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, AppConstants.AUTH_USERNAME_REQUIRED);
        }
        if (request.password() == null || request.password().length() < 8) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, AppConstants.AUTH_PASSWORD_TOO_SHORT);
        }

        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        CustomerOnboarding onboarding = customerOnboardingRepository.findByCustomerId(customer.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.ONBOARDING_NOT_FOUND));

        if (onboarding.state() == OnboardingState.EMAIL_PENDING || onboarding.state() == OnboardingState.REJECTED) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_NOT_ALLOWED);
        }
        if (userCredentialsRepository.findByCustomerId(customer.id()).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_ALREADY_SET);
        }
        if (userCredentialsRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, AppConstants.AUTH_USERNAME_EXISTS);
        }
        if (adminUserRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS, AppConstants.AUTH_USERNAME_EXISTS);
        }

        UserCredentials credentials = UserCredentials.createNew(
                customer.id(),
                request.username(),
                passwordEncoder.encode(request.password())
        );
        userCredentialsRepository.save(credentials);
        if (!CustomerStatus.ACTIVE.name().equals(customer.status())) {
            customerRepository.save(customer.withStatus(CustomerStatus.ACTIVE.name()));
        }
        userIdentityService.evictIdentity(request.email());
        userIdentityService.evictIdentity(request.username());
        userIdentityService.evictIdentity(customer.phone());

        return new AuthResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_AUTH_PASSWORD_SET);
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        if (request.username() == null || request.username().isBlank() || request.password() == null || request.password().isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        AdminUser adminUser = adminUserRepository.findByUsernameAndStatus(request.username(), AppConstants.ACTIVE).orElse(null);
        if (adminUser != null && passwordEncoder.matches(request.password(), adminUser.passwordHash())) {
            String token = jwtTokenService.generateToken(adminUser.username(), adminUser.role());
            HttpSession session = servletRequest.getSession(true);
            session.setAttribute("AUTH_USERNAME", adminUser.username());
            log.info("Staff login username={} role={}", adminUser.username(), adminUser.role());
            return new LoginResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_AUTH_LOGIN_SUCCESS, token, adminUser.role());
        }

        UserCredentials customerCredentials = userCredentialsRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), customerCredentials.passwordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        Customer customer = customerRepository.findById(customerCredentials.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        if (!CustomerStatus.ACTIVE.name().equals(customer.status())) {
            if (CustomerStatus.PENDING_VERIFICATION.name().equals(customer.status())) {
                customer = customerRepository.save(customer.withStatus(CustomerStatus.ACTIVE.name()));
            } else {
                log.warn("Login blocked inactive customerId={} username={}", customer.id(), customerCredentials.username());
                throw new BusinessException(ErrorCode.CUSTOMER_INACTIVE);
            }
        }
        String token = jwtTokenService.generateToken(customerCredentials.username(), customerCredentials.role());
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute("AUTH_USERNAME", customerCredentials.username());

        log.info("Customer login customerId={} username={}", customer.id(), customerCredentials.username());
        return new LoginResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_AUTH_LOGIN_SUCCESS, token, customerCredentials.role());
    }

    public AuthResponse logout(HttpServletRequest servletRequest) {
        if (servletRequest.getSession(false) != null) {
            servletRequest.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return new AuthResponse(AppConstants.SUCCESS, AppConstants.RESPONSE_AUTH_LOGOUT_SUCCESS);
    }

    public Long getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String username) || username.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        UserIdentity identity = userIdentityService.getCustomerFromUsernameEmailOrPhone(username);
        if (identity == null || identity.customerId() == null) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        Customer customer = customerRepository.findById(identity.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        if (!CustomerStatus.ACTIVE.name().equals(customer.status())) {
            throw new BusinessException(ErrorCode.CUSTOMER_INACTIVE);
        }
        return identity.customerId();
    }

    public String getCurrentInitiatorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof String initiatorId) || initiatorId.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        return initiatorId;
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String username) || username.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        return username;
    }
}
