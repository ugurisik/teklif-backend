package com.teklif.app.mapper;

import com.teklif.app.dto.response.ActivityLogResponse;
import com.teklif.app.dto.response.UserBasicResponse;
import com.teklif.app.entity.ActivityLog;
import com.teklif.app.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    default ActivityLogResponse toResponse(ActivityLog log) {
        if (log == null) {
            return null;
        }

        UserBasicResponse user = null;
        if (log.getUser() != null) {
            user = UserBasicResponse.builder()
                    .id(log.getUser().getId())
                    .firstName(log.getUser().getFirstName())
                    .lastName(log.getUser().getLastName())
                    .email(log.getUser().getEmail())
                    .build();
        }

        return ActivityLogResponse.builder()
                .id(log.getId())
                .logDate(log.getLogDate())
                .logType(log.getLogType())
                .targetId(log.getTargetId())
                .title(log.getTitle())
                .message(log.getMessage())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .osName(log.getOsName())
                .osVersion(log.getOsVersion())
                .browserName(log.getBrowserName())
                .browserVersion(log.getBrowserVersion())
                .deviceType(log.getDeviceType())
                .tenantId(log.getTenantId())
                .userId(log.getUserId())
                .user(user)
                .createdAt(log.getCreatedAt())
                .build();
    }
}
