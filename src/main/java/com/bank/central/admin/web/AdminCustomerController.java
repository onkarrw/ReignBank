package com.bank.central.admin.web;

import com.bank.central.auth.service.AuthService;
import com.bank.central.admin.dto.AdminAuditDto;
import com.bank.central.admin.dto.AdminCashAdjustmentRequest;
import com.bank.central.admin.dto.AdminCashAdjustmentResponse;
import com.bank.central.admin.dto.CustomerAdminDto;
import com.bank.central.admin.service.AdminCustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;
    private final AuthService authService;

    public AdminCustomerController(AdminCustomerService adminCustomerService, AuthService authService) {
        this.adminCustomerService = adminCustomerService;
        this.authService = authService;
    }

    @GetMapping("/search")
    public List<CustomerAdminDto> searchCustomers(@RequestParam String q) {
        return adminCustomerService.searchCustomers(q);
    }

    @GetMapping("/{customerId}/audit")
    public List<AdminAuditDto> getCustomerAudit(@PathVariable Long customerId) {
        return adminCustomerService.getCustomerAudit(customerId);
    }

    @PostMapping("/{customerId}/activate")
    public CustomerAdminDto activateCustomer(@PathVariable Long customerId) {
        return adminCustomerService.activateCustomer(customerId, authService.getCurrentUsername());
    }

    @PostMapping("/{customerId}/deactivate")
    public CustomerAdminDto deactivateCustomer(@PathVariable Long customerId) {
        return adminCustomerService.deactivateCustomer(customerId, authService.getCurrentUsername());
    }

    @PostMapping("/{customerId}/cash/add")
    public AdminCashAdjustmentResponse addCash(@PathVariable Long customerId, @RequestBody AdminCashAdjustmentRequest request) {
        return adminCustomerService.addCash(customerId, authService.getCurrentUsername(), request);
    }

    @PostMapping("/{customerId}/cash/remove")
    public AdminCashAdjustmentResponse removeCash(@PathVariable Long customerId, @RequestBody AdminCashAdjustmentRequest request) {
        return adminCustomerService.removeCash(customerId, authService.getCurrentUsername(), request);
    }
}
