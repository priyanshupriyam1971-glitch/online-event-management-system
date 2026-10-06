package com.events.eventmanagement.dto;

import com.events.eventmanagement.entity.TicketItem;
import com.events.eventmanagement.enums.PaymentStatus;

import java.math.BigDecimal;

public record TicketItemResponse(
        String ticketTypeName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        PaymentStatus paymentStatus
) {
    public static TicketItemResponse from(TicketItem item) {
        return new TicketItemResponse(
                item.getTicketType().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalAmount(),
                item.getPaymentStatus()
        );
    }
}
