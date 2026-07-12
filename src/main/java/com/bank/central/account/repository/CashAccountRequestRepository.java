package com.bank.central.account.repository;

import com.bank.central.account.domain.CashAccountRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CashAccountRequestRepository extends CrudRepository<CashAccountRequest, Long> {

    List<CashAccountRequest> findAllByStatusOrderByCreatedAtAsc(String status);
    Optional<CashAccountRequest> findByCustomerIdAndStatus(Long customerId, String status);
}
