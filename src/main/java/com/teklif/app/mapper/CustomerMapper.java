package com.teklif.app.mapper;

import com.teklif.app.dto.request.CustomerRequest;
import com.teklif.app.dto.response.CustomerBasicResponse;
import com.teklif.app.dto.response.CustomerResponse;
import com.teklif.app.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "offerCount", source = "offers", qualifiedByName = "offersSize")
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "offers", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Customer toEntity(CustomerRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "offers", ignore = true)
    void updateEntity(CustomerRequest request, @MappingTarget Customer customer);

    @Named("offersSize")
    default Long offersSize(java.util.List<?> offers) {
        return offers != null ? (long) offers.size() : 0L;
    }

    default CustomerBasicResponse toBasicResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerBasicResponse.builder()
                .id(customer.getId())
                .companyName(customer.getCompanyName())
                .contactPerson(customer.getContactPerson())
                .build();
    }
}