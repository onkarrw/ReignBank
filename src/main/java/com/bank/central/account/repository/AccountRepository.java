package com.bank.central.account.repository;

import com.bank.central.account.domain.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {

    boolean existsByCustomerId(Long customerId);
    Optional<Account> findFirstByCustomerIdOrderByCreatedAtAsc(Long customerId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
