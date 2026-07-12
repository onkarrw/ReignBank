package com.bank.central.customer.repository;

import com.bank.central.customer.domain.CustomerOnboarding;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerOnboardingRepository extends CrudRepository<CustomerOnboarding, Long> {

    Optional<CustomerOnboarding> findByCustomerId(Long customerId);
}
