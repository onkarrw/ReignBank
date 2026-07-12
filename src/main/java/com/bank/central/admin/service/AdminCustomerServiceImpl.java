package com.bank.central.admin.service;

import com.bank.central.common.constants.AppConstants;
import com.bank.central.common.exception.BusinessException;
import com.bank.central.common.exception.ErrorCode;
import com.bank.central.account.domain.Account;
import com.bank.central.account.repository.AccountRepository;
import com.bank.central.admin.domain.AdminCustomerAudit;
import com.bank.central.admin.repository.AdminCustomerAuditRepository;
import com.bank.central.auth.domain.UserCredentials;
import com.bank.central.auth.repository.UserCredentialsRepository;
import com.bank.central.auth.domain.UserIdentity;
import com.bank.central.auth.service.UserIdentityService;
import com.bank.central.transaction.domain.BankTransaction;
import com.bank.central.transaction.repository.BankTransactionRepository;
import com.bank.central.customer.domain.Customer;
import com.bank.central.customer.domain.CustomerStatus;
import com.bank.central.admin.dto.AdminAuditDto;
import com.bank.central.admin.dto.AdminCashAdjustmentRequest;
import com.bank.central.admin.dto.AdminCashAdjustmentResponse;
import com.bank.central.admin.dto.CustomerAdminDto;
import com.bank.central.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private static final Logger log = LoggerFactory.getLogger(AdminCustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final UserCredentialsRepository userCredentialsRepository;
    private final UserIdentityService userIdentityService;
    private final AdminCustomerAuditRepository adminCustomerAuditRepository;
    private final BankTransactionRepository bankTransactionRepository;

    public AdminCustomerServiceImpl(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            UserCredentialsRepository userCredentialsRepository,
            UserIdentityService userIdentityService,
            AdminCustomerAuditRepository adminCustomerAuditRepository,
            BankTransactionRepository bankTransactionRepository
    ) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.userCredentialsRepository = userCredentialsRepository;
        this.userIdentityService = userIdentityService;
        this.adminCustomerAuditRepository = adminCustomerAuditRepository;
        this.bankTransactionRepository = bankTransactionRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerAdminDto> searchCustomers(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String term = query.trim();
        LinkedHashMap<Long, Customer> results = new LinkedHashMap<Long, Customer>();
        if (term.matches("\\d+")) {
            customerRepository.findById(Long.parseLong(term)).ifPresent(customer -> results.put(customer.id(), customer));
        }
        for (Customer customer : customerRepository.searchByTerm("%" + term + "%")) {
            results.putIfAbsent(customer.id(), customer);
        }
        log.info("Admin customer search term={} results={}", term, results.size());
        return results.values().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminAuditDto> getCustomerAudit(Long customerId) {
        customerRepository.findById(customerId).orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        return adminCustomerAuditRepository.findRecentByCustomerId(customerId).stream().map(this::toAuditDto).toList();
    }

    @Transactional
    public CustomerAdminDto activateCustomer(Long customerId, String adminUsername) {
        return updateStatus(customerId, CustomerStatus.ACTIVE.name(), adminUsername, AdminCustomerAudit::activate);
    }

    @Transactional
    public CustomerAdminDto deactivateCustomer(Long customerId, String adminUsername) {
        return updateStatus(customerId, CustomerStatus.INACTIVE.name(), adminUsername, AdminCustomerAudit::deactivate);
    }

    @Transactional
    public AdminCashAdjustmentResponse addCash(Long customerId, String adminUsername, AdminCashAdjustmentRequest request) {
        return adjustCash(customerId, adminUsername, request, true);
    }

    @Transactional
    public AdminCashAdjustmentResponse removeCash(Long customerId, String adminUsername, AdminCashAdjustmentRequest request) {
        return adjustCash(customerId, adminUsername, request, false);
    }

    private CustomerAdminDto updateStatus(Long customerId, String status, String adminUsername, AuditFactory auditFactory) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        if (status.equals(customer.status())) {
            return toDto(customer);
        }
        Customer updated = customerRepository.save(customer.withStatus(status));
        AdminCustomerAudit audit = adminCustomerAuditRepository.save(auditFactory.create(adminUsername, customerId, "Status changed to " + status));
        evictIdentity(updated);
        log.info("Admin {} set customerId={} status={} auditId={}", adminUsername, customerId, status, audit.id());
        return toDto(updated);
    }

    private AdminCashAdjustmentResponse adjustCash(Long customerId, String adminUsername, AdminCashAdjustmentRequest request, boolean add) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER, AppConstants.INVALID_TRANSFER_AMOUNT);
        }
        customerRepository.findById(customerId).orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        Account account = accountRepository.findFirstByCustomerIdOrderByCreatedAtAsc(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (!AppConstants.ACTIVE.equals(account.status())) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        }
        BigDecimal balanceBefore = account.balance();
        if (!add && balanceBefore.compareTo(request.amount()) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_FUNDS);
        }
        BigDecimal balanceAfter = add ? balanceBefore.add(request.amount()) : balanceBefore.subtract(request.amount());
        accountRepository.save(account.withBalance(balanceAfter));
        String referenceId = "ADM" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String note = request.note() == null ? "" : request.note().trim();
        BankTransaction txn = add
                ? BankTransaction.adminDeposit(account.id(), request.amount(), account.currency(), referenceId, note)
                : BankTransaction.adminWithdraw(account.id(), request.amount(), account.currency(), referenceId, note);
        bankTransactionRepository.save(txn);
        AdminCustomerAudit audit = adminCustomerAuditRepository.save(add
                ? AdminCustomerAudit.cashAdd(adminUsername, customerId, account.id(), request.amount(), balanceBefore, balanceAfter, note)
                : AdminCustomerAudit.cashRemove(adminUsername, customerId, account.id(), request.amount(), balanceBefore, balanceAfter, note));
        log.info("Admin {} {} customerId={} accountId={} amount={} balanceBefore={} balanceAfter={} auditId={}",
                adminUsername, add ? "credited" : "debited", customerId, account.id(), request.amount(), balanceBefore, balanceAfter, audit.id());
        return new AdminCashAdjustmentResponse(AppConstants.SUCCESS, add ? AppConstants.RESPONSE_ADMIN_CASH_ADDED : AppConstants.RESPONSE_ADMIN_CASH_REMOVED, balanceAfter, audit.id());
    }

    private void evictIdentity(Customer customer) {
        UserCredentials creds = userCredentialsRepository.findByCustomerId(customer.id()).orElse(null);
        String username = creds != null ? creds.username() : customer.email();
        String role = creds != null ? creds.role() : "USER";
        userIdentityService.evictAllKeys(new UserIdentity(username, role, customer.id(), customer.email(), customer.phone(), AppConstants.CUSTOMER));
    }

    private CustomerAdminDto toDto(Customer customer) {
        return new CustomerAdminDto(customer.id(), customer.firstName(), customer.lastName(), customer.email(), customer.phone(), customer.status());
    }

    private AdminAuditDto toAuditDto(AdminCustomerAudit audit) {
        return new AdminAuditDto(audit.id(), audit.adminUsername(), audit.customerId(), audit.accountId(), audit.actionType(), audit.amount(), audit.balanceBefore(), audit.balanceAfter(), audit.note(), audit.createdAt());
    }

    @FunctionalInterface
    private interface AuditFactory {
        AdminCustomerAudit create(String adminUsername, Long customerId, String note);
    }
}
