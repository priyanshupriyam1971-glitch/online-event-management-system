package com.events.eventmanagement.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RegistrationRequest(
        @NotEmpty List<TicketItemRequest> items
) {}
