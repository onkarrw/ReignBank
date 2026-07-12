package com.bank.central.admin.repository;

import com.bank.central.admin.domain.AdminCustomerAudit;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AdminCustomerAuditRepository extends CrudRepository<AdminCustomerAudit, Long> {

    @Query("""
            SELECT * FROM admin_customer_audit
            WHERE customer_id = :customerId
            ORDER BY created_at DESC
            LIMIT 20
            """)
    List<AdminCustomerAudit> findRecentByCustomerId(Long customerId);
}
