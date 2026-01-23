package com.teklif.app.mapper;

import com.teklif.app.dto.response.OfferItemResponse;
import com.teklif.app.dto.response.OfferResponse;
import com.teklif.app.entity.Offer;
import com.teklif.app.entity.OfferItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface OfferMapper {

    @Mapping(target = "customer", source = "customer")
    OfferResponse toResponse(Offer offer);

    OfferItemResponse toItemResponse(OfferItem item);
}