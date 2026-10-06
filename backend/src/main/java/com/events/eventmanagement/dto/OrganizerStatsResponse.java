package com.events.eventmanagement.dto;

import java.math.BigDecimal;

public record OrganizerStatsResponse(
        long myEvents,
        long totalRegistrations,
        int ticketsSold,
        BigDecimal revenue
) {}
