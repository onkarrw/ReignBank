package com.bank.central.transaction.repository;

import com.bank.central.transaction.domain.BankTransaction;
import org.springframework.data.repository.CrudRepository;

public interface BankTransactionRepository extends CrudRepository<BankTransaction, Long> {
}
