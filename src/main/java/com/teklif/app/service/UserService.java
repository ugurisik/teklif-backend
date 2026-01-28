package com.teklif.app.service;

import com.teklif.app.dto.request.CreateUserRequest;
import com.teklif.app.dto.request.UpdateUserRequest;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.PaginationResponse;
import com.teklif.app.dto.response.UserResponse;
import com.teklif.app.entity.User;
import com.teklif.app.enums.LogType;
import com.teklif.app.enums.Role;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.UserMapper;
import com.teklif.app.repository.UserRepository;
import com.teklif.app.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public PagedResponse<UserResponse> getAllUsers(
            String search,
            Role role,
            Boolean isActive,
            int page,
            int limit
    ) {
        String tenantId = TenantContext.getTenantId();
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        // Kullanıcının rolünü kontrol et
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Page<User> userPage;

        if (authentication != null && authentication.getPrincipal() instanceof com.teklif.app.security.CustomUserDetails) {
            com.teklif.app.security.CustomUserDetails userDetails =
                    (com.teklif.app.security.CustomUserDetails) authentication.getPrincipal();
            Role userRole = userDetails.getUser().getRole();

            // TENANT_ADMIN ise kendi tenantı ve alt tenantlarındaki kullanıcıları getir
            if (userRole == Role.TENANT_ADMIN) {
                userPage = userRepository.findUsersInTenantAndSubTenants(
                        tenantId, search, role, isActive, pageable
                );
            } else {
                // SUPER_ADMIN ise tüm kullanıcıları getir (tenantId null yaparak)
                userPage = userRepository.findAllWithFilters(
                        null, search, role, isActive, pageable
                );
            }
        } else {
            // Authentication yoksa boş sonuç
            userPage = Page.empty();
        }

        List<UserResponse> items = userPage.getContent().stream()
                .map(userMapper::toResponse)
                .toList();

        PaginationResponse pagination = PaginationResponse.of(
                userPage.getTotalElements(), page, limit
        );

        return PagedResponse.<UserResponse>builder()
                .items(items)
                .pagination(pagination)
                .build();
    }

    public UserResponse getUserById(String id) {
        String tenantId = TenantContext.getTenantId();
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> CustomException.notFound("User not found"));

        // Check tenant access
        if (!user.getTenantId().equals(tenantId)) {
            throw CustomException.forbidden("Access denied");
        }

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String tenantId = TenantContext.getTenantId();

        // Check if email exists
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw CustomException.badRequest("Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setTenantId(request.getTenantId() != null ? request.getTenantId() : tenantId);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        // Create log
        activityLogService.createLog(LogType.USER_CREATED, user.getId(),
                "Kullanıcı Oluşturuldu",
                user.getEmail() + " e-posta adresli kullanıcı oluşturuldu",
                null);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        String tenantId = TenantContext.getTenantId();
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> CustomException.notFound("User not found"));

        if (!user.getTenantId().equals(tenantId)) {
            throw CustomException.forbidden("Access denied");
        }

        userMapper.updateEntity(request, user);
        user = userRepository.save(user);

        // Create log
        activityLogService.createLog(LogType.USER_UPDATED, user.getId(),
                "Kullanıcı Güncellendi",
                user.getEmail() + " e-posta adresli kullanıcı güncellendi",
                null);

        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(String id) {
        String tenantId = TenantContext.getTenantId();
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> CustomException.notFound("User not found"));

        if (!user.getTenantId().equals(tenantId)) {
            throw CustomException.forbidden("Access denied");
        }

        String userEmail = user.getEmail();

        user.setIsDeleted(true);
        userRepository.save(user);

        // Create log
        activityLogService.createLog(LogType.USER_DELETED, id,
                "Kullanıcı Silindi",
                userEmail + " e-posta adresli kullanıcı silindi",
                null);
    }

    @Transactional
    public UserResponse toggleUserStatus(String id) {
        String tenantId = TenantContext.getTenantId();
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> CustomException.notFound("User not found"));

        if (!user.getTenantId().equals(tenantId)) {
            throw CustomException.forbidden("Access denied");
        }

        user.setIsActive(!user.getIsActive());
        user = userRepository.save(user);

        // Create log
        activityLogService.createLog(LogType.USER_UPDATED, user.getId(),
                "Kullanıcı Durumu Değiştirildi",
                user.getEmail() + " e-posta adresli kullanıcı durumu " + (user.getIsActive() ? "aktif" : "pasif") + " yapıldı",
                null);

        return userMapper.toResponse(user);
    }
}