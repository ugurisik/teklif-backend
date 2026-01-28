package com.teklif.app.service;

import com.teklif.app.controller.OfferController;
import com.teklif.app.dto.request.CreateOfferRequest;
import com.teklif.app.dto.request.OfferItemRequest;
import com.teklif.app.dto.response.OfferResponse;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.PaginationResponse;
import com.teklif.app.entity.Offer;
import com.teklif.app.entity.OfferItem;
import com.teklif.app.entity.User;
import com.teklif.app.enums.LogType;
import com.teklif.app.enums.NotificationType;
import com.teklif.app.enums.OfferStatus;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.OfferMapper;
import com.teklif.app.repository.CustomerRepository;
import com.teklif.app.repository.OfferRepository;
import com.teklif.app.security.CustomUserDetails;
import com.teklif.app.util.Func;
import com.teklif.app.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final CustomerRepository customerRepository;
    private final OfferMapper offerMapper;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public PagedResponse<OfferResponse> getAllOffers(
            String search,
            OfferStatus status,
            String customerId,
            Instant startDate,
            Instant endDate,
            int page,
            int limit
    ) {
        String tenantId = TenantContext.getTenantId();
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        Page<Offer> offerPage = offerRepository.findAllWithFilters(
                tenantId, search, status, customerId, startDate, endDate, pageable
        );

        List<OfferResponse> items = offerPage.getContent().stream()
                .map(offerMapper::toResponse)
                .toList();

        PaginationResponse pagination = PaginationResponse.of(
                offerPage.getTotalElements(), page, limit
        );

        return PagedResponse.<OfferResponse>builder()
                .items(items)
                .pagination(pagination)
                .build();
    }

    public OfferResponse getOfferById(String id) {
        String tenantId = TenantContext.getTenantId();
        Offer offer = offerRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        return offerMapper.toResponse(offer);
    }

    @Transactional
    public OfferResponse createOffer(CreateOfferRequest request) {
        String tenantId = TenantContext.getTenantId();

        // Validate customer
        customerRepository.findByIdAndTenantIdAndIsDeletedFalse(request.getCustomerId(), tenantId)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));

        Offer offer = Offer.builder()
                .offerNo(generateOfferNo())
                .tenantId(tenantId)
                .customerId(request.getCustomerId())
                .currency(request.getCurrency())
                .validUntil(request.getValidUntil())
                .notes(request.getNotes())
                .showTlEquivalent(request.getShowTlEquivalent() != null ? request.getShowTlEquivalent() : false)
                .showExchangeRateInfo(request.getShowExchangeRateInfo() != null ? request.getShowExchangeRateInfo() : false)
                .status(OfferStatus.DRAFT)
                .build();

        // Link settings
        if (request.getLinkSettings() != null) {
            offer.setLinkPassword(request.getLinkSettings().getPassword());
            offer.setOneTimeView(request.getLinkSettings().getOneTimeView());
        }

        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal vatTotal = BigDecimal.ZERO;

        for (OfferItemRequest itemReq : request.getItems()) {
            OfferItem item = createOfferItem(itemReq);
            item.setOffer(offer);
            offer.getItems().add(item);

            subtotal = subtotal.add(item.getSubtotal());
            vatTotal = vatTotal.add(item.getVatAmount());
        }

        offer.setSubtotal(subtotal);
        offer.setVatTotal(vatTotal);
        offer.setTotal(subtotal.add(vatTotal));

        offer = offerRepository.save(offer);

        // Create notification
        createNotification(offer.getId(), tenantId, NotificationType.OFFER_CREATED,
                "Yeni Teklif Oluşturuldu",
                offer.getOfferNo() + " numaralı teklif taslak olarak oluşturuldu.");

        // Create log
        activityLogService.createLog(LogType.OFFER_CREATED, offer.getId(),
                "Teklif Oluşturuldu",
                offer.getOfferNo() + " numaralı teklif oluşturuldu. Toplam: " + offer.getTotal() + " " + offer.getCurrency(),
                null);

        return offerMapper.toResponse(offer);
    }

    private OfferItem createOfferItem(OfferItemRequest request) {
        BigDecimal quantity = BigDecimal.valueOf(request.getQuantity());
        BigDecimal unitPrice = request.getUnitPrice();
        BigDecimal discountRate = request.getDiscountRate();

        // Calculate subtotal
        BigDecimal subtotal = unitPrice.multiply(quantity);

        // Apply discount
        if (discountRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = subtotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            subtotal = subtotal.subtract(discount);
        }

        // Calculate VAT
        BigDecimal vatRate = BigDecimal.valueOf(request.getVatRate());
        BigDecimal vatAmount = subtotal.multiply(vatRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        // Calculate total
        BigDecimal total = subtotal.add(vatAmount);

        return OfferItem.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .unitPrice(unitPrice)
                .vatRate(request.getVatRate())
                .discountRate(discountRate)
                .subtotal(subtotal)
                .vatAmount(vatAmount)
                .total(total)
                .build();
    }

    private String generateOfferNo() {
        String pref =generateOfferNoPrefix( (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        String prefix = pref+"-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        long count = offerRepository.countByOfferNoPrefix(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    private String generateOfferNoPrefix(CustomUserDetails cud) {
        if (cud == null || cud.getUser() == null) {
            return "USR";
        }

        User u = cud.getUser();
        String firstName = Func.normalize(u.getFirstName());
        String lastName  = Func.normalize(u.getLastName());

        String prefix;

        if (!firstName.isEmpty() && !lastName.isEmpty()) {
            // F + L + L
            prefix = ""
                    + firstName.charAt(0)
                    + lastName.charAt(0)
                    + (lastName.length() > 1 ? lastName.charAt(1) : lastName.charAt(0));
        }
        else if (!firstName.isEmpty()) {
            prefix = Func.padToThree(firstName);
        }
        else if (!lastName.isEmpty()) {
            prefix = Func.padToThree(lastName);
        }
        else {
            prefix = "USR";
        }

        return prefix.toUpperCase();
    }


    @Transactional
    public OfferResponse updateOffer(String id, CreateOfferRequest request) {
        String tenantId = TenantContext.getTenantId();
        Offer offer = offerRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        // Validate customer
        customerRepository.findByIdAndTenantIdAndIsDeletedFalse(request.getCustomerId(), tenantId)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));

        // Update basic fields
        offer.setCustomerId(request.getCustomerId());
        offer.setCurrency(request.getCurrency());
        offer.setValidUntil(request.getValidUntil());
        offer.setNotes(request.getNotes());
        offer.setShowTlEquivalent(request.getShowTlEquivalent() != null ? request.getShowTlEquivalent() : false);
        offer.setShowExchangeRateInfo(request.getShowExchangeRateInfo() != null ? request.getShowExchangeRateInfo() : false);

        // Update link settings
        if (request.getLinkSettings() != null) {
            offer.setLinkPassword(request.getLinkSettings().getPassword());
            offer.setOneTimeView(request.getLinkSettings().getOneTimeView());
        }

        // Handle items - replace all items
        offer.getItems().clear();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal vatTotal = BigDecimal.ZERO;

        for (OfferItemRequest itemReq : request.getItems()) {
            OfferItem item = createOfferItem(itemReq);
            item.setOffer(offer);
            offer.getItems().add(item);

            subtotal = subtotal.add(item.getSubtotal());
            vatTotal = vatTotal.add(item.getVatAmount());
        }

        offer.setSubtotal(subtotal);
        offer.setVatTotal(vatTotal);
        offer.setTotal(subtotal.add(vatTotal));

        offer = offerRepository.save(offer);


        notificationService.createNotificationForOffer(offer.getId(), offer.getTenantId(), NotificationType.OFFER_UPDATED,"Teklif Güncellendi", offer.getOfferNo() + " numaralı teklif güncellendi. Yeni toplam: " + offer.getTotal() + " " + offer.getCurrency());

        // Create log
        activityLogService.createLog(LogType.OFFER_UPDATED, offer.getId(),
                "Teklif Güncellendi",
                offer.getOfferNo() + " numaralı teklif güncellendi. Yeni toplam: " + offer.getTotal() + " " + offer.getCurrency(),
                null);

        return offerMapper.toResponse(offer);
    }

    @Transactional
    public OfferResponse sendOffer(String id) {
        String tenantId = TenantContext.getTenantId();
        Offer offer = offerRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw CustomException.badRequest("Only draft offers can be sent");
        }

        offer.setStatus(OfferStatus.SENT);
        offer.setSentAt(Instant.now());

        offer = offerRepository.save(offer);

        // Create notification
        createNotification(offer.getId(), tenantId, NotificationType.OFFER_SENT,
                "Teklif Gönderildi",
                offer.getOfferNo() + " numaralı teklif müşteriye gönderildi.");

        // Create log
        activityLogService.createLog(LogType.OFFER_SENT, offer.getId(),
                "Teklif Gönderildi",
                offer.getOfferNo() + " numaralı teklif müşteriye gönderildi.",
                null);

        // TODO: Send email notification

        return offerMapper.toResponse(offer);
    }

    @Transactional
    public OfferResponse duplicateOffer(String id) {
        String tenantId = TenantContext.getTenantId();
        Offer original = offerRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        Offer duplicate = Offer.builder()
                .offerNo(generateOfferNo())
                .tenantId(original.getTenantId())
                .customerId(original.getCustomerId())
                .currency(original.getCurrency())
                .validUntil(original.getValidUntil())
                .notes(original.getNotes())
                .subtotal(original.getSubtotal())
                .vatTotal(original.getVatTotal())
                .total(original.getTotal())
                .status(OfferStatus.DRAFT)
                .build();

        // Copy items
        for (OfferItem originalItem : original.getItems()) {
            OfferItem duplicateItem = OfferItem.builder()
                    .productId(originalItem.getProductId())
                    .productName(originalItem.getProductName())
                    .description(originalItem.getDescription())
                    .quantity(originalItem.getQuantity())
                    .unit(originalItem.getUnit())
                    .unitPrice(originalItem.getUnitPrice())
                    .vatRate(originalItem.getVatRate())
                    .discountRate(originalItem.getDiscountRate())
                    .subtotal(originalItem.getSubtotal())
                    .vatAmount(originalItem.getVatAmount())
                    .total(originalItem.getTotal())
                    .offer(duplicate)
                    .build();

            duplicate.getItems().add(duplicateItem);
        }

        duplicate = offerRepository.save(duplicate);

        // Create log
        activityLogService.createLog(LogType.OFFER_DUPLICATED, duplicate.getId(),
                "Teklif Çoğaltıldı",
                duplicate.getOfferNo() + " numaralı teklif, " + original.getOfferNo() + " numaralı tekliften çoğaltıldı.",
                null);

        return offerMapper.toResponse(duplicate);
    }

    @Transactional
    public void deleteOffer(String id) {
        String tenantId = TenantContext.getTenantId();
        Offer offer = offerRepository.findByIdAndTenantIdAndIsDeletedFalse(id, tenantId)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        String offerNo = offer.getOfferNo();
        offer.setIsDeleted(true);
        offerRepository.save(offer);

        // Create log
        activityLogService.createLog(LogType.OFFER_DELETED, id,
                "Teklif Silindi",
                offerNo + " numaralı teklif silindi.",
                null);
    }

    // Public methods (no auth required)

    public OfferResponse getPublicOffer(String uuid) {
        Offer offer = offerRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        // Check if one-time view and already viewed
        if (offer.getOneTimeView() && offer.getHasBeenViewed()) {
            throw CustomException.forbidden("This offer has already been viewed");
        }

        OfferResponse response = new OfferResponse();
        if(offer.getLinkPassword() != null) {
            response.setPasswordRequired(true);
        }else{
            response = offerMapper.toResponse(offer);
        }
        return response;
    }

    public OfferResponse getOffer(String uuid){
        Offer offer = offerRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));
        return offerMapper.toResponse(offer);
    }


    @Transactional
    public boolean recordOfferView(String uuid, OfferController.ViewRequest request) {
        Offer offer = offerRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        if(offer.getLinkPassword() != null && !offer.getLinkPassword().equals(request.getPassword())){
            return false;
        }

        if (!offer.getHasBeenViewed()) {
            offer.setStatus(OfferStatus.VIEWED);
            offer.setHasBeenViewed(true);
            offer.setViewedAt(Instant.now());
            offer.setViewedBy(request.getName());
            offerRepository.save(offer);

            // Create notification
            createNotification(offer.getId(), offer.getTenantId(), NotificationType.OFFER_VIEWED,
                    "Teklif Görüntülendi",
                    offer.getOfferNo() + " numaralı teklif " + request.getName() + " tarafından görüntülendi.");

            // Create log
            activityLogService.createLog(LogType.OFFER_VIEWED, offer.getId(),
                    "Teklif Görüntülendi",
                    offer.getOfferNo() + " numaralı teklif " + request.getName() + " tarafından görüntülendi.",
                    null);
        }
        return true;
    }

    @Transactional
    public void acceptOffer(String uuid, String acceptedBy, String note) {
        Offer offer = offerRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        if (offer.getStatus() == OfferStatus.ACCEPTED || offer.getStatus() == OfferStatus.REJECTED) {
            throw CustomException.badRequest("Offer has already been " + offer.getStatus().name().toLowerCase());
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setAcceptedAt(Instant.now());
        offer.setAcceptedBy(acceptedBy);

        offerRepository.save(offer);

        // Create notification
        createNotification(offer.getId(), offer.getTenantId(), NotificationType.OFFER_ACCEPTED,
                "Teklif Kabul Edildi",
                offer.getOfferNo() + " numaralı teklif " + acceptedBy + " tarafından kabul edildi.");

        // Create log
        activityLogService.createLog(LogType.OFFER_ACCEPTED, offer.getId(),
                "Teklif Kabul Edildi",
                offer.getOfferNo() + " numaralı teklif " + acceptedBy + " tarafından kabul edildi.",
                null);
    }

    @Transactional
    public void rejectOffer(String uuid, String rejectedBy, String note) {
        Offer offer = offerRepository.findByUuidAndIsDeletedFalse(uuid)
                .orElseThrow(() -> CustomException.notFound("Offer not found"));

        if (offer.getStatus() == OfferStatus.ACCEPTED || offer.getStatus() == OfferStatus.REJECTED) {
            throw CustomException.badRequest("Offer has already been " + offer.getStatus().name().toLowerCase());
        }

        offer.setStatus(OfferStatus.REJECTED);
        offer.setRejectedAt(Instant.now());
        offer.setRejectedBy(rejectedBy);
        offer.setRejectionNote(note);

        offerRepository.save(offer);

        // Create notification
        createNotification(offer.getId(), offer.getTenantId(), NotificationType.OFFER_REJECTED,
                "Teklif Reddedildi",
                offer.getOfferNo() + " numaralı teklif " + rejectedBy + " tarafından reddedildi." +
                        (note != null && !note.isEmpty() ? " Not: " + note : ""));

        // Create log
        activityLogService.createLog(LogType.OFFER_REJECTED, offer.getId(),
                "Teklif Reddedildi",
                offer.getOfferNo() + " numaralı teklif " + rejectedBy + " tarafından reddedildi." +
                        (note != null && !note.isEmpty() ? " Not: " + note : ""),
                null);
    }

    private void createNotification(String offerId, String tenantId, NotificationType type, String title, String message) {
        notificationService.createNotificationForOffer(offerId, tenantId, type, title, message);
    }
}