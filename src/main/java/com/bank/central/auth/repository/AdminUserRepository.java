package com.bank.central.auth.repository;

import com.bank.central.auth.domain.AdminUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdminUserRepository extends CrudRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);
    Optional<AdminUser> findByUsernameAndStatus(String username, String status);
}
