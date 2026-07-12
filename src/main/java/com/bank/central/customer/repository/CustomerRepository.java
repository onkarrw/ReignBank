package com.bank.central.customer.repository;

import com.bank.central.customer.domain.Customer;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);
    long countByPhone(String phone);

    @Query("""
            SELECT * FROM customer
            WHERE LOWER(email) LIKE LOWER(:term)
               OR phone LIKE :term
               OR LOWER(first_name) LIKE LOWER(:term)
               OR LOWER(last_name) LIKE LOWER(:term)
            ORDER BY id
            LIMIT 20
            """)
    List<Customer> searchByTerm(String term);
}