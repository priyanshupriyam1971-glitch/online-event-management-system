package com.events.eventmanagement.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank String title,
        String description,
        String category,
        String venue,
        @NotNull @Future LocalDateTime eventDate
) {}
