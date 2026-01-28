package com.teklif.app.dto.response;

import com.teklif.app.enums.LogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private String id;
    private Instant logDate;
    private LogType logType;
    private String targetId;
    private String title;
    private String message;
    private String ipAddress;
    private String userAgent;
    private String osName;
    private String osVersion;
    private String browserName;
    private String browserVersion;
    private String deviceType;
    private String tenantId;
    private String userId;
    private UserBasicResponse user;
    private Instant createdAt;
}
