package com.bank.central.branch.repository;

import com.bank.central.branch.domain.Branch;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BranchRepository extends CrudRepository<Branch, Long> {

    Optional<Branch> findFirstByOrderByIdAsc();
}
