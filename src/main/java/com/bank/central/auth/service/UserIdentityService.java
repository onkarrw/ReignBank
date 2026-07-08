package com.bank.central.auth.service;

import com.bank.central.auth.repository.AdminUserRepository;
import com.bank.central.auth.repository.UserCredentialsRepository;
import com.bank.central.auth.domain.AdminUser;
import com.bank.central.auth.domain.UserCredentials;
import com.bank.central.auth.domain.UserIdentity;
import com.bank.central.common.constants.AppConstants;
import com.bank.central.customer.domain.Customer;
import com.bank.central.customer.repository.CustomerRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserIdentityService {

    private final AdminUserRepository adminUserRepository;
    private final UserCredentialsRepository userCredentialsRepository;
    private final CustomerRepository customerRepository;
    private final CacheManager cacheManager;

    public UserIdentityService(
            AdminUserRepository adminUserRepository,
            UserCredentialsRepository userCredentialsRepository,
            CustomerRepository customerRepository,
            CacheManager cacheManager
    ) {
        this.adminUserRepository = adminUserRepository;
        this.userCredentialsRepository = userCredentialsRepository;
        this.customerRepository = customerRepository;
        this.cacheManager = cacheManager;
    }

    @Cacheable(cacheNames = "userIdentity", key = "#identifier.toLowerCase()", unless = "#result == null")
    public UserIdentity getCustomerFromUsernameEmailOrPhone(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        String value = identifier.trim();
        if (value.contains("@")) {
            return fromEmail(value);
        }
        if (value.matches("\\d{10,15}")) {
            return fromPhone(value);
        }
        return fromUsername(value);
    }

    @CacheEvict(cacheNames = "userIdentity", key = "#identifier.toLowerCase()")
    public void evictIdentity(String identifier) {
    }

    public void evictAllKeys(UserIdentity identity) {
        if (identity == null) {
            return;
        }
        Cache cache = cacheManager.getCache("userIdentity");
        if (cache == null) {
            return;
        }
        if (identity.username() != null) {
            cache.evict(identity.username().toLowerCase());
        }
        if (identity.email() != null) {
            cache.evict(identity.email().toLowerCase());
        }
        if (identity.phone() != null) {
            cache.evict(identity.phone());
        }
    }

    private UserIdentity fromUsername(String username) {
        AdminUser admin = adminUserRepository.findByUsernameAndStatus(username, AppConstants.ACTIVE).orElse(null);
        if (admin != null) {
            return new UserIdentity(admin.username(), admin.role(), null, null, null, AppConstants.STAFF);
        }
        UserCredentials creds = userCredentialsRepository.findByUsername(username).orElse(null);
        if (creds == null) {
            return null;
        }
        Customer customer = customerRepository.findById(creds.customerId()).orElse(null);
        if (customer == null) {
            return null;
        }
        return new UserIdentity(creds.username(), creds.role(), customer.id(), customer.email(), customer.phone(), AppConstants.CUSTOMER);
    }

    private UserIdentity fromEmail(String email) {
        Customer customer = customerRepository.findByEmail(email).orElse(null);
        if (customer == null) {
            return null;
        }
        return fromCustomer(customer);
    }

    private UserIdentity fromPhone(String phone) {
        Customer customer = customerRepository.findByPhone(phone).orElse(null);
        if (customer == null) {
            return null;
        }
        return fromCustomer(customer);
    }

    private UserIdentity fromCustomer(Customer customer) {
        UserCredentials creds = userCredentialsRepository.findByCustomerId(customer.id()).orElse(null);
        if (creds == null) {
            return null;
        }
        return new UserIdentity(creds.username(), creds.role(), customer.id(), customer.email(), customer.phone(), AppConstants.CUSTOMER);
    }
}
