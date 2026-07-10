package com.bank.central.account.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountBranchRepository {

    private final JdbcClient jdbc;

    public AccountBranchRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<String> findIfscByAccountId(Long accountId) {
        return jdbc.sql("""
                SELECT b.ifsc_code FROM branch b
                INNER JOIN account_branch ab ON ab.branch_id = b.id
                WHERE ab.account_id = ?
                LIMIT 1
                """)
                .param(accountId)
                .query(String.class)
                .optional();
    }

    public void linkAccountToBranch(Long accountId, Long branchId) {
        jdbc.sql("""
                INSERT INTO account_branch (account_id, branch_id)
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """)
                .param(accountId)
                .param(branchId)
                .update();
    }
}
