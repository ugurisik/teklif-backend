package com.teklif.app.controller;

import com.teklif.app.dto.response.ActivityLogResponse;
import com.teklif.app.dto.response.ApiResponse;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.enums.LogType;
import com.teklif.app.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
@Tag(name = "Activity Logs", description = "Activity log endpoints")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    @Operation(summary = "Get all activity logs with filters")
    public ResponseEntity<ApiResponse<PagedResponse<ActivityLogResponse>>> getLogs(
            @RequestParam(required = false) LogType logType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PagedResponse<ActivityLogResponse> response = activityLogService.getLogs(
                logType, targetId, startDate, endDate, page, limit
        );
        return ResponseEntity.ok(ApiResponse.success(response, response.getPagination()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get log by ID")
    public ResponseEntity<ApiResponse<ActivityLogResponse>> getLogById(@PathVariable String id) {
        ActivityLogResponse response = activityLogService.getLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/target/{targetId}")
    @Operation(summary = "Get logs by target ID")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getLogsByTargetId(@PathVariable String targetId) {
        List<ActivityLogResponse> response = activityLogService.getLogsByTargetId(targetId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete log")
    public ResponseEntity<ApiResponse<Void>> deleteLog(@PathVariable String id) {
        activityLogService.deleteLog(id);
        return ResponseEntity.ok(ApiResponse.successWithMessage("Log deleted successfully"));
    }
}
