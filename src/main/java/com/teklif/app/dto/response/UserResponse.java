package com.teklif.app.dto.response;

import com.teklif.app.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String tenantId;
    private CompanyBasicResponse company;
    private Boolean isActive;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
}