package com.teklif.app.controller;

import com.teklif.app.dto.request.CreateOfferRequest;
import com.teklif.app.dto.response.ApiResponse;
import com.teklif.app.dto.response.OfferResponse;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.enums.OfferStatus;
import com.teklif.app.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@Tag(name = "Offers", description = "Offer management endpoints")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    @Operation(summary = "Get all offers")
    public ResponseEntity<ApiResponse<PagedResponse<OfferResponse>>> getAllOffers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OfferStatus status,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PagedResponse<OfferResponse> response = offerService.getAllOffers(
                search, status, customerId, startDate, endDate, page, limit
        );
        return ResponseEntity.ok(ApiResponse.success((PagedResponse<OfferResponse>) response.getItems(), response.getPagination()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get offer by ID")
    public ResponseEntity<ApiResponse<OfferResponse>> getOfferById(@PathVariable String id) {
        OfferResponse response = offerService.getOfferById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create new offer")
    public ResponseEntity<ApiResponse<OfferResponse>> createOffer(@Valid @RequestBody CreateOfferRequest request) {
        OfferResponse response = offerService.createOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send offer")
    public ResponseEntity<ApiResponse<OfferResponse>> sendOffer(@PathVariable String id) {
        OfferResponse response = offerService.sendOffer(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate offer")
    public ResponseEntity<ApiResponse<OfferResponse>> duplicateOffer(@PathVariable String id) {
        OfferResponse response = offerService.duplicateOffer(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete offer")
    public ResponseEntity<ApiResponse<Void>> deleteOffer(@PathVariable String id) {
        offerService.deleteOffer(id);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Offer deleted successfully"));
    }

    // Public endpoints (no authentication required)

    @GetMapping("/public/{uuid}")
    @Operation(summary = "Get public offer")
    public ResponseEntity<ApiResponse<OfferResponse>> getPublicOffer(@PathVariable String uuid) {
        OfferResponse response = offerService.getPublicOffer(uuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/public/{uuid}/view")
    @Operation(summary = "Record offer view")
    public ResponseEntity<ApiResponse<Void>> recordOfferView(
            @PathVariable String uuid,
            @RequestBody ViewRequest request
    ) {
        offerService.recordOfferView(uuid, request.getName());
        return ResponseEntity.ok(ApiResponse.successWithMessage("View recorded successfully"));
    }

    @PostMapping("/public/{uuid}/accept")
    @Operation(summary = "Accept offer")
    public ResponseEntity<ApiResponse<Void>> acceptOffer(
            @PathVariable String uuid,
            @RequestBody AcceptRejectRequest request
    ) {
        offerService.acceptOffer(uuid, request.getName(), request.getNote());
        return ResponseEntity.ok(ApiResponse.successWithMessage("Offer accepted successfully"));
    }

    @PostMapping("/public/{uuid}/reject")
    @Operation(summary = "Reject offer")
    public ResponseEntity<ApiResponse<Void>> rejectOffer(
            @PathVariable String uuid,
            @RequestBody AcceptRejectRequest request
    ) {
        offerService.rejectOffer(uuid, request.getName(), request.getNote());
        return ResponseEntity.ok(ApiResponse.successWithMessage("Offer rejected successfully"));
    }

    @Data
    static class ViewRequest {
        private String name;
    }

    @Data
    static class AcceptRejectRequest {
        private String name;
        private String note;
    }
}