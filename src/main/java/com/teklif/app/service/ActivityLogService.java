package com.teklif.app.service;

import com.teklif.app.dto.response.ActivityLogResponse;
import com.teklif.app.dto.response.PagedResponse;
import com.teklif.app.dto.response.PaginationResponse;
import com.teklif.app.entity.ActivityLog;
import com.teklif.app.entity.Offer;
import com.teklif.app.enums.LogType;
import com.teklif.app.exception.CustomException;
import com.teklif.app.mapper.ActivityLogMapper;
import com.teklif.app.repository.ActivityLogRepository;
import com.teklif.app.repository.OfferRepository;
import com.teklif.app.security.CustomUserDetails;
import com.teklif.app.util.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;
    private final OfferRepository offerRepository;

    public PagedResponse<ActivityLogResponse> getLogs(
            LogType logType,
            String targetId,
            Instant startDate,
            Instant endDate,
            int page,
            int limit
    ) {
        String tenantId = TenantContext.getTenantId();
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("logDate").descending());

        Page<ActivityLog> logPage = activityLogRepository.findByTenantIdWithFilters(
                tenantId, logType, targetId, startDate, endDate, pageable
        );

        List<ActivityLogResponse> items = logPage.getContent().stream()
                .map(activityLogMapper::toResponse)
                .collect(Collectors.toList());

        PaginationResponse pagination = PaginationResponse.of(
                logPage.getTotalElements(), page, limit
        );

        return PagedResponse.<ActivityLogResponse>builder()
                .items(items)
                .pagination(pagination)
                .build();
    }

    public List<ActivityLogResponse> getLogsByTargetId(String targetId) {
        List<ActivityLog> logs = activityLogRepository.findByTargetIdOrderByLogDateDesc(targetId);
        return logs.stream()
                .map(activityLogMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ActivityLogResponse getLogById(String id) {
        ActivityLog log = activityLogRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Log not found"));
        return activityLogMapper.toResponse(log);
    }

    @Transactional
    public void createLog(
            LogType logType,
            String targetId,
            String title,
            String message,
            HttpServletRequest request
    ) {
        String tenantId = TenantContext.getTenantId();
        String userId = getCurrentUserId();

        if(tenantId == null && logType.getValue().startsWith("OFFER_")){
            Offer o = offerRepository.findById(targetId).orElseThrow(() -> CustomException.notFound("Offer not found"));
            tenantId = o.getTenantId();
        }



        // If request is null, try to get from RequestContextHolder
        if (request == null) {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    request = attributes.getRequest();
                }
            } catch (Exception ignored) {
            }
        }

        ActivityLog log = ActivityLog.builder()
                .logDate(Instant.now())
                .logType(logType)
                .targetId(targetId)
                .title(title)
                .message(message)
                .tenantId(tenantId)
                .userId(userId)
                .build();

        if (request != null) {
            log.setIpAddress(getClientIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));

            // Parse user agent
            Map<String, String> userAgentInfo = parseUserAgent(request.getHeader("User-Agent"));
            log.setOsName(userAgentInfo.get("osName"));
            log.setOsVersion(userAgentInfo.get("osVersion"));
            log.setBrowserName(userAgentInfo.get("browserName"));
            log.setBrowserVersion(userAgentInfo.get("browserVersion"));
            log.setDeviceType(userAgentInfo.get("deviceType"));
        }

        activityLogRepository.save(log);
    }

    @Transactional
    public void deleteLog(String id) {
        ActivityLog log = activityLogRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Log not found"));
        log.setIsDeleted(true);
        activityLogRepository.save(log);
    }

    // Helper methods

    private String getCurrentUserId() {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            return userDetails.getUser().getId();
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private Map<String, String> parseUserAgent(String userAgent) {
        if (userAgent == null) {
            return Map.of(
                    "osName", "Unknown",
                    "osVersion", "",
                    "browserName", "Unknown",
                    "browserVersion", "",
                    "deviceType", "Unknown"
            );
        }

        String osName = "Unknown";
        String osVersion = "";
        String browserName = "Unknown";
        String browserVersion = "";
        String deviceType = "Desktop";

        // OS Detection
        if (userAgent.contains("Windows")) {
            osName = "Windows";
            if (userAgent.contains("Windows NT 10.0")) osVersion = "10";
            else if (userAgent.contains("Windows NT 6.3")) osVersion = "8.1";
            else if (userAgent.contains("Windows NT 6.2")) osVersion = "8";
            else if (userAgent.contains("Windows NT 6.1")) osVersion = "7";
        } else if (userAgent.contains("Mac OS X")) {
            osName = "macOS";
            int start = userAgent.indexOf("Mac OS X") + 9;
            if (start > 8) {
                int end = userAgent.indexOf(")", start);
                if (end > start) {
                    osVersion = userAgent.substring(start, end).replace("_", ".");
                }
            }
        } else if (userAgent.contains("Linux")) {
            osName = "Linux";
            if (userAgent.contains("Android")) {
                osName = "Android";
                deviceType = "Mobile";
            }
        } else if (userAgent.contains("Android")) {
            osName = "Android";
            deviceType = "Mobile";
        } else if (userAgent.contains("iOS") || userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            osName = "iOS";
            deviceType = userAgent.contains("iPad") ? "Tablet" : "Mobile";
        }

        // Browser Detection
        if (userAgent.contains("Edg/")) {
            browserName = "Edge";
            int start = userAgent.indexOf("Edg/") + 4;
            int end = userAgent.indexOf(" ", start);
            if (end > start) browserVersion = userAgent.substring(start, end);
        } else if (userAgent.contains("Chrome/") && !userAgent.contains("Edg/")) {
            browserName = "Chrome";
            int start = userAgent.indexOf("Chrome/") + 7;
            int end = userAgent.indexOf(" ", start);
            if (end > start) browserVersion = userAgent.substring(start, end);
        } else if (userAgent.contains("Firefox/")) {
            browserName = "Firefox";
            int start = userAgent.indexOf("Firefox/") + 8;
            int end = userAgent.indexOf(" ", start);
            if (end > start) browserVersion = userAgent.substring(start, end);
        } else if (userAgent.contains("Safari/") && !userAgent.contains("Chrome")) {
            browserName = "Safari";
            int start = userAgent.indexOf("Version/") + 8;
            int end = userAgent.indexOf(" ", start);
            if (end > start) browserVersion = userAgent.substring(start, end);
        }

        return Map.of(
                "osName", osName,
                "osVersion", osVersion,
                "browserName", browserName,
                "browserVersion", browserVersion,
                "deviceType", deviceType
        );
    }
}
