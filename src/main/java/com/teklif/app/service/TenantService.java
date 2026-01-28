package com.teklif.app.service;

import com.teklif.app.dto.request.TenantRequest;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.PaginationResponse;
import com.teklif.app.dto.response.TenantResponse;
import com.teklif.app.dto.response.UserTenantResponse;
import com.teklif.app.entity.Tenant;
import com.teklif.app.entity.User;
import com.teklif.app.entity.UserTenant;
import com.teklif.app.enums.LogType;
import com.teklif.app.enums.Role;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.TenantMapper;
import com.teklif.app.repository.TenantRepository;
import com.teklif.app.repository.UserRepository;
import com.teklif.app.repository.UserTenantRepository;
import com.teklif.app.security.CustomUserDetails;
import com.teklif.app.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final ActivityLogService activityLogService;
    private final UserTenantRepository userTenantRepository;
    private final UserRepository userRepository;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            // CustomUserDetails'dan userId al
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // JWT'den userId'yi almak için JwtUtil kullanabiliriz veya CustomUserDetails'e cast edebiliriz
            // Şimdilik basit bir approach kullanıyoruz
            return null; // TODO: JWT'den userId al
        }
        return null;
    }

    public PagedResponse<TenantResponse> getAllTenants(
            String search,
            Boolean isActive,
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        // Kullanıcının rolünü kontrol et
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Page<Tenant> tenantPage;

        if (authentication != null && authentication.getPrincipal() instanceof com.teklif.app.security.CustomUserDetails) {
            com.teklif.app.security.CustomUserDetails userDetails =
                    (com.teklif.app.security.CustomUserDetails) authentication.getPrincipal();
            Role userRole = userDetails.getUser().getRole();

            // TENANT_ADMIN ise sadece kendi tenantı ve alt tenantlarını getir
            if (userRole == Role.TENANT_ADMIN) {
                String tenantId = TenantContext.getTenantId();
                tenantPage = tenantRepository.findTenantAndSubTenantsWithFilters(
                        tenantId, search, isActive, pageable
                );
            } else {
                // SUPER_ADMIN ise tüm tenantları getir
                tenantPage = tenantRepository.findAllWithFilters(
                        search, isActive, pageable
                );
            }
        } else {
            // Authentication yoksa boş sonuç
            tenantPage = Page.empty();
        }

        List<TenantResponse> items = tenantPage.getContent().stream()
                .map(tenantMapper::toResponse)
                .toList();

        PaginationResponse pagination = PaginationResponse.of(
                tenantPage.getTotalElements(), page, limit
        );

        return PagedResponse.<TenantResponse>builder()
                .items(items)
                .pagination(pagination)
                .build();
    }

    public TenantResponse getTenantById(String id) {
        Tenant tenant = tenantRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> CustomException.notFound("Tenant not found"));

        return tenantMapper.toResponse(tenant);
    }

    public TenantResponse getTenantBySlug(String slug) {
        Tenant tenant = tenantRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> CustomException.notFound("Tenant not found"));

        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        // Check if slug already exists
        if (tenantRepository.existsBySlugAndIsDeletedFalse(request.getSlug())) {
            throw CustomException.badRequest("Slug already exists");
        }

        Tenant tenant = tenantMapper.toEntity(request);
        tenant = tenantRepository.save(tenant);

        // Create log
        activityLogService.createLog(LogType.TENANT_CREATED, tenant.getId(),
                "Şirket Oluşturuldu",
                tenant.getName() + " isimli şirket oluşturuldu",
                null);

        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    public TenantResponse updateTenant(String id, TenantRequest request) {
        Tenant tenant = tenantRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> CustomException.notFound("Tenant not found"));

        // Check if slug already exists (excluding current tenant)
        if (!tenant.getSlug().equals(request.getSlug()) &&
            tenantRepository.existsBySlugAndIsDeletedFalse(request.getSlug())) {
            throw CustomException.badRequest("Slug already exists");
        }

        tenantMapper.updateEntity(request, tenant);

        // Manually set template (MapStruct generated code needs rebuild)
        if (request.getTemplate() != null) {
            tenant.setTemplate(request.getTemplate());
        }

        tenant = tenantRepository.save(tenant);

        // Create log
        activityLogService.createLog(LogType.TENANT_UPDATED, tenant.getId(),
                "Şirket Güncellendi",
                tenant.getName() + " isimli şirket güncellendi",
                null);

        return tenantMapper.toResponse(tenant);
    }

    @Transactional
    public void deleteTenant(String id) {
        Tenant tenant = tenantRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> CustomException.notFound("Tenant not found"));

        String tenantName = tenant.getName();

        tenant.setIsDeleted(true);
        tenantRepository.save(tenant);

        // Create log
        activityLogService.createLog(LogType.TENANT_DELETED, id,
                "Şirket Silindi",
                tenantName + " isimli şirket silindi",
                null);
    }

    // ========== Sub-Tenant Methods ==========

    @Transactional
    public TenantResponse createSubTenant(String parentTenantId, TenantRequest request) {
        String currentTenantId = TenantContext.getTenantId();

        // Sadece kendi tenantı altında alt tenant oluşturabilir
        if (!currentTenantId.equals(parentTenantId)) {
            throw CustomException.forbidden("Can only create sub-tenants under your own tenant");
        }

        Tenant parentTenant = tenantRepository.findByIdAndIsDeletedFalse(parentTenantId)
                .orElseThrow(() -> CustomException.notFound("Parent tenant not found"));

        // Zaten alt tenantı olan bir tenant başkasının altında olamaz (one-level)
        if (parentTenant.getParentTenantId() != null) {
            throw CustomException.badRequest("Sub-tenants cannot have their own sub-tenants");
        }

        // Sub-tenant slug'ı benzersiz olmalı
        if (tenantRepository.existsBySlugAndIsDeletedFalse(request.getSlug())) {
            throw CustomException.badRequest("Slug already exists");
        }

        Tenant subTenant = tenantMapper.toEntity(request);
        subTenant.setParentTenantId(parentTenantId);
        subTenant = tenantRepository.save(subTenant);

        // Log
        activityLogService.createLog(LogType.TENANT_CREATED, subTenant.getId(),
                "Alt Tenant Oluşturuldu",
                parentTenant.getName() + " altına " + subTenant.getName() + " oluşturuldu",
                null);

        return tenantMapper.toResponse(subTenant);
    }

    // ========== User-Tenant Methods ==========

    public List<UserTenantResponse> getUserTenants(String userId) {
        List<UserTenant> userTenants = userTenantRepository.findByUserIdAndIsDeletedFalse(userId);

        return userTenants.stream()
                .map(ut -> UserTenantResponse.builder()
                        .id(ut.getTenantId())
                        .tenantId(ut.getTenantId())
                        .name(ut.getTenant().getName())
                        .slug(ut.getTenant().getSlug())
                        .parentTenantId(ut.getTenant().getParentTenantId())
                        .isDefault(ut.getIsDefault())
                        .createdAt(ut.getCreatedAt())
                        .build())
                .toList();
    }

    public List<UserTenantResponse> getMyTenants() {
        // JWT'den userId ve rol al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw CustomException.unauthorized("User not authenticated");
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getUserId();
        Role userRole = userDetails.getUser().getRole();

        // TENANT_USER: Sadece atanmış tenantlar
        if (userRole == Role.TENANT_USER) {
            return getUserTenants(userId);
        }

        // TENANT_ADMIN: Kendi tenantı + alt tenantları
        if (userRole == Role.TENANT_ADMIN) {
            String tenantId = TenantContext.getTenantId();

            UserTenant defaultUserTenant = userTenantRepository.findDefaultByUserId(userId).orElse(null);

            Tenant ownTenant = tenantRepository.findByIdAndIsDeletedFalse(tenantId)
                    .orElse(null);

            if(ownTenant == null){
                ownTenant = tenantRepository.findByIdAndIsDeletedFalse(userDetails.getUser().getTenantId())
                        .orElse(null);
            }

            if(ownTenant.getParentTenantId() != null){
                tenantId = ownTenant.getParentTenantId();
                ownTenant = tenantRepository.findByIdAndIsDeletedFalse(tenantId)
                        .orElse(null);
            }

            List<Tenant> subTenants = tenantRepository.findByParentTenantId(tenantId);

            List<UserTenantResponse> result = new java.util.ArrayList<>();

            if (ownTenant != null) {
                result.add(UserTenantResponse.builder()
                        .id(ownTenant.getId())
                        .tenantId(ownTenant.getId())
                        .name(ownTenant.getName())
                        .slug(ownTenant.getSlug())
                        .parentTenantId(ownTenant.getParentTenantId())
                        .isDefault(defaultUserTenant.getTenantId().equals(ownTenant.getId()))
                        .createdAt(ownTenant.getCreatedAt())
                        .build());
            }

            // Alt tenantları ekle
            for (Tenant subTenant : subTenants) {
                result.add(UserTenantResponse.builder()
                        .id(subTenant.getId())
                        .tenantId(subTenant.getId())
                        .name(subTenant.getName())
                        .slug(subTenant.getSlug())
                        .parentTenantId(subTenant.getParentTenantId())
                        .isDefault(defaultUserTenant.getTenantId().equals(subTenant.getId()))
                        .createdAt(subTenant.getCreatedAt())
                        .build());
            }

            return result;
        }

        // SUPER_ADMIN: Tüm tenantlar
        if (userRole == Role.SUPER_ADMIN) {
            String defTenantId = null;
            UserTenant defaultUserTenant = userTenantRepository.findDefaultByUserId(userId).orElse(null);
            if(defaultUserTenant == null){
                defTenantId = userDetails.getTenantId();
            }else{
                defTenantId = defaultUserTenant.getTenantId();
            }

            if(defTenantId == null){
                defTenantId = userDetails.getUser().getTenantId();
            }

            List<Tenant> allTenants = tenantRepository.findAll().stream()
                    .filter(t -> !t.getIsDeleted())
                    .toList();
            final String dT = defTenantId;
            return allTenants.stream()
                    .map(t -> UserTenantResponse.builder()
                            .id(t.getId())
                            .tenantId(t.getId())
                            .name(t.getName())
                            .slug(t.getSlug())
                            .parentTenantId(t.getParentTenantId())
                            .isDefault(t.getId().equals(dT))
                            .createdAt(t.getCreatedAt())
                            .build())
                    .toList();
        }

        // Diğer durumlar için boş liste
        return java.util.Collections.emptyList();
    }

    @Transactional
    public void assignTenantToUser(String userId, String tenantId, Boolean isDefault) {
        // Tenant mevcut mu?
        Tenant tenant = tenantRepository.findByIdAndIsDeletedFalse(tenantId)
                .orElseThrow(() -> CustomException.notFound("Tenant not found"));

        // Kullanıcı mevcut mu?
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> CustomException.notFound("User not found"));

        // Zaten atanmış mı?
        if (userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)) {
            throw CustomException.badRequest("User already assigned to this tenant");
        }

        // Eğer isDefault=true, diğerlerinin default'unu kaldır
        if (isDefault != null && isDefault) {
            userTenantRepository.findByUserIdAndIsDeletedFalse(userId)
                    .forEach(ut -> {
                        ut.setIsDefault(false);
                        userTenantRepository.save(ut);
                    });
        }

        UserTenant userTenant = UserTenant.builder()
                .userId(userId)
                .tenantId(tenantId)
                .isDefault(isDefault != null ? isDefault : false)
                .build();

        userTenantRepository.save(userTenant);
    }

    @Transactional
    public void removeTenantFromUser(String userId, String tenantId) {
        UserTenant userTenant = userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> CustomException.notFound("Assignment not found"));

       // userTenant.setIsDeleted(true);
        userTenantRepository.delete(userTenant);
    }

    @Transactional
    public void setDefaultTenant(String userId, String tenantId, Role r) {
        // Kullanıcının bu tenant'a erişimi var mı?
        boolean hasAccess = userTenantRepository.existsByUserIdAndTenantId(userId, tenantId);

        // Eğer erişim yoksa ve SUPER_ADMIN ise, otomatik atama yap
        if (!hasAccess && r.equals(Role.SUPER_ADMIN)) {
            // Tüm tenantların default'unu false yap
            userTenantRepository.findByUserIdAndIsDeletedFalse(userId)
                    .forEach(ut -> {
                        ut.setIsDefault(false);
                        userTenantRepository.save(ut);
                    });

            // Yeni UserTenant oluştur
            UserTenant newUserTenant = UserTenant.builder()
                    .userId(userId)
                    .tenantId(tenantId)
                    .isDefault(true)
                    .build();

            userTenantRepository.save(newUserTenant);
            return;
        }

        // Eğer erişim yoksa ve SUPER_ADMIN değilse hata fırlat
        if (!hasAccess) {
            throw CustomException.badRequest("User does not have access to this tenant");
        }

        // Tüm tenantların default'unu false yap
        userTenantRepository.findByUserIdAndIsDeletedFalse(userId)
                .forEach(ut -> {
                    ut.setIsDefault(false);
                    userTenantRepository.save(ut);
                });

        // Seçilen tenant'ı default yap
        UserTenant userTenant = userTenantRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> CustomException.notFound("Assignment not found"));

        userTenant.setIsDefault(true);
        userTenantRepository.save(userTenant);

        // Not: TenantContext artık JwtAuthenticationFilter'da otomatik set ediliyor
        // Her request'te user_tenants tablosundaki default tenant kullanılacak
    }

    @Transactional
    public void setMyDefaultTenant(String tenantId) {
        // JWT'den userId al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;
        User u = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getUserId();
            u = userDetails.getUser();
        }

        if (userId == null) {
            throw CustomException.unauthorized("User not authenticated");
        }

        setDefaultTenant(userId, tenantId, u.getRole());
    }
}
