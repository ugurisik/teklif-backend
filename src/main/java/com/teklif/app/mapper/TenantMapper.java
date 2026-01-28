package com.teklif.app.mapper;

import com.teklif.app.dto.request.TenantRequest;
import com.teklif.app.dto.response.CompanyBasicResponse;
import com.teklif.app.dto.response.TenantResponse;
import com.teklif.app.entity.Tenant;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    TenantResponse toResponse(Tenant tenant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Tenant toEntity(TenantRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(TenantRequest request, @MappingTarget Tenant tenant);

    default CompanyBasicResponse toCompanyBasicResponse(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        return CompanyBasicResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .logo(tenant.getLogo())
                .email(tenant.getEmail())
                .phone(tenant.getPhone())
                .address(tenant.getAddress())
                .template(tenant.getTemplate())
                .primaryColor(tenant.getPrimaryColor())
                .secondaryColor(tenant.getSecondaryColor())
                .build();
    }
}
