package com.teklif.app.controller;

import com.teklif.app.dto.request.TenantRequest;
import com.teklif.app.dto.response.ApiResponse;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.TenantResponse;
import com.teklif.app.dto.response.UserTenantResponse;
import com.teklif.app.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies (Tenants)", description = "Company/Tenant management endpoints")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    @Operation(summary = "Get all companies")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<TenantResponse>>> getAllCompanies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PagedResponse<TenantResponse> response = tenantService.getAllTenants(search, isActive, page, limit);
        return ResponseEntity.ok(ApiResponse.success(response, response.getPagination()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'TENANT_USER')")
    public ResponseEntity<ApiResponse<TenantResponse>> getCompanyById(@PathVariable String id) {
        TenantResponse response = tenantService.getTenantById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get company by slug")
    public ResponseEntity<ApiResponse<TenantResponse>> getCompanyBySlug(@PathVariable String slug) {
        TenantResponse response = tenantService.getTenantBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new company")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> createCompany(@Valid @RequestBody TenantRequest request) {
        request.setIsActive(true);
        request.setTemplate("default");
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateCompany(
            @PathVariable String id,
            @Valid @RequestBody TenantRequest request
    ) {
        TenantResponse response = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete company")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable String id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Company deleted successfully"));
    }

    // ========== Sub-Tenant Endpoints ==========

    @PostMapping("/sub-tenants/{parentTenantId}")
    @Operation(summary = "Create sub-tenant under parent tenant")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<TenantResponse>> createSubTenant(
            @PathVariable String parentTenantId,
            @Valid @RequestBody TenantRequest request
    ) {
        TenantResponse response = tenantService.createSubTenant(parentTenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    // ========== User-Tenant Endpoints ==========

    @GetMapping("/my-tenants")
    @Operation(summary = "Get current user's accessible tenants")
    public ResponseEntity<ApiResponse<List<UserTenantResponse>>> getMyTenants() {
        List<UserTenantResponse> tenants = tenantService.getMyTenants();
        return ResponseEntity.ok(ApiResponse.success(tenants));
    }

    @GetMapping("/users/{userId}/tenants")
    @Operation(summary = "Get user's accessible tenants")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_USER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserTenantResponse>>> getUserTenants(@PathVariable String userId) {
        List<UserTenantResponse> tenants = tenantService.getUserTenants(userId);
        return ResponseEntity.ok(ApiResponse.success(tenants));
    }

    @PostMapping("/{tenantId}/users/{userId}")
    @Operation(summary = "Assign tenant to user")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignTenantToUser(
            @PathVariable String tenantId,
            @PathVariable String userId,
            @RequestParam(required = false, defaultValue = "false") Boolean isDefault
    ) {
        tenantService.assignTenantToUser(userId, tenantId, isDefault);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Tenant assigned to user"));
    }

    @DeleteMapping("/{tenantId}/users/{userId}")
    @Operation(summary = "Remove tenant from user")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeTenantFromUser(
            @PathVariable String tenantId,
            @PathVariable String userId
    ) {
        tenantService.removeTenantFromUser(userId, tenantId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Tenant removed from user"));
    }

    @PatchMapping("/default/{tenantId}")
    @Operation(summary = "Set default tenant for current user")
    public ResponseEntity<ApiResponse<Void>> setDefaultTenant(@PathVariable String tenantId) {
        tenantService.setMyDefaultTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Default tenant changed"));
    }
}
