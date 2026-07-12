package com.bank.central.account.service;

import com.bank.central.account.repository.AccountBranchRepository;
import com.bank.central.branch.repository.BranchRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class AccountSupport {

    private final BranchRepository branchRepository;
    private final AccountBranchRepository accountBranchRepository;

    public AccountSupport(BranchRepository branchRepository, AccountBranchRepository accountBranchRepository) {
        this.branchRepository = branchRepository;
        this.accountBranchRepository = accountBranchRepository;
    }

    public String generateAccountNumber() {
        return "AC" + ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L);
    }

    public void linkDefaultBranch(Long accountId) {
        branchRepository.findFirstByOrderByIdAsc().ifPresent(branch -> accountBranchRepository.linkAccountToBranch(accountId, branch.id()));
    }

    public String resolveIfsc(Long accountId) {
        Optional<String> linked = accountBranchRepository.findIfscByAccountId(accountId);
        if (linked.isPresent()) {
            return linked.get();
        }
        return branchRepository.findFirstByOrderByIdAsc().map(branch -> {
            accountBranchRepository.linkAccountToBranch(accountId, branch.id());
            return branch.ifscCode();
        }).orElse(null);
    }
}
