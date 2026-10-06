package com.events.eventmanagement.dto;

import com.events.eventmanagement.entity.TicketType;

import java.math.BigDecimal;

public record TicketTypeResponse(
        Long id,
        String name,
        BigDecimal price,
        int quantity,
        int soldQuantity,
        int available,
        boolean active
) {
    public static TicketTypeResponse from(TicketType ticketType) {
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getQuantity(),
                ticketType.getSoldQuantity(),
                ticketType.getAvailable(),
                ticketType.isActive()
        );
    }
}
