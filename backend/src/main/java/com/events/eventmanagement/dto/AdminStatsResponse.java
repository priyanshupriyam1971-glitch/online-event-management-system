package com.events.eventmanagement.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminStatsResponse(
        long users,
        long events,
        long pendingEvents,
        long approvedEvents,
        long registrations,
        long pendingRegistrations,
        int ticketsSold,
        BigDecimal revenue,
        List<ActivityLogResponse> recentActivities
) {}
