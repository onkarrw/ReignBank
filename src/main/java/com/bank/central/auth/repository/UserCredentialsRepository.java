package com.bank.central.auth.repository;

import com.bank.central.auth.domain.UserCredentials;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserCredentialsRepository extends CrudRepository<UserCredentials, Long> {

    Optional<UserCredentials> findByUsername(String username);
    Optional<UserCredentials> findByCustomerId(Long customerId);
}
