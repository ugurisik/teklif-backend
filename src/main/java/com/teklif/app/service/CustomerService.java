package com.teklif.app.service;

import com.teklif.app.dto.request.CustomerRequest;
import com.teklif.app.dto.response.CustomerResponse;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.PaginationResponse;
import com.teklif.app.entity.Customer;
import com.teklif.app.enums.CustomerType;
import com.teklif.app.enums.LogType;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.CustomerMapper;
import com.teklif.app.repository.CustomerRepository;
import com.teklif.app.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ActivityLogService activityLogService;

    private String getCustomerDisplayName(Customer customer) {
        if (customer.getType() == CustomerType.COMPANY) {
            return customer.getCompanyName();
        } else {
            return customer.getFirstName() + " " + customer.getLastName();
        }
    }

    public PagedResponse<CustomerResponse> getAllCustomers(
            String search,
            CustomerType type,
            Boolean isActive,
            int page,
            int limit
    ) {
        String tenantId = TenantContext.getTenantId();
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<Customer> customerPage = customerRepository.findAllWithFilters(
                tenantId, search, type, isActive, pageable
        );

        List<CustomerResponse> items = customerPage.getContent().stream()
                .map(customerMapper::toResponse)
                .toList();

        PaginationResponse pagination = PaginationResponse.of(
                customerPage.getTotalElements(), page, limit
        );

        return PagedResponse.<CustomerResponse>builder()
                .items(items)
                .pagination(pagination)
                .build();
    }

    public CustomerResponse getCustomerById(String id) {
        String tenantId = TenantContext.getTenantId();
        Customer customer = customerRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));

        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        String tenantId = TenantContext.getTenantId();

        Customer customer = customerMapper.toEntity(request);
        customer.setTenantId(tenantId);

        customer = customerRepository.save(customer);

        // Create log
        activityLogService.createLog(LogType.CUSTOMER_CREATED, customer.getId(),
                "Müşteri Oluşturuldu",
                getCustomerDisplayName(customer) + " isimli müşteri oluşturuldu",
                null);

        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(String id, CustomerRequest request) {
        String tenantId = TenantContext.getTenantId();
        Customer customer = customerRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));

        customerMapper.updateEntity(request, customer);
        customer = customerRepository.save(customer);

        // Create log
        activityLogService.createLog(LogType.CUSTOMER_UPDATED, customer.getId(),
                "Müşteri Güncellendi",
                getCustomerDisplayName(customer) + " isimli müşteri güncellendi",
                null);

        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void deleteCustomer(String id) {
        String tenantId = TenantContext.getTenantId();
        Customer customer = customerRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));

        String customerName = getCustomerDisplayName(customer);

        customer.setIsDeleted(true);
        customerRepository.save(customer);

        // Create log
        activityLogService.createLog(LogType.CUSTOMER_DELETED, id,
                "Müşteri Silindi",
                customerName + " isimli müşteri silindi",
                null);
    }
}