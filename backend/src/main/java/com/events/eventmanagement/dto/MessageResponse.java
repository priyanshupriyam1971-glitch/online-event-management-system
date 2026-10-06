package com.events.eventmanagement.dto;

import com.events.eventmanagement.entity.Notification;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        String title,
        String content,
        String eventTitle,
        String organizerName,
        LocalDateTime createdAt
) {
    public static MessageResponse from(Notification notification) {
        return new MessageResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getEvent().getTitle(),
                notification.getOrganizer().getName(),
                notification.getCreatedAt()
        );
    }
}
