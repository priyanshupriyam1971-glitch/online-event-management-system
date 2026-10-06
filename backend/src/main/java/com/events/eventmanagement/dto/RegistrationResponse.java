package com.events.eventmanagement.dto;

import com.events.eventmanagement.entity.Registration;
import com.events.eventmanagement.enums.RegistrationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RegistrationResponse(
        Long id,
        String attendeeName,
        String attendeeEmail,
        String eventTitle,
        LocalDateTime eventDate,
        String venue,
        LocalDateTime registrationDate,
        RegistrationStatus status,
        BigDecimal totalAmount,
        List<TicketItemResponse> items
) {
    public static RegistrationResponse from(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getAttendee().getName(),
                registration.getAttendee().getEmail(),
                registration.getEvent().getTitle(),
                registration.getEvent().getEventDate(),
                registration.getEvent().getVenue(),
                registration.getRegistrationDate(),
                registration.getStatus(),
                registration.getTotalAmount(),
                registration.getItems().stream().map(TicketItemResponse::from).toList()
        );
    }
}
