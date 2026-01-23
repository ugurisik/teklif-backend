package com.teklif.app.service;

import com.teklif.app.dto.response.OfferResponse;
import com.teklif.app.entity.Offer;
import com.teklif.app.enums.OfferStatus;
import com.teklif.app.mapper.OfferMapper;
import com.teklif.app.repository.OfferRepository;
import com.teklif.app.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OfferRepository offerRepository;
    private final OfferMapper offerMapper;

    public Map<String, Object> getDashboardStats(String period) {
        String tenantId = TenantContext.getTenantId();

        Map<String, Object> stats = new HashMap<>();

        // Count offers by status
        long totalOffers = offerRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, null);
        long draftOffers = offerRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, OfferStatus.DRAFT);
        long sentOffers = offerRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, OfferStatus.SENT);
        long viewedOffers = offerRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, OfferStatus.VIEWED);
        long acceptedOffers = offerRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, OfferStatus.ACCEPTED);
        long rejectedOffers = offerRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, OfferStatus.REJECTED);
        long expiredOffers = offerRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, OfferStatus.EXPIRED);

        stats.put("totalOffers", totalOffers);
        stats.put("draftOffers", draftOffers);
        stats.put("sentOffers", sentOffers);
        stats.put("viewedOffers", viewedOffers);
        stats.put("acceptedOffers", acceptedOffers);
        stats.put("rejectedOffers", rejectedOffers);
        stats.put("expiredOffers", expiredOffers);

        // Calculate revenue
        Double acceptedRevenue = offerRepository.sumTotalByTenantIdAndStatus(tenantId, OfferStatus.ACCEPTED);
        Double pendingRevenue = offerRepository.sumTotalByTenantIdAndStatus(tenantId, OfferStatus.SENT);
        pendingRevenue += offerRepository.sumTotalByTenantIdAndStatus(tenantId, OfferStatus.VIEWED);

        Double totalRevenue = (acceptedRevenue != null ? acceptedRevenue : 0.0) +
                (pendingRevenue != null ? pendingRevenue : 0.0);

        stats.put("totalRevenue", totalRevenue);
        stats.put("acceptedRevenue", acceptedRevenue != null ? acceptedRevenue : 0.0);
        stats.put("pendingRevenue", pendingRevenue != null ? pendingRevenue : 0.0);

        // Get recent offers
        Pageable pageable = PageRequest.of(0, 5);
        List<Offer> recentOffersList = offerRepository.findRecentOffers(tenantId, pageable).getContent();
        List<OfferResponse> recentOffers = recentOffersList.stream()
                .map(offerMapper::toResponse)
                .toList();

        stats.put("recentOffers", recentOffers);

        // TODO: Add monthly stats based on period

        return stats;
    }
}