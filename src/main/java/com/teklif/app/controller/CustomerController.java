package com.teklif.app.controller;

import com.teklif.app.dto.request.CustomerRequest;
import com.teklif.app.dto.response.ApiResponse;
import com.teklif.app.dto.response.CustomerResponse;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.enums.CustomerType;
import com.teklif.app.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getAllCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerType type,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PagedResponse<CustomerResponse> response = customerService.getAllCustomers(search, type, isActive, page, limit);
        return ResponseEntity.ok(ApiResponse.success((PagedResponse<CustomerResponse>) response.getItems(), response.getPagination()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable String id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerRequest request
    ) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Customer deleted successfully"));
    }
}