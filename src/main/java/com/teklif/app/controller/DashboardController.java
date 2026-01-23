package com.teklif.app.controller;

import com.teklif.app.dto.response.ApiResponse;
import com.teklif.app.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @RequestParam(defaultValue = "month") String period
    ) {
        Map<String, Object> stats = dashboardService.getDashboardStats(period);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}