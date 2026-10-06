package com.events.eventmanagement.dto;

import com.events.eventmanagement.entity.Event;
import com.events.eventmanagement.entity.TicketType;
import com.events.eventmanagement.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Long id,
        String title,
        String description,
        String category,
        String venue,
        LocalDateTime eventDate,
        EventStatus status,
        String rejectionReason,
        String organizerName,
        String organizerEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TicketTypeResponse> tickets
) {
    public static EventResponse from(Event event, List<TicketType> tickets) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getVenue(),
                event.getEventDate(),
                event.getStatus(),
                event.getRejectionReason(),
                event.getOrganizer().getName(),
                event.getOrganizer().getEmail(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                tickets == null ? List.of() : tickets.stream().map(TicketTypeResponse::from).toList()
        );
    }
}
