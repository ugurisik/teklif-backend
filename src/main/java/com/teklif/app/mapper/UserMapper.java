package com.teklif.app.mapper;

import com.teklif.app.dto.request.CreateUserRequest;
import com.teklif.app.dto.request.UpdateUserRequest;
import com.teklif.app.dto.response.CompanyBasicResponse;
import com.teklif.app.dto.response.UserResponse;
import com.teklif.app.entity.Tenant;
import com.teklif.app.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "company", source = "tenant")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    User toEntity(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);

    default CompanyBasicResponse tenantToCompanyBasic(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        return CompanyBasicResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .logo(tenant.getLogo())
                .build();
    }
}