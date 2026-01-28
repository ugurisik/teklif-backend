package com.teklif.app.mapper;

import com.teklif.app.dto.response.CustomerBasicResponse;
import com.teklif.app.dto.response.NotificationReadResponse;
import com.teklif.app.dto.response.NotificationResponse;
import com.teklif.app.dto.response.OfferBasicResponse;
import com.teklif.app.entity.Notification;
import com.teklif.app.entity.NotificationRead;
import com.teklif.app.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TenantMapper.class, CustomerMapper.class})
public interface NotificationMapper {

    @Mapping(target = "offer", source = "offer")
    @Mapping(target = "tenant", source = "tenant")
    @Mapping(target = "reads", ignore = true)
    @Mapping(target = "isReadByCurrentUser", ignore = true)
    NotificationResponse toResponse(Notification notification);

    NotificationReadResponse toReadResponse(NotificationRead notificationRead);

    default OfferBasicResponse toOfferBasicResponse(Offer offer) {
        if (offer == null) {
            return null;
        }
        CustomerBasicResponse customer = offer.getCustomer() != null
                ? CustomerBasicResponse.builder()
                    .id(offer.getCustomer().getId())
                    .companyName(offer.getCustomer().getCompanyName())
                    .contactPerson(offer.getCustomer().getContactPerson())
                    .email(offer.getCustomer().getEmail())
                    .phone(offer.getCustomer().getPhone())
                    .build()
                : null;

        return OfferBasicResponse.builder()
                .id(offer.getId())
                .offerNo(offer.getOfferNo())
                .customerId(offer.getCustomerId())
                .customer(customer)
                .build();
    }
}
