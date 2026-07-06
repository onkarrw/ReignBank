package com.bank.central.auth.security;

import com.bank.central.common.exception.BusinessException;
import com.bank.central.auth.domain.UserIdentity;
import com.bank.central.auth.service.UserIdentityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserIdentityService userIdentityService;

    public JwtAuthFilter(JwtTokenService jwtTokenService, UserIdentityService userIdentityService) {
        this.jwtTokenService = jwtTokenService;
        this.userIdentityService = userIdentityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        authenticateFromBearer(request);
        if (!hasApplicationAuthentication()) {
            authenticateFromSession(request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateFromBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }
        try {
            String token = header.substring(7).trim();
            if (token.isEmpty()) {
                return;
            }
            Map<String, String> parsed = jwtTokenService.parseToken(token);
            HttpSession session = request.getSession(true);
            session.setAttribute("AUTH_USERNAME", parsed.get("username"));
            setAuthFromUsername(parsed.get("username"), session.getId());
        } catch (BusinessException ignored) {
        }
    }

    private void authenticateFromSession(HttpServletRequest request) {
        Object sessionUsername = request.getSession(false) != null
                ? request.getSession(false).getAttribute("AUTH_USERNAME")
                : null;
        if (sessionUsername instanceof String username) {
            String initiatorId = request.getSession(true).getId();
            setAuthFromUsername(username, initiatorId);
        }
    }

    private boolean hasApplicationAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getPrincipal() instanceof String username
                && !username.isBlank();
    }

    private void setAuthFromUsername(String username, String initiatorId) {
        if (username == null || username.isBlank()) {
            return;
        }
        UserIdentity identity = userIdentityService.getCustomerFromUsernameEmailOrPhone(username);
        if (identity == null) {
            return;
        }
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(identity.username(), null, List.of(new SimpleGrantedAuthority("ROLE_" + identity.role())));
        auth.setDetails(initiatorId);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
