package com.bank.central.admin.service;

import com.bank.central.admin.dto.AdminAuditDto;
import com.bank.central.admin.dto.AdminCashAdjustmentRequest;
import com.bank.central.admin.dto.AdminCashAdjustmentResponse;
import com.bank.central.admin.dto.CustomerAdminDto;

import java.util.List;

public interface AdminCustomerService {

    List<CustomerAdminDto> searchCustomers(String query);

    List<AdminAuditDto> getCustomerAudit(Long customerId);

    CustomerAdminDto activateCustomer(Long customerId, String adminUsername);

    CustomerAdminDto deactivateCustomer(Long customerId, String adminUsername);

    AdminCashAdjustmentResponse addCash(Long customerId, String adminUsername, AdminCashAdjustmentRequest request);

    AdminCashAdjustmentResponse removeCash(Long customerId, String adminUsername, AdminCashAdjustmentRequest request);
}
