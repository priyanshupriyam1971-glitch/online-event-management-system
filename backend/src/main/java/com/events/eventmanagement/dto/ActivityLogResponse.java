package com.events.eventmanagement.dto;

import com.events.eventmanagement.entity.ActivityLog;

import java.time.LocalDateTime;

public record ActivityLogResponse(
        String actor,
        String action,
        String details,
        LocalDateTime createdAt
) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(log.getActor(), log.getAction(), log.getDetails(), log.getCreatedAt());
    }
}
